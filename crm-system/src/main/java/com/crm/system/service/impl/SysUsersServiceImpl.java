package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.crm.common.utils.HttpClientUtil;
import com.crm.common.utils.SignUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.domain.SysLogYx;
import com.crm.system.domain.SysOrg;
import com.crm.system.domain.SysUsers;
import com.crm.system.domain.bo.SysUsersBo;
import com.crm.system.domain.vo.SysUsersVo;
import com.crm.system.mapper.SysLogYxMapper;
import com.crm.system.mapper.SysOrgMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.ISysParamService;
import com.crm.system.service.ISysUsersService;
import com.crm.system.service.SysLogYxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.crm.system.mapper.SysUserMapper;
import com.crm.system.service.ISysUserService;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户(客户的员工)Service业务层处理
 *
 * @author crm
 * @date 2025-12-23
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysUsersServiceImpl implements ISysUsersService {

    private final SysUsersMapper baseMapper;

    @Resource
    private ISysParamService sysParamService;

    @Resource
    private SysOrgMapper sysOrgMapper;

    @Resource
    private SysLogYxService sysLogYxService;

    /**
     * 查询用户(客户的员工)
     */
    @Override
    public SysUsersVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询用户(客户的员工)列表
     */
    @Override
    public TableDataInfo<SysUsersVo> queryPageList(SysUsersBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysUsers> lqw = buildQueryWrapper(bo);
        Page<SysUsersVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询用户(客户的员工)列表
     */
    @Override
    public List<SysUsersVo> queryList(SysUsersBo bo) {
        LambdaQueryWrapper<SysUsers> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysUsers> buildQueryWrapper(SysUsersBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysUsers> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getName()), SysUsers::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getNameCh()), SysUsers::getNameCh, bo.getNameCh());
        lqw.eq(StringUtils.isNotBlank(bo.getRelationTel()), SysUsers::getRelationTel, bo.getRelationTel());
        lqw.eq(StringUtils.isNotBlank(bo.getType()), SysUsers::getType, bo.getType());
        lqw.eq(bo.getOrgId() != null, SysUsers::getOrgId, bo.getOrgId());
        lqw.eq(StringUtils.isNotBlank(bo.getPassword()), SysUsers::getPassword, bo.getPassword());
        lqw.eq(StringUtils.isNotBlank(bo.getPasswordSalt()), SysUsers::getPasswordSalt, bo.getPasswordSalt());
        lqw.eq(StringUtils.isNotBlank(bo.getFunctionList()), SysUsers::getFunctionList, bo.getFunctionList());
        lqw.eq(StringUtils.isNotBlank(bo.getRoleList()), SysUsers::getRoleList, bo.getRoleList());
        lqw.eq(StringUtils.isNotBlank(bo.getRemark()), SysUsers::getRemark, bo.getRemark());
        lqw.eq(bo.getCreator() != null, SysUsers::getCreator, bo.getCreator());
        lqw.eq(bo.getModifier() != null, SysUsers::getModifier, bo.getModifier());
        lqw.eq(bo.getModifyTime() != null, SysUsers::getModifyTime, bo.getModifyTime());
        lqw.eq(StringUtils.isNotBlank(bo.getIsValid()), SysUsers::getIsValid, bo.getIsValid());
        lqw.eq(StringUtils.isNotBlank(bo.getWxUserFlag()), SysUsers::getWxUserFlag, bo.getWxUserFlag());
        lqw.eq(StringUtils.isNotBlank(bo.getWxPhone()), SysUsers::getWxPhone, bo.getWxPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getWxNo()), SysUsers::getWxNo, bo.getWxNo());
        lqw.eq(StringUtils.isNotBlank(bo.getWxEmail()), SysUsers::getWxEmail, bo.getWxEmail());
        lqw.eq(StringUtils.isNotBlank(bo.getWxFlag()), SysUsers::getWxFlag, bo.getWxFlag());
        lqw.eq(bo.getStealth() != null, SysUsers::getStealth, bo.getStealth());
        lqw.eq(StringUtils.isNotBlank(bo.getCustomerNo()), SysUsers::getCustomerNo, bo.getCustomerNo());
        lqw.eq(StringUtils.isNotBlank(bo.getOaAccount()), SysUsers::getOaAccount, bo.getOaAccount());
        lqw.eq(StringUtils.isNotBlank(bo.getEmail()), SysUsers::getEmail, bo.getEmail());
        lqw.eq(bo.getFirstLogin() != null, SysUsers::getFirstLogin, bo.getFirstLogin());
        lqw.eq(StringUtils.isNotBlank(bo.getIncludeType()), SysUsers::getIncludeType, bo.getIncludeType());
        lqw.eq(StringUtils.isNotBlank(bo.getUnicomType()), SysUsers::getUnicomType, bo.getUnicomType());
        lqw.eq(StringUtils.isNotBlank(bo.getUnicomType2()), SysUsers::getUnicomType2, bo.getUnicomType2());
        lqw.eq(StringUtils.isNotBlank(bo.getData()), SysUsers::getData, bo.getData());
        lqw.eq(StringUtils.isNotBlank(bo.getToken()), SysUsers::getToken, bo.getToken());
        lqw.eq(StringUtils.isNotBlank(bo.getMarkunits()), SysUsers::getMarkunits, bo.getMarkunits());
        lqw.eq(StringUtils.isNotBlank(bo.getDeveloperCodes()), SysUsers::getDeveloperCodes, bo.getDeveloperCodes());
        lqw.eq(StringUtils.isNotBlank(bo.getCardNumber()), SysUsers::getCardNumber, bo.getCardNumber());
        lqw.eq(StringUtils.isNotBlank(bo.getStaffType()), SysUsers::getStaffType, bo.getStaffType());
        lqw.eq(StringUtils.isNotBlank(bo.getUserRoleYx()), SysUsers::getUserRoleYx, bo.getUserRoleYx());
        lqw.eq(StringUtils.isNotBlank(bo.getZqbusinessFlag()), SysUsers::getZqbusinessFlag, bo.getZqbusinessFlag());
        lqw.eq(StringUtils.isNotBlank(bo.getRecSmsBeginTime()), SysUsers::getRecSmsBeginTime, bo.getRecSmsBeginTime());
        lqw.eq(StringUtils.isNotBlank(bo.getRecSmsEndTime()), SysUsers::getRecSmsEndTime, bo.getRecSmsEndTime());
        lqw.eq(StringUtils.isNotBlank(bo.getAssociatedDeveloper()), SysUsers::getAssociatedDeveloper, bo.getAssociatedDeveloper());
        lqw.eq(StringUtils.isNotBlank(bo.getDefaultDeveloper()), SysUsers::getDefaultDeveloper, bo.getDefaultDeveloper());
        lqw.eq(StringUtils.isNotBlank(bo.getCheckDeveloper()), SysUsers::getCheckDeveloper, bo.getCheckDeveloper());
        return lqw;
    }

    /**
     * 新增用户(客户的员工)
     */
    @Override
    public Boolean insertByBo(SysUsersBo bo) {
        SysUsers add = BeanUtil.toBean(bo, SysUsers.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户(客户的员工)
     */
    @Override
    public Boolean updateByBo(SysUsersBo bo) {
        SysUsers update = BeanUtil.toBean(bo, SysUsers.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysUsers entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除用户(客户的员工)
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 向亚信楼宇同步人员数据
     * @param user 新增或修改后的user
     * @param operType 操作类型  add   mod  del
     * @return
     */
    @Override
    public String doPostForYxUser(SysUsers user, String operType) {
        try {
            // 获取用户组织信息
            SysOrg org = null;
            if (user.getOrgId() != null) {
                org = sysOrgMapper.selectById(user.getOrgId());
            }

            if (org == null) {
                log.error("亚信同步失败：用户组织信息为空，userId:{}", user.getId());
                return "修改成功，同步亚信故障，详情见日志！";
            }

            String jsonStr = setUserPostForYx(user, operType, org);
            log.info("亚信楼宇同步接口生成数据:{}", jsonStr);

            String baseMsg;
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
        } catch (Exception e) {
            log.error("同步亚信用户信息异常", e);
            return "修改成功，同步亚信故障，详情见日志！";
        }
    }

    /**
     * 构建亚信同步JSON数据
     */
    private String setUserPostForYx(SysUsers user, String operType, SysOrg org) {
        JSONObject json = new JSONObject();
        String cityOrgId = "";
        String cityOrgName = "";
        String countyOrgId = "";
        String countyOrgName = "";
        String gridOrgId = "";
        String gridOrgName = "";
        String gridCode = "";
        String userRoleYx = user.getUserRoleYx();

        if (StringUtils.isEmpty(org.getCode())) {
            log.error("亚信同步失败：组织编码为空");
            return "error";
        }

        String[] s = org.getCode().split("_");
        switch (s.length) {
            case 1:
                // 浙江省
                cityOrgId = "Z";
                cityOrgName = "浙江";
                countyOrgId = "Z00";
                countyOrgName = "省中心_00";
                gridOrgId = "1415";
                gridOrgName = "省公司";
                //渠道网格编码就是网格编码
                gridCode = "1415";
                break;
            case 2:
                // 地市
                if (checkDataForYx(org, user, operType)) {
                    return "error";
                }
                cityOrgId = org.getOrgIdYx();
                cityOrgName = org.getOrgNameYx();
                break;
            case 3:
                // 区县
                SysOrg dsOrg = sysOrgMapper.selectById(org.getParent().longValue());
                if (dsOrg == null || checkDataForYx(org, user, operType) || checkDataForYx(dsOrg, user, operType)) {
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
                // 网格
                SysOrg qxOrg = sysOrgMapper.selectById(org.getParent().longValue());
                if (qxOrg == null) {
                    return "error";
                }
                SysOrg dsOrg1 = sysOrgMapper.selectById(qxOrg.getParent().longValue());
                if (dsOrg1 == null || checkDataForYx(org, user, operType) || checkDataForYx(dsOrg1, user, operType) || checkDataForYx(qxOrg, user, operType)) {
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
            default:
                log.error("亚信同步失败：组织编码格式错误，code:{}", org.getCode());
                return "error";
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

    /**
     * 检查县分ID
     */
    private boolean checkCountryIdForYx(String countyOrgId, String userRoleYx) {
        if ("3".equals(userRoleYx) || "4".equals(userRoleYx) || "5".equals(userRoleYx)) {
            // 如果选择区县、网格、楼长角色，则针对第三级组织判断县分渠道组织ID是否为空
            return StringUtils.isEmpty(countyOrgId);
        }
        return false;
    }

    /**
     * 检查数据有效性
     */
    private boolean checkDataForYx(SysOrg org, SysUsers user, String operType) {
        if (StringUtils.isEmpty(org.getOrgIdYx()) || StringUtils.isEmpty(org.getOrgNameYx())) {
            SysLogYx logYx = new SysLogYx();
            logYx.setType("user");
            logYx.setInfoId(user.getId().intValue());
            logYx.setCreateTime(new Date());
            logYx.setStatus("2");
            logYx.setErroMsg("人员组织结构数据错误");
            logYx.setErroType(operType);
            sysLogYxService.save(logYx);
            return true;
        }
        return false;
    }

    /**
     * 调用亚信同步接口
     */
    private String doPostForUserYx(SysUsers user, String jsonStr, String operType, String returnMsg) {
        String yxReturnMsg = "";
        JSONObject retObj = new JSONObject();
        int i = 0;
        do {
            try {
                String url = getYxSyncUrl(jsonStr);
                log.info("亚信同步url信息:{}", url);
                log.info("亚信同步信息:{}", jsonStr);
                String msg = HttpClientUtil.doPost(url, jsonStr, "utf-8");
                log.info("亚信同步返回信息:{}", msg);
                retObj = JSONObject.parseObject(msg);

                // 调用亚信结果
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
                    log.error("线程休眠异常", e);
                }
                i++;
                if (i == 5) {
                    yxReturnMsg = returnMsg;
                    SysLogYx logYx = new SysLogYx();
                    logYx.setType("user");
                    logYx.setInfoId(user.getId().intValue());
                    logYx.setCreateTime(new Date());
                    logYx.setStatus("2");
                    logYx.setErroMsg(retObj.getString("respDesc"));
                    logYx.setErroType(operType);
                    sysLogYxService.save(logYx);
                }
            } catch (Exception e) {
                yxReturnMsg = returnMsg;
                SysLogYx logYx = new SysLogYx();
                logYx.setType("user");
                logYx.setInfoId(user.getId().intValue());
                logYx.setCreateTime(new Date());
                logYx.setStatus("2");
                logYx.setErroMsg("接口调用失败");
                logYx.setErroType(operType);
                sysLogYxService.save(logYx);
                break;
            }
        } while (i < 5);
        return yxReturnMsg;
    }

    /**
     * 获取亚信同步URL
     */
    private String getYxSyncUrl(String jsonStr) throws Exception {
        Map<String, String> param = sysParamService.findValueMapsByType("doPostForYx");
        if (param == null || param.isEmpty()) {
            throw new Exception("未配置亚信同步参数");
        }
            
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
        param.remove("doPostUrl");
        param.remove("appKey");
            
        // 调用签名方法，传入jsonStr作为业务参数
        String sign = SignUtil.sign(param, jsonStr, "HmacSHA256", appKey);
        log.info("亚信同步签名参数:{}", param.toString());
            
        url += "?method=" + method + "&format=" + format + "&appId=" + appId + "&timestamp=" + timestamp + "&busiSerial=" + busiSerial + "&sign=" + sign;
        return url;
    }

    /**
     * 简单值过滤器，将null转换为空字符串
     */
    static class SimpleValueFilter implements ValueFilter {
        @Override
        public Object process(Object object, String name, Object value) {
            if (value == null) {
                return "";
            }
            return value;
        }
    }
}
