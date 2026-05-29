package com.crm.job.base;

import com.crm.system.service.ISysParamService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Data
public abstract class BaseJob {

    public static final String FTP_DOWNLOAD_END_FIX = ".txt";

    @Resource
    protected ISysParamService sysParamService;

    protected static String ftpUsername;
    protected static String ftpPassword;
    protected static String ftpHost;
    protected static String ftpPort;
    protected static String ftDirectory;
    protected static String localPath;

    protected void initStaticParam(String type) {
        try {
            Map<String, String> param = sysParamService.findValueMapsByType(type);
            if (param != null) {
                ftpUsername = param.get("ftp_username");
                ftpPassword = param.get("ftp_password");
                ftpHost = param.get("ftp_host");
                ftpPort = param.get("ftp_port");
                ftDirectory = param.get("ftp_directory");
                localPath = param.get("ftp_local");
            }
        } catch (Exception e) {
            log.error("FTP参数初始化失败, type:{}", type, e);
        }
    }
}
