package com.crm.job.utils;

import cn.hutool.extra.ssh.Sftp;
import com.crm.job.config.properties.SftpProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 大文件行处理器接口
 * 用于处理按行读取的文件内容
 */
@FunctionalInterface
interface LineProcessor {
    /**
     * 处理一行数据
     * @param line 行数据
     * @param lineNumber 行号
     * @return 是否继续处理下一行
     */
    boolean process(String line, long lineNumber);
}

/**
 * SFTP工具类
 * 提供SFTP连接管理和文件操作功能
 *
 * @author crm
 */
@Slf4j
@Component
public class SftpUtil {

    @Autowired
    private SftpProperties sftpProperties;

    /**
     * 获取SFTP连接
     *
     * @return SFTP连接对象
     */
    public Sftp getSftp() {
        try {
            log.info("创建SFTP连接: [host={}, port={}, username={}]", 
                    sftpProperties.getHost(), sftpProperties.getPort(), sftpProperties.getUsername());
            
            // 配置会话超时
            com.jcraft.jsch.JSch jsch = new com.jcraft.jsch.JSch();
            com.jcraft.jsch.Session session = jsch.getSession(
                    sftpProperties.getUsername(),
                    sftpProperties.getHost(),
                    sftpProperties.getPort()
            );
            session.setPassword(sftpProperties.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(sftpProperties.getTimeout());
            session.connect();
            
            // 创建SFTP连接
            return new Sftp(session);
        } catch (Exception e) {
            log.error("SFTP连接创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("SFTP连接创建失败", e);
        }
    }

    /**
     * 关闭SFTP连接
     *
     * @param sftp SFTP连接对象
     */
    public void close(Sftp sftp) {
        if (sftp != null) {
            try {
                sftp.close();
                log.info("SFTP连接已关闭");
            } catch (Exception e) {
                log.error("SFTP连接关闭失败: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 读取SFTP上的文件内容为字符串
     * 注意：此方法适合小文件，对于大文件请使用readFileByLine方法
     *
     * @param filePath 文件路径
     * @return 文件内容字符串
     */
    public String readFileToString(String filePath) {
        Sftp sftp = null;
        try {
            sftp = getSftp();
            log.info("开始读取文件: {}", filePath);
            
            try (InputStream is = sftp.getClient().get(filePath);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                if (is == null) {
                    throw new RuntimeException("文件不存在: " + filePath);
                }
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                
                String content = baos.toString(StandardCharsets.UTF_8.name());
                log.info("文件读取成功, 文件大小: {}字节", content.length());
                return content;
            }
        } catch (IOException e) {
            log.error("文件读取失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件读取失败", e);
        } catch (Exception e) {
            // 处理SFTP相关异常
            log.error("SFTP操作失败: {}", e.getMessage(), e);
            throw new RuntimeException("SFTP操作失败", e);
        } finally {
            close(sftp);
        }
    }
    
    /**
     * 按行读取SFTP上的大文件
     * 适合处理大文件，避免一次性将整个文件加载到内存
     *
     * @param filePath 文件路径
     * @param lineProcessor 行处理器，处理每一行数据
     * @return 处理的总行数
     */
    public long readFileByLine(String filePath, LineProcessor lineProcessor) {
        Sftp sftp = null;
        long lineCount = 0;
        
        try {
            sftp = getSftp();
            log.info("开始按行读取大文件: {}", filePath);
            
            try (InputStream is = sftp.getClient().get(filePath);
                 InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {
                
                if (is == null) {
                    throw new RuntimeException("文件不存在: " + filePath);
                }
                
                String line;
                while ((line = br.readLine()) != null) {
                    lineCount++;
                    // 处理进度日志，每10000行记录一次
                    if (lineCount % 10000 == 0) {
                        log.info("已处理 {} 行数据", lineCount);
                    }
                    
                    // 调用处理器处理当前行，如果返回false则停止处理
                    if (!lineProcessor.process(line, lineCount)) {
                        log.info("用户中断文件处理，已处理 {} 行", lineCount);
                        break;
                    }
                }
                
                log.info("文件读取完成, 共处理 {} 行数据", lineCount);
                return lineCount;
            }
        } catch (IOException e) {
            log.error("大文件按行读取失败: {}", e.getMessage(), e);
            throw new RuntimeException("大文件按行读取失败", e);
        } catch (Exception e) {
            // 处理SFTP相关异常
            log.error("SFTP操作失败: {}", e.getMessage(), e);
            throw new RuntimeException("SFTP操作失败", e);
        } finally {
            close(sftp);
        }
    }
    
    /**
     * 默认缓冲区大小（8KB）
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    
    /**
     * 批量处理文件数据
     * 将文件按批次读取并处理，每批包含指定数量的行
     *
     * @param filePath 文件路径
     * @param batchSize 每批处理的行数
     * @param batchProcessor 批处理器
     * @return 处理的总行数
     */
    public long processFileInBatches(String filePath, int batchSize, BatchProcessor batchProcessor) {
        return processFileInBatches(filePath, batchSize, batchProcessor, DEFAULT_BUFFER_SIZE);
    }
    
    /**
     * 批量处理文件数据（带缓冲配置）
     * 将文件按批次读取并处理，每批包含指定数量的行
     * 支持自定义缓冲区大小以优化大文件读取性能
     *
     * @param filePath 文件路径
     * @param batchSize 每批处理的行数
     * @param batchProcessor 批处理器
     * @param bufferSize 缓冲区大小（字节）
     * @return 处理的总行数
     */
    public long processFileInBatches(String filePath, int batchSize, BatchProcessor batchProcessor, int bufferSize) {
        Sftp sftp = null;
        long lineCount = 0;
        List<String> batch = new java.util.ArrayList<>(batchSize);
        long startTime = System.currentTimeMillis();
        long lastLogTime = startTime;
        
        try {
            sftp = getSftp();
            log.info("开始批量处理文件: {}, 每批 {} 行, 缓冲区大小: {}KB", 
                    filePath, batchSize, bufferSize / 1024);
            
            // 使用更大的缓冲区以提高读取性能
            try (InputStream is = sftp.getClient().get(filePath);
                 InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr, bufferSize)) {
                
                if (is == null) {
                    throw new RuntimeException("文件不存在: " + filePath);
                }
                
                String line;
                while ((line = br.readLine()) != null) {
                    lineCount++;
                    batch.add(line);
                    
                    // 当批次达到指定大小时，进行处理
                    if (batch.size() >= batchSize) {
                        // 记录处理速度
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastLogTime >= 30000) { // 每30秒记录一次速度
                            double seconds = (currentTime - startTime) / 1000.0;
                            double linesPerSecond = lineCount / seconds;
                            log.info("处理进度: {} 行, 处理速度: {:.2f} 行/秒", 
                                    lineCount, linesPerSecond);
                            lastLogTime = currentTime;
                        }
                        
                        log.info("处理批次, 当前累计 {} 行", lineCount);
                        boolean continueProcessing = batchProcessor.processBatch(new java.util.ArrayList<>(batch));
                        batch.clear(); // 清空批次
                        
                        if (!continueProcessing) {
                            log.info("用户中断文件处理，已处理 {} 行", lineCount);
                            break;
                        }
                    }
                }
                
                // 处理最后一批不足batchSize的数据
                if (!batch.isEmpty()) {
                    log.info("处理最后一批数据, 共 {} 行", batch.size());
                    batchProcessor.processBatch(batch);
                    batch.clear(); // 显式清空最后一批数据，释放内存
                }
                
                // 计算总处理时间和平均速度
                long endTime = System.currentTimeMillis();
                double totalSeconds = (endTime - startTime) / 1000.0;
                double avgSpeed = totalSeconds > 0 ? lineCount / totalSeconds : 0;
                
                log.info("文件批量处理完成, 共处理 {} 行数据, 总耗时: {:.2f}秒, 平均速度: {:.2f}行/秒", 
                        lineCount, totalSeconds, avgSpeed);
                
                // 清理资源
                batch = null;
                
                return lineCount;
            }
        } catch (IOException e) {
            log.error("文件批量处理失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件批量处理失败", e);
        } catch (Exception e) {
            // 处理SFTP相关异常
            log.error("SFTP操作失败: {}", e.getMessage(), e);
            throw new RuntimeException("SFTP操作失败", e);
        } finally {
            close(sftp);
        }
    }
    
    /**
     * 批处理器接口
     * 用于处理批量数据
     */
    @FunctionalInterface
    public interface BatchProcessor {
        /**
         * 处理一批数据
         * @param batchData 批次数据列表
         * @return 是否继续处理
         */
        boolean processBatch(List<String> batchData);
    }

    /**
     * 列出SFTP目录下的所有文件
     *
     * @param directory 目录路径
     * @return 文件列表
     */
    public List<String> listFiles(String directory) {
        Sftp sftp = null;
        try {
            sftp = getSftp();
            log.info("开始列出目录文件: {}", directory);
            
            List<String> files = sftp.ls(directory);
            log.info("目录文件列表: {}", files);
            return files;
        } catch (Exception e) {
            log.error("列出目录文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("列出目录文件失败", e);
        } finally {
            close(sftp);
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    public boolean exists(String filePath) {
        Sftp sftp = null;
        try {
            sftp = getSftp();
            // 使用ls方法检查文件是否存在
            List<String> files = sftp.ls(filePath);
            return files != null && !files.isEmpty();
        } catch (Exception e) {
            // 检查是否为SftpException且错误码为2（文件不存在）
            if (e instanceof com.jcraft.jsch.SftpException && ((com.jcraft.jsch.SftpException)e).id == 2) {
                return false;
            }
            log.error("检查文件存在性失败: {}", e.getMessage(), e);
            return false;
        } finally {
            close(sftp);
        }
    }
    
    /**
     * 根据日期生成文件夹路径
     * 格式: /opt/home/bi/dataout/ZQ/YYYYMMDD
     *
     * @param date 指定日期
     * @return 文件夹路径
     */
    public String generateFolderPath(Date date) {
        SimpleDateFormat folderSdf = new SimpleDateFormat(sftpProperties.getFolderFormat());
        String dateStr = folderSdf.format(date);
        return sftpProperties.getDataRootPath() + "/" + dateStr;
    }
    
    /**
     * 根据日期生成文件名
     * 格式: GRP_GROUP_INFO_YYYYMMDD.txt
     *
     * @param date 指定日期
     * @return 文件名
     */
    public String generateFileName(Date date) {
        SimpleDateFormat fileSdf = new SimpleDateFormat(sftpProperties.getFileDateFormat());
        String dateStr = fileSdf.format(date);
        return sftpProperties.getFilePrefix() + "_" + dateStr + sftpProperties.getFileExtension();
    }
    
    /**
     * 根据日期生成完整的文件路径
     * 格式: /opt/home/bi/dataout/ZQ/YYYYMMDD/GRP_GROUP_INFO_YYYYMMDD.txt
     *
     * @param date 指定日期
     * @return 完整文件路径
     */
    public String generateFullFilePath(Date date) {
        String folderPath = generateFolderPath(date);
        String fileName = generateFileName(date);
        return folderPath + "/" + fileName;
    }
    
    /**
     * 获取当前日期的完整文件路径
     *
     * @return 当前日期的完整文件路径
     */
    public String getCurrentDateFilePath() {
        return generateFullFilePath(new Date());
    }
    
    /**
     * 获取指定日期的文件夹下所有文件
     *
     * @param date 指定日期
     * @return 文件列表
     */
    public List<String> listFilesByDate(Date date) {
        String folderPath = generateFolderPath(date);
        return listFiles(folderPath);
    }
}