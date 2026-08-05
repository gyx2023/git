package com.crm.job.service.file;

import com.crm.job.base.AbstractFtpImportJob;
import com.crm.job.service.warning.IWarningStartService;
import com.crm.job.utils.GroupFileParser;
import com.crm.job.utils.SftpWebUtil;
import com.crm.system.domain.dao.FtpBroadbandMarketCust;
import com.crm.system.domain.dao.FtpBroadbandMarketUser;
import com.crm.system.mapper.FtpBroadbandMarketCustMapper;
import com.crm.system.mapper.FtpBroadbandMarketUserMapper;
import com.crm.system.mapper.sys.SysCodeMapper;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.utils.Constants;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 宽带专项营销文件导入 + 工作流启动
 * <p>
 * 流程：解析账期 → 下载客户/用户文件 → 解析入库 → 重建产品字典 → 调用 warningStartService 启动流程
 * <p>
 * 流程启动委托给 CustSalesWarningStrategy + WarningStartService（single_approval 通用审批流程），
 * 不再直接操作 Flowable API。
 * <p>
 * XXL-Job 参数：只传 yyyyMM 账期（如 202607），不传则默认上月
 *
 * @author crm
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CustSalesFileJob extends AbstractFtpImportJob<FtpBroadbandMarketCust> {

    // ==================== 文件常量 ====================
    private static final String CUST_FILE_NAME = "TC_CUST_BROADBAND_MARKETING_M_";
    private static final String USER_FILE_NAME = "TC_USER_BROADBAND_MARKETING_M_";

    /** 用户文件本地路径（download 时赋值，afterImport 中使用） */
    private String userLocalPath;

    /** 空串归一缓存：类 → 该类全部 String 字段（构建时一次性 setAccessible，线程安全） */
    private static final Map<Class<?>, Field[]> BLANK_STRING_FIELD_CACHE = new ConcurrentHashMap<>();

    // ==================== 注入 ====================
    private final ISysParamService sysParamService;

    private final FtpBroadbandMarketCustMapper ftpBroadbandMarketCustMapper;

    private final FtpBroadbandMarketUserMapper ftpBroadbandMarketUserMapper;

    private final SysCodeMapper sysCodeMapper;

    private final IWarningStartService warningStartService;

    private final GroupFileParser groupFileParser;

    // ==================== AbstractFtpImportJob 实现 ====================

    @Override
    protected String getJobName() { return "custSalesFileJob"; }

    @Override
    protected String getConfigType() { return Constants.JF_COMMON_FTP; }

    @Override
    protected String getFileDateType() { return FILE_DATE_TYPE_MONTHLY; }

    @Override
    protected String getSeparator() { return "\\x1b"; }

    @Override
    protected String getFileName(String dateStr) {
        return CUST_FILE_NAME + dateStr + ".txt";
    }

    @Override
    protected FtpBroadbandMarketCust parseRow(String[] cols) { return null; }

    /**
     * 行解析：按字段声明顺序（与文件列一致）映射为客户实体。
     */
    @Override
    protected FtpBroadbandMarketCust parseRowFromLine(String line) {
        return groupFileParser.parseLine(line, FtpBroadbandMarketCust.class, getSeparator());
    }

    /**
     * 客户实体入库前逐条处理：空串归一为 null（避免空串写入数值列，行为与用户文件一致）
     */
    @Override
    protected void preProcess(FtpBroadbandMarketCust entity) {
        normalizeBlankToNull(entity);
    }

    @Override
    protected int doInsert(List<FtpBroadbandMarketCust> batch) {
        ftpBroadbandMarketCustMapper.insertBatch(batch);
        return batch.size();
    }

    @Override
    protected int getDefaultDaysBefore() { return 30; } // 默认上月

    @Override
    protected int getBatchSize() { return 1000; }

    // ==================== 覆盖 download()：使用 SftpWebUtil 下载两个文件 ====================

    /**
     * 使用 SftpWebUtil 下载客户文件 + 用户文件
     * 返回客户文件路径（基类用其解析入库）
     */
    @Override
    protected String download(String configType, String dateStr, String fileName) {
        Map<String, String> param = sysParamService.findValueMapsByType(configType);
        SftpWebUtil ftpUtil = new SftpWebUtil(
            param.get("sftp.username"),
            param.get("sftp.password"),
            param.get("sftp.host"),
            MapUtils.getIntValue(param, "sftp.port")
        );

        try {
            log.info("宽带专项营销下载文件开始");
            ftpUtil.login();

            String dataRootPath = param.get("sftp.dataRootPath");
            if (dataRootPath != null && !dataRootPath.endsWith("/") && !dataRootPath.endsWith("\\")) {
                dataRootPath = dataRootPath + "/";
            }
            // 下载客户文件
            String custFileDirectory = dataRootPath + dateStr;
            String custLocalPath = MapUtils.getString(param, "sftp.dataLocalPath") + File.separator + fileName;
            log.info("开始下载客户文件: {}", custFileDirectory + "/" + fileName);
            ftpUtil.download(custFileDirectory, fileName, custLocalPath);
            log.info("客户文件下载成功: {}", custLocalPath);

            // 下载用户文件
            String userFileName = USER_FILE_NAME + dateStr + ".txt";
            String userFileDirectory = dataRootPath + dateStr;
            this.userLocalPath = MapUtils.getString(param, "sftp.dataLocalPath") + File.separator + userFileName;
            log.info("开始下载用户文件: {}", userFileDirectory + "/" + userFileName);
            ftpUtil.download(userFileDirectory, userFileName, this.userLocalPath);
            log.info("用户文件下载成功: {}", this.userLocalPath);

            if (!new File(custLocalPath).exists()) {
                log.error("【{}】客户文件下载后不存在: {}", getJobName(), custLocalPath);
                return null;
            }
            return custLocalPath;
        } catch (Exception e) {
            log.error("宽带专项营销文件下载失败", e);
            return null;
        } finally {
            ftpUtil.logout();
        }
    }

    // ==================== 覆盖 beforeImport()：清理历史数据 ====================

    @Override
    protected void beforeImport() {
        log.info("开始清理{}账期历史数据", currentDateStr);
        ftpBroadbandMarketCustMapper.deleteByStatCycle(currentDateStr);
        ftpBroadbandMarketUserMapper.deleteByStatCycle(currentDateStr);
        log.info("{}账期历史数据清理完成", currentDateStr);
    }

    // ==================== 覆盖 afterImport()：用户文件入库 + 重建字典 + 委托启动流程 ====================

    @Override
    protected void afterImport(int custCount) {
        // 1. 解析用户文件并入库
        log.info("开始解析用户文件: {}", userLocalPath);
        int userCount = parseAndInsertUserFile(userLocalPath);
        log.info("用户文件入库完成，共 {} 条", userCount);

        // 2. 重建产品字典
        sysCodeMapper.deleteByType("cust_sale_product");
        sysCodeMapper.insertCustSaleProduct();
        log.info("产品字典重建完成");

        // 3. 委托 WarningStartService 启动流程
        //  CustSalesWarningStrategy 会从 ftp_broadband_market_cust 表查询未生成订单的记录
        //  逐条启动 single_approval 流程（流程定义 key=single_approval），创建工单并设置任务变量
        warningStartService.startByType("cust_sales", currentDateStr);

        log.info("【{}】完成: 客户{}条, 用户{}条", getJobName(), custCount, userCount);
    }

    // ==================== 用户文件解析 ====================

    private int parseAndInsertUserFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            log.error("宽带专项营销用户文件不存在: {}", filePath);
            return 0;
        }

        int total = 0;
        List<FtpBroadbandMarketUser> batch = new ArrayList<>(1000);

        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    // 与客户文件一致：按字段声明顺序解析
                    FtpBroadbandMarketUser user = groupFileParser.parseLine(
                            line, FtpBroadbandMarketUser.class, getSeparator());
                    if (user == null) {
                        continue;
                    }
                    // 空串归一为 null，避免空串写入数值列
                    normalizeBlankToNull(user);
                    batch.add(user);

                    if (batch.size() >= 1000) {
                        ftpBroadbandMarketUserMapper.insertBatch(batch);
                        total += batch.size();
                        batch.clear();
                    }
                } catch (Exception e) {
                    log.warn("用户文件解析行失败: {}",
                            line.length() > 100 ? line.substring(0, 100) : line, e);
                }
            }
            if (!batch.isEmpty()) {
                ftpBroadbandMarketUserMapper.insertBatch(batch);
                total += batch.size();
            }
        } catch (IOException e) {
            log.error("用户文件读取失败", e);
        }
        return total;
    }

    /**
     * 空串归一：将实体中值为空字符串（""）的 String 字段置为 null。
     * 文件导入规范：文件列空值按 null 处理
     */
    private static void normalizeBlankToNull(Object entity) {
        if (entity == null) {
            return;
        }
        Class<?> clazz = entity.getClass();
        Field[] fields = BLANK_STRING_FIELD_CACHE.computeIfAbsent(clazz, c -> {
            Field[] fs = Arrays.stream(c.getDeclaredFields())
                    .filter(f -> f.getType() == String.class)
                    .toArray(Field[]::new);
            for (Field f : fs) {
                f.setAccessible(true);
            }
            return fs;
        });
        try {
            for (Field f : fields) {
                Object v = f.get(entity);
                if (v instanceof String && ((String) v).isEmpty()) {
                    f.set(entity, null);
                }
            }
        } catch (IllegalAccessException e) {
            log.warn("空串归一为 null 失败: {}", entity.getClass().getSimpleName(), e);
        }
    }

    // ==================== XXL-Job 入口 ====================

    @XxlJob("custSalesFileJob")
    public void start() {
        execute();
    }
}
