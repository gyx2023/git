package com.crm.job.base;

import com.crm.system.service.sys.ISysParamService;
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
                //用户名
                ftpUsername = param.get("ftp_username");
                //密码
                ftpPassword = param.get("ftp_password");
                //ip地址
                ftpHost = param.get("ftp_host");
                //端口号
                ftpPort = param.get("ftp_port");
                //文件存放目录
                ftDirectory = param.get("ftp_directory");
                //本地下载目录
                localPath = param.get("ftp_local");
            } else {
                log.error("type:{}的数据为空", type);
            }
        } catch (Exception e) {
            log.error("FTP参数初始化失败, type:{}", type, e);
        }
    }
}
