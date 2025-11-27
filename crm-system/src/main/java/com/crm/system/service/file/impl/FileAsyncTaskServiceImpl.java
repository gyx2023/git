package com.crm.system.service.file.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.core.domain.entity.SysDictType;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.helper.LoginHelper;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.domain.bo.file.FileAsyncTaskBo;
import com.crm.system.domain.file.FileAsyncTaskDO;
import com.crm.system.domain.vo.file.FileAsyncTaskVo;
import com.crm.system.mapper.file.FileAsyncTaskMapper;
import com.crm.system.service.file.IFileAsyncTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletResponse;

/**
 * 任务管理Service业务层处理
 *
 * @author crm
 * @date 2025-11-24
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class FileAsyncTaskServiceImpl implements IFileAsyncTaskService {

    // SFTP配置
    @Value("${sftp.host:localhost}")
    private String sftpHost;

    @Value("${sftp.port:22}")
    private int sftpPort;

    @Value("${sftp.username:liutong}")
    private String sftpUsername;

    @Value("${sftp.password:123123}")
    private String sftpPassword;

    @Value("${sftp.base-path:/Users/liutong/upload}")
    private String sftpBasePath;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private FileAsyncTaskMapper baseMapper;

    /**
     * 查询任务管理
     */
    @Override
    public FileAsyncTaskVo queryById(Long taskId){
        return baseMapper.selectVoById(taskId);
    }

    /**
     * 根据任务ID下载文件
     * @param taskId 任务ID
     * @param response HttpServletResponse
     * @throws Exception 异常信息
     */
    @Override
    public void downloadFile(Long taskId, HttpServletResponse response) throws Exception {
        // 1. 根据taskId查询任务信息
        FileAsyncTaskVo taskVo = queryById(taskId);
        if (taskVo == null) {
            log.error("任务不存在，任务ID: {}", taskId);
            throw new RuntimeException("任务不存在");
        }

        String fileUrl = taskVo.getFileUrl();
        if (StringUtils.isBlank(fileUrl)) {
            log.error("任务文件路径为空，任务ID: {}", taskId);
            throw new RuntimeException("任务文件路径为空");
        }

        String fileName = taskVo.getFileName();
        if (StringUtils.isBlank(fileName)) {
            // 如果文件名不存在，从fileUrl中提取
            int lastSlashIndex = fileUrl.lastIndexOf("/");
            if (lastSlashIndex > -1 && lastSlashIndex < fileUrl.length() - 1) {
                fileName = fileUrl.substring(lastSlashIndex + 1);
            } else {
                fileName = "download_file_" + taskId;
            }
        }

        cn.hutool.extra.ssh.Sftp sftp = null;
        try {
            // 2. 创建SFTP连接
            sftp = cn.hutool.extra.ssh.JschUtil.createSftp(sftpHost, sftpPort, sftpUsername, sftpPassword);

            // 3. 检查文件是否存在
            if (!sftp.exist(fileUrl)) {
                log.error("SFTP文件不存在，路径: {}", fileUrl);
                throw new RuntimeException("文件不存在");
            }

            // 4. 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));

            // 5. 从SFTP读取文件并写入响应流
            try (InputStream in = sftp.getClient().get(fileUrl);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }

            log.info("文件下载成功，任务ID: {}, 文件路径: {}", taskId, fileUrl);
        } catch (Exception e) {
            log.error("文件下载失败，任务ID: {}, 文件路径: {}", taskId, fileUrl, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        } finally {
            if (sftp != null) {
                try {
                    sftp.close();
                } catch (Exception e) {
                    log.warn("关闭SFTP连接失败", e);
                }
            }
        }
    }

    /**
     * 查询任务管理列表
     */
    @Override
    public TableDataInfo<FileAsyncTaskVo> queryPageList(FileAsyncTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<FileAsyncTaskDO> lqw = buildQueryWrapper(bo);
        Page<FileAsyncTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询任务管理列表
     */
    @Override
    public List<FileAsyncTaskVo> queryList(FileAsyncTaskBo bo) {
        LambdaQueryWrapper<FileAsyncTaskDO> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<FileAsyncTaskDO> buildQueryWrapper(FileAsyncTaskBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<FileAsyncTaskDO> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getTaskId() != null, FileAsyncTaskDO::getTaskId, bo.getTaskId());
        lqw.like(StringUtils.isNotBlank(bo.getTaskName()), FileAsyncTaskDO::getTaskName, bo.getTaskName());
        lqw.eq(StringUtils.isNotBlank(bo.getTaskType()), FileAsyncTaskDO::getTaskType, bo.getTaskType());
        lqw.like(StringUtils.isNotBlank(bo.getFileName()), FileAsyncTaskDO::getFileName, bo.getFileName());
        lqw.eq(StringUtils.isNotBlank(bo.getFileUrl()), FileAsyncTaskDO::getFileUrl, bo.getFileUrl());
        lqw.eq(bo.getFileSize() != null, FileAsyncTaskDO::getFileSize, bo.getFileSize());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessType()), FileAsyncTaskDO::getBusinessType, bo.getBusinessType());
        lqw.eq(bo.getTotalRows() != null, FileAsyncTaskDO::getTotalRows, bo.getTotalRows());
        lqw.eq(bo.getSuccessRows() != null, FileAsyncTaskDO::getSuccessRows, bo.getSuccessRows());
        lqw.eq(bo.getFailRows() != null, FileAsyncTaskDO::getFailRows, bo.getFailRows());
        lqw.eq(bo.getStatus() != null, FileAsyncTaskDO::getStatus, bo.getStatus());
        lqw.eq(bo.getStartTime() != null, FileAsyncTaskDO::getStartTime, bo.getStartTime());
        lqw.eq(bo.getFinishTime() != null, FileAsyncTaskDO::getFinishTime, bo.getFinishTime());
        lqw.eq(StringUtils.isNotBlank(bo.getRequestParams()), FileAsyncTaskDO::getRequestParams, bo.getRequestParams());
        lqw.like(StringUtils.isNotBlank(bo.getResultFileName()), FileAsyncTaskDO::getResultFileName, bo.getResultFileName());
        lqw.eq(StringUtils.isNotBlank(bo.getResultFileUrl()), FileAsyncTaskDO::getResultFileUrl, bo.getResultFileUrl());
        lqw.eq(bo.getCreateUserId() != null, FileAsyncTaskDO::getCreateBy, bo.getCreateUserId());
        lqw.like(StringUtils.isNotBlank(bo.getCreateBy()), FileAsyncTaskDO::getCreateBy, bo.getCreateBy());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            FileAsyncTaskDO::getCreateTime, params.get("beginTime"), params.get("endTime"));
        return lqw;
    }

    /**
     * 新增任务管理
     */
    @Override
    public Boolean insertByBo(FileAsyncTaskBo bo) {
        FileAsyncTaskDO add = BeanUtil.toBean(bo, FileAsyncTaskDO.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setTaskId(add.getTaskId());
        }
        return flag;
    }

    /**
     * 修改任务管理
     */
    @Override
    public Boolean updateByBo(FileAsyncTaskBo bo) {
        FileAsyncTaskDO update = BeanUtil.toBean(bo, FileAsyncTaskDO.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(FileAsyncTaskDO entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除任务管理信息
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 获取当前日期的4位序列号
     * 使用Redis的INCR命令生成序列号，确保分布式环境下的唯一性和高性能
     * 每天从0001开始计数
     */
    private String getDailySequence() {
        String currentDateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Redis键名，格式为: task:sequence:20251124
        String redisKey = String.format("task:sequence:%s", currentDateStr);

        try {
            // 使用Redis的INCR命令原子递增获取序列号
            Long sequence = redisTemplate.opsForValue().increment(redisKey);

            // 设置键的过期时间为明天凌晨，确保第二天自动重置为1
            // 计算当前时间到明天凌晨的秒数
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime tomorrow = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long expireSeconds = java.time.Duration.between(now, tomorrow).getSeconds();
            redisTemplate.expire(redisKey, expireSeconds, java.util.concurrent.TimeUnit.SECONDS);

            // 确保序列号不超过4位
            if (sequence > 9999) {
                sequence = sequence % 10000;
            }
            return String.format("%04d", sequence);
        } catch (Exception e) {
            log.error("Redis生成序列号失败，回退到数据库查询方式", e);
            // Redis失败时回退到数据库查询方式
            return getSequenceFromDatabase(currentDateStr);
        }
    }

    /**
     * 当Redis不可用时，从数据库获取序列号（降级方案）
     */
    private synchronized String getSequenceFromDatabase(String currentDateStr) {
        // 查询数据库中当天的最大任务ID
        Long maxTaskId = baseMapper.selectMaxTaskIdByDate(currentDateStr);

        int sequence = 1;
        if (maxTaskId != null) {
            // 提取序列号部分（后4位）
            String maxTaskIdStr = String.valueOf(maxTaskId);
            if (maxTaskIdStr.length() >= 4) {
                String sequenceStr = maxTaskIdStr.substring(maxTaskIdStr.length() - 4);
                try {
                    sequence = Integer.parseInt(sequenceStr) + 1;
                } catch (NumberFormatException e) {
                    log.warn("解析序列号失败，使用默认值1");
                }
            }
        }

        // 确保序列号不超过4位
        if (sequence > 9999) {
            sequence = sequence % 10000;
        }

        return String.format("%04d", sequence);
    }

    @Override
    public Long applyImport(MultipartFile file, String businessType,String taskName) {
        try {
            // 使用原始文件名，SFTP会自动覆盖同名文件
            String originalFilename = file.getOriginalFilename();
            // 确保文件名不为空
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                throw new RuntimeException("文件名不能为空");
            }
            String remoteFilePath = sftpBasePath + "/" + originalFilename;

            // 上传文件到SFTP
            String fileUrl = uploadFileToSFTP(file, originalFilename, remoteFilePath);

            // 创建异步任务
            FileAsyncTaskBo taskBo = new FileAsyncTaskBo();
            taskBo.setTaskType("import");
            taskBo.setFileName(originalFilename);
            taskBo.setFileUrl(fileUrl);
            taskBo.setFileSize(file.getSize());
            taskBo.setBusinessType(businessType);
            taskBo.setStatus(0L); // 0-待处理
            taskBo.setCreateUserId(LoginHelper.getUserId());

            // 生成任务ID：yyyymmdd+0001（4位序列号）格式
            String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            String sequence = getDailySequence();
            String taskIdStr = dateStr + sequence;
            Long taskId = Long.valueOf(taskIdStr);
            taskBo.setTaskId(taskId);
            taskBo.setTaskName(taskName);
            taskBo.setStartTime(LocalDateTime.now());

            // 保存到数据库
            boolean success = insertByBo(taskBo);
            if (success) {
                log.info("导入任务申请成功，任务ID: {}", taskId);
                return taskId;
            } else {
                throw new RuntimeException("导入任务申请失败");
            }
        } catch (Exception e) {
            log.error("导入任务申请异常", e);
            throw new RuntimeException("导入任务申请异常: " + e.getMessage());
        }
    }

    @Override
    public Long applyExport(String businessType, String requestParams,String taskName) {
        try {
            // 创建异步任务
            FileAsyncTaskBo taskBo = new FileAsyncTaskBo();
            taskBo.setTaskType("export");
            taskBo.setBusinessType(businessType);
            taskBo.setRequestParams(requestParams);
            taskBo.setStatus(0L); // 0-待处理
            taskBo.setCreateUserId(LoginHelper.getUserId());

            // 生成任务ID：yyyymmdd+0001（4位序列号）格式
            String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            String sequence = getDailySequence();
            String taskIdStr = dateStr + sequence;
            Long taskId = Long.valueOf(taskIdStr);
            taskBo.setTaskId(taskId);
            taskBo.setTaskName(taskName);
            taskBo.setStartTime(LocalDateTime.now());

            // 保存到数据库
            boolean success = insertByBo(taskBo);
            if (success) {
                log.info("导出任务申请成功，任务ID: {}", taskId);
                return taskId;
            } else {
                throw new RuntimeException("导出任务申请失败");
            }
        } catch (Exception e) {
            log.error("导出任务申请异常", e);
            throw new RuntimeException("导出任务申请异常: " + e.getMessage());
        }
    }

    /**
     * 上传文件到SFTP服务器
     */
    private String uploadFileToSFTP(MultipartFile file, String newFilename, String remoteFilePath) {
        cn.hutool.extra.ssh.Sftp sftp = null;
        File tempFile = null;
        try {
            // 获取原始文件扩展名
            String fileExtension = "";
            if (newFilename != null && newFilename.contains(".")) {
                fileExtension = newFilename.substring(newFilename.lastIndexOf("."));
            }
            // 创建临时文件，使用原始文件的扩展名
            tempFile = File.createTempFile("temp_", fileExtension);
            try (InputStream inputStream = file.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // 使用JschUtil创建SFTP连接
            sftp = cn.hutool.extra.ssh.JschUtil.createSftp(sftpHost, sftpPort, sftpUsername, sftpPassword);
            // 确保目录存在
            String directory = remoteFilePath.substring(0, remoteFilePath.lastIndexOf("/"));
            if (!sftp.exist(directory)) {
                sftp.mkDirs(directory);
            }
            // 上传文件
            sftp.upload(remoteFilePath, tempFile);
            log.info("文件上传到SFTP成功，路径: {}", remoteFilePath);

            // 直接返回SFTP文件路径，不构建URL
            return remoteFilePath;
        } catch (Exception e) {
            log.error("文件上传到SFTP失败", e);
            throw new RuntimeException("文件上传到SFTP失败: " + e.getMessage());
        } finally {
            if (sftp != null) {
                sftp.close();
            }
            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }
}
