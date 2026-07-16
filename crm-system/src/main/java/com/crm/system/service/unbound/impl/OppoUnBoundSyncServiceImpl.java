package com.crm.system.service.unbound.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.constant.OppoSyncConstants;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.JsonUtils;
import com.crm.system.domain.dao.sys.SysLog;
import com.crm.system.domain.bo.SysCodeBo;
import com.crm.system.domain.bo.topp.ToppBidSuboppoBo;
import com.crm.system.domain.bo.topp.ToppContractEformBo;
import com.crm.system.domain.bo.topp.ToppMemberBo;
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.system.domain.vo.SystemCustomerVo;
import com.crm.system.domain.vo.tcomm.TcommCustomerVo;
import com.crm.system.domain.vo.tcomm.TcommTaskVo;
import com.crm.system.domain.vo.topp.*;
import com.crm.system.service.*;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.system.service.sys.ISysLogService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.service.sys.ISysUserService;
import com.crm.system.service.tcomm.ITcommCustomerService;
import com.crm.system.service.tcomm.TcommTaskService;
import com.crm.system.service.topp.*;
import com.crm.system.service.unbound.OppoUnBoundSyncService;
import com.crm.system.utils.Constants;
import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.utils.SSLClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OppoUnBoundSyncServiceImpl implements OppoUnBoundSyncService {
    @Resource
    private ISysCodeService sysCodeService;
    @Resource
    private ITcommCustomerService customerService;

    @Resource
    private IToppOppPreEstimateService preEstimateService;

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private IToppMemberService memberService;

    @Resource
    private TcommTaskService taskService;

    @Resource
    private IToppContractService contractService;

    @Resource
    private IToppContractEformService contractEformService;

    @Resource
    private ISystemCustomerService systemCustomerService;

    @Resource
    private ISysLogService sysLogService;
    private static SysLog sysLog;
    @Resource
    private IToppOpportunityService oppotunityService;
    @Resource
    private IToppBidSuboppoService bidSuboppoService;
    @Resource
    private ISysParamService paramService;


    @Override
    public String dealOppoBoJsonStr(ToppOpportunityVo oppo, String sendSource) {
        Map<String, String> params = paramService.findValueMapsByType("oppSyncGroupParam");
        //快速商机不需要同步集团
        if (oppo.getIsSimpleOpp() != null && oppo.getIsSimpleOpp() == 1) {
            return "商机";
        }
        try {
            TcommCustomerVo customerVo = customerService.queryById(oppo.getCustId());
            //自然客户id为空不同步
            if (ObjectUtil.isEmpty(customerVo.getCustNo())) {
                return "客户经理编码为空";
            }
            Map<String, Object> baseMap = new HashMap<String, Object>();
            baseMap.put("UNI_BSS_HEAD", getHead(params));
            baseMap.put("UNI_BSS_BODY", getBody(oppo, customerVo, params));
            String jsonString = JsonUtils.toJsonString(baseMap);
            log.info("商机信息同步集团接口转化后的json为:{}", jsonString);
            return doPost(MapUtil.getStr(params, "postUrl"), jsonString, "utf-8", "集团商机信息同步接口");
        } catch (Exception e) {
            saveLog("集团商机信息同步接口", "商机同步接口异常:" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    /****
     * 设置头部节点
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    private Map<String, Object> getHead(Map<String, String> map) {
        return UniBssApiClient.buildHead(
                MapUtil.getStr(map, "appId"), MapUtil.getStr(map, "appKey"));
    }


    /***
     * 设置消息体
     */
    private Map getBody(ToppOpportunityVo oppo, TcommCustomerVo customerVo, Map<String, String> map) throws Exception {
        try {
            //报文头（放置系统参数）
            Map<String, Object> UNI_BSS_BODY = new HashMap<String, Object>();
            Map reqMap = new HashMap<String, Object>();
            //设置oppoInfo 商机信息
            setOppoInfo(reqMap, oppo, customerVo, map);
            //设置signInfo 合同信息
            setSignInfo(reqMap, oppo);
            //设置supportInfo 支撑信息
            setSupportInfo(reqMap, oppo);
            //设置扩展信息,前评估信息
            setExtendInfo(reqMap, oppo);
            //设置bidInfo 标讯信息
            setBidInfo(reqMap, oppo);
            UNI_BSS_BODY.put("SYN_PROVINCE_OPPORTUNITY_REQ", reqMap);
            return UNI_BSS_BODY;
        } catch (Exception e) {
            throw e;
        }

    }

    /****
     * 设置body中的req部分的商机信息
     * @param reqMap
     */
    private void setOppoInfo(Map reqMap, ToppOpportunityVo oppo, TcommCustomerVo customerVo, Map<String, String> map) throws Exception {
        try {
            Map<String, Object> oppoMap = new HashMap<String, Object>();
            //商机名称
            oppoMap.put("oppoName", oppo.getOppName());
            //商机编号
            //36为浙江省标识
            oppoMap.put("oppoNumber", OppoSyncConstants.PROVINCE_CODE + "-" + oppo.getOppCode());
            //商机来源编码
            oppoMap.put("source", "source_qt");

            //商机来源默认其他,防止报错
            // 商机来源
            SysCodeVo sysCode = sysCodeService.findCode(Constants.CLUE_SOURCE, oppo.getOppSource());
            if (ObjectUtil.isNotEmpty(sysCode)) {
                oppoMap.put("source", sysCode.getSettingStr1());
            }
            //客户投资预算金额（万元）保留两位小数
            oppoMap.put("clientInvestBudgetAmount", "0");
            if (ObjectUtil.isNotEmpty(oppo.getClientInvestBudgetAmount())) {
                oppoMap.put("clientInvestBudgetAmount", new DecimalFormat(OppoSyncConstants.DECIMAL_FORMAT_TWO).format(oppo.getClientInvestBudgetAmount()));
            }
            //预计合同总金额万元保留两位小数
            oppoMap.put("estimatTotalContratAmount", "0");
            //预期收入万元保留两位小数
            oppoMap.put("estimatedIncome", "0");
            if (ObjectUtil.isNotEmpty(oppo.getExpectContractMoney())) {
                oppoMap.put("estimatTotalContratAmount", new DecimalFormat(OppoSyncConstants.DECIMAL_FORMAT_TWO).format(oppo.getExpectContractMoney()));
                oppoMap.put("estimatedIncome", new DecimalFormat(OppoSyncConstants.DECIMAL_FORMAT_TWO).format(oppo.getExpectContractMoney()));
            }
            //预计签约时间:yyyyMMddHHmmss格式
            oppoMap.put("estimatedSignTime", "");
            if (ObjectUtil.isNotEmpty(oppo.getSignTime())) {
                oppoMap.put("estimatedSignTime", new SimpleDateFormat(OppoSyncConstants.DATE_TIME_FORMAT).format(oppo.getSignTime()));
            }
            oppoMap.put("productCode", "1");
            oppoMap.put("productName", "其他");
            oppoMap.put("customerTypeCode", "400218");
            oppoMap.put("customerTypeName", "其他");
            //1.主要产品/解决方案名称: 当productCode码值对应为“其它”时，省分可传自己系统中的主要产品/解决方案名称;
            //2.归属总部行业/客户分类编码值: 当”主要产品/解决方案名称“编码值不为“1”时，按照附录1中的对应关系，对应附录2中的编码值
            //3.归属省分行业/客户分类名称: 当customerTypeCode码值对应为“其它”时，省分可传自己系统中的归属总部行业/客户分类名称

            // 商机主业务是组网业务默认同步其他（但是数据库存的还是组网业务对应的code，不同步给总部）
            if (!"opp_type_zwyw".equals(oppo.getOppType()) && ObjectUtil.isNotEmpty(oppo.getProductCode())) {
                oppoMap.put("productCode", oppo.getProductCode());
                if ("1".equals(oppo.getProductCode())) {
                    oppoMap.put("productName", oppo.getSfProductName() == null ? "" : oppo.getSfProductName());
                    oppoMap.put("customerTypeName", oppo.getSfCustomerType() == null ? "" : sysCodeService.findCode(Constants.CODE_UNICOM_TYPE, oppo.getSfCustomerType()).getName());
                } else {
                    oppoMap.put("productName", oppo.getProductCode() == null ? "" : sysCodeService.findCode(Constants.CODE_MAIN_PRODUCT, oppo.getProductCode()).getName());
                    SysCodeVo productCodeSysCode = sysCodeService.findCode(Constants.CODE_MAIN_PRODUCT, oppo.getProductCode());
                    SysCodeVo customerTypeCodeSysCode = sysCodeService.findCode("industry", productCodeSysCode.getSettingStr1());
                    oppoMap.put("customerTypeCode", customerTypeCodeSysCode.getCode());
                    oppoMap.put("customerTypeName", customerTypeCodeSysCode.getName());
                }
            }

            //集团处室id
            oppoMap.put("headquartersIndustry", ObjectUtil.isNotEmpty(customerVo.getFristCallingType())?customerVo.getFristCallingType():"");
            SysCodeBo sysCodeBo = new SysCodeBo();
            sysCodeBo.setType("opp_type");

            Map<String, String> codeMap = sysCodeService.queryList(sysCodeBo).stream()
                .collect(Collectors.toMap(SysCodeVo::getCode, SysCodeVo::getSettingStr1));
            //20230412商机同步接口修改，增加次要业务类型、类型名字段，且原先为‘主要业务类型’的字段名传值改为次要类型
            //主要业务类型码值: 参照后续章节的码值
            oppoMap.put("primaryOppoTypeCode", MapUtil.getStr(codeMap, oppo.getOppType(), "busin_qt"));
            //主要业务类型名称: 当oppoTypeCode码值对应为“其它”时，省分可传自己系统中的业务类型名称
            String primaryOppoTypeName =sysCodeService.findCode("opp_sf_type", MapUtil.getStr(codeMap, oppo.getOppType(), "busin_qt")).getName();
            if (ObjectUtil.isNotEmpty(primaryOppoTypeName)) {
                oppoMap.put("primaryOppoTypeName", primaryOppoTypeName);
            }else{
                oppoMap.put("primaryOppoTypeName", sysCodeService.findCode("opp_sf_type","busin_qt").getName());
            }
            //时间有限，规避风险
            //次要业务类型码值: 包含业务多个（不允许存在主要类型的编码）
            oppoMap.put("oppoTypeCode", "");
            //次要业务类型名称: 包含业务多个（不允许存在主要类型的编码）
            oppoMap.put("oppoTypeName", "");

            //如果商机的集团客户ID为36开头，并且长度为19位，则根据该编码查询集团客户信息，获取bbssCustId和custId
            // 因为2022年9月8号以后这两个字段就得必填了
            oppoMap.put("natureCustId", ObjectUtil.isNotEmpty(customerVo.getCustNo())?customerVo.getCustNo():"");
            //客户名称在传输过程中括号半角被转全角了
            oppoMap.put("natureCustName", customerVo.getCustName().replace("（", "(").replace("）", ")"));
            if (ObjectUtil.isNotEmpty(customerVo.getBbssCustId())) {
                oppoMap.put("bbssCustId", customerVo.getBbssCustId());//客户中心对应集客custid
            }
            oppoMap.put("isClearBudget","1");//是否清空预算，1是，0否
            //默认行业Z
            oppoMap.put("isClearBudget","1");//是否清空预算，1是，0否
            oppoMap.put("industry", "99");    //一级行业
            oppoMap.put("industryName", "其他");
            oppoMap.put("industry2", "9901");//二级行业
            oppoMap.put("industry2Name", "其他");
            oppoMap.put("industry3", "");//三级行业
            //客户联系人
            oppoMap.put("custContact", ObjectUtil.isNotEmpty(oppo.getCustContact())?oppo.getCustContact():"");
            //客户联系人电话
            oppoMap.put("contactTelephone",ObjectUtil.isNotEmpty( oppo.getCustContactTel())?oppo.getCustContactTel():"");
            //判断是否名单制客户,默认非名单制客户
            //是否名单制客户，0不是，1是
            oppoMap.put("isListCust", "0");
            //名单制客户类型
            oppoMap.put("custType", "");
            //名单制客户信息
            oppoMap.put("custInfo", "");
            //集团客户不为空的情况下
            if (ObjectUtil.isNotEmpty(customerVo)) {
                //20230427 行业类型由远程接口直接查自然客户行业
                if (ObjectUtil.isNotEmpty(customerVo.getFristPvCallingType()) &&
                    ObjectUtil.isNotEmpty(customerVo.getSecondPvCallingType())) {
                    //一级行业
                    oppoMap.put("industry", customerVo.getFristPvCallingType());
                    // 一级行业名称
                    oppoMap.put("industryName", customerVo.getFristPvCallingTypeName());
                    //二级行业
                    oppoMap.put("industry2", customerVo.getSecondPvCallingType());
                    // 二级行业名称
                    oppoMap.put("industry2Name", customerVo.getSecondPvCallingTypeName());
                    oppoMap.put("industry3", ObjectUtil.isNotEmpty(customerVo.getThirdPvCallingType())?customerVo.getThirdPvCallingType():"");
                }
                SystemCustomerVo sysCustByName = systemCustomerService.findSysCustByName(customerVo.getCustName());
                //是否名单制客户，0不是，1是
                if (ObjectUtil.isNotEmpty(sysCustByName)) {
                    oppoMap.put("isListCust", "1");
                    //名单制客户类型
                    oppoMap.put("custType", "属地业务");
                    //名单制客户信息
                    oppoMap.put("custInfo", customerVo.getCustName());
                }
            }
            //客户需求简介
            oppoMap.put("custReq", ObjectUtil.isNotEmpty(oppo.getCustNeed())?oppo.getCustNeed():"");
            //新型ICT产品字段  非必填
            if(ObjectUtil.isNotEmpty(oppo.getNewIct())){
                oppoMap.put("ictProductId", oppo.getNewIct());
            }
            //备注
            oppoMap.put("remark",  ObjectUtil.isNotEmpty(oppo.getMemo())?oppo.getMemo():"");
            //创建时间:yyyyMMddHHmmss格式
            oppoMap.put("createTime", new SimpleDateFormat(OppoSyncConstants.DATE_TIME_FORMAT).format(oppo.getCreateTime()));
            //更新时间:yyyyMMddHHmmss格式
            oppoMap.put("updateTime", new SimpleDateFormat(OppoSyncConstants.DATE_TIME_FORMAT).format(new Date()));
            //原为创建人登录名或手机号，改为客户经理的登录名或手机号
            SysUser sysuser = sysUserService.selectUserById(oppo.getCustManagerId());
            if (ObjectUtil.isNotEmpty(sysuser.getUserName())) {
                oppoMap.put("creator", sysuser.getUserName());
            } else {
                oppoMap.put("creator", sysuser.getPhonenumber());
            }
            //流程状态: 参照后续章节的码值
            oppoMap.put("oppoState", getOppStateInfo(oppo));
            //废弃原因
            oppoMap.put("discardReason", ObjectUtil.isNotEmpty(oppo.getEndOppTypeMemo())?oppo.getEndOppTypeMemo():"");
            //省分编码
            oppoMap.put("synBusOppoSources", OppoSyncConstants.PROVINCE_CODE);
            //如果五融信息没有填写
            if (ObjectUtil.isEmpty(oppo.getFiveMergeState())) {
                oppoMap.put("fiveMergeState", 0);
                //是否涉及终端
                oppoMap.put("referTerminal", 0);
            } else {
                oppoMap.put("fiveMergeState", oppo.getFiveMergeState());
                if (oppo.getFiveMergeState() == 1) {
                    String fiveMergeCode = oppo.getFiveMergeCode();
                    if (fiveMergeCode.endsWith(",")) {
                        fiveMergeCode = fiveMergeCode.substring(0, fiveMergeCode.length() - 1);
                    }
                    oppoMap.put("fiveMergeCode", fiveMergeCode);
                }
                //是否涉及终端
                oppoMap.put("referTerminal", oppo.getReferTerminal());
            }

            //营销线索穿透流程ID
            oppoMap.put("dispatchId", "");
            if (ObjectUtil.isNotEmpty(oppo.getGroupClueKey())) {
                oppoMap.put("dispatchId", oppo.getGroupClueKey());
            }
            // 增加是否包挂责任领导 1-是 0-否（先写死0）
            oppoMap.put("responsibleLeadership", "0");
            // 责任领导工号，当是否包挂责任领导为是时必填
            reqMap.put("oppoInfo", oppoMap);
        } catch (Exception e) {
            throw e;
        }

    }

    /****
     * 设置body中的req部分的合同信息
     * @param reqMap
     */
    private void setSignInfo(Map reqMap, ToppOpportunityVo oppo) throws Exception {
        try {
            List<ToppContractVo> contractList = contractService.findContractDetailById(oppo.getId());
            List<Map> signInfoList = new ArrayList();
            //合同列表
            for (ToppContractVo contract : contractList) {
                Map<String, Object> signInfoMap = new HashMap<String, Object>();
                //签约类：值为“合同”或“受理单”
                if ("1".equals(contract.getContractAddType())) {
                    signInfoMap.put("signType", OppoSyncConstants.SIGN_TYPE_APPLICATION);
                    // 业务受理单
                } else if ("2".equals(contract.getContractAddType())) {
                    signInfoMap.put("signType", OppoSyncConstants.SIGN_TYPE_CONTRACT);
                }
                //合同名称
                signInfoMap.put("signName", contract.getContractName());
                //客户id,可为空
                signInfoMap.put("clientoId", oppo.getCustNo());
                //客户名称
                signInfoMap.put("clientoName", oppo.getCustName());
                //合同编号
                signInfoMap.put("contractId", contract.getContractNo());
                //签约时间:yyyyMMddHHmmss格式
                signInfoMap.put("signDate", new SimpleDateFormat(OppoSyncConstants.DATE_TIME_FORMAT).format(contract.getSignDate()));
                //合同类型
                signInfoMap.put("contractType", OppoSyncConstants.CONTRACT_TYPE_PROJECT);
                //签约主体
                signInfoMap.put("signSub", contract.getOtherCompany());
                //合同收入万元保留两位小数
                signInfoMap.put("contractIncome", new DecimalFormat(OppoSyncConstants.DECIMAL_FORMAT_TWO).format(contract.getTotalMoney()));
                //合同生效日期:yyyyMMddHHmmss格式
                signInfoMap.put("effectDate", new SimpleDateFormat(OppoSyncConstants.DATE_TIME_FORMAT).format(contract.getStartDate()));
                //合同结束日期:yyyyMMddHHmmss格式
                signInfoMap.put("endDate", new SimpleDateFormat(OppoSyncConstants.DATE_TIME_FORMAT).format(contract.getEndDate()));
                //合同年收入万元
                signInfoMap.put("annualIncome", "");
                //计收类型: 一次性计收/按年计收/按月计收
                signInfoMap.put("countType", contract.getIncomeType());
                //计收时间
                signInfoMap.put("countTime", "");
                //计收金额万元保留两位小数
                signInfoMap.put("countMoney", "");
                //合同中软件与服务费万元保留两位小数
                signInfoMap.put("signExpense", "");
                signInfoList.add(signInfoMap);
            }
            // 获取电子化表单合同
            ToppContractEformBo toppContractEformBo = new ToppContractEformBo();
            toppContractEformBo.setProjectId(oppo.getId());
            toppContractEformBo.setIsValid(1);
            List<ToppContractEformVo> eformList = contractEformService.queryList(toppContractEformBo);
            SimpleDateFormat formatter = new SimpleDateFormat(OppoSyncConstants.DATE_TIME_FORMAT);
            for (ToppContractEformVo eform : eformList) {
                Map<String, Object> signInfoMap = new HashMap<String, Object>();
                signInfoMap.put("signType", "电子表单");
                signInfoMap.put("signName", eform.getEformName());
                //客户id,可为空
                signInfoMap.put("clientoId", oppo.getCustNo());
                //客户名称
                signInfoMap.put("clientoName", eform.getCustName());
                //合同编号
                signInfoMap.put("contractId", eform.getEformNo());
                //签约时间:yyyyMMddHHmmss格式
                signInfoMap.put("signDate", formatter.format(DateUtils.parseDate(eform.getSignTime())));
                //签约主体
                signInfoMap.put("signSub", eform.getOurCompany());
                //合同收入万元保留两位小数
                signInfoMap.put("contractIncome", new DecimalFormat(OppoSyncConstants.DECIMAL_FORMAT_TWO).format(eform.getTotalMoney()));
                //合同生效日期:yyyyMMddHHmmss格式
                signInfoMap.put("effectDate", formatter.format(DateUtils.parseDate(eform.getStartDate() + " 00:00:00")));
                //合同结束日期:yyyyMMddHHmmss格式
                signInfoMap.put("endDate", formatter.format(DateUtils.parseDate(eform.getEndDate() + " 23:59:59")));
                signInfoList.add(signInfoMap);
            }

            reqMap.put("signInfo", signInfoList);
        } catch (Exception e) {
            throw e;
        }

    }


    /****
     * 设置body中的req部分的支撑信息
     * @param reqMap
     */
    private void setSupportInfo(Map reqMap, ToppOpportunityVo oppo) throws Exception {
        try {
            ToppMemberBo member = new ToppMemberBo();
            member.setProjectId(oppo.getId());
            member.setMemberRole(Constants.MEMBER_ROLE_ZCJL);
            List<ToppMemberVo> list = memberService.queryList(member);
            //登陆名:主责支撑经理
            if (oppo.getDesigner() != null) {
                Map<String, Object> supportInfo = new HashMap<String, Object>();
                // 支撑内容
                SysUser sysUser = sysUserService.selectUserById(oppo.getDesigner());
                supportInfo.put("requirement", sysUser.getNickName() + "支撑该商机");
                String email1 = "";
                if (ObjectUtil.isNotEmpty(sysUser.getEmail())) {
                    email1 = sysUser.getEmail().split("@")[0];
                }
                supportInfo.put("suppManager", email1);
                //申请时间:yyyyMMddHHmmss格式,取商机的确定支撑经理时间
                supportInfo.put("applicationTime", new SimpleDateFormat(OppoSyncConstants.DATE_TIME_FORMAT).format(oppo.getDesignerTime()));
                //登陆名:省分处理人
                supportInfo.put("groupManager", "");
                // 根据商机Id查询待办列表
                List<TcommTaskVo> taskList = taskService.getTasksByInfoId(oppo.getId());
                List<Integer> scorelist = new ArrayList<Integer>();
                if (ObjectUtil.isNotEmpty(taskList)) {
                    for (TcommTaskVo task : taskList) {
                        if ("1.2".equals(task.getScore().toString())) {
                            scorelist.add(5);
                        } else if ("1.1".equals(task.getScore().toString())) {
                            scorelist.add(4);
                        } else if ("1.0".equals(task.getScore().toString())) {
                            scorelist.add(3);
                        } else if ("0.9".equals(task.getScore().toString())) {
                            scorelist.add(2);
                        } else if ("0.8".equals(task.getScore().toString())) {
                            scorelist.add(1);
                        }
                    }
                }
                //评价分值默认5.0 modby张文龙
                double avgAge = 5;
                // 如果不为空，就计算平均值
                if (ObjectUtil.isNotEmpty(scorelist)) {
                    avgAge = calculateAverage(scorelist);
                }
                //支撑评价：1-5分1为最差，5为最好。总部会定期检查该字段值的正确性。四舍五入
                supportInfo.put("suppEva", String.valueOf((int) (avgAge + 0.5)));
                //省分系统订单编号,定定义
                supportInfo.put("opposfId", OppoSyncConstants.PROVINCE_CODE + "-" + oppo.getOppCode());
                //
                List<Map> supMgrList = new ArrayList<Map>();

                //先判断是否做过支撑贡献度打分
                boolean isSupMgrContri = false;
                for (ToppMemberVo m : list) {
                    if (ObjectUtil.isNotEmpty(m.getContri())) {
                        isSupMgrContri = true;
                        break;
                    }
                }

                for (ToppMemberVo m : list) {
                    Map<String, Object> supMgrBo = new HashMap<String, Object>();
                    SysUser memberUser = sysUserService.selectUserById(m.getMemberId());
                    String email = memberUser.getEmail();
                    //做过支撑贡献度,那么按照每个人实际记录的支撑贡献度记录
                    if (isSupMgrContri) {
                        if (ObjectUtil.isEmpty(m.getContri())) {
                            supMgrBo.put("supMgrContri", 0);
                        } else {
                            supMgrBo.put("supMgrContri", new DecimalFormat("#").format(m.getContri()));
                        }
                    } else { //如果没做过支撑贡献度打分,那么按照100分平均下
                        supMgrBo.put("supMgrContri", new DecimalFormat("#").format(100.00 / list.size()));
                    }
                    //支撑经理账号
                    if (ObjectUtil.isNotEmpty(email) && email.contains("@")) {
                        supMgrBo.put("supMgr", email.substring(0, email.indexOf("@")));
                    }
                    supMgrList.add(supMgrBo);
                }
                supportInfo.put("supMgrList", supMgrList);
                reqMap.put("supportInfo", supportInfo);
            }
        } catch (Exception e) {

        }


    }


    /****
     * 设置扩展信息
     * @param reqMap
     */
    private void setExtendInfo(Map reqMap, ToppOpportunityVo oppo) throws Exception {
        try {
            Map extendInfoMap = new HashMap();
            //商机前评估信息
            ToppOppPreEstimateVo oppoPreEstimate = preEstimateService.getPreEstimateByOppId(oppo.getId());
            if (ObjectUtil.isNotEmpty(oppoPreEstimate) && ObjectUtil.isNotEmpty(oppoPreEstimate.getOppDesigner())) {
                extendInfoMap.put("intent_login", sysUserService.selectUserById(oppoPreEstimate.getOppDesigner()).getUserName());
            }
            reqMap.put("extendInfo", extendInfoMap);
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    /***
     * 设置bidInfo 标讯信息（接口规范: 单个object, 约束?=0-1）
     * 字段: infoId, releaseDate(yyyy-MM-dd HH:mm:ss), projectName, projectNumber
     */
    private void setBidInfo(Map reqMap, ToppOpportunityVo oppo) throws Exception {
        try {
            ToppBidSuboppoBo queryBo = new ToppBidSuboppoBo();
            queryBo.setProjectId(oppo.getId());
            queryBo.setValid(true);
            List<ToppBidSuboppoVo> bidList = bidSuboppoService.queryList(queryBo);
            if (bidList == null || bidList.isEmpty()) return;
            // 接口规范: bidInfo是单个对象(?=0-1)，取第一条有效记录
            ToppBidSuboppoVo bid = bidList.get(0);
            Map<String, Object> bidInfo = new HashMap<>();
            bidInfo.put("infoId", bid.getInfoId() != null ? bid.getInfoId() : "");
            if (bid.getReleaseDate() != null) {
                bidInfo.put("releaseDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bid.getReleaseDate()));
            } else {
                bidInfo.put("releaseDate", "");
            }
            bidInfo.put("projectName", bid.getProjectName() != null ? bid.getProjectName() : "");
            bidInfo.put("projectNumber", bid.getProjectCode() != null ? bid.getProjectCode() : "");
            reqMap.put("bidInfo", bidInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算List<Integer>平均值
     *
     * @param marks
     * @return
     */
    private double calculateAverage(List<Integer> marks) {
        Integer sum = 0;
        if (!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }

    private String getOppStateInfo(ToppOpportunityVo oppo) {
        //获取状态以前先更新状态
        String oppoState = oppotunityService.updateOppoState(oppo.getId());
        // 商机录入
        if (Constants.OPPO_STATUS_CJSJ.equals(oppoState) || Constants.OPPO_STATUS_SJZC.equals(oppoState)) {
            // 支撑中
            if (ObjectUtil.isNotEmpty(oppo.getDesigner())) {
                return Constants.TYPE_ZCZ;
            }
            return Constants.TYPE_SFPS;
            // 转实施,合同审核完成后,会修改商机状态为实施
        } else if (Constants.OPPO_STATUS_SJSS.equals(oppoState)) {
            return Constants.TYPE_YQY;
            // 已签约
        } else if (Constants.OPPO_STATUS_SJQY.equals(oppoState) || Constants.OPPO_STATUS_SJDC.equals(oppoState)) {
            return Constants.TYPE_YQY;
            // 废弃
        } else if (Constants.OPPO_STATUS_END.equals(oppoState)) {
            return Constants.TYPE_FQ;
        } else {
            return Constants.TYPE_ZCZ;
        }


    }


    /**
     * 进行http post请求
     *
     * @param url
     * @param jsonstr
     * @param charset
     * @return
     */
    public String doPost(String url, String jsonstr, String charset, String str) {
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try {
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(OppoSyncConstants.HTTP_CONNECT_TIMEOUT)
                .build();
            httpPost.setConfig(requestConfig);
            httpPost.addHeader("Content-Type", OppoSyncConstants.CONTENT_TYPE_JSON);
            httpPost.addHeader("Accept", OppoSyncConstants.ACCEPT_JSON);
            httpPost.addHeader("Accept-Encoding", OppoSyncConstants.ACCEPT_ENCODING_UTF8);
            sysLog = saveLog(str, jsonstr);
            if (ObjectUtil.isNotEmpty(jsonstr)) {
                StringEntity se = new StringEntity(jsonstr, "utf-8");
                httpPost.setEntity(se);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, charset);
                    JSONObject json = JSONUtil.parseObj(result);
                    log.info("请求接口返回消息" + json);
                    sysLog.setRemark(json.toString());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            sysLog.setRemark("请求接口出错" + ex.getMessage());
            result = "(请求接口出错:" + ex.getMessage() + ")";
            log.error("请求接口出错" + ex.getMessage());
        } finally {
            sysLogService.saveLog(sysLog);

        }
        return result;
    }

    public static SysLog saveLog(String type, String content) {
        sysLog = new SysLog();
        sysLog.setType(type);
        sysLog.setContent(content);
        sysLog.setCreateTime(LocalDateTime.now());
        return sysLog;
    }

    /**
     * 同步商机状态给集团
     */
    @Override
    public String syncOppoStatus(ToppOpportunityVo oppo, String status) {

        Map<String, String> params = paramService.findValueMapsByType("oppSyncGroupParam");
        //快速商机不需要同步集团
        if (oppo.getIsSimpleOpp() != null && oppo.getIsSimpleOpp() == 1) {
            return null;
        }
        try {
            Map<String, Object> baseMap = new HashMap<String, Object>();
            baseMap.put("UNI_BSS_HEAD", getHead(params));
            baseMap.put("UNI_BSS_BODY", getSyncOppoStatusBody(oppo, status));
            String jsonString = JsonUtils.toJsonString(baseMap);
            log.info("同步商机状态给集团转化后的json为:" + jsonString);
            String result = doPost(MapUtils.getString(params, "postSyncOppoStatusUrl"), jsonString, "utf-8", "集团商机状态同步接口");
            log.info("同步商机状态给集团返回结果:" + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 设置同步商机状态接口消息体
     */
    private Map getSyncOppoStatusBody(ToppOpportunityVo oppo, String status) throws Exception {
        try {
            Map<String, Object> UNI_BSS_BODY = new HashMap<String, Object>();//报文头（放置系统参数）
            Map reqMap = new HashMap<String, Object>();
            setOppoStatusInfo(reqMap, oppo, status);
            UNI_BSS_BODY.put("SYN_SEGMENT_TIME_REQ", reqMap);
            return UNI_BSS_BODY;
        } catch (Exception e) {
            throw e;
        }
    }


    private void setOppoStatusInfo(Map reqMap, ToppOpportunityVo oppo, String status) throws Exception {
        try {
            reqMap.put("oppoState", status);
            // 当前环节到达时间
            LocalDateTime arriveTime = LocalDateTime.now();
            // 商机编号
            reqMap.put("oppoNumber", OppoSyncConstants.PROVINCE_CODE + "-" + oppo.getOppCode()); // 36为浙江省标识
            // 当前环节到达时间
            reqMap.put("arriveTime", arriveTime);
            // 距上一环节用时.格式:时间戳
            if (OppoSyncConstants.OPP_STATUS_SAVE.equals(status)) { // 商机录入(oppo_save) - 商机录入时间
                reqMap.put("time", "0"); // 距上一环节用时.格式:时间戳
            } else if (OppoSyncConstants.OPP_STATUS_EVALUATION.equals(status)) { // 商机评审(oppo_evaluation) - 商机审核通过时间
                // 当前时间减去商机创建时间
                if (ObjectUtil.isEmpty(oppo.getAuditTime())) {
                    reqMap.put("time", 0);
                } else {
                    Duration duration = Duration.between(oppo.getCreateTime().toInstant()
                        .atZone(ZoneId.systemDefault()), arriveTime);
                    long durationResult = duration.toMillis();
                    reqMap.put("time", String.valueOf(durationResult));
                }
            } else if (OppoSyncConstants.OPP_STATUS_SUPPORT_ORDER_ARRIVED.equals(status)) { // 支撑负责人收到工单时间
                Duration duration = Duration.between(oppo.getAuditTime().toInstant() // 将 Date 转为 Instant（时间戳，无时区）
                    .atZone(ZoneId.systemDefault()) // 关联时区（关键步骤）
                    .toLocalDateTime(), arriveTime);
                // 当前时间减去商机审核时间
                long durationResult = duration.toMillis();
                reqMap.put("time", String.valueOf(durationResult));
            } else if (OppoSyncConstants.OPP_STATUS_SUPPORT_FIRST_DEAL.equals(status)) { // 支撑负责人首次处理时间
                // 当前时间减去支撑负责人收到工单时间
                Duration duration = Duration.between(oppo.getAuditTime().toInstant() // 将 Date 转为 Instant（时间戳，无时区）
                    .atZone(ZoneId.systemDefault()) // 关联时区（关键步骤）
                    .toLocalDateTime(), arriveTime);
                long durationResult = duration.toMillis();
                reqMap.put("time", String.valueOf(durationResult));
            } else if (OppoSyncConstants.OPP_STATUS_SUPPORT_COMPLETE.equals(status)) { // 支撑完成(oppo_support_complete) - 第一次添加合同发起时间
                // 当前时间减去客户经理提交合同开启审核流程时间
                Duration duration = Duration.between(oppo.getAuditTime().toInstant() // 将 Date 转为 Instant（时间戳，无时区）
                    .atZone(ZoneId.systemDefault()) // 关联时区（关键步骤）
                    .toLocalDateTime(), arriveTime);
                long durationResult = duration.toMillis();
                reqMap.put("time", String.valueOf(durationResult));
            } else if (OppoSyncConstants.OPP_STATUS_SIGN.equals(status)) { // 签约(oppo_sign) - 第一次合同审核通过时间
                // 当前时间减去第一条合同创建时间
                ToppContractVo contract = contractService.findContractDetailById(oppo.getId()).get(0);
                Duration duration = Duration.between(contract.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), arriveTime);
                long durationResult = duration.toMillis();
                reqMap.put("time", String.valueOf(durationResult));
            } else if (OppoSyncConstants.OPP_STATUS_ABANDON.equals(status)) { // 废弃(oppo_abandon) - 商机终止审核通过时间
                // 当前时间减去商机创建时间
                if (Constants.NEW_OPP_PHASE_OPP_END_LINK.equals(oppo.getOppoState())) {
                    Duration duration = Duration.between(oppo.getCreateTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(), arriveTime);
                    long durationResult = duration.toMillis();
                    reqMap.put("time", String.valueOf(durationResult));
                }
            }
            if (ObjectUtil.isNotEmpty(MapUtils.getLong(reqMap, "time")) && MapUtils.getLong(reqMap, "time") < 0) {
                reqMap.put("time", 0);
            }
        } catch (Exception e) {
            throw e;
        }
    }


}
