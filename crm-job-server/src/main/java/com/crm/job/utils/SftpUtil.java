package com.crm.job.utils;

import cn.hutool.extra.ssh.Sftp;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.mapper.param.SysParamMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class SftpUtil implements InitializingBean {

    @Resource
    private SysParamMapper sysParamMapper;

    // SFTP配置参数的键前缀
    private static final String SFTP_CONFIG_PREFIX = "sftp.";
    /**
     * -- GETTER --
     *  获取配置类型
     *
     * @return 配置类型标识
     */
    // SFTP配置类型标识（默认为"SFTP"，可通过setter方法修改）
    @Getter
    private String configType = "SFTP";
    // 多SFTP配置缓存，按配置类型分组
    private final Map<String, Map<String, String>> sftpConfigCaches = new HashMap<>();

    // 当前活跃的配置类型
    @Getter
    private String activeConfigType = "SFTP";

    // 默认配置值
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 22;
    private static final String DEFAULT_USERNAME = "";
    private static final String DEFAULT_PASSWORD = "";
    private static final int DEFAULT_TIMEOUT = 30000;
    private static final String DEFAULT_DATA_ROOT_PATH = "/opt/home/bi/dataout/ZQ";
    private static final String DEFAULT_FOLDER_FORMAT = "yyyyMMdd";
    private static final String DEFAULT_FILE_PREFIX = "GRP_GROUP_INFO";
    private static final String DEFAULT_FILE_DATE_FORMAT = "yyyyMMdd";
    private static final String DEFAULT_FILE_EXTENSION = ".txt";

    /**
     * 默认缓冲区大小（8KB）
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    // 缓存锁，用于保证缓存初始化和刷新的线程安全
    private final ReentrantLock cacheLock = new ReentrantLock();

    // 定时刷新配置的线程池
    private ScheduledExecutorService scheduler;
    // 配置刷新间隔（分钟）
    private static final long CONFIG_REFRESH_INTERVAL = 30; // 30分钟刷新一次

    /**
     * 设置当前活跃的配置类型
     * @param activeConfigType 配置类型标识
     */
    public void setActiveConfigType(String activeConfigType) {
        if (activeConfigType != null && !activeConfigType.trim().isEmpty()) {
            String oldType = this.activeConfigType;
            this.activeConfigType = activeConfigType.trim();
            log.info("SFTP活跃配置类型已从 '{}' 更改为 '{}'", oldType, activeConfigType);

            // 如果该配置类型的缓存不存在，则初始化它
            if (!sftpConfigCaches.containsKey(activeConfigType)) {
                refreshConfigCache(activeConfigType);
            }
        } else {
            log.warn("尝试设置无效的配置类型，保持当前值: {}", this.activeConfigType);
        }
    }

    /**
     * 设置配置类型（向后兼容方法）
     * 此方法是setActiveConfigType的别名，用于保持向后兼容性
     *
     * @param configType 配置类型
     * @deprecated 使用setActiveConfigType代替
     */
    @Deprecated
    public void setConfigType(String configType) {
        setActiveConfigType(configType);
    }

    /**
     * 初始化指定配置类型的缓存
     * 如果缓存不存在，则创建并刷新
     * @param configType 配置类型
     */
    private void initConfigCache(String configType) {
        if (!sftpConfigCaches.containsKey(configType)) {
            log.info("初始化SFTP配置类型 '{}' 的缓存", configType);
            refreshConfigCache(configType);
        }
    }

    /**
     * 初始化当前活跃配置类型的缓存
     * 保持向后兼容性的方法
     */
    private void initConfigCache() {
        initConfigCache(activeConfigType);
    }

    /**
     * 刷新指定配置类型的缓存
     * @param configType 配置类型
     */
    public void refreshConfigCache(String configType) {
        cacheLock.lock();
        try {
            log.info("刷新SFTP配置类型 '{}' 的缓存", configType);

            // 按type查询所有SFTP配置
            List<SysParamDo> sftpParams = sysParamMapper.selectList(
                new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getType, configType)
            );
            log.debug("使用配置类型 '{}' 查询系统参数", configType);

            Map<String, String> newCache = new HashMap<>();
            if (sftpParams != null && !sftpParams.isEmpty()) {
                log.info("找到 {} 个系统参数记录", sftpParams.size());
                int loadedCount = 0;

                for (SysParamDo param : sftpParams) {
                    if (param.getCode() != null && param.getValue() != null &&
                        param.getCode().startsWith(SFTP_CONFIG_PREFIX)) {
                        // 验证配置值不为空
                        String value = param.getValue().trim();
                        if (!value.isEmpty()) {
                            newCache.put(param.getCode(), value);
                            loadedCount++;
                            log.debug("加载SFTP配置: {}={}", param.getCode(), value);
                        } else {
                            log.warn("忽略空值配置项: {}", param.getCode());
                        }
                    }
                }

                if (loadedCount == 0) {
                    log.warn("未找到有效的SFTP配置项，将使用默认值");
                }
            } else {
                log.warn("未找到类型为 '{}' 的系统参数配置，将使用默认值", configType);
            }

            // 更新缓存
            sftpConfigCaches.put(configType, newCache);
            log.info("SFTP配置类型 '{}' 的缓存刷新完成，加载了 {} 个配置项", configType, newCache.size());
        } catch (Exception e) {
            log.error("刷新SFTP配置类型 '{}' 的缓存失败: {}", configType, e.getMessage(), e);
            // 确保即使失败也有一个空映射，而不是没有映射
            sftpConfigCaches.put(configType, new HashMap<>());
        } finally {
            cacheLock.unlock();
        }
    }

    /**
     * 刷新当前活跃配置类型的缓存
     * 保持向后兼容性的方法
     */
    public void refreshConfigCache() {
        refreshConfigCache(activeConfigType);
    }

    /**
     * 从指定配置类型的缓存获取SFTP配置值
     * 如果缓存未初始化或获取失败，回退到直接查询数据库
     *
     * @param configType 配置类型
     * @param paramKey 配置键名（不包含前缀）
     * @param defaultValue 默认值
     * @return 配置值
     */
    private String getSftpParam(String configType, String paramKey, String defaultValue) {
        // 确保缓存已初始化
        initConfigCache(configType);

        // 从缓存中获取配置
        Map<String, String> configCache = sftpConfigCaches.get(configType);
        if (configCache != null) {
            String value = configCache.get(paramKey);
            if (value != null) {
                return value;
            }
            log.debug("SFTP配置类型 '{}' 的配置项 {} 在缓存中未找到，使用默认值: {}",
                     configType, paramKey, defaultValue);
        } else {
            log.warn("SFTP配置类型 '{}' 的缓存未初始化，回退到直接查询数据库", configType);
            // 缓存未初始化时，回退到直接查询数据库
        //    String fullKey = SFTP_CONFIG_PREFIX + paramKey;
            try {
                SysParamDo param = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>()
                        .eq(SysParamDo::getType, configType)
                        .eq(SysParamDo::getCode, paramKey));
                if (param != null && param.getValue() != null) {
                    return param.getValue();
                }
            } catch (Exception e) {
                log.error("获取SFTP配置类型 '{}' 的配置项 {} 失败: {}",
                         configType, paramKey, e.getMessage());
            }
        }
        return defaultValue;
    }

    /**
     * 从当前活跃配置类型的缓存获取SFTP配置值
     * 保持向后兼容性的方法
     *
     * @param paramKey 配置键名（不包含前缀）
     * @param defaultValue 默认值
     * @return 配置值
     */
    private String getSftpParam(String paramKey, String defaultValue) {
        return getSftpParam(activeConfigType, paramKey, defaultValue);
    }

    /**
     * 获取指定配置类型的SFTP主机地址
     *
     * @param configType 配置类型
     * @return 主机地址
     */
    public String getHost(String configType) {
        return getSftpParam(configType, "host", DEFAULT_HOST);
    }

    /**
     * 获取SFTP主机地址（使用当前活跃配置类型）
     *
     * @return SFTP主机地址
     */
    public String getHost() {
        return getHost(activeConfigType);
    }

    /**
     * 获取指定配置类型的SFTP端口
     *
     * @param configType 配置类型
     * @return 端口号
     */
    public int getPort(String configType) {
        String portStr = getSftpParam(configType, "port", String.valueOf(DEFAULT_PORT));
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            log.error("SFTP配置类型 '{}' 的端口号格式错误: {}, 使用默认值: {}",
                     configType, portStr, DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }

    /**
     * 获取SFTP端口（使用当前活跃配置类型）
     *
     * @return SFTP端口号
     */
    public int getPort() {
        return getPort(activeConfigType);
    }

    /**
     * 获取指定配置类型的SFTP用户名
     *
     * @param configType 配置类型
     * @return 用户名
     */
    public String getUsername(String configType) {
        return getSftpParam(configType, "username", DEFAULT_USERNAME);
    }

    /**
     * 获取SFTP用户名（使用当前活跃配置类型）
     *
     * @return SFTP用户名
     */
    public String getUsername() {
        return getUsername(activeConfigType);
    }

    /**
     * 获取指定配置类型的SFTP密码
     *
     * @param configType 配置类型
     * @return 密码
     */
    public String getPassword(String configType) {
        return getSftpParam(configType, "password", DEFAULT_PASSWORD);
    }

    /**
     * 获取SFTP密码（使用当前活跃配置类型）
     *
     * @return SFTP密码
     */
    public String getPassword() {
        return getPassword(activeConfigType);
    }

    /**
     * 获取指定配置类型的SFTP连接超时时间
     *
     * @param configType 配置类型
     * @return 超时时间（毫秒）
     */
    public int getTimeout(String configType) {
        String timeoutStr = getSftpParam(configType, "timeout", String.valueOf(DEFAULT_TIMEOUT));
        try {
            return Integer.parseInt(timeoutStr);
        } catch (NumberFormatException e) {
            log.error("SFTP配置类型 '{}' 的超时时间格式错误: {}, 使用默认值: {}",
                     configType, timeoutStr, DEFAULT_TIMEOUT);
            return DEFAULT_TIMEOUT;
        }
    }

    /**
     * 获取SFTP连接超时时间（使用当前活跃配置类型）
     *
     * @return 超时时间（毫秒）
     */
    public int getTimeout() {
        return getTimeout(activeConfigType);
    }

    /**
     * 获取指定配置类型的数据文件根路径
     *
     * @param configType 配置类型
     * @return 数据文件根路径
     */
    public String getDataRootPath(String configType) {
        return getSftpParam(configType, "sftp.dataRootPath", DEFAULT_DATA_ROOT_PATH);
    }

    /**
     * 获取数据文件根路径（使用当前活跃配置类型）
     *
     * @return 数据文件根路径
     */
    public String getDataRootPath() {
        return getDataRootPath(activeConfigType);
    }

    /**
     * 获取指定配置类型的文件夹日期格式
     *
     * @param configType 配置类型
     * @return 文件夹日期格式
     */
    public String getFolderFormat(String configType) {
        return getSftpParam(configType, "folderFormat", DEFAULT_FOLDER_FORMAT);
    }

    /**
     * 获取指定配置类型的文件名前缀
     *
     * @param configType 配置类型
     * @return 文件名前缀
     */
    public String getFilePrefix(String configType) {
        return getSftpParam(configType, "filePrefix", DEFAULT_FILE_PREFIX);
    }

    /**
     * 获取文件名前缀（使用当前活跃配置类型）
     *
     * @return 文件名前缀
     */
    public String getFilePrefix() {
        return getFilePrefix(activeConfigType);
    }

    /**
     * 获取指定配置类型的文件名日期格式
     *
     * @param configType 配置类型
     * @return 文件名日期格式
     */
    public String getFileDateFormat(String configType) {
        return getSftpParam(configType, "fileDateFormat", DEFAULT_FILE_DATE_FORMAT);
    }

    /**
     * 获取文件名日期格式（使用当前活跃配置类型）
     *
     * @return 文件名日期格式
     */
    public String getFileDateFormat() {
        return getFileDateFormat(activeConfigType);
    }

    /**
     * 获取指定配置类型的文件扩展名
     *
     * @param configType 配置类型
     * @return 文件扩展名
     */
    public String getFileExtension(String configType) {
        return getSftpParam(configType, "fileExtension", DEFAULT_FILE_EXTENSION);
    }

    /**
     * 获取文件扩展名（使用当前活跃配置类型）
     *
     * @return 文件扩展名
     */
    public String getFileExtension() {
        return getFileExtension(activeConfigType);
    }

    /**
     * Spring初始化完成后执行
     * 启动定时刷新配置的任务
     */
    @Override
    public void afterPropertiesSet() {
        log.info("初始化SFTP配置管理，启动定时刷新任务");

        // 创建单线程调度池
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "sftp-config-refresh-thread");
            thread.setDaemon(true); // 设置为守护线程，避免阻止JVM关闭
            return thread;
        });

        // 定时执行配置刷新任务
        // 首次延迟0秒执行，之后每间隔CONFIG_REFRESH_INTERVAL分钟执行一次
        scheduler.scheduleAtFixedRate(() -> {
            // 刷新所有已加载的配置类型
            for (String configType : new ArrayList<>(sftpConfigCaches.keySet())) {
                refreshConfigCache(configType);
            }
        }, 0, CONFIG_REFRESH_INTERVAL, TimeUnit.MINUTES);

        log.info("SFTP配置定时刷新任务启动成功，刷新间隔: {}分钟", CONFIG_REFRESH_INTERVAL);
    }

    /**
     * 使用指定配置类型获取SFTP连接
     * 此方法可以在不改变活跃配置类型的情况下使用不同的SFTP配置
     *
     * @param configType 配置类型
     * @return SFTP连接对象
     */
    public Sftp getSftp(String configType) {
        try {
            String host = getSftpParam(configType, "sftp.host", DEFAULT_HOST);
            String portStr = getSftpParam(configType, "sftp.port", String.valueOf(DEFAULT_PORT));
            String username = getSftpParam(configType, "sftp.username", DEFAULT_USERNAME);
            String password = getSftpParam(configType, "sftp.password", DEFAULT_PASSWORD);
            String timeoutStr = getSftpParam(configType, "sftp.timeout", String.valueOf(DEFAULT_TIMEOUT));

            int port;
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                log.error("SFTP配置类型 '{}' 的端口号格式错误: {}, 使用默认值: {}",
                         configType, portStr, DEFAULT_PORT);
                port = DEFAULT_PORT;
            }

            int timeout;
            try {
                timeout = Integer.parseInt(timeoutStr);
            } catch (NumberFormatException e) {
                log.error("SFTP配置类型 '{}' 的超时时间格式错误: {}, 使用默认值: {}",
                         configType, timeoutStr, DEFAULT_TIMEOUT);
                timeout = DEFAULT_TIMEOUT;
            }

            log.info("使用配置类型 '{}' 创建SFTP连接: [host={}, port={}, username={}]",
                     configType, host, port, username);

            // 配置会话超时
            com.jcraft.jsch.JSch jsch = new com.jcraft.jsch.JSch();
            com.jcraft.jsch.Session session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(timeout);
            session.connect();

            // 创建SFTP连接
            return new Sftp(session);
        } catch (Exception e) {
            log.error("使用配置类型 '{}' 创建SFTP连接失败: {}", configType, e.getMessage(), e);
            throw new RuntimeException("SFTP连接创建失败", e);
        }
    }

    /**
     * 获取SFTP连接（使用当前活跃配置类型）
     *
     * @return SFTP连接对象
     */
    public Sftp getSftp() {
        return getSftp(activeConfigType);
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
     * 使用指定配置类型批量处理文件数据
     * 将文件按批次读取并处理，每批包含指定数量的行
     *
     * @param configType 配置类型
     * @param filePath 文件路径
     * @param batchSize 每批处理的行数
     * @param batchProcessor 批处理器
     * @return 处理的总行数
     */
    public long processFileInBatches(String configType, String filePath, int batchSize, BatchProcessor batchProcessor) {
        return processFileInBatches(configType, filePath, batchSize, batchProcessor, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 批量处理文件数据（使用当前活跃配置类型）
     * 将文件按批次读取并处理，每批包含指定数量的行
     *
     * @param filePath 文件路径
     * @param batchSize 每批处理的行数
     * @param batchProcessor 批处理器
     * @return 处理的总行数
     */
    public long processFileInBatches(String filePath, int batchSize, BatchProcessor batchProcessor) {
        return processFileInBatches(activeConfigType, filePath, batchSize, batchProcessor, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 使用指定配置类型批量处理文件数据（带缓冲配置）
     * 将文件按批次读取并处理，每批包含指定数量的行
     * 支持自定义缓冲区大小以优化大文件读取性能
     *
     * @param configType 配置类型
     * @param filePath 文件路径
     * @param batchSize 每批处理的行数
     * @param batchProcessor 批处理器
     * @param bufferSize 缓冲区大小（字节）
     * @return 处理的总行数
     */
    public long processFileInBatches(String configType, String filePath, int batchSize, BatchProcessor batchProcessor, int bufferSize) {
        Sftp sftp = null;
        long lineCount = 0;
        List<String> batch = new ArrayList<>(batchSize);
        long startTime = System.currentTimeMillis();
        long lastLogTime = startTime;

        try {
            sftp = getSftp(configType);
            log.info("使用配置类型 '{}' 开始批量处理文件: {}, 每批 {} 行, 缓冲区大小: {}KB",
                    configType, filePath, batchSize, bufferSize / 1024);

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
                        boolean continueProcessing = batchProcessor.processBatch(new ArrayList<>(batch));
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
     * 检查指定配置类型的文件是否存在
     *
     * @param configType 配置类型
     * @param filePath 文件路径
     * @return 是否存在
     */
    public boolean exists(String configType, String filePath) {
        Sftp sftp = null;
        try {
            sftp = getSftp(configType);
            // 使用ls方法检查文件是否存在
            List<String> files = sftp.ls(filePath);
            return files != null && !files.isEmpty();
        } catch (Exception e) {
            // 检查是否为SftpException且错误码为2（文件不存在）
            if (e instanceof com.jcraft.jsch.SftpException && ((com.jcraft.jsch.SftpException)e).id == 2) {
                return false;
            }
            log.error("使用配置类型 '{}' 检查文件存在性失败: {}", configType, e.getMessage(), e);
            return false;
        } finally {
            close(sftp);
        }
    }

    /**
     * 检查文件是否存在（使用当前活跃配置类型）
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    public boolean exists(String filePath) {
        return exists(activeConfigType, filePath);
    }

    /**
     * 根据日期生成指定配置类型的文件夹路径
     * 格式: /opt/home/bi/dataout/ZQ/YYYYMMDD
     *
     * @param configType 配置类型
     * @param date 指定日期
     * @return 文件夹路径
     */
    public String generateFolderPath(String configType, Date date) {
        SimpleDateFormat folderSdf = new SimpleDateFormat(getFolderFormat(configType));
        String dateStr = folderSdf.format(date);
        return getDataRootPath(configType) + "/" + dateStr;
    }



    /**
     * 上传文件到SFTP服务器
     *
     * @param configType 配置类型，如果为null或空字符串则使用默认配置
     * @param localFilePath 本地文件路径
     * @param remoteFilePath 远程文件路径
     */
    public void uploadFile(String configType, String localFilePath, String remoteFilePath, String date) {
        Sftp sftp = null;
        try {
            // 如果configType为null或空字符串，则使用默认配置类型
            String actualConfigType = (configType == null || configType.trim().isEmpty()) ? activeConfigType : configType;
            sftp = getSftp(actualConfigType);
            String remortDir = getSftpParam(configType,"sftp.directory","");
            String remotePath = remortDir.endsWith("/") ? remortDir + date + "/" + remoteFilePath : remortDir + "/" + date + "/" + remoteFilePath;

            // 获取远程目录路径（不包含文件名）
            String remoteDir = remotePath.substring(0, remotePath.lastIndexOf("/"));

            // 检查远程目录是否存在，如果不存在则创建
            if (!sftp.exist(remoteDir)) {
                sftp.mkdir(remoteDir); // 创建目录
                log.info("创建远程目录: {}", remoteDir);
            }

            sftp.getClient().put(localFilePath, remotePath);
            log.info("文件上传成功，配置类型: {}, 本地文件: {}, 远程文件: {}", actualConfigType, localFilePath, remotePath);
        } catch (Exception e) {
            String actualConfigType = (configType == null || configType.trim().isEmpty()) ? activeConfigType : configType;
            log.error("使用配置类型 '{}' 上传文件失败: {}", actualConfigType, e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        } finally {
            close(sftp);
        }
    }

    /**
     * 从SFTP服务器下载文件到本地
     *
     * @param configType 配置类型
     * @param remoteFilePath 远程文件路径
     * @param localFilePath 本地文件路径
     */
    public void downloadFile(String configType, String remoteFilePath, String localFilePath) {
        Sftp sftp = null;
        String msg = "";
        try {
            sftp = getSftp(configType);
            // 使用ls方法检查文件是否存在
            List<String> files = sftp.ls(remoteFilePath);
            if(files == null || files.isEmpty()) {
                msg = "远程文件不存在: " + remoteFilePath;
                throw new RuntimeException("远程文件不存在: " + remoteFilePath);
            }

            sftp = getSftp(configType);
            log.info("开始从SFTP下载文件，配置类型: {}, 远程文件: {}, 本地文件: {}",
                    configType, remoteFilePath, localFilePath);

            // 确保本地文件的父目录存在
            File localFile = new File(localFilePath);
            File parentDir = localFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                log.info("创建本地目录: {}", parentDir.getAbsolutePath());
            }

            // 下载文件
            sftp.getClient().get(remoteFilePath, localFilePath);

            // 验证文件是否下载成功
            if (!localFile.exists()) {
                msg = "文件下载失败，本地文件不存在: " + localFilePath;
                throw new RuntimeException("文件下载失败，本地文件不存在: " + localFilePath);
            }

            long fileSize = localFile.length();
            log.info("文件下载成功，配置类型: {}, 远程文件: {}, 本地文件: {}, 文件大小: {} bytes",
                    configType, remoteFilePath, localFilePath, fileSize);
        } catch (Exception e) {
            log.error("使用配置类型 '{}' 下载文件失败，远程文件: {}, 错误: {}",
                    configType, remoteFilePath, e.getMessage(), e);
            throw new RuntimeException(msg);
        } finally {
            close(sftp);
        }
    }

    /**
     * 从SFTP服务器下载文件到本地（使用当前活跃配置类型）
     *
     * @param remoteFilePath 远程文件路径
     * @param localFilePath 本地文件路径
     */
    public void downloadFile(String remoteFilePath, String localFilePath) {
        downloadFile(activeConfigType, remoteFilePath, localFilePath);
    }


}
