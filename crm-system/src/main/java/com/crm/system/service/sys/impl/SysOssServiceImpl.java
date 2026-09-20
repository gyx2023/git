package com.crm.system.service.sys.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.constant.CacheNames;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.service.OssService;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.file.FileUtils;
import com.crm.common.utils.file.OssUploadValidator;
import com.crm.common.utils.spring.SpringUtils;
import com.crm.oss.core.OssClient;
import com.crm.oss.entity.UploadResult;
import com.crm.oss.enumd.AccessPolicyType;
import com.crm.oss.factory.OssFactory;
import com.crm.system.domain.dao.sys.SysOss;
import com.crm.system.domain.bo.SysOssBo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.mapper.sys.SysOssMapper;
import com.crm.system.service.sys.ISysOssService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件上传 服务层实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class SysOssServiceImpl implements ISysOssService, OssService {

    private final SysOssMapper baseMapper;

    @Override
    public TableDataInfo<SysOssVo> queryPageList(SysOssBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysOss> lqw = buildQueryWrapper(bo);
        Page<SysOssVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        List<SysOssVo> filterResult = result.getRecords().stream().map(this::matchingUrl).collect(Collectors.toList());
        result.setRecords(filterResult);
        return TableDataInfo.build(result);
    }

    @Override
    public List<SysOssVo> listByIds(Collection<Long> ossIds) {
        List<SysOssVo> list = new ArrayList<>();
        for (Long id : ossIds) {
            SysOssVo vo = SpringUtils.getAopProxy(this).getById(id);
            if (ObjectUtil.isNotNull(vo)) {
                list.add(this.matchingUrl(vo));
            }
        }
        return list;
    }

    @Override
    public String selectUrlByIds(String ossIds) {
        List<String> list = new ArrayList<>();
        for (Long id : StringUtils.splitTo(ossIds, Convert::toLong)) {
            SysOssVo vo = SpringUtils.getAopProxy(this).getById(id);
            if (ObjectUtil.isNotNull(vo)) {
                list.add(this.matchingUrl(vo).getUrl());
            }
        }
        return String.join(StringUtils.SEPARATOR, list);
    }

    private LambdaQueryWrapper<SysOss> buildQueryWrapper(SysOssBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysOss> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getFileName()), SysOss::getFileName, bo.getFileName());
        lqw.like(StringUtils.isNotBlank(bo.getOriginalName()), SysOss::getOriginalName, bo.getOriginalName());
        lqw.eq(StringUtils.isNotBlank(bo.getFileSuffix()), SysOss::getFileSuffix, bo.getFileSuffix());
        lqw.eq(StringUtils.isNotBlank(bo.getUrl()), SysOss::getUrl, bo.getUrl());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            SysOss::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
        lqw.eq(StringUtils.isNotBlank(bo.getCreateBy()), SysOss::getCreateBy, bo.getCreateBy());
        lqw.eq(StringUtils.isNotBlank(bo.getService()), SysOss::getService, bo.getService());
        return lqw;
    }

    @Cacheable(cacheNames = CacheNames.SYS_OSS, key = "#ossId")
    @Override
    public SysOssVo getById(Long ossId) {
        return baseMapper.selectVoById(ossId);
    }

    @Override
    public void download(Long ossId, HttpServletResponse response) throws IOException {
        SysOssVo sysOss = SpringUtils.getAopProxy(this).getById(ossId);
        if (ObjectUtil.isNull(sysOss)) {
            throw new ServiceException("文件数据不存在!");
        }
        FileUtils.setAttachmentResponseHeader(response, sysOss.getOriginalName());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE + "; charset=UTF-8");
        OssClient storage = OssFactory.instance(sysOss.getService());
        try(InputStream inputStream = storage.getObjectContent(sysOss.getUrl())) {
            int available = inputStream.available();
            IoUtil.copy(inputStream, response.getOutputStream(), available);
            response.setContentLength(available);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 获取文件内容
     * 与download方法不同，此方法不写入response，而是返回文件的InputStream
     *
     * @param ossId 文件ID
     * @return 文件输入流
     * @throws IOException IO异常
     */
    @Override
    public java.io.InputStream getFileContent(Long ossId) throws IOException {
        SysOssVo sysOss = SpringUtils.getAopProxy(this).getById(ossId);
        if (ObjectUtil.isNull(sysOss)) {
            throw new ServiceException("文件数据不存在!");
        }
        OssClient storage = OssFactory.instance(sysOss.getService());
        try {
            // 直接返回从S3获取的InputStream，不进行IO复制操作
            return storage.getObjectContent(sysOss.getUrl());
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public SysOssVo upload(MultipartFile file) {
        String originalfileName = file.getOriginalFilename();
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
        // 【安全】白名单 + 高危内容/魔数校验，成功才返回服务端判定 Content-Type（不信任客户端声明）
        String contentType;
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            contentType = OssUploadValidator.validate(in, originalfileName, OssUploadValidator.SCAN_BYTES);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(e.getMessage());
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
        String suffix = OssUploadValidator.resolveExtension(originalfileName);
        OssClient storage = OssFactory.instance();
        UploadResult uploadResult;
        try {
            uploadResult = storage.uploadSuffix(bytes, suffix, contentType);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
        // 保存文件信息
        SysOss oss = new SysOss();
        oss.setUrl(uploadResult.getUrl());
        oss.setFileSuffix(suffix);
        oss.setFileName(uploadResult.getFilename());
        oss.setOriginalName(originalfileName);
        oss.setService(storage.getConfigKey());
        baseMapper.insert(oss);
        SysOssVo sysOssVo = new SysOssVo();
        BeanCopyUtils.copy(oss, sysOssVo);
        return this.matchingUrl(sysOssVo);
    }

    /**
     * 上传文件
     *
     * @param file 文件对象
     * @return 文件上传信息
     */
    @Override
    public SysOssVo upload(File file) {
        String originalfileName = file.getName();
        return upload(file, originalfileName);
    }

    /**
     * 上传文件
     *
     * @param file 文件对象
     * @param originalName 原始文件名（用于下载时显示）
     * @return 文件上传信息
     */
    @Override
    public SysOssVo upload(File file, String originalName) {
        OssClient storage = OssFactory.instance();
        UploadResult uploadResult;
        byte[] fileBytes;
        try {
            fileBytes = IoUtil.readBytes(new FileInputStream(file));
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
        // 【安全】白名单 + 高危内容/魔数校验；Content-Type 服务端判定（不再据文件 GET 猜测 MIME）
        String contentType;
        try (InputStream in = new ByteArrayInputStream(fileBytes)) {
            contentType = OssUploadValidator.validate(in, originalName, OssUploadValidator.SCAN_BYTES);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(e.getMessage());
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
        String suffix = OssUploadValidator.resolveExtension(originalName);
        try {
            uploadResult = storage.uploadSuffix(fileBytes, suffix, contentType);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
        // 保存文件信息
        SysOss oss = new SysOss();
        oss.setUrl(uploadResult.getUrl());
        oss.setFileSuffix(suffix);
        oss.setFileName(uploadResult.getFilename());
        oss.setOriginalName(originalName);
        oss.setService(storage.getConfigKey());
        baseMapper.insert(oss);
        SysOssVo sysOssVo = new SysOssVo();
        return BeanCopyUtils.copy(oss, sysOssVo);
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // 做一些业务上的校验,判断是否需要校验
        }
        List<SysOss> list = baseMapper.selectBatchIds(ids);
        for (SysOss sysOss : list) {
            OssClient storage = OssFactory.instance(sysOss.getService());
            storage.delete(sysOss.getUrl());
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 匹配Url
     *
     * @param oss OSS对象
     * @return oss 匹配Url的OSS对象
     */
    private SysOssVo matchingUrl(SysOssVo oss) {
        OssClient storage = OssFactory.instance(oss.getService());
        // 仅修改桶类型为 private 的URL，临时URL时长为120s
        if (AccessPolicyType.PRIVATE == storage.getAccessPolicy()) {
            oss.setUrl(storage.getPrivateUrl(oss.getFileName(), 120));
        }
        return oss;
    }

    @Override
    public SysOssVo uploadFileByPath(File file, String path) {
        String originalfileName = file.getName();
        OssClient storage = OssFactory.instance();
        UploadResult uploadResult;

        try {
            byte[] bytes = IoUtil.readBytes(new FileInputStream(file));
            // 【安全】同样统一校验（白名单 + 高危标记/魔数），type 服务端判定
            String contentType;
            try (InputStream in = new ByteArrayInputStream(bytes)) {
                contentType = OssUploadValidator.validate(in, originalfileName, OssUploadValidator.SCAN_BYTES);
            }
            // 核心：自定义路径 + 文件名（文件名后缀已在校验层限定为白名单扩展，防脏后缀透传）
            String fileName = path + "/" + originalfileName;

            uploadResult = storage.upload(bytes, fileName, contentType);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("文件上传失败：" + e.getMessage());
        } catch (IOException e) {
            throw new ServiceException("文件上传失败：" + e.getMessage());
        }

        // 保存记录
        SysOss oss = new SysOss();
        oss.setUrl(uploadResult.getUrl());
        oss.setFileSuffix(OssUploadValidator.resolveExtension(originalfileName));
        oss.setFileName(uploadResult.getFilename());
        oss.setOriginalName(originalfileName);
        oss.setService(storage.getConfigKey());
        baseMapper.insert(oss);

        SysOssVo sysOssVo = new SysOssVo();
        BeanCopyUtils.copy(oss, sysOssVo);
        return this.matchingUrl(sysOssVo);
    }
}
