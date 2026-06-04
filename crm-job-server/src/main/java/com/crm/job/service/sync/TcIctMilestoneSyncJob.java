package com.crm.job.service.sync;

import com.crm.common.utils.StringUtils;
import com.crm.common.utils.file.FieldAnnotaionUtil;
import com.crm.job.utils.SftpWebUtil;
import com.crm.system.domain.TcIctMilestone;
import com.crm.system.mapper.TcIctMilestoneMapper;
import com.crm.system.service.ISysParamService;
import com.crm.system.service.TcIctMilestoneService;
import com.crm.system.utils.Constants;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 合同里程碑信息同步任务
 * @author xyj
 */
@Slf4j
@Component
public class TcIctMilestoneSyncJob {
    private static final String ftpDownloadName = "TC_ICT_MILESTONE_D_";// 下载的文件名前缀

    private static String ftpUsername;
    private static String ftpPassword;
    private static String ftpHost;
    private static String ftpPort;
    private static String ftpDirectory;
    private static String localPath;

    @Autowired
    private ISysParamService sysParamService;
    @Autowired
    private TcIctMilestoneService tcIctMilestoneService;
    @Autowired
    private TcIctMilestoneMapper tcIctMilestoneMapper;

    /**
     * 初始化静态参数
     */
    @PostConstruct
    public void initStaticParam() {
        Map<String, String> param = sysParamService.findValueMapsByType("tc_ict_milestone");
        ftpUsername = param.get("ftp_username");  //用户名
        ftpPassword = param.get("ftp_password");  //密码
        ftpHost = param.get("ftp_host");  //ip地址
        ftpPort = param.get("ftp_port");  //端口号
        ftpDirectory = param.get("ftp_directory");  //文件存放目录
        localPath = param.get("ftp_local");  //本地下载目录
    }

    @XxlJob(value="tcIctMilestoneSyncJob")
    public void execute(){
        String yyyyMMdd;
        String jobParam= XxlJobHelper.getJobParam();
        if(StringUtils.isNotEmpty(jobParam)){
            yyyyMMdd = jobParam;
            log.info("使用任务参数指定的日期: {}", yyyyMMdd);
        }else{
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -2);
            yyyyMMdd = new SimpleDateFormat(Constants.YEAR_MONTH_DAY_PATTERN).format(calendar.getTime());
            log.info("使用默认日期（前2天）: {}", yyyyMMdd);
        }

        //开始同步
        log.info("========== 开始同步合同里程碑信息 ==========");
        SftpWebUtil sftpWebUtil = new SftpWebUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort));
        sftpWebUtil.login();
        log.info("SFTP登录成功");

        //获取ftp配置
        Map<String, String> param = sysParamService.findValueMapsByType("tc_ict_milestone");
        String ftpDirectory = param.get("ftp_directory");  	//文件存放目录
        ftpDirectory += yyyyMMdd;
        String saveFile = ftpDownloadName + yyyyMMdd + ".txt";
        String localFile = localPath + File.separator + saveFile;

        log.info("FTP目录: {}", ftpDirectory);
        log.info("文件名: {}", saveFile);
        log.info("本地文件路径: {}", localFile);

        Date date = new Date();
        try{
            sftpWebUtil.download(ftpDirectory,saveFile,localFile);
            log.info("文件下载成功");

            File file = new File(localFile);
            if(file.exists()){
                log.info("文件存在，开始处理数据");
                try{
                    //清空表
                    log.info("清空合同里程碑表数据");
                    tcIctMilestoneMapper.clearMilestoneTable();

                    FileReader reader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    int count = 0;
                    String currentLine;
                    List<TcIctMilestone> dataList = new ArrayList<>();
                    Calendar calendar = Calendar.getInstance();

                    while ((currentLine = bufferedReader.readLine()) != null) {
                        count++;
                        TcIctMilestone data = new TcIctMilestone();
                        FieldAnnotaionUtil.setFiledValFromValuesByOrder(data, currentLine.split("\\x1b", -1), 3);

                        //设置预计回款时间
                        if(data.getCashLagMonths() != null && data.getCashLagMonths().intValue() != 0){
                            calendar.setTime(data.getMilestoneDate());
                            calendar.add(Calendar.MONTH, data.getCashLagMonths().intValue());
                            data.setPredictCashDate(calendar.getTime());
                        }else{
                            data.setPredictCashDate(data.getMilestoneDate());
                        }
                        data.setCreateDate(date);
                        dataList.add(data);

                        if (count % 1000 == 0) {
                            tcIctMilestoneService.saveBatch(dataList);
                            log.info("已处理 {} 条数据，批量插入完成", count);
                            dataList = new ArrayList<>();
                        }
                    }

                    //处理剩余数据
                    if (dataList.size() != 0) {
                        tcIctMilestoneService.saveBatch(dataList);
                        log.info("处理剩余 {} 条数据，批量插入完成", dataList.size());
                    }

                    bufferedReader.close();
                    log.info("========== 合同里程碑信息同步完成，共同步 {} 条数据 ==========", count);
                }catch (Exception e){
                    log.error("处理文件数据失败", e);
                    throw e;
                }

                //删除临时文件
                boolean deleted = file.delete();
                if(deleted){
                    log.info("临时文件已删除: {}", localFile);
                }else{
                    log.warn("临时文件删除失败: {}", localFile);
                }
            }else{
                log.error("文件不存在: {}", localFile);
            }
        } catch (Exception e) {
            log.error("同步合同里程碑信息失败", e);
        } finally {
            sftpWebUtil.logout();
            log.info("SFTP连接已释放");
        }
    }
}
