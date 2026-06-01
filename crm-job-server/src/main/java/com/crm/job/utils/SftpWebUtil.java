package com.crm.job.utils;

import cn.hutool.core.util.ObjectUtil;
import com.amazonaws.util.IOUtils;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

/**
 * 轻量 SFTP 工具类（适配 sys_param 数据库 ftp_xxx 配置）
 * 数据库配置示例：
 * type=tc_wx_strategy
 * code: ftp_host / ftp_port / ftp_username / ftp_password / ftp_directory
 */
@Slf4j
@Component
public class SftpWebUtil implements AutoCloseable {

    private ChannelSftp sftp;
    private Session session;

    /** SFTP 登录用户名 */
    private String username;
    /** SFTP 登录密码 */
    private String password;
    /** 私钥 */
    private String privateKey;
    /** SFTP 服务器地址IP地址 */
    private String host;
    /** SFTP 端口 */
    private int port;

    // -------------------------- 构造器 --------------------------
    public SftpWebUtil(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    public SftpWebUtil(String username, String host, int port, String privateKey) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.privateKey = privateKey;
    }

    public SftpWebUtil() {
    }

    // -------------------------- 从数据库配置创建实例 --------------------------
    public static SftpWebUtil createByDbConfig(String configType) {
        SftpUtil sftpUtil = new SftpUtil();
        sftpUtil.setActiveConfigType(configType);

        String host = sftpUtil.getHost();
        int port = sftpUtil.getPort();
        String username = sftpUtil.getUsername();
        String password = sftpUtil.getPassword();

        return new SftpWebUtil(username, password, host, port);
    }

    // -------------------------- 连接 / 关闭 --------------------------
    public void login() {
        if (session != null && session.isConnected()) {
            log.debug("SFTP 已连接，无需重复登录");
            return;
        }

        try {
            JSch jsch = new JSch();
            if (privateKey != null) {
                jsch.addIdentity(privateKey);
            }

            session = jsch.getSession(username, host, port);
            log.info("SFTP 开始登录：host={}, port={}, username={}", host, port, username);

            if (password != null) {
                session.setPassword(password);
            }

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            // 连接超时 5s
            session.connect(5000);

            Channel channel = session.openChannel("sftp");
            channel.connect(5000);
            sftp = (ChannelSftp) channel;

            log.info("SFTP 登录成功");
        } catch (JSchException e) {
            log.error("SFTP 登录失败：{}", e.getMessage(), e);
            throw new RuntimeException("SFTP 登录失败", e);
        }
    }

    /**
     * 登出并释放资源（安全关闭）
     */
    public void logout() {
        if (sftp != null && sftp.isConnected()) {
            sftp.disconnect();
            log.debug("sftp channel 已断开");
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.debug("ssh session 已断开");
        }
        log.info("SFTP 连接已完全关闭");
    }

    // -------------------------- 上传文件（自动创建目录） --------------------------
    public void upload(String basePath, String directory, String sftpFileName, InputStream input) throws SftpException {
        if (sftp == null || !sftp.isConnected()) {
            throw new IllegalStateException("SFTP 未连接，请先调用 login()");
        }

        try {
            sftp.cd(basePath);
            sftp.cd(directory);
        } catch (SftpException e) {
            // 目录不存在 → 递归创建
            String[] dirs = directory.split("/");
            String tempPath = basePath;
            for (String dir : dirs) {
                if (ObjectUtil.isEmpty(dir)) {
                    continue;
                }
                tempPath += "/" + dir;
                try {
                    sftp.cd(tempPath);
                } catch (SftpException ex) {
                    sftp.mkdir(tempPath);
                    sftp.cd(tempPath);
                    log.debug("创建SFTP目录：{}", tempPath);
                }
            }
        }

        sftp.put(input, sftpFileName);
        log.info("SFTP 文件上传成功：{}", sftpFileName);
    }

    // -------------------------- 下载文件 --------------------------
    public void download(String directory, String downloadFile, String saveFile) throws SftpException, IOException {
        if (ObjectUtil.isNotEmpty(directory)) {
            sftp.cd(directory);
        }
        try (FileOutputStream out = new FileOutputStream(new File(saveFile))) {
            sftp.get(downloadFile, out);
        }
    }

    public byte[] download(String directory, String downloadFile) throws SftpException, IOException {
        try (InputStream is = downloadInputStream(directory, downloadFile)) {
            return IOUtils.toByteArray(is);
        }
    }

    public InputStream downloadInputStream(String directory, String downloadFile) throws SftpException {
        if (ObjectUtil.isNotEmpty(directory)) {
            sftp.cd(directory);
        }
        return sftp.get(downloadFile);
    }

    public InputStream downloadInputStream(String fullName) throws SftpException {
        return sftp.get(fullName);
    }

    // -------------------------- 文件操作 --------------------------
    public void delete(String directory, String deleteFile) throws SftpException {
        sftp.cd(directory);
        sftp.rm(deleteFile);
    }

    public void rename(String directory, String oldName, String newName) throws SftpException {
        sftp.cd(directory);
        sftp.rename(oldName, newName);
    }

    public Vector<?> listFiles(String directory) throws SftpException {
        return sftp.ls(directory);
    }

    public boolean existFile(String fullName) {
        try {
            Vector<?> v = sftp.ls(fullName);
            return ObjectUtil.isNotEmpty(v);
        } catch (SftpException e) {
            return false;
        }
    }

    // -------------------------- getter --------------------------
    public ChannelSftp getSftp() {
        return sftp;
    }

    /**
     * 【关键】AutoCloseable 自动关闭
     * 配合 try-with-resources 自动调用 logout()
     */
    @Override
    public void close() {
        logout();
    }
}
