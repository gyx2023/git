package com.crm.job.service.file.strategy.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.ssh.JschRuntimeException;
import cn.hutool.extra.ssh.Sftp;
import com.alibaba.excel.EasyExcel;
import com.crm.job.service.file.strategy.AbstractImportStrategy;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.file.FileAsyncTaskDO;
import com.crm.system.domain.vo.CustomerManagerImportResultVo;
import com.crm.system.domain.vo.CustomerManagerTemplateVo;
import com.crm.system.service.IDwdRepSysPersonDService;
import com.crm.system.service.IZqGrpGroupInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 批量分配客户经理导入策略实现类
 * 处理批量分配客户经理数据导入的具体业务逻辑
 *
 * @author lingma
 * @date 2025-11-28
 */
@Component
@Slf4j
public class CustomerManagerImportStrategy extends AbstractImportStrategy {

    @Value("${sftp.host:localhost}")
    private String sftpHost;

    @Value("${sftp.port:22}")
    private int sftpPort;

    @Value("${sftp.username:root}")
    private String sftpUsername;

    @Value("${sftp.password:}")
    private String sftpPassword;

    @Resource
    private IZqGrpGroupInfoService zqGrpGroupInfoService;

    @Resource
    private IDwdRepSysPersonDService dwdRepSysPersonDService;

    @Override
    public String getBusinessType() {
        return "grp_manager_import";
    }

    /**
     * 执行具体的导入任务
     *
     * @param task 任务信息
     * @return 是否导入成功
     * @throws Exception 导入过程中的异常
     */
    @Override
    protected boolean doImportTask(FileAsyncTaskDO task) throws Exception {
        log.info("开始处理批量分配客户经理导入任务，任务ID: {}", task.getTaskId());

        // 获取项目部署路径作为基础路径
        String basePath = System.getProperty("user.dir");
        // 在项目路径下创建临时文件目录
        String tempDirPath = basePath + File.separator + "temp";
        File tempDir = new File(tempDirPath);
        // 确保临时目录存在
        if (!tempDir.exists()) {
            tempDir.mkdirs();
            log.info("创建临时目录: {}", tempDirPath);
        }

        // 本地临时文件路径
        String localTempFilePath = tempDirPath + File.separator + "temp_" + System.currentTimeMillis() + "_" + task.getFileName();
        File localTempFile = new File(localTempFilePath);

        String resultFilePath = tempDirPath + File.separator + "result_" + System.currentTimeMillis() + "_" + task.getFileName();
        File resultFile = new File(resultFilePath);

        long totalRows = 0;
        long successRows = 0;
        long failRows = 0;

        try {
            // 下载文件到本地临时路径
            downloadFile(task.getFileUrl(), localTempFile);

            // 验证本地临时文件是否存在
            if (!localTempFile.exists() || !localTempFile.isFile()) {
                log.error("下载的临时文件不存在: {}", localTempFilePath);
                throw new RuntimeException("下载的临时文件不存在");
            }

            log.info("开始读取导入文件: {}", localTempFilePath);

            // 读取Excel文件
            List<CustomerManagerTemplateVo> dataList = EasyExcel.read(localTempFile)
                    .head(CustomerManagerTemplateVo.class)
                    .sheet()
                    .doReadSync();

            log.info("文件读取完成，共读取 {} 条数据", dataList.size());
            totalRows = dataList.size();

            // 处理结果列表
            List<CustomerManagerImportResultVo> resultList = new ArrayList<>();

            // 逐条处理数据
            for (CustomerManagerTemplateVo data : dataList) {
                CustomerManagerImportResultVo resultVo = convertToResultVo(data);
                try {
                    // 处理单条记录
                    boolean success = processSingleRecord(data, resultVo);
                    if (success) {
                        resultVo.setProcessResult("成功");
                        successRows++;
                    } else {
                        resultVo.setProcessResult("失败");
                        if (resultVo.getErrorMessage() == null || resultVo.getErrorMessage().isEmpty()) {
                            resultVo.setErrorMessage("更新客户经理信息失败");
                        }
                        failRows++;
                    }
                } catch (Exception e) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage(e.getMessage());
                    failRows++;
                    log.error("处理记录失败，总部集团编码: {}", data.getHqGrpCode(), e);
                }
                resultList.add(resultVo);
            }

            // 生成结果文件
            EasyExcel.write(resultFile, CustomerManagerImportResultVo.class).sheet("导入结果").doWrite(resultList);

            // 设置任务结果信息
            task.setTotalRows(totalRows);
            task.setSuccessRows(successRows);
            task.setFailRows(failRows);
            task.setResultFileName("导入结果_" + task.getFileName());
            task.setResultFileUrl(resultFilePath); // 临时存放本地路径，后续可能需要上传到SFTP或OSS
            task.setRemark("批量分配客户经理导入完成，成功: " + successRows + " 条，失败: " + failRows + " 条");

            return successRows > 0; // 只要有成功记录就认为导入成功
        } catch (Exception e) {
            log.error("批量分配客户经理导入失败，任务ID: {}", task.getTaskId(), e);
            task.setRemark("导入失败: " + e.getMessage());
            throw e;
        } finally {
            // 清理临时文件
            if (localTempFile.exists()) {
                boolean deleted = localTempFile.delete();
                log.info("清理临时文件: {}, 结果: {}", localTempFilePath, deleted);
            }
            // 注意：暂时保留结果文件以便测试，生产环境中应根据实际情况决定是否删除
            // if (resultFile.exists()) {
            //     boolean deleted = resultFile.delete();
            //     log.info("清理结果文件: {}, 结果: {}", resultFilePath, deleted);
            // }
        }
    }

    /**
     * 处理单条记录
     *
     * @param data 客户经理数据
     * @param resultVo 结果VO，用于记录错误信息
     * @return 是否处理成功
     */
    private boolean processSingleRecord(CustomerManagerTemplateVo data, CustomerManagerImportResultVo resultVo) {
        try {
            // 1. 根据总部集团编码查询集团信息
            ZqGrpGroupInfo groupInfo = zqGrpGroupInfoService.selectByHqGrpCode(data.getHqGrpCode());
            if (groupInfo == null) {
                log.warn("未找到总部集团编码对应的集团信息: {}", data.getHqGrpCode());
                resultVo.setErrorMessage("未找到对应的集团信息");
                return false;
            }

            // 2. 获取客户经理可选列表并验证Excel中填写的客户经理是否在可选列表中
            // 验证省级客户经理
            if (isManagerInfoProvided(data.getProvManagerName(), data.getProvManagerOauser())) {
                List<Map<String, Object>> provManagers = dwdRepSysPersonDService.queryManagersByCityCodeAndType(
                        groupInfo.getAreaId(), "01");
                if (!isManagerInList(data.getProvManagerName(), data.getProvManagerOauser(), provManagers)) {
                    resultVo.setErrorMessage("省级客户经理信息不匹配或不在可选列表中");
                    return false;
                }
            }

            // 验证市级客户经理
            if (isManagerInfoProvided(data.getCityManagerName(), data.getCityManagerOauser())) {
                List<Map<String, Object>> cityManagers = dwdRepSysPersonDService.queryManagersByCityCodeAndType(
                        groupInfo.getAreaId(), "02");
                if (!isManagerInList(data.getCityManagerName(), data.getCityManagerOauser(), cityManagers)) {
                    resultVo.setErrorMessage("市级客户经理信息不匹配或不在可选列表中");
                    return false;
                }
            }

            // 验证区县级客户经理
            if (isManagerInfoProvided(data.getCountyManagerName(), data.getCountyManagerOauser())) {
                List<Map<String, Object>> countyManagers = dwdRepSysPersonDService.queryManagersByCityCodeAndType(
                        groupInfo.getAreaId(), "03");
                if (!isManagerInList(data.getCountyManagerName(), data.getCountyManagerOauser(), countyManagers)) {
                    resultVo.setErrorMessage("区县级客户经理信息不匹配或不在可选列表中");
                    return false;
                }
            }

            // 3. 构造更新参数
            ZqGrpGroupInfoBo updateBo = new ZqGrpGroupInfoBo();
            updateBo.setGrpId(groupInfo.getGrpId());

            // 设置省级客户经理
            if (data.getProvManagerName() != null) {
                updateBo.setProvManagerName(data.getProvManagerName());
            }
            if (data.getProvManagerOauser() != null) {
                updateBo.setProvManagerOauser(data.getProvManagerOauser());
            }

            // 设置市级客户经理
            if (data.getCityManagerName() != null) {
                updateBo.setCityManagerName(data.getCityManagerName());
            }
            if (data.getCityManagerOauser() != null) {
                updateBo.setCityManagerOauser(data.getCityManagerOauser());
            }

            // 设置区县级客户经理
            if (data.getCountyManagerName() != null) {
                updateBo.setCountyManagerName(data.getCountyManagerName());
            }
            if (data.getCountyManagerOauser() != null) {
                updateBo.setCountyManagerOauser(data.getCountyManagerOauser());
            }

            // 4. 调用更新接口
            // 直接调用服务方法更新客户经理信息
            return zqGrpGroupInfoService.updateByBo1(updateBo);
        } catch (Exception e) {
            log.error("处理单条记录失败: {}", data.getHqGrpCode(), e);
            resultVo.setErrorMessage("处理记录时发生异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 判断客户经理信息是否已提供（姓名和OA账号至少有一个非空）
     *
     * @param managerName 客户经理姓名
     * @param managerOa 客户经理OA账号
     * @return 是否提供了客户经理信息
     */
    private boolean isManagerInfoProvided(String managerName, String managerOa) {
        return (managerName != null && !managerName.trim().isEmpty()) ||
               (managerOa != null && !managerOa.trim().isEmpty());
    }

    /**
     * 检查指定的客户经理姓名和OA账号是否在可选列表中且匹配
     *
     * @param managerName 客户经理姓名
     * @param managerOa 客户经理OA账号
     * @param managers 可选客户经理列表
     * @return 是否匹配且在列表中
     */
    private boolean isManagerInList(String managerName, String managerOa, List<Map<String, Object>> managers) {
        if ((managerName == null || managerName.isEmpty()) ||
            (managerOa == null || managerOa.isEmpty()) ||
            managers == null || managers.isEmpty()) {
            return false;
        }

        for (Map<String, Object> category : managers) {
            List<Map<String, Object>> managerList = (List<Map<String, Object>>) category.get("managers");
            if (managerList != null) {
                for (Map<String, Object> manager : managerList) {
                    String name = (String) manager.get("name");
                    String oa = (String) manager.get("oaAccount"); // 注意：此处字段名为oaAccount，不是oa
                    // 姓名和OA账号都需要匹配
                    if (managerName.equals(name) && managerOa.equals(oa)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 转换为结果VO
     *
     * @param data 模板数据
     * @return 结果VO
     */
    private CustomerManagerImportResultVo convertToResultVo(CustomerManagerTemplateVo data) {
        CustomerManagerImportResultVo resultVo = new CustomerManagerImportResultVo();
        resultVo.setHqGrpCode(data.getHqGrpCode());
        resultVo.setProvManagerName(data.getProvManagerName());
        resultVo.setProvManagerOauser(data.getProvManagerOauser());
        resultVo.setCityManagerName(data.getCityManagerName());
        resultVo.setCityManagerOauser(data.getCityManagerOauser());
        resultVo.setCountyManagerName(data.getCountyManagerName());
        resultVo.setCountyManagerOauser(data.getCountyManagerOauser());
        return resultVo;
    }

    /**
     * 下载文件
     *
     * @param sftpFilePath SFTP文件路径
     * @param localFile 本地文件
     */
    private void downloadFile(String sftpFilePath, File localFile) throws Exception {
        log.info("开始从SFTP获取导入文件: {}", sftpFilePath);

        // 确保本地目录存在
        if (!localFile.getParentFile().exists()) {
            localFile.getParentFile().mkdirs();
        }

        // 使用hutool的SFTP工具下载文件
        log.info("使用hutool SFTP工具下载文件");
        Sftp sftp = null;
        try {
            // 处理SFTP路径，确保路径格式正确
            if (!sftpFilePath.startsWith("/")) {
                sftpFilePath = "/" + sftpFilePath;
            }

            // 根据认证方式创建SFTP实例
            // 使用密码连接
            sftp = new Sftp(sftpHost, sftpPort, sftpUsername, sftpPassword);

            // 下载文件到本地临时路径
            sftp.download(sftpFilePath, localFile);
            log.info("hutool SFTP文件下载成功: {} -> {}", sftpFilePath, localFile.getAbsolutePath());
        } catch (JschRuntimeException e) {
            log.error("hutool SFTP文件下载失败: {} -> {}", sftpFilePath, localFile.getAbsolutePath());
            throw new RuntimeException("SFTP文件下载失败: " + e.getMessage(), e);
        } finally {
            // 安全关闭SFTP连接
            if (sftp != null) {
                try {
                    sftp.close();
                    log.info("hutool SFTP连接已关闭");
                } catch (Exception e) {
                    log.error("关闭SFTP连接时发生错误");
                }
            }
        }
    }
}
