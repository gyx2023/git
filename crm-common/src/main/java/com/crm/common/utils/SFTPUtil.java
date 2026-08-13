package com.crm.common.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Properties;

/**
 * SFTP工具类
 * @deprecated 请使用 {@link com.crm.job.utils.SftpUtil} 替代，SftpUtil 支持 sftp.xxx / ftp_xxx 双键名配置，
 *             自动管理连接生命周期，无需手动 login/logout
 */
@Deprecated
@Slf4j
public class SFTPUtil {

    private ChannelSftp sftp;
    private Session session;
    /** SFTP 登录用户名*/
    private String username;
    /** SFTP 登录密码*/
    private String password;
    /** 私钥 */
    private String privateKey;
    /** SFTP 服务器地址IP地址*/
    private String host;
    /** SFTP 端口*/
    private int port;


    /**
     * 构造基于密码认证的sftp对象
     */
    public SFTPUtil(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /**
     * 构造基于秘钥认证的sftp对象
     */
    public SFTPUtil(String username, String host, int port, String privateKey) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.privateKey = privateKey;
    }

    public SFTPUtil(){}


    /**
     * 连接sftp服务器
     */
    public void login(){
        try {
            JSch jsch = new JSch();
            if (privateKey != null) {
                jsch.addIdentity(privateKey);// 设置私钥
            }

            session = jsch.getSession(username, host, port);
            log.info(username+"---"+host+"---"+port+"---开始登陆sftp");

            if (password != null) {
                session.setPassword(password);
            }
            Properties config = new Properties();
            config.put("userauth.gssapi-with-mic", "no");
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(2000);
            log.info("session连接成功");

            Channel channel = session.openChannel("sftp");
            channel.connect(2000);
            log.info("channel连接成功");

            sftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            log.error("SFTP登录失败", e);
        }
    }

    /**
     * 关闭连接 server
     */
    public void logout(){
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
            }
            log.info("sftp断开连接");
        }
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
            }
            log.info("sftp断开session");
        }
    }

    /**
     * 下载文件。
     * @param directory 下载目录
     * @param downloadFile 下载的文件
     * @param saveFile 存在本地的路径
     */
    public void download(String directory, String downloadFile, String saveFile) throws SftpException, FileNotFoundException{
        if (directory != null && !"".equals(directory)) {
            sftp.cd(directory);
        }
        File file = new File(saveFile);
        sftp.get(downloadFile, new FileOutputStream(file));
    }

    /**
     * 下载文件
     * @param directory 下载目录
     * @param downloadFile 下载的文件名
     * @return 字节数组
     */
    public byte[] download(String directory, String downloadFile) throws SftpException, IOException{
        InputStream is = this.downloadInputStream(directory, downloadFile);
        byte[] fileData = IOUtils.toByteArray(is);
        return fileData;
    }

    /**
     * 下载文件,返回文件流
     * @param directory 下载目录
     * @param downloadFile 下载的文件名
     * @return InputStream
     */
    public InputStream downloadInputStream(String directory, String downloadFile) throws SftpException{
        if (directory != null && !"".equals(directory)) {
            sftp.cd(directory);
        }
        return sftp.get(downloadFile);
    }

    /**
     * 将输入流的数据上传到sftp作为文件。文件完整路径=basePath+directory
     * @param basePath  服务器的基础路径
     * @param directory  上传到该目录
     * @param sftpFileName  sftp端文件名
     * @param input   输入流
     */
    public void upload(String basePath, String directory, String sftpFileName, InputStream input) throws SftpException {
        if (basePath != null && !"".equals(basePath)) {
            try {
                sftp.cd(basePath);
            } catch (SftpException e) {
                mkdirs(basePath);
                sftp.cd(basePath);
            }
        }
        if (directory != null && !"".equals(directory)) {
            try {
                sftp.cd(directory);
            } catch (SftpException e) {
                // directory是相对路径时，拼上basePath构建完整路径
                String fullPath = directory.startsWith("/") ? directory : basePath + "/" + directory;
                mkdirs(fullPath);
            }
        }
        sftp.put(input, sftpFileName);
    }

    /**
     * 递归创建多级目录
     * @param path 目录路径
     */
    private void mkdirs(String path) throws SftpException {
        String[] dirs = path.split("/");
        String tempPath = "";
        for (String dir : dirs) {
            if (dir != null && !"".equals(dir)) {
                tempPath += "/" + dir;
                try {
                    sftp.cd(tempPath);
                } catch (SftpException e) {
                    sftp.mkdir(tempPath);
                    sftp.cd(tempPath);
                }
            }
        }
    }

    /**
     * 修改文件名
     * @param directory 要修改文件所在目录
     * @param oldPath 旧名
     * @param oldPath 新名
     */
    public void rename(String directory, String oldPath,String newPath) throws SftpException{
        sftp.cd(directory);
        sftp.rename(oldPath,newPath);
    }
}

