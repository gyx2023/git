package com.crm.job.service.file.strategy.impl;

import com.crm.job.service.file.strategy.AbstractImportStrategy;
import com.crm.system.domain.file.FileAsyncTaskDO;
import cn.hutool.extra.ssh.JschRuntimeException;
import cn.hutool.extra.ssh.Sftp;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户归属部门维护导入策略实现类
 * 处理客户归属部门数据导入的具体业务逻辑
 *
 * @author crm
 * @date 2025-11-24
 */
@Component
@Slf4j
public class GroupBusinessDeptImportStrategy extends AbstractImportStrategy {

    @Value("${sftp.base-path:/Users/liutong/upload}")
    private String sftpBasePath;

    @Value("${sftp.host:localhost}")
    private String sftpHost;

    @Value("${sftp.port:22}")
    private int sftpPort;

    @Value("${sftp.username:root}")
    private String sftpUsername;

    @Value("${sftp.password:}")
    private String sftpPassword;

    @Override
    public String getBusinessType() {
        return "grp_busi_dept_import";
    }

    @Override
    protected boolean doImportTask(FileAsyncTaskDO task) throws Exception {
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

        try {
            // SFTP文件路径
            String sftpFilePath = task.getFileUrl();
            log.info("开始从SFTP获取导入文件: {}", sftpFilePath);

            // 确保本地目录存在
            if (!localTempFile.getParentFile().exists()) {
                localTempFile.getParentFile().mkdirs();
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
                sftp.download(sftpFilePath, localTempFile);
                log.info("hutool SFTP文件下载成功: {} -> {}", sftpFilePath, localTempFilePath);
            } catch (JschRuntimeException e) {
                log.error("hutool SFTP文件下载失败: {} -> {}", sftpFilePath, localTempFilePath);
                e.printStackTrace();
                throw new RuntimeException("SFTP文件下载失败: " + e.getMessage(), e);
            } finally {
                // 安全关闭SFTP连接
                if (sftp != null) {
                    try {
                        sftp.close();
                        log.info("hutool SFTP连接已关闭");
                    } catch (Exception e) {
                        log.error("关闭SFTP连接时发生错误");
                        e.printStackTrace();
                    }
                }
            }

            // 验证本地临时文件是否存在
            if (!localTempFile.exists() || !localTempFile.isFile()) {
                log.error("下载的临时文件不存在: {}", localTempFilePath);
                throw new RuntimeException("下载的临时文件不存在");
            }

            log.info("开始读取导入文件: {}", localTempFilePath);

            // TODO: 根据实际文件格式读取数据
            // 这里需要根据实际的文件格式（Excel、CSV等）读取数据
            List<Object> dataList = new ArrayList<>();
            try (FileInputStream fis = new FileInputStream(localTempFile)) {
                // 模拟文件读取过程
                // dataList = ExcelUtils.readExcel(fis, GroupBusinessDept.class);
                log.info("文件读取完成，共读取 {} 条数据", dataList.size());
            }

            // TODO: 调用业务服务处理数据
            // 这里需要注入对应的业务服务来处理数据
            // ImportResult result = groupBusinessDeptService.importData(dataList);

            // 模拟处理结果
            long totalRows = 100; // 模拟总数据量
            long successRows = 95;
            long failRows = 5;

            // 设置任务结果信息
            task.setTotalRows(totalRows);
            task.setSuccessRows(successRows);
            task.setFailRows(failRows);
            task.setRemark("客户归属部门数据导入完成，成功: " + successRows + " 条，失败: " + failRows + " 条");

            // 如果有失败记录，可以生成错误文件

            return successRows > 0; // 只要有成功记录就认为导入成功
        } catch (Exception e) {
            log.error("客户归属部门数据导入失败，任务ID: {}", task.getTaskId(), e);
            task.setRemark("导入失败: " + e.getMessage());
            throw e;
        } finally {
            // 清理临时文件
            if (localTempFile.exists()) {
                boolean deleted = localTempFile.delete();
                log.info("清理临时文件: {}, 结果: {}", localTempFilePath, deleted);
            }
        }
    }

    /**
     * 从SFTP服务器下载文件的方法已使用hutool的SFTP工具替代
     * 相关逻辑已整合到doImportTask方法中
     */
}
