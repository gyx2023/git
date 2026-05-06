package com.crm.job.utils;

import cn.hutool.core.util.ObjectUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class SftpWebUtil {

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

    // -------------------------- 构造器（保留原有） --------------------------
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

    public SftpWebUtil() {}

    // -------------------------- 【重点】从数据库配置创建实例 --------------------------
    /**
     * 从 sys_param 数据库加载 ftp_xxx 配置，创建 SFTP 实例
     * @param configType 配置类型：tc_wx_strategy
     */
    public static SftpWebUtil createByDbConfig(String configType) {
        // 这里复用你之前 SftpUtil 的数据库读取逻辑（直接调即可）
        SftpUtil sftpUtil = new SftpUtil();
        sftpUtil.setActiveConfigType(configType);

        String host = sftpUtil.getHost();
        int port = sftpUtil.getPort();
        String username = sftpUtil.getUsername();
        String password = sftpUtil.getPassword();

        return new SftpWebUtil(username, password, host, port);
    }

    // -------------------------- 连接/关闭 --------------------------
    public void login() {
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
            session.connect(5000); // 超时5秒
            Channel channel = session.openChannel("sftp");
            channel.connect(5000);
            sftp = (ChannelSftp) channel;
            log.info("SFTP 登录成功");
        } catch (JSchException e) {
            log.error("SFTP 登录失败：{}", e.getMessage(), e);
            throw new RuntimeException("SFTP 登录失败", e);
        }
    }

    public void logout() {
        if (sftp != null && sftp.isConnected()) {
            sftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        log.info("SFTP 连接已关闭");
    }

    // -------------------------- 上传（适配你数据库的 ftp_directory） --------------------------
    /**
     * 上传文件（自动使用数据库配置的 ftp_directory）
     * @param sftpFileName 远程文件名
     * @param input 输入流
     */
    public void upload(String basePath,String directory, String sftpFileName, InputStream input) throws SftpException{
        try {
            sftp.cd(basePath);
            sftp.cd(directory);
        } catch (SftpException e) {
            //目录不存在，则创建文件夹
            String [] dirs=directory.split("/");
            String tempPath=basePath;
            for(String dir:dirs){
                if(null== dir || "".equals(dir)) continue;
                tempPath+="/"+dir;
                try{
                    sftp.cd(tempPath);
                }catch(SftpException ex){
                    sftp.mkdir(tempPath);
                    sftp.cd(tempPath);
                }
            }
        }
        sftp.put(input, sftpFileName);  //上传文件
    }

    // 递归创建目录
    private void mkdirs(String dir) throws SftpException {
        String[] dirs = dir.split("/");
        String temp = "";
        for (String d : dirs) {
            if (ObjectUtil.isEmpty(d)) continue;
            temp += "/" + d;
            try {
                sftp.cd(temp);
            } catch (SftpException e) {
                sftp.mkdir(temp);
                sftp.cd(temp);
            }
        }
    }

    // -------------------------- 下载 --------------------------
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

    // -------------------------- 删除/重命名/列表/存在性判断 --------------------------
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

    // -------------------------- 对外获取连接（可选） --------------------------
    public ChannelSftp getSftp() {
        return sftp;
    }
}
