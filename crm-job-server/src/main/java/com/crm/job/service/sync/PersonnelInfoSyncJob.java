package com.crm.job.service.sync;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.file.FieldAnnotaionUtil;
import com.crm.job.utils.SftpWebUtil;
import com.crm.system.domain.*;
import com.crm.system.domain.oldsyssyn.OldSysOrgDo;
import com.crm.system.domain.vo.SysUsersVo;
import com.crm.system.mapper.SysLogYxMapper;
import com.crm.system.mapper.SysUserOperateLogMapper;
import com.crm.system.mapper.oldsyssyn.OldSysOrgMapper;
import com.crm.system.service.*;
import com.crm.system.utils.SSLClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.crm.common.constant.Constants.GBK;
import static com.crm.system.utils.Constants.PERSONNEL_FILE_FTP_INFO;


/**
 * 管理中台人员信息同步文件定时任务
 */
@Slf4j
@Component
public class PersonnelInfoSyncJob {

    @Resource
    private ISysParamService sysParamService;

    @Resource
    private PersonnelInfoService personnelInfoService;

    @Resource
    private ISysUsersService iSysUsersService;

    @Resource
    private OldSysOrgMapper oldSysOrgMapper;

    @Resource
    private ISysAreaService iSysAreaService;

    @Resource
    private SysUserOperateLogMapper sysUserOperateLogMapper;

    @Resource
    private SysLogYxMapper sysLogYxMapper;

    /**
     * 文件名前缀
     */
    private static final String FTP_DOWNLOAD_NAME = "REP_UC_EMP_ADD_";
    /**
     * 下载的文件名扩展名
     */
    private static final String FTP_DOWNLOAD_END_FIX = ".txt";
    /**
     * 下载的文件行分隔符
     */
    private static final String FILE_SEPARATOR_ROW = "\u0006";
    /**
     * 下载的文件行分隔符-新
     */
    private static final String FILE_SEPARATOR_ROW_NEW = "\r\n";
    /**
     * 下载的文件列分隔符
     */
    private static final String FILE_SEPARATOR_LIST = "\u0007";

    private static final String OPERATE_TYPE_ADD = "add"; //添加
    private static final String OPERATE_TYPE_MOD = "mod"; //修改
    private static final String OPERATE_TYPE_DEL = "del"; //删除

    private static final String VALID_TRUE = "1";  //有效
    private static final String VALID_FALSE = "0"; //无效

    static String FTP_USERNAME;
    static String FTP_PASSWORD;
    static String FTP_HOST;
    static String FTP_PORT;
    static String FTP_DIRECTORY;
    static String LOCAL_PATH;

    @PostConstruct
    public void initStaticParam() {
        Map<String, String> param = sysParamService.findValueMapsByType(PERSONNEL_FILE_FTP_INFO);
        FTP_USERNAME = param.get("ftp_username");  //用户名
        FTP_PASSWORD = param.get("ftp_password");  //密码
        FTP_HOST = param.get("ftp_host");  //ip地址
        FTP_PORT = param.get("ftp_port");  //端口号
        FTP_DIRECTORY = param.get("ftp_directory");  //文件存放目录
        LOCAL_PATH = param.get("ftp_local");  //本地下载目录
    }



    /**
     * 管理中台人员信息同步文件定时任务
     * 在xxl-job调度中心配置JobHandler为：personnelInfoSyncJobNew
     * 支持传入参数格式：
     * 1. 单日期参数：yyyyMMdd - 指定日期 - 例：20260519
     * 参数样例：20260519
     */
    @XxlJob(value = "personnelInfoSyncJobNew")
    public void personnelInfoSyncJob() {
        log.info("【管理中台人员信息同步文件】任务开始");
        XxlJobHelper.log("【管理中台人员信息同步文件】任务开始");

        // 任务参数
        String targetDate = DateUtils.dateTime(); // 默认账期

        // 获取定时任务传入参数
        String jobStr = XxlJobHelper.getJobParam();
        if (StringUtils.isNotBlank(jobStr)) {
            targetDate = jobStr;
        }
        log.info("数据参数：1.日期参数：{}", targetDate);
        XxlJobHelper.log("数据参数：1.日期参数：{}", targetDate);

        SftpWebUtil ftpUtil = null;

        String saveFile = FTP_DOWNLOAD_NAME + targetDate + FTP_DOWNLOAD_END_FIX;
        String saveFileNew = FTP_DOWNLOAD_NAME + targetDate + "_new" + FTP_DOWNLOAD_END_FIX;
        String localFile = LOCAL_PATH + File.separator + saveFile;
        String localFileNew = LOCAL_PATH + File.separator + saveFileNew;


        try {
            ftpUtil = new SftpWebUtil(FTP_USERNAME, FTP_PASSWORD, FTP_HOST, Integer.parseInt(FTP_PORT));
            ftpUtil.login();

            log.info("saveFile:{}", saveFile);
            XxlJobHelper.log("saveFile:{}", saveFile);
            log.info("localFile:{}", localFile);
            XxlJobHelper.log("localFile:{}", localFile);

            ftpUtil.download(FTP_DIRECTORY, saveFile, localFile);
            log.info("ob库人员增量同步文件接口download完成：{}", localFile);
            XxlJobHelper.log("ob库人员增量同步文件接口download完成：{}", localFile);

            File file = new File(localFile);
            Scanner scanner = new Scanner(file, GBK);
            File fileNew = new File(localFileNew);
            PrintWriter out = new PrintWriter(fileNew);
            // 换行符是0x06 需要下载完文件根据换行符重新生成一份文件，方便按照行读取数据,否则下载下来的文件换行不对
            while (scanner.hasNext()) {
                String str = scanner.nextLine();
                String[] ss = str.split(FILE_SEPARATOR_ROW);
                for (String s : ss) {
                    out.write(s + FILE_SEPARATOR_ROW_NEW);//在子字符串后加上换行符并输出文本
                }
            }
            // 处理好换行文件写出
            scanner.close();
            out.flush();
            out.close();


            if (fileNew.exists()) {
                FileReader reader = null;
                BufferedReader bufferedReader = null;
                try {
                    // 获取数据并入库
                    reader = new FileReader(fileNew);
                    bufferedReader = new BufferedReader(reader);
                    //获取当前文件的长度
                    String currentLine;
                    while ((currentLine = bufferedReader.readLine()) != null) {
                        PersonnelInfo personnelInfo = new PersonnelInfo();
                        String[] strs = currentLine.split(FILE_SEPARATOR_LIST, -1);
                        if (strs.length != 35) continue;
                        FieldAnnotaionUtil.setFiledValFromValuesByIndex(personnelInfo, strs);

                        List<PersonnelInfo> personnelInfoList = personnelInfoService.findList(personnelInfo);
                        if (personnelInfoList.size() > 0) {
                            personnelInfo.setId(personnelInfoList.get(0).getId());
                            BeanUtils.copyProperties(personnelInfo, personnelInfoList.get(0));
                            personnelInfoService.insertOrUpdate(personnelInfoList.get(0));
                        } else {
                            personnelInfoService.insert(personnelInfo);
                        }

                        //更新或新增sysuser表数据
                        updateSysUserByPersonnelInfo(personnelInfo);
                    }
                    log.info("人员增量更新完成。。。。。。。。。。。。。。。。。");
                    XxlJobHelper.log("人员增量更新完成");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 关闭流
                    bufferedReader.close();
                    reader.close();
                }
                // 删除下载和生成文件
                file.delete();
                fileNew.delete();
            } else {
                log.info("人员增量文件接口 下载更新失败 文件未找到 error>>>>>>>>>>>>");
                XxlJobHelper.log("人员增量文件接口 下载更新失败 文件未找到 error>>>>>>>>>>>>");
            }

            log.info("【管理中台人员信息同步文件】任务结束");
            XxlJobHelper.log("【管理中台人员信息同步文件】任务结束");
        } catch (Exception e) {
            log.info("【管理中台人员信息同步文件】任务失败", e);
            XxlJobHelper.log("【管理中台人员信息同步文件】任务失败：{}", e.getMessage());
        } finally {
            if (ftpUtil != null) {
                try {
                    ftpUtil.close();
                    log.info("SFTP连接已关闭，文件：{}", localFile);
                    XxlJobHelper.log("【管理中台人员信息同步文件】SFTP连接已关闭");
                } catch (Exception e) {
                    log.error("关闭SFTP连接失败，文件：{}", localFile, e);
                    XxlJobHelper.log("【管理中台人员信息同步文件】关闭SFTP连接失败，错误信息：{}", e.getMessage());
                }
            }
        }
    }

    /**
     * 抽取的更新sys_user表方法，与原逻辑保持一致
     */
    public void updateSysUserByPersonnelInfo(PersonnelInfo personnelInfo) throws Exception {
        // 查询手机号对应的用户
        List<SysUsers> userList = iSysUsersService.findList(personnelInfo);

        if (userList.size() > 0 && StringUtils.isNotEmpty(personnelInfo.getMobileTel())) {
            // 如果找到手机号对应的用户
            for (int j = 0; j < userList.size(); j++) {
                // 更新身份证号
                if (StringUtils.isNotEmpty(personnelInfo.getMobileTel()) && StringUtils.isNotEmpty(userList.get(j).getRelationTel())
                    && userList.get(j).getRelationTel().equals(personnelInfo.getMobileTel())) {
                    userList.get(j).setCardNumber(personnelInfo.getEmpCode());
                }

                // 更新OA账号
                userList.get(j).setOaAccount(personnelInfo.getUserId());

                String operateType = OPERATE_TYPE_MOD;
                userList.get(j).setIsValid(VALID_TRUE);

                // commandType=3表示删除; 状态为非0也执行删除
                if ((personnelInfo.getCommandType() != null && personnelInfo.getCommandType() == 3) || !VALID_FALSE.equals(personnelInfo.getStatus())) {
                    userList.get(j).setIsValid(VALID_FALSE);
                    operateType = OPERATE_TYPE_DEL;
                }

                // 修改/保存用户信息
                iSysUsersService.saveOrUpdate(userList.get(j));
                // 修改/保存用户操作日志
                synUserOperate(userList.get(j), operateType);
            }
        } else if (StringUtils.isNotEmpty(personnelInfo.getMobileTel())) {
            // 如果该手机号未注册，但手机号不为空
            // 存在用户更换手机号的情况，先按照oa查询一下用户
            List<SysUsers> userList2 = iSysUsersService.findOaList(personnelInfo);

            if (userList2.size() > 0) {
                for (int j = 0; j < userList2.size(); j++) {
                    userList2.get(j).setName(personnelInfo.getMobileTel());
                    String operateType = OPERATE_TYPE_MOD;
                    userList2.get(j).setIsValid(VALID_TRUE);

                    // commandType=3表示删除; 状态为非0也执行删除
                    if ((ObjectUtil.isNotEmpty(personnelInfo.getCommandType()) && personnelInfo.getCommandType() == 3)
                        || !VALID_FALSE.equals(personnelInfo.getStatus())) {
                        userList2.get(j).setIsValid(VALID_FALSE);
                        operateType = OPERATE_TYPE_DEL;
                    }

                    iSysUsersService.saveOrUpdate(userList2.get(j));
                    synUserOperate(userList2.get(j), operateType);
                }
            } else {
                // 如果都找不到，则新增用户
                SysUsers user = setNewUser(personnelInfo);
                iSysUsersService.insertSysUser(user);
                synUserOperate(user, OPERATE_TYPE_ADD);
            }
        }
    }

    private SysUsers setNewUser(PersonnelInfo personnelInfo) {
        SysUsers user = new SysUsers();
        String tel = personnelInfo.getMobileTel();
        if (StringUtils.isNotEmpty(personnelInfo.getMobileTel()) && validatePhoneNumber(personnelInfo.getMobileTel()) == true) {
            tel = personnelInfo.getMobileTel().substring(personnelInfo.getMobileTel().length() - 4);
        } else {
            tel = "";
        }
        String pwd = SecureUtil.md5(personnelInfo.getUserId() + tel);
        //密码为OA账号+手机号后四位
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
        user.setCreator(12668L);

        LambdaQueryWrapper<OldSysOrgDo> wrapper = new LambdaQueryWrapper<OldSysOrgDo>()
            .eq(OldSysOrgDo::getName, "浙江省联通本部")
            .last("LIMIT 1");
        OldSysOrgDo sysOrg = oldSysOrgMapper.selectOne(wrapper);

        // 先判断：确定areaId
        Integer areaId = determineAreaId(personnelInfo);

        // 后处理: 获取SysOrg
        user.setOrgId(Long.valueOf(getSysOrgByAreaId(areaId, sysOrg)));

        return user;
    }

    public boolean validatePhoneNumber(String mobiles) {
        String telRegex = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(147,145))\\d{8}$";
        return StringUtils.isNotEmpty(mobiles) && mobiles.matches(telRegex);
    }

    // 判断逻辑：确定areaId
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
            List<SysArea> list = iSysAreaService.findEmpAreaIdList(empAreaId);

            // 处理查询结果的公共逻辑
            Integer areaId = processAreaList(list);
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

    // 根据city获取areaId的公共方法
    private Integer getAreaIdByCity(String city) {
        if (StringUtils.isNotEmpty(city) && !"本部".equals(city)) {
            List<SysArea> list = iSysAreaService.queryLikeList(city);
            Integer areaId = processAreaList(list);
            if (areaId != null) {
                return areaId;
            }
        }
        // 如果原有逻辑也未找到，则默认为 1（省公司）
        return 1;
    }

    // 处理SysArea列表的公共逻辑
    private Integer processAreaList(List<SysArea> list) {
        if (list != null && list.size() > 0) {
            SysArea area = list.get(0);
            if ("三级".equals(area.getType())) {
                // 县
                return Math.toIntExact(area.getParent());
            } else if ("二级".equals(area.getType())) {
                // 市
                return Math.toIntExact(area.getId());
            } else if ("一级".equals(area.getType())) {
                // 省
                return Math.toIntExact(area.getId());
            }
        }
        return null; // 表示未找到匹配项
    }

    // 根据areaId获取SysOrg的公共方法
    private Integer getSysOrgByAreaId(Integer areaId, OldSysOrgDo sysOrg) {
        if (areaId != null) {
            LambdaQueryWrapper<OldSysOrgDo> wrapper = new LambdaQueryWrapper<OldSysOrgDo>()
                .eq(OldSysOrgDo::getAreaId, areaId)
                .orderByAsc(OldSysOrgDo::getId)
                .last("LIMIT 1");
            List<OldSysOrgDo> orgList = oldSysOrgMapper.selectList(wrapper);

            if (orgList != null && orgList.size() > 0) {
                return orgList.get(0).getId();
            }
        }
        return sysOrg.getId();
    }

    // 根据areaId获取SysOrg的公共方法
    private void synUserOperate(SysUsers sysUsers, String operateType) {
        // 此时统一给到管理员
        SysUsersVo operator = iSysUsersService.queryById(2L);
        SysUserOperateLog operateLog = new SysUserOperateLog();
        BeanUtils.copyProperties(sysUsers, operateLog);
        operateLog.setOperateType(operateType);
        operateLog.setOperateTime(new Date());
        operateLog.setOperator(Math.toIntExact(operator.getId()));
        operateLog.setInfoId(sysUsers.getId());
        String userRoleYx = sysUsers.getUserRoleYx();
        sysUserOperateLogMapper.insertOrUpdate(operateLog);

        if (StringUtils.isNotEmpty(userRoleYx) && !VALID_FALSE.equals(userRoleYx) && operateType.equals(OPERATE_TYPE_DEL)) {
            try {
                String msg = doPostForYxUser(sysUsers, operateType);
                XxlJobHelper.log("用户：{}，同步结果：{}", sysUsers.getNameCh(), msg);
            } catch (Exception e) {
                XxlJobHelper.log("用户：{}，同步亚信接口异常，详情见日志！", operateLog.getNameCh());
                e.printStackTrace();
            }
        }
    }

    /**
     * 向亚信楼宇同步人员数据
     *
     * @param user     新增或修改后的user
     * @param operType 操作类型  add   mod  del
     * @return
     */
    public String doPostForYxUser(SysUsers user, String operType) {
        OldSysOrgDo oldSysOrgDo = oldSysOrgMapper.selectById(user.getOrgId());
        String jsonStr = setUserPostForYx(user, operType, oldSysOrgDo);
        log.info("亚信楼宇同步接口生成数据:" + jsonStr);
        String baseMsg = "";
        if ("add".equals(operType)) {
            baseMsg = "添加成功";
        } else if ("mod".equals(operType)) {
            baseMsg = "修改成功";
        } else {
            baseMsg = "设置成功";
        }
        String msg = baseMsg + "，同步亚信故障，详情见日志！";
        if ("error".equals(jsonStr)) {
            return msg;
        }

        return doPostForUserYx(user, jsonStr, operType, msg);
    }

    public String setUserPostForYx(SysUsers user, String operType, OldSysOrgDo org) {
        JSONObject json = new JSONObject();
        String cityOrgId = "";
        String cityOrgName = "";
        String countyOrgId = "";
        String countyOrgName = "";
        String gridOrgId = "";
        String gridOrgName = "";
        String gridCode = "";
        String userRoleYx = user.getUserRoleYx();

        String[] s = org.getCode().split("_");
        switch (s.length) {
            case 1:
                //浙江省
                cityOrgId = "Z";
                cityOrgName = "浙江";
                countyOrgId = "Z00";
                countyOrgName = "省中心_00";
                gridOrgId = "1415";
                gridOrgName = "省公司";
                gridCode = "1415";  //渠道网格编码就是网格编码
                break;
            case 2:
                //地市
                if (checkDataForYx(org, user, operType)) {
                    return "error";
                }
                cityOrgId = org.getOrgIdYx();
                cityOrgName = org.getOrgNameYx();
                break;
            case 3:
                //区县
                OldSysOrgDo dsOrg = oldSysOrgMapper.selectById(org.getParent());
                if (checkDataForYx(org, user, operType) || checkDataForYx(dsOrg, user, operType)) {
                    return "error";
                }
                cityOrgId = dsOrg.getOrgIdYx();
                cityOrgName = dsOrg.getOrgNameYx();
                countyOrgId = org.getOrgIdYx();
                countyOrgName = org.getOrgNameYx();
                if (checkCountryIdForYx(countyOrgId, userRoleYx)) {
                    return "error";
                }
                break;
            case 4:
                //网格
                OldSysOrgDo qxOrg = oldSysOrgMapper.selectById(org.getParent());
                OldSysOrgDo dsOrg1 = oldSysOrgMapper.selectById(qxOrg.getParent());

                if (checkDataForYx(org, user, operType) || checkDataForYx(dsOrg1, user, operType) || checkDataForYx(qxOrg, user, operType)) {
                    return "error";
                }
                cityOrgId = dsOrg1.getOrgIdYx();
                cityOrgName = dsOrg1.getOrgNameYx();
                countyOrgId = qxOrg.getOrgIdYx();
                countyOrgName = qxOrg.getOrgNameYx();
                gridOrgId = org.getOrgIdYx();
                gridOrgName = org.getOrgNameYx();
                gridCode = org.getGridCode();
                if (checkCountryIdForYx(countyOrgId, userRoleYx)) {
                    return "error";
                }
                break;
        }
        json.put("cityOrgId", cityOrgId);
        json.put("cityOrgName", cityOrgName);
        json.put("countyOrgId", countyOrgId);
        json.put("countyOrgName", countyOrgName);
        json.put("gridOrgId", gridOrgId);
        json.put("gridOrgName", gridOrgName);
        json.put("gridCode", gridCode);
        json.put("userId", user.getId());
        json.put("userName", user.getNameCh());
        json.put("userPhone", user.getName());
        json.put("userRoles", user.getUserRoleYx());
        json.put("developerCodes", user.getDeveloperCodes());
        json.put("cardNumber", user.getCardNumber());
        String email = user.getEmail();
        String oaAccount = "";
        if (StringUtils.isNotEmpty(email)) {
            oaAccount = email.split("@")[0];
        }
        json.put("oaAccount", oaAccount);
        json.put("staffType", user.getStaffType());
        json.put("operType", operType);

        return JSON.toJSONString(json, new SimpleValueFilter());
    }

    public boolean checkDataForYx(OldSysOrgDo org, SysUsers user, String operType) {
        if (StringUtils.isEmpty(org.getOrgIdYx()) || StringUtils.isEmpty(org.getOrgNameYx())) {
            SysLogYxDo logYx = new SysLogYxDo("user", Math.toIntExact(user.getId()), new Date(), null, "2", "人员组织结构数据错误", operType);
            sysLogYxMapper.insertOrUpdate(logYx);
            return false;
        }
        return false;
    }

    public boolean checkCountryIdForYx(String countyOrgId, String userRoleYx) {
        if ("3".equals(userRoleYx) || "4".equals(userRoleYx) || "5".equals(userRoleYx)) {
            //如果选择区县、网格、楼长、园区长四个角色，(园区长角色删除了)
            // 则针对第三级组织判断县分渠道组织ID是否为空
            return StringUtils.isEmpty(countyOrgId);
        }
        return false;
    }

    static class SimpleValueFilter implements ValueFilter {
        @Override
        public Object process(Object o, String s, Object v) {
            if (v == null) {
                return "";
            }
            return v;
        }
    }

    public String doPostForUserYx(SysUsers user, String jsonStr, String operType, String returnMsg) {
        String yxReturnMsg = "";
        JSONObject retObj = new JSONObject();
        int i = 0;
        do {
            try {
                String url = getYxSyncUrl(jsonStr);
                log.info("亚信同步url信息:" + url);
                log.info("亚信同步信息:" + jsonStr);
                String msg = doPost(url, jsonStr, "utf-8");
                log.info("亚信同步返回信息:" + msg);
                retObj = JSONObject.parseObject(msg);
                //调用亚信结果
                if (!"00000".equals(retObj.getString("respCode")) || !"00000".equals(retObj.getJSONObject("result").getString("code"))) {
                    yxReturnMsg = "修改成功,同步楼宇信息失败!";
                    break;
                }
                if ("00000".equals(retObj.getString("respCode")) && "00000".equals(retObj.getJSONObject("result").getString("code"))) {
                    yxReturnMsg = "修改成功";
                    break;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
                if (i == 5) {
                    yxReturnMsg = returnMsg;
                    SysLogYxDo logYx = new SysLogYxDo("user", Math.toIntExact(user.getId()), new Date(), null, "2", retObj.getString("respDesc"), operType);
                    sysLogYxMapper.insertOrUpdate(logYx);
                }
            } catch (Exception e) {
                yxReturnMsg = returnMsg;
                SysLogYxDo logYx = new SysLogYxDo("user", Math.toIntExact(user.getId()), new Date(), null, "2", "接口调用失败", operType);
                sysLogYxMapper.insertOrUpdate(logYx);
                break;
            }
        } while (i < 5);
        return yxReturnMsg;
    }

    public String getYxSyncUrl(String jsonStr) throws Exception {
        Map<String, String> param = sysParamService.findValueMapsByType("doPostForYx");
        String url = param.get("doPostUrl");
        String appKey = param.get("appKey");
        String method = param.get("method");
        String format = param.get("format");
        String appId = param.get("appId");
        String timestamp = System.currentTimeMillis() + "";
        String random = ((int) (Math.random() * 9999) + 1000) + "";
        String busiSerial = timestamp + random;
        param.put("timestamp", timestamp);
        param.put("busiSerial", busiSerial);
        param.put("content", jsonStr);
        param.remove("doPostUrl");
        param.remove("appKey");
        String sign = sign(param, "HmacSHA256", appKey);
        log.info(param.toString());
        url += "?method=" + method + "&format=" + format + "&appId=" + appId + "&timestamp=" + timestamp + "&busiSerial=" + busiSerial + "&sign=" + sign;
        return url;
    }

    public static String sign(Map<String, String> params, String algorithm, String secret) {
        String signStr = params.entrySet().stream()
            .filter(e -> e.getValue() != null && !"sign".equals(e.getKey()))
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("&"));

        return hmacSign(signStr, secret, algorithm);
    }

    private static String hmacSign(String data, String key, String algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            mac.init(spec);
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("签名失败", e);
        }
    }

    public static String doPost(String url, String jsonstr, String charset) {
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try {
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            if (StringUtils.isNotEmpty(jsonstr)) {
                StringEntity se = new StringEntity(jsonstr, "utf-8");
                se.setContentType("text/json");
                se.setContentEncoding(new BasicHeader("Content-Type", "application/json"));
                se.setContentEncoding(new BasicHeader("ticket", "接口获取得值"));
                httpPost.setEntity(se);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, charset);
                    // 释放资源
                    resEntity.consumeContent();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    // 关闭连接器管理
                    httpClient.getConnectionManager().shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


}
