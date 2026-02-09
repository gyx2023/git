package com.crm.system.service.file.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.crm.common.core.domain.BaseEntity;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.helper.LoginHelper;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.domain.SysOss;
import com.crm.system.domain.bo.file.FileAsyncTaskBo;
import com.crm.system.domain.file.FileAsyncTaskDo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.file.FileAsyncTaskVo;
import com.crm.system.mapper.SysOssMapper;
import com.crm.system.mapper.file.FileAsyncTaskMapper;
import com.crm.system.service.ISysOssService;
import com.crm.system.service.file.IFileAsyncTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Collection;
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

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private FileAsyncTaskMapper baseMapper;

    @Resource
    private ISysOssService iSysOssService;

    @Resource
    private SysOssMapper ossMapper;

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
    public void downloadFile(Long taskId,String fileType, HttpServletResponse response) throws Exception {
        // 1. 根据taskId查询任务信息
        FileAsyncTaskVo taskVo = queryById(taskId);
        if (taskVo == null) {
            log.error("任务不存在，任务ID: {}", taskId);
            throw new RuntimeException("任务不存在");
        }
        String fileUrl="";
        if(fileType.equals("export")){
            fileUrl = taskVo.getFileUrl();
        }else if(fileType.equals("import")){
            fileUrl = taskVo.getResultFileUrl();
        }
        try {
            SysOss sysOss = ossMapper.selectOne(new LambdaQueryWrapper<SysOss>().eq(SysOss::getUrl, fileUrl));
            iSysOssService.download(sysOss.getOssId(), response);
            log.info("文件下载成功，任务ID: {}, 文件路径: {}", taskId, fileUrl);
        } catch (Exception e) {
            log.error("文件下载失败，任务ID: {}, 文件路径: {}", taskId, fileUrl, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 查询任务管理列表
     */
    @Override
    public TableDataInfo<FileAsyncTaskVo> queryPageList(FileAsyncTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<FileAsyncTaskDo> lqw = buildQueryWrapper(bo);
        Page<FileAsyncTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        result.getRecords().forEach(task -> {
            if(task.getTaskType().equals("import") && StringUtils.isNotEmpty(task.getResultFileUrl())){
                SysOss sysOss = ossMapper.selectOne(new LambdaQueryWrapper<SysOss>().eq(SysOss::getUrl, task.getResultFileUrl()));
                task.setResultFileUrlOssId(sysOss.getOssId());
            }else if(task.getTaskType().equals("export") && StringUtils.isNotEmpty(task.getFileUrl())){
                SysOss sysOss = ossMapper.selectOne(new LambdaQueryWrapper<SysOss>().eq(SysOss::getUrl, task.getFileUrl()));
                task.setFileUrlOssId(sysOss.getOssId());
            }
        });
        return TableDataInfo.build(result);
    }

    /**
     * 查询任务管理列表
     */
    @Override
    public List<FileAsyncTaskVo> queryList(FileAsyncTaskBo bo) {
        LambdaQueryWrapper<FileAsyncTaskDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<FileAsyncTaskDo> buildQueryWrapper(FileAsyncTaskBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<FileAsyncTaskDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getTaskId() != null, FileAsyncTaskDo::getTaskId, bo.getTaskId());
        lqw.like(StringUtils.isNotBlank(bo.getTaskName()), FileAsyncTaskDo::getTaskName, bo.getTaskName());
        lqw.eq(StringUtils.isNotBlank(bo.getTaskType()), FileAsyncTaskDo::getTaskType, bo.getTaskType());
        lqw.like(StringUtils.isNotBlank(bo.getFileName()), FileAsyncTaskDo::getFileName, bo.getFileName());
        lqw.eq(StringUtils.isNotBlank(bo.getFileUrl()), FileAsyncTaskDo::getFileUrl, bo.getFileUrl());
        lqw.eq(bo.getFileSize() != null, FileAsyncTaskDo::getFileSize, bo.getFileSize());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessType()), FileAsyncTaskDo::getBusinessType, bo.getBusinessType());
        lqw.eq(bo.getTotalRows() != null, FileAsyncTaskDo::getTotalRows, bo.getTotalRows());
        lqw.eq(bo.getSuccessRows() != null, FileAsyncTaskDo::getSuccessRows, bo.getSuccessRows());
        lqw.eq(bo.getFailRows() != null, FileAsyncTaskDo::getFailRows, bo.getFailRows());
        lqw.eq(bo.getStatus() != null, FileAsyncTaskDo::getStatus, bo.getStatus());
        lqw.eq(bo.getStartTime() != null, FileAsyncTaskDo::getStartTime, bo.getStartTime());
        lqw.eq(bo.getFinishTime() != null, FileAsyncTaskDo::getFinishTime, bo.getFinishTime());
        lqw.eq(StringUtils.isNotBlank(bo.getRequestParams()), FileAsyncTaskDo::getRequestParams, bo.getRequestParams());
        lqw.like(StringUtils.isNotBlank(bo.getResultFileName()), FileAsyncTaskDo::getResultFileName, bo.getResultFileName());
        lqw.eq(StringUtils.isNotBlank(bo.getResultFileUrl()), FileAsyncTaskDo::getResultFileUrl, bo.getResultFileUrl());
        lqw.eq(bo.getCreateUserId() != null, FileAsyncTaskDo::getCreateBy, bo.getCreateUserId());
        lqw.like(StringUtils.isNotBlank(bo.getCreateBy()), FileAsyncTaskDo::getCreateBy, bo.getCreateBy());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            FileAsyncTaskDo::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(BaseEntity::getCreateTime);
        lqw.eq(FileAsyncTaskDo::getCreateUserId,LoginHelper.getUserId());
        return lqw;
    }

    /**
     * 新增任务管理
     */
    @Override
    public Boolean insertByBo(FileAsyncTaskBo bo) {
        FileAsyncTaskDo add = BeanUtil.toBean(bo, FileAsyncTaskDo.class);
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
        FileAsyncTaskDo update = BeanUtil.toBean(bo, FileAsyncTaskDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(FileAsyncTaskDo entity){
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
        // 查询数据库中当前分钟的最大任务ID
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
        SysOssVo upload = iSysOssService.upload(file);

        try {
            // 创建异步任务
            FileAsyncTaskBo taskBo = new FileAsyncTaskBo();
            taskBo.setTaskType("import");

            // 根据业务类型设置文件名
            if ("grp_manager_import".equals(businessType)) {
                taskBo.setFileName("集团客户经理导入" + upload.getFileName());
            } else {
                taskBo.setFileName(upload.getFileName());
            }

            taskBo.setFileUrl(upload.getUrl());
            taskBo.setFileSize(file.getSize());
            taskBo.setBusinessType(businessType);
            taskBo.setStatus(0L); // 0-待处理
            taskBo.setCreateUserId(LoginHelper.getUserId());

            // 生成任务ID：yyyymmddHHmm+0001（4位序列号）格式
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            String sequence = getDailySequence();
            String taskIdStr = "10" + dateStr + sequence;
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
            // 获取当前登录用户信息
            LoginUser loginUser = LoginHelper.getLoginUser();
            String areaCode = loginUser.getAreaCode();

            // 判断当前用户是否为管理员
            boolean isAdmin = LoginHelper.isAdmin();

            // 解析原始请求参数
            JSONObject jsonObject = JSONUtil.parseObj(requestParams);
            // 添加areaCode到请求参数中
            jsonObject.set("areaCode", areaCode);
            // 添加isAdmin到请求参数中
            jsonObject.set("isAdmin", isAdmin);
            // 将修改后的参数转回字符串
            String modifiedRequestParams = jsonObject.toString();

            // 创建异步任务
            FileAsyncTaskBo taskBo = new FileAsyncTaskBo();
            taskBo.setTaskType("export");
            taskBo.setBusinessType(businessType);
            taskBo.setRequestParams(modifiedRequestParams);
            taskBo.setStatus(0L); // 0-待处理
            taskBo.setCreateUserId(LoginHelper.getUserId());


            // 生成任务ID：yyyymmddHHmm+0001（4位序列号）格式
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
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
}
