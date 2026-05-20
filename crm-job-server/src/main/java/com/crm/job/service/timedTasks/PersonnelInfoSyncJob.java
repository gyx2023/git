package com.crm.job.service.timedTasks;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.constant.Constants;
import com.crm.common.utils.FieldAnnotaionUtil;
import com.crm.common.utils.MD5Utils;
import com.crm.common.utils.SFTPUtil;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.PersonnelInfo;
import com.crm.system.domain.SysArea;
import com.crm.system.domain.SysOrg;
import com.crm.system.domain.SysUsers;
import com.crm.system.mapper.SysAreaMapper;
import com.crm.system.mapper.SysOrgMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.ISysParamService;
import com.crm.system.service.PersonnelInfoService;
import com.crm.system.service.SysUserOperateLogService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 人员信息同步Job - 管理中台在职人员信息同步
 */
@Slf4j
@Service
public class PersonnelInfoSyncJob {

    private static final String FTP_DOWNLOAD_NAME = "REP_UC_EMP_ADD_";
    // 下载的文件名扩展名
    private static final String FTP_DOWNLOAD_END_FIX = ".txt";

    @Resource
    private ISysParamService sysParamService;

    @Resource
    private PersonnelInfoService personnelInfoService;

    @Resource
    private SysUserOperateLogService sysUserOperateLogService;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Resource
    private SysAreaMapper sysAreaMapper;

    @Resource
    private SysOrgMapper sysOrgMapper;

    // FTP配置参数
    static String ftpUsername;
    static String ftpPassword;
    static String ftpHost;
    static String ftpPort;
    static String ftDirectory;
    static String localPath;

    /**
     * 初始化FTP配置参数
     */
    @PostConstruct
    public void initStaticParam() {
        Map<String, String> param = sysParamService.findValueMapsByType("personnel_file_ftp_info");
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
        }else {
            log.error("未配置人员信息同步FTP参数");
        }
    }

    /**
     * 每天凌晨四点下发文件，四点半执行定时任务(管理中台提供的新文件接口)
     */
    @XxlJob(value = "personnelInfoSyncJobNew")
    public void syncFileNew() {
        long startTime = System.currentTimeMillis();
        XxlJobHelper.log("开始执行人员信息同步Job");
        log.info("开始执行人员信息同步Job");

        try {
            String yyyyMMdd;
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 0);
            yyyyMMdd = new SimpleDateFormat(Constants.YEAR_MONTH_DAY_PATTERN).format(calendar.getTime());

            String jobparam = XxlJobHelper.getJobParam();
            if (StringUtils.isNotEmpty(jobparam)) {
                yyyyMMdd = jobparam;
            }

            downloadFile(yyyyMMdd);

            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            log.info("人员信息同步Job执行完成,耗时:{}ms", costTime);
            XxlJobHelper.log("人员信息同步Job执行完成,耗时:{}ms", costTime);

        } catch (Exception e) {
            log.error("人员信息同步Job执行异常", e);
            XxlJobHelper.log("人员信息同步Job执行异常:{}", e.getMessage());
        }
    }

    /**
     * 下载并处理人员增量文件
     */
    private void downloadFile(String yyyyMMdd) {
        log.info("ob库人员增量同步文件接口开始download>>>>>>>>>>>> : ");
        SFTPUtil ftpUtil = new SFTPUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort));
        ftpUtil.login();

        String saveFile = FTP_DOWNLOAD_NAME + yyyyMMdd + FTP_DOWNLOAD_END_FIX;
        String localFile = localPath + File.separator + saveFile;
        String localFileNew = localPath + File.separator + FTP_DOWNLOAD_NAME + yyyyMMdd + "_new" + FTP_DOWNLOAD_END_FIX;

        try {
            ftpUtil.download(ftDirectory, saveFile, localFile);
            log.info("ob库人员增量同步文件接口download完成>>>>>>>>>>>> " + localFile);

            File file = new File(localFile);
            Scanner scanner = new Scanner(file, "GBK");
            File fileNew = new File(localFileNew);
            PrintWriter out = new PrintWriter(fileNew);

            // 换行符是0x06 需要下载完文件根据换行符重新生成一份文件，方便按照行读取数据
            while (scanner.hasNext()) {
                String str = scanner.nextLine();
                String[] ss = str.split("\u0006");
                for (String s : ss) {
                    out.write(s + "\r\n");//在子字符串后加上换行符并输出文本
                }
            }

            // 处理好换行文件写出
            out.flush();
            out.close();

            if (fileNew.exists()) {
                FileReader reader = null;
                BufferedReader bufferedReader = null;
                try {
                    //获取数据并入库
                    reader = new FileReader(fileNew);
                    bufferedReader = new BufferedReader(reader);
                    //获取当前文件长度
                    String currentLine;
                    int successCount = 0;
                    int skipCount = 0;

                    while ((currentLine = bufferedReader.readLine()) != null) {
                        PersonnelInfo personnelInfo = new PersonnelInfo();
                        String[] strs = currentLine.split("\u0007", -1);

                        if (strs.length != 35) {
                            skipCount++;
                            continue;
                        }

                        // 设置字段值
                        FieldAnnotaionUtil.setFiledValFromValuesByIndex(personnelInfo, strs);

                        // 查询是否已存在
                        PersonnelInfo existingInfo = personnelInfoService.getByUserId(personnelInfo.getUserId());

                        if (existingInfo != null) {
                            personnelInfo.setId(existingInfo.getId());
                            BeanUtils.copyProperties(personnelInfo, existingInfo);
                            personnelInfoService.updateById(existingInfo);
                        } else {
                            personnelInfoService.save(personnelInfo);
                        }

                        //更新或新增sys_user表数据
                        updateSysUserByPersonnelInfo(personnelInfo);
                        successCount++;
                    }

                    log.info("人员增量更新完成，成功:{}条，跳过:{}条", successCount, skipCount);
                    XxlJobHelper.log("人员增量更新完成，成功:{}条，跳过:{}条", successCount, skipCount);

                } catch (Exception e) {
                    log.error("处理人员增量文件异常", e);
                    XxlJobHelper.log("处理人员增量文件异常:{}", e.getMessage());
                } finally {
                    // 关闭流
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (reader != null) {
                        reader.close();
                    }
                }

                // 删除下载和生成文件
                file.delete();
                fileNew.delete();
            } else {
                log.error("人员增量文件接口 下载更新失败 文件未找到 error>>>>>>>>>>>>");
                XxlJobHelper.log("人员增量文件接口 下载更新失败 文件未找到");
            }
        } catch (Exception e) {
            log.error("人员增量文件接口 下载更新失败 error>>>>>>>>>>>>", e);
            XxlJobHelper.log("人员增量文件接口 下载更新失败:{}", e.getMessage());
        } finally {
            log.info("人员增量文件同步数据更新完成  download over>>>>>>>>>>>>");
            ftpUtil.logout();// 释放连接
        }
    }

    /**
     * 抽取的更新sys_user表方法，与原逻辑保持一致
     */
    private void updateSysUserByPersonnelInfo(PersonnelInfo personnelInfo) {
        try {
            // 查询手机号对应的用户
            LambdaQueryWrapper<SysUsers> lqw = new LambdaQueryWrapper<>();
            lqw.eq(SysUsers::getName, personnelInfo.getMobileTel())
               .orderByDesc(SysUsers::getCreateTime)
               .last("limit 1");
            List<SysUsers> userList = sysUsersMapper.selectList(lqw);

            if (!userList.isEmpty() && StringUtils.isNotEmpty(personnelInfo.getMobileTel())) {
                // 如果找到手机号对应的用户
                for (SysUsers user : userList) {
                    // 更新身份证号
                    if (StringUtils.isNotEmpty(personnelInfo.getMobileTel()) &&
                            StringUtils.isNotEmpty(user.getRelationTel()) &&
                            user.getRelationTel().equals(personnelInfo.getMobileTel())) {
                        user.setCardNumber(personnelInfo.getEmpCode());
                    }

                    // 更新OA账号
                    user.setOaAccount(personnelInfo.getUserId());

                    String operateType = "mod";
                    user.setIsValid("1");

                    // commandType=3表示删除; 状态为非0也执行删除
                    if ((personnelInfo.getCommandType() != null && personnelInfo.getCommandType() == 3)
                            || !"0".equals(personnelInfo.getStatus())) {
                        user.setIsValid("0");
                        operateType = "del";
                    }

                    sysUsersMapper.updateById(user);
                    sysUserOperateLogService.synUserOperate(user, operateType);
                }
            } else if (StringUtils.isNotEmpty(personnelInfo.getMobileTel())) {
                // 如果该手机号未注册，但手机号不为空
                // 存在用户更换手机号的情况，先按照oa查询一下用户
                LambdaQueryWrapper<SysUsers> lqw2 = new LambdaQueryWrapper<>();
                lqw2.eq(SysUsers::getOaAccount, personnelInfo.getUserId());
                List<SysUsers> userList2 = sysUsersMapper.selectList(lqw2);

                if (!userList2.isEmpty()) {
                    for (SysUsers user : userList2) {
                        user.setName(personnelInfo.getMobileTel());
                        String operateType = "mod";
                        user.setIsValid("1");

                        // commandType=3表示删除; 状态为非0也执行删除
                        if ((personnelInfo.getCommandType() != null && personnelInfo.getCommandType() == 3)
                                || !"0".equals(personnelInfo.getStatus())) {
                            user.setIsValid("0");
                            operateType = "del";
                        }

                        sysUsersMapper.updateById(user);
                        sysUserOperateLogService.synUserOperate(user, operateType);
                    }
                } else {
                    // 如果都找不到，则新增用户
                    SysUsers user = setNewUser(personnelInfo);
                    sysUsersMapper.insert(user);
                    sysUserOperateLogService.synUserOperate(user, "add");
                }
            }
        } catch (Exception e) {
            log.error("更新SysUser异常,userId:{},mobileTel:{}", personnelInfo.getUserId(), personnelInfo.getMobileTel(), e);
        }
    }

    /**
     * 创建新用户
     */
    private SysUsers setNewUser(PersonnelInfo personnelInfo) {
        SysUsers user = new SysUsers();
        String tel;

        if (StringUtils.isNotEmpty(personnelInfo.getMobileTel()) && validatePhoneNumber(personnelInfo.getMobileTel())) {
            tel = personnelInfo.getMobileTel().substring(personnelInfo.getMobileTel().length() - 4);
        } else {
            tel = "";
        }

        //密码为OA账号+手机号后四位的MD5
        String pwd = MD5Utils.MD5(personnelInfo.getUserId() + tel);
        user.setPassword(pwd);

        //按手机号为新增账号(w钉钉登录账号)
        user.setRelationTel(personnelInfo.getMobileTel());
        //按手机号为新增账号(web登录账号)
        user.setName(personnelInfo.getMobileTel());
        user.setNameCh(personnelInfo.getEmpName());
        //普通用户
        user.setType("ut3");
        // 非隐身
        user.setStealth(0);
        //楼宇角色
        user.setUserRoleYx("0");
        //OA账号
        user.setOaAccount(personnelInfo.getUserId());
        //邮箱
        user.setEmail(personnelInfo.getEmailAddress());
        user.setWxUserFlag("0");
        user.setCreateTime(new Date());
        user.setIsValid("1");
        user.setData("0");
        user.setZqbusinessFlag("0");
        // 默认创建人ID
        user.setCreator(12668L);

        SysOrg sysOrg = getSysOrgByName("浙江省联通本部");

        // 先判断：确定areaId
        Integer areaId = determineAreaId(personnelInfo);

        // 后处理: 获取SysOrg
        SysOrg org = getSysOrgByAreaId(areaId, sysOrg);
        if (org != null) {
            user.setOrgId(org.getId().longValue());
        } else {
            log.error("未找到orgId, mdmOrgCode:{}", personnelInfo.getMdmOrgCode());
        }

        return user;
    }

    /**
     * 判断逻辑：确定areaId
     */
    private Integer determineAreaId(PersonnelInfo personnelInfo) {
        String mdmOrgCode = personnelInfo.getMdmOrgCode();

        // 情况1：mdmOrgCode为空，按原有逻辑处理
        if (StringUtils.isEmpty(mdmOrgCode)) {
            return getAreaIdByCity(personnelInfo.getCity());
        }

        // 情况2：mdmOrgCode为"0033"，产互字典未配置，返回1（省公司）
        if ("0033".equals(mdmOrgCode)) {
            return 1;
        }

        // 情况3：正常的mdmOrgCode，按匹配逻辑处理
        String processedCode = mdmOrgCode;
        if (mdmOrgCode.startsWith("00") && mdmOrgCode.length() > 2) {
            processedCode = mdmOrgCode.substring(2);
        }

        try {
            Integer empAreaId = Integer.valueOf(processedCode);
            LambdaQueryWrapper<SysArea> lqw = new LambdaQueryWrapper<>();
            lqw.eq(SysArea::getEmpAreaId, empAreaId);
            List<SysArea> list = sysAreaMapper.selectList(lqw);

            // 处理查询结果的公共逻辑
            Integer areaId = Math.toIntExact(processAreaList(list));
            if (areaId != null) {
                return areaId;
            }
        } catch (NumberFormatException e) {
            // 转换失败，按原有逻辑处理
            return getAreaIdByCity(personnelInfo.getCity());
        }

        // 以上情况都不满足，按原有逻辑处理
        return getAreaIdByCity(personnelInfo.getCity());
    }

    /**
     * 处理SysArea列表的公共逻辑
     */
    private Long processAreaList(List<SysArea> list) {
        if (list != null && !list.isEmpty()) {
            SysArea area = list.get(0);
            if ("三级".equals(area.getType())) {
                // 县
                return area.getParent();
            } else if ("二级".equals(area.getType())) {
                // 市
                return area.getId();
            } else if ("一级".equals(area.getType())) {
                // 省
                return area.getId();
            }
        }
        // 表示未找到匹配项
        return null;
    }

    /**
     * 根据city获取areaId的公共方法
     */
    private Integer getAreaIdByCity(String city) {
        if (StringUtils.isNotEmpty(city) && !"本部".equals(city)) {
            LambdaQueryWrapper<SysArea> lqw = new LambdaQueryWrapper<>();
            lqw.like(SysArea::getName, city)
               .likeRight(SysArea::getId, "33")
               .last("limit 1");
            List<SysArea> list = sysAreaMapper.selectList(lqw);
            Integer areaId = Math.toIntExact(processAreaList(list));
            if (areaId != null) {
                return areaId;
            }
        }
        // 如果原有逻辑也未找到，则默认为 1（省公司）
        return 1;
    }

    /**
     * 根据areaId获取SysOrg的公共方法
     */
    private SysOrg getSysOrgByAreaId(Integer areaId, SysOrg defaultOrg) {
        if (areaId != null) {
            LambdaQueryWrapper<SysOrg> lqw = new LambdaQueryWrapper<>();
            lqw.eq(SysOrg::getAreaId, areaId)
               .orderByAsc(SysOrg::getId)
               .last("limit 1");
            List<SysOrg> orgList = sysOrgMapper.selectList(lqw);
            if (orgList != null && !orgList.isEmpty()) {
                return orgList.get(0);
            }
        }
        return defaultOrg;
    }

    /**
     * 根据名称获取组织
     */
    private SysOrg getSysOrgByName(String orgName) {
        LambdaQueryWrapper<SysOrg> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SysOrg::getName, orgName);
        List<SysOrg> orgList = sysOrgMapper.selectList(lqw);
        if (orgList != null && !orgList.isEmpty()) {
            return orgList.get(0);
        }
        return null;
    }

    /**
     * 验证手机号
     */
    private boolean validatePhoneNumber(String mobiles) {
        String telRegex = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(147,145))\\d{8}$";
        return StringUtils.isNotEmpty(mobiles) && mobiles.matches(telRegex);
    }

}
