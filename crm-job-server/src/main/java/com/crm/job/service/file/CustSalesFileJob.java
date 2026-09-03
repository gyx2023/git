package com.crm.job.service.file;

import com.crm.common.utils.StringUtils;
import com.crm.job.utils.SftpWebUtil;
import com.crm.system.domain.dao.FtpBroadbandMarketCust;
import com.crm.system.domain.dao.FtpBroadbandMarketResult;
import com.crm.system.domain.dao.FtpBroadbandMarketUser;
import com.crm.system.mapper.FtpBroadbandMarketCustMapper;
import com.crm.system.mapper.FtpBroadbandMarketResultMapper;
import com.crm.system.mapper.FtpBroadbandMarketUserMapper;
import com.crm.system.mapper.sys.SysCodeMapper;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.service.custsales.CustSalesOrderCreateService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class CustSalesFileJob {
    // 客户文件
    private static final String  CUST_FILE_NAME="TC_CUST_BROADBAND_MARKETING_M_";
    private static String CUST_FILE_FULL_PATCH;
    // 用户文件
    private static final String  USER_FILE_NAME="TC_USER_BROADBAND_MARKETING_M_";
    private static String USER_FILE_FULL_PATCH;
    // 结果文件
    private static final String RESULT_FILE_NAME = "TC_USER_BROADBAND_MARKETING_RESULT_M_";

    @Autowired
    private ISysParamService sysParamService;
    @Autowired
    private FtpBroadbandMarketCustMapper ftpBroadbandMarketCustMapper;
    @Autowired
    private FtpBroadbandMarketUserMapper ftpBroadbandMarketUserMapper;
    @Autowired
    private FtpBroadbandMarketResultMapper ftpBroadbandMarketResultMapper;
    @Autowired
    private SysCodeMapper sysCodeMapper;

    @Autowired
    private CustSalesOrderCreateService custSalesOrderCreateService;

    @XxlJob(value = "custSalesFileJob")
    public void custSalesFileJob() {
        String jobParam = XxlJobHelper.getJobParam();
        String yyyyMM;
        if (jobParam != null && !jobParam.trim().isEmpty()) {
            yyyyMM = jobParam;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            yyyyMM = new SimpleDateFormat("yyyyMM").format(calendar.getTime());
        }

        //下载文件
        if(download(yyyyMM)){
            //删除两张表中的该账期数据
            ftpBroadbandMarketCustMapper.deleteByStatCycle(yyyyMM);
            ftpBroadbandMarketUserMapper.deleteByStatCycle(yyyyMM);

            //解析两个文件并数据批量入库
            parseAndInsertCustFile();
            parseAndInsertUserFile();

            //重建产品的数据字典
            //先清空sys_code表中类型为cust_sale_product的所有数据
            sysCodeMapper.deleteByType("cust_sale_product");
            //批量插入
            sysCodeMapper.insertCustSaleProduct();

            //启动任务（流程启动下沉到领域层，job 不再直连 flowable 引擎）
            custSalesOrderCreateService.startTasks(yyyyMM);
        }
    }

    @XxlJob(value = "resultSalesFileJob")
    public void resultSalesFileJob() {
        // 1. 获取账期（支持任务传参，默认上个月）
        String jobParam = XxlJobHelper.getJobParam();
        String yyyyMM;
        if (jobParam != null && !jobParam.trim().isEmpty()) {
            yyyyMM = jobParam;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            yyyyMM = new SimpleDateFormat("yyyyMM").format(calendar.getTime());
        }

        // 2. 获取参数并下载文件
        Map<String, String> param = sysParamService.findValueMapsByType("broadband_market");
        String resultFileDirectory = param.get("ftp_result_directory") + yyyyMM;
        String resultFileName = RESULT_FILE_NAME + yyyyMM + ".txt";
        String localFilePath = MapUtils.getString(param, "ftp_local") + File.separator + resultFileName;

        SftpWebUtil ftpUtil = new SftpWebUtil(
            param.get("ftp_username"),
            param.get("ftp_password"),
            param.get("ftp_host"),
            MapUtils.getIntValue(param, "ftp_port")
        );

        boolean downloadSuccess = false;
        try {
            log.info("宽带专项营销下载结果文件开始");
            ftpUtil.login();
            ftpUtil.download(resultFileDirectory, resultFileName, localFilePath);
            log.info("宽带专项营销下载结果文件成功");
            downloadSuccess = true;
        } catch (Exception e) {
            log.error("宽带专项营销文件下载失败", e);
        } finally {
            ftpUtil.logout();
        }

        // 3. 下载失败直接退出
        if (!downloadSuccess) {
            log.error("文件下载失败，终止后续数据入库流程");
            return;
        }

        // 4. 清理历史数据
        log.info("开始清理{}账期历史数据", yyyyMM);
        ftpBroadbandMarketResultMapper.deleteByStatCycle(yyyyMM);
        log.info("{}账期历史数据清理完成，开始数据入库", yyyyMM);

        // 5. 文件解析入库
        File file = new File(localFilePath);
        if (!file.exists()) {
            log.error("宽带专项营销业务受理结果文件不存在");
            return;
        }

        log.info("开始入库宽带专项营销业务受理结果数据");
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
             BufferedReader bufferedReader = new BufferedReader(isr)) {

            List<FtpBroadbandMarketResult> dataList = new ArrayList<>();
            String currentLine;
            int count = 0;

            while ((currentLine = bufferedReader.readLine()) != null) {
                count++;
                String[] data = currentLine.split("\\x1b", -1);

                FtpBroadbandMarketResult result = new FtpBroadbandMarketResult();
                if(StringUtils.isNotEmpty(data[0])){
                    result.setStatCycle(data[0]);
                }
                if(StringUtils.isNotEmpty(data[1])){
                    result.setMarketingType(data[1]);
                }
                if(StringUtils.isNotEmpty(data[2])){
                    result.setMarketingName(data[2]);
                }
                if(StringUtils.isNotEmpty(data[3])){
                    result.setUserMarketingId(data[3]);
                }
                if(StringUtils.isNotEmpty(data[4])){
                    result.setUserId(data[4]);
                }
                if(StringUtils.isNotEmpty(data[5])){
                    result.setSvcNum(data[5]);
                }
                if(StringUtils.isNotEmpty(data[6])){
                    result.setIsSucc(data[6]);
                }
                if(StringUtils.isNotEmpty(data[7])){
                    result.setCreateDate(data[7]);
                }
                if(StringUtils.isNotEmpty(data[8])){
                    result.setStartDate(data[8]);
                }
                if(StringUtils.isNotEmpty(data[9])){
                    result.setEndDate(data[9]);
                }
                if(StringUtils.isNotEmpty(data[10])){
                    result.setOrderProductCode(data[10]);
                }
                if(StringUtils.isNotEmpty(data[11])){
                    result.setOrderProductName(data[11]);
                }
                if(StringUtils.isNotEmpty(data[12])){
                    result.setOrderDiscntCode(data[12]);
                }
                if(StringUtils.isNotEmpty(data[13])){
                    result.setOrderDiscntName(data[13]);
                }
                if(StringUtils.isNotEmpty(data[14])){
                    result.setNewSpeed(data[14]);
                }

                dataList.add(result);

                // 每1000条批量插入
                if (count % 1000 == 0) {
                    log.info("---------->开始入库第{}至{}行数据", count - 999, count);
                    ftpBroadbandMarketResultMapper.insertBatch(dataList);
                    log.info("---------->入库第{}至{}行数据成功", count - 999, count);
                    dataList.clear();
                }
            }

            // 插入剩余数据
            if (!dataList.isEmpty()) {
                ftpBroadbandMarketResultMapper.insertBatch(dataList);
                log.info("---------->入库最后{}行数据成功", dataList.size());
            }

            log.info("入库宽带专项营销业务受理结果数据完成，共入库{}条数据", count);

        } catch (UnsupportedEncodingException e) {
            log.error("文件编码异常", e);
        } catch (IOException e) {
            log.error("文件读取异常", e);
        }
    }

    /**
     * 下载客户文件和用户文件
     * @param yyyyMM
     * @return
     */
    private boolean download(String yyyyMM){
        Map<String, String> param = sysParamService.findValueMapsByType("broadband_market");
        SftpWebUtil ftpUtil = new SftpWebUtil(
            param.get("ftp_username"),
            param.get("ftp_password"),
            param.get("ftp_host"),
            MapUtils.getIntValue(param, "ftp_port")
        );

        try{
            log.info("宽带专项营销下载文件开始");
            ftpUtil.login();
            //下载宽带专项营销客户文件
            String custFileDirectory = param.get("ftp_cust_directory")+yyyyMM;  //文件存放目录
            String custFileName=CUST_FILE_NAME+yyyyMM+".txt";
            CUST_FILE_FULL_PATCH=MapUtils.getString(param,"ftp_local")+File.separator+custFileName;
            ftpUtil.download(custFileDirectory, custFileName, CUST_FILE_FULL_PATCH);
            //下载宽带专项用户客户文件
            String userFileDirectory = param.get("ftp_user_directory")+yyyyMM;  //文件存放目录
            String userFileName=USER_FILE_NAME+yyyyMM+".txt";
            USER_FILE_FULL_PATCH=MapUtils.getString(param,"ftp_local")+File.separator+userFileName;
            ftpUtil.download(userFileDirectory, userFileName, USER_FILE_FULL_PATCH);
            log.info("宽带专项营销下载文件成功");
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally{
            ftpUtil.logout();
        }
    }

    /**
     * 解析宽带营销客户文件并批量入库
     */
    private void parseAndInsertCustFile() {
        // 获取客户文件本地路径
        File custFile = new File(CUST_FILE_FULL_PATCH);
        // 校验文件是否存在
        if (!custFile.exists()) {
            log.error("宽带专项营销客户文件不存在，文件路径：{}", CUST_FILE_FULL_PATCH);
            return;
        }

        // 自动关闭文件流
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(custFile), "UTF-8");
             BufferedReader bufferedReader = new BufferedReader(isr)) {

            List<FtpBroadbandMarketCust> dataList = new ArrayList<>();
            String currentLine;
            // 数据总行数
            int count = 0;

            // 逐行读取文件 + 直接解析单行数据
            while ((currentLine = bufferedReader.readLine()) != null) {
                count++;
                // 解析成对象
                String[] data = currentLine.split("\\x1b", -1);
                FtpBroadbandMarketCust custData = new FtpBroadbandMarketCust();

                // 所有字段统一添加非空判断，非空时才赋值
                if(StringUtils.isNotEmpty(data[0])){
                    custData.setStatCycle(data[0]);
                }
                if(StringUtils.isNotEmpty(data[1])){
                    custData.setCustMarketingId(data[1]);
                }
                if(StringUtils.isNotEmpty(data[2])){
                    custData.setAreaName(data[2]);
                }
                if(StringUtils.isNotEmpty(data[3])){
                    custData.setCountyName(data[3]);
                }
                if(StringUtils.isNotEmpty(data[4])){
                    custData.setCustGrpId(data[4]);
                }
                if(StringUtils.isNotEmpty(data[5])){
                    custData.setGrpCode(data[5]);
                }
                if(StringUtils.isNotEmpty(data[6])){
                    custData.setGrpName(data[6]);
                }
                if(StringUtils.isNotEmpty(data[7])){
                    custData.setServManagerId(data[7]);
                }
                if(StringUtils.isNotEmpty(data[8])){
                    custData.setServManagerName(data[8]);
                }
                if(StringUtils.isNotEmpty(data[9])){
                    custData.setGrpExpandType(data[9]);
                }
                if(StringUtils.isNotEmpty(data[10])){
                    custData.setGrpBusiDeptType(data[10]);
                }
                if(StringUtils.isNotEmpty(data[11])){
                    custData.setMarketingType(data[11]);
                }
                if(StringUtils.isNotEmpty(data[12])){
                    custData.setMarketingName(data[12]);
                }
                if(StringUtils.isNotEmpty(data[13])){
                    custData.setUserScale(data[13]);
                }

                // 添加到批量集合
                dataList.add(custData);

                // 每1000条执行一次批量插入，避免内存溢出，提升性能
                if (count % 1000 == 0) {
                    log.info("---------->客户数据批量入库：第{}至{}行", count - 999, count);
                    ftpBroadbandMarketCustMapper.insertBatch(dataList);
                    // 插入后清空集合，释放内存
                    dataList.clear();
                }
            }

            // 插入最后不足1000条的剩余数据
            if (!dataList.isEmpty()) {
                ftpBroadbandMarketCustMapper.insertBatch(dataList);
                log.info("---------->客户数据入库最后{}条剩余数据成功", dataList.size());
            }

            log.info("宽带专项营销客户数据全部入库完成，总入库条数：{}", count);

        } catch (UnsupportedEncodingException e) {
            log.error("客户文件编码异常，仅支持UTF-8编码", e);
        } catch (IOException e) {
            log.error("客户文件读取失败", e);
        }
    }

    /**
     * 解析宽带营销用户文件并批量入库
     */
    private void parseAndInsertUserFile() {
        // 获取用户文件本地路径
        File userFile = new File(USER_FILE_FULL_PATCH);
        // 校验文件是否存在
        if (!userFile.exists()) {
            log.error("宽带专项营销用户文件不存在，文件路径：{}", USER_FILE_FULL_PATCH);
            return;
        }

        // 自动关闭文件流
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(userFile), "UTF-8");
             BufferedReader bufferedReader = new BufferedReader(isr)) {

            List<FtpBroadbandMarketUser> dataList = new ArrayList<>();
            String currentLine;
            // 数据总行数
            int count = 0;

            // 逐行读取文件 + 直接解析单行数据
            while ((currentLine = bufferedReader.readLine()) != null) {
                count++;
                // 解析成对象
                String[] data = currentLine.split("\\x1b", -1);
                FtpBroadbandMarketUser userData = new FtpBroadbandMarketUser();

                // 统一非空判断后赋值
                if(StringUtils.isNotEmpty(data[0])){
                    userData.setStatCycle(data[0]);
                }
                if(StringUtils.isNotEmpty(data[1])){
                    userData.setMarketingType(data[1]);
                }
                if(StringUtils.isNotEmpty(data[2])){
                    userData.setMarketingName(data[2]);
                }
                if(StringUtils.isNotEmpty(data[3])){
                    userData.setUserMarketingId(data[3]);
                }
                if(StringUtils.isNotEmpty(data[4])){
                    userData.setAreaName(data[4]);
                }
                if(StringUtils.isNotEmpty(data[5])){
                    userData.setUserId(data[5]);
                }
                if(StringUtils.isNotEmpty(data[6])){
                    userData.setSvcNum(data[6]);
                }
                if(StringUtils.isNotEmpty(data[7])){
                    userData.setCountyName(data[7]);
                }
                if(StringUtils.isNotEmpty(data[8])){
                    userData.setOpenDate(data[8]);
                }
                if(StringUtils.isNotEmpty(data[9])){
                    userData.setCustKind(data[9]);
                }
                if(StringUtils.isNotEmpty(data[10])){
                    userData.setCustId(data[10]);
                }
                if(StringUtils.isNotEmpty(data[11])){
                    userData.setCustName(data[11]);
                }
                if(StringUtils.isNotEmpty(data[12])){
                    userData.setCertType(data[12]);
                }
                if(StringUtils.isNotEmpty(data[13])){
                    userData.setIsGrpKey(data[13]);
                }
                if(StringUtils.isNotEmpty(data[14])){
                    userData.setCustGrpId(data[14]);
                }
                if(StringUtils.isNotEmpty(data[15])){
                    userData.setGrpCode(data[15]);
                }
                if(StringUtils.isNotEmpty(data[16])){
                    userData.setGrpName(data[16]);
                }
                if(StringUtils.isNotEmpty(data[17])){
                    userData.setServManagerId(data[17]);
                }
                if(StringUtils.isNotEmpty(data[18])){
                    userData.setServManagerName(data[18]);
                }
                if(StringUtils.isNotEmpty(data[19])){
                    userData.setGrpExpandType(data[19]);
                }
                if(StringUtils.isNotEmpty(data[20])){
                    userData.setGrpBusiDeptType(data[20]);
                }
                if(StringUtils.isNotEmpty(data[21])){
                    userData.setProdId(data[21]);
                }
                if(StringUtils.isNotEmpty(data[22])){
                    userData.setProdName(data[22]);
                }
                if(StringUtils.isNotEmpty(data[23])){
                    userData.setUserStatus(data[23]);
                }
                if(StringUtils.isNotEmpty(data[24])){
                    userData.setHqSvcType(data[24]);
                }
                if(StringUtils.isNotEmpty(data[25])){
                    userData.setCustCountyName(data[25]);
                }
                if(StringUtils.isNotEmpty(data[26])){
                    userData.setDeveloperId(data[26]);
                }
                if(StringUtils.isNotEmpty(data[27])){
                    userData.setDeveloperName(data[27]);
                }
                if(StringUtils.isNotEmpty(data[28])){
                    userData.setSpeed(data[28]);
                }
                if(StringUtils.isNotEmpty(data[29])){
                    userData.setUserAddr(data[29]);
                }
                if(StringUtils.isNotEmpty(data[30])){
                    userData.setCycleStartTime(data[30]);
                }
                if(StringUtils.isNotEmpty(data[31])){
                    userData.setCycleEndTime(data[31]);
                }
                if(StringUtils.isNotEmpty(data[32])){
                    userData.setCycleLength(data[32]);
                }
                if(StringUtils.isNotEmpty(data[33])){
                    userData.setCycleFee(data[33]);
                }
                if(StringUtils.isNotEmpty(data[34])){
                    userData.setMktProductId1(data[34]);
                }
                if(StringUtils.isNotEmpty(data[35])){
                    userData.setMktProductName1(data[35]);
                }
                if(StringUtils.isNotEmpty(data[36])){
                    userData.setMktDiscntCode1(data[36]);
                }
                if(StringUtils.isNotEmpty(data[37])){
                    userData.setMktDiscntName1(data[37]);
                }
                if(StringUtils.isNotEmpty(data[38])){
                    userData.setMktDiscntFee1(data[38]);
                }
                if(StringUtils.isNotEmpty(data[39])){
                    userData.setMktProductId2(data[39]);
                }
                if(StringUtils.isNotEmpty(data[40])){
                    userData.setMktProductName2(data[40]);
                }
                if(StringUtils.isNotEmpty(data[41])){
                    userData.setMktDiscntCode2(data[41]);
                }
                if(StringUtils.isNotEmpty(data[42])){
                    userData.setMktDiscntName2(data[42]);
                }
                if(StringUtils.isNotEmpty(data[43])){
                    userData.setMktDiscntFee2(data[43]);
                }

                // 添加到批量集合
                dataList.add(userData);

                // 每1000条执行一次批量插入，避免内存溢出，提升性能
                if (count % 1000 == 0) {
                    log.info("---------->用户数据批量入库：第{}至{}行", count - 999, count);
                    ftpBroadbandMarketUserMapper.insertBatch(dataList);
                    // 插入后清空集合，释放内存
                    dataList.clear();
                }
            }

            // 插入最后不足1000条的剩余数据
            if (!dataList.isEmpty()) {
                ftpBroadbandMarketUserMapper.insertBatch(dataList);
                log.info("---------->用户数据入库最后{}条剩余数据成功", dataList.size());
            }

            log.info("宽带专项营销用户数据全部入库完成，总入库条数：{}", count);

        } catch (UnsupportedEncodingException e) {
            log.error("用户文件编码异常，仅支持UTF-8编码", e);
        } catch (IOException e) {
            log.error("用户文件读取失败", e);
        }
    }
}
