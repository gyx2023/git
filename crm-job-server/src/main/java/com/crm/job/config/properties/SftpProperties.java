package com.crm.job.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SFTP配置属性类
 * 用于管理SFTP连接参数
 *
 * @author crm
 */
@Data
@Component
@ConfigurationProperties(prefix = "sftp")
public class SftpProperties {

    /**
     * SFTP服务器主机地址
     */
    private String host="127.0.0.1";

    /**
     * SFTP服务器端口号
     */
    private int port = 22;

    /**
     * SFTP登录用户名
     */
    private String username="liutong";

    /**
     * SFTP登录密码
     */
    private String password="123123";

    /**
     * 文件上传下载根目录
     */
    private String rootPath;

    /**
     * 连接超时时间（毫秒）
     */
    private int timeout = 30000;

    /**
     * 数据文件根路径，格式：/opt/home/bi/dataout/ZQ
     */
    //private String dataRootPath = "/opt/home/bi/dataout/ZQ";
    private String dataRootPath = "/Users/liutong/tool";

    /**
     * 文件夹日期格式，默认为YYYYMMDD
     */
    private String folderFormat = "yyyyMMdd";

    /**
     * 文件名前缀，默认为GRP_GROUP_INFO
     */
    private String filePrefix = "GRP_GROUP_INFO";

    /**
     * 文件名日期格式，默认为YYYYMMDD
     */
    private String fileDateFormat = "yyyyMMdd";

    /**
     * 文件扩展名，默认为.txt
     */
    private String fileExtension = ".txt";

    /**
     * 集团信息文件路径（保留用于向后兼容）
     */
    private String groupFilePath;
}
