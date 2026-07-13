package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.DwdContractsDDo;
import com.crm.system.domain.bo.DwdContractsDBo;
import com.crm.system.domain.vo.ContractEformRespVo;
import com.crm.system.domain.vo.DwdContractsDVo;
import com.crm.system.mapper.DwdContractsDMapper;
import com.crm.system.service.IDwdContractsDService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.client.unibss.UniBssApiClient.UniBssResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DWD_CONTRACTS_D 服务层实现
 *
 * @author 万超凡
 * @date 2026-01-19
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class DwdContractsDServiceImpl implements IDwdContractsDService {

    private final DwdContractsDMapper baseMapper;
    @Resource
    private ISysParamService paramService;
    @Resource
    private UniBssApiClient uniBssApiClient;

    /**
     * 根据合同编号查询DWD_CONTRACTS_D
     *
     * @param contractNoLocal 合同编号
     * @return DWD_CONTRACTS_D
     */
    @DS("slave_2")  // 使用达梦数据库
    @Override
    public DwdContractsDVo selectDwdContractsDById(String contractNoLocal) {
        // 使用合同编号作为查询条件，而不是按主键查询
        // 注意：这里使用contractNoLocal作为参数值去查询ARRAIR_NO字段，因为根据业务需求，
        // 传入的合同编号实际上应该对应数据库中的ARRAIR_NO字段
        DwdContractsDDo dwdContractsDDo = baseMapper.selectByArrairNo(contractNoLocal);
        return BeanUtil.toBean(dwdContractsDDo, DwdContractsDVo.class);
    }

    /**
     * 查询DWD_CONTRACTS_D列表
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @return DWD_CONTRACTS_D集合
     */
    @DS("slave_2")  // 使用达梦数据库
    @Override
    public List<DwdContractsDVo> selectDwdContractsDList(DwdContractsDBo dwdContractsDBo) {
        LambdaQueryWrapper<DwdContractsDDo> lqw = buildQueryWrapper(dwdContractsDBo);
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 分页查询DWD_CONTRACTS_D列表
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @param pageQuery       分页查询对象
     * @return DWD_CONTRACTS_D集合
     */
    @DS("slave_2")  // 使用达梦数据库
    @Override
    public TableDataInfo<DwdContractsDVo> selectDwdContractsDPageList(DwdContractsDBo dwdContractsDBo, PageQuery pageQuery) {
        LambdaQueryWrapper<DwdContractsDDo> lqw = buildQueryWrapper(dwdContractsDBo);
        Page<DwdContractsDVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 构建查询条件
     *
     * @param dwdContractsDBo 查询条件
     * @return 查询条件构造器
     */
    private LambdaQueryWrapper<DwdContractsDDo> buildQueryWrapper(DwdContractsDBo dwdContractsDBo) {
        LambdaQueryWrapper<DwdContractsDDo> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getCustomerCode()), DwdContractsDDo::getCustomerCode, dwdContractsDBo.getCustomerCode());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getAreaName()), DwdContractsDDo::getAreaName, dwdContractsDBo.getAreaName());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getCountyCode()), DwdContractsDDo::getCountyCode, dwdContractsDBo.getCountyCode());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getCountyName()), DwdContractsDDo::getCountyName, dwdContractsDBo.getCountyName());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getContractName()), DwdContractsDDo::getContractName, dwdContractsDBo.getContractName());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getArrairNo()), DwdContractsDDo::getArrairNo, dwdContractsDBo.getArrairNo());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getContractNoLocal()), DwdContractsDDo::getContractNoLocal, dwdContractsDBo.getContractNoLocal());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getDeptName()), DwdContractsDDo::getDeptName, dwdContractsDBo.getDeptName());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getTakerName()), DwdContractsDDo::getTakerName, dwdContractsDBo.getTakerName());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getContractType()), DwdContractsDDo::getContractType, dwdContractsDBo.getContractType());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getContractStatusName()), DwdContractsDDo::getContractStatusName, dwdContractsDBo.getContractStatusName());
        return lqw;
    }

    /**
     * 新增DWD_CONTRACTS_D
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @return 结果
     */
    @DS("slave_2")  // 使用达梦数据库
    @Override
    public Boolean insertByBo(DwdContractsDBo dwdContractsDBo) {
        // 达梦数据库通常只读，不允许新增
        throw new UnsupportedOperationException("达梦数据库不允许新增操作");
    }

    /**
     * 修改DWD_CONTRACTS_D
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @return 结果
     */
    @DS("slave_2")  // 使用达梦数据库
    @Override
    public Boolean updateByBo(DwdContractsDBo dwdContractsDBo) {
        // 达梦数据库通常只读，不允许修改
        throw new UnsupportedOperationException("达梦数据库不允许修改操作");
    }

    /**
     * 批量删除DWD_CONTRACTS_D
     *
     * @param ids     需要删除的DWD_CONTRACTS_D主键集合
     * @param isValid 是否验证
     * @return 结果
     */
    @DS("slave_2")  // 使用达梦数据库
    @Override
    public Boolean deleteWithValidByIds(List<String> ids, Boolean isValid) {
        // 达梦数据库通常只读，不允许删除
        throw new UnsupportedOperationException("达梦数据库不允许删除操作");
    }


    @Override
    public ContractEformRespVo getEformInfoByNo(String eFormNo) {
        Map<String, String> params = paramService.findValueMapsByType("oppSyncGroupParam");
        ContractEformRespVo eform = new ContractEformRespVo();
        try {
            Map<String, Object> head = uniBssApiClient.buildHead();
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("contract_code", eFormNo);
            reqMap.put("province_code", "36");
            reqMap.put("contract_type", "04");
            Map<String, Object> body = new HashMap<>();
            body.put("QRY_EFORM_CONTRACT_REQ", reqMap);

            UniBssResponse resp = uniBssApiClient.post(
                    MapUtil.getStr(params, "qryEformContractUrl"), head, body);
            log.info("查询表单电子化合同响应: {}", resp);

            JSONObject rspObj = resp.getRawJson();
            if (rspObj == null) return null;

            String retCode = JSONPath.eval(rspObj, "$UNI_BSS_BODY.QRY_EFORM_CONTRACT_RSP.RSP_CODE").toString();
            if (!"0000".equals(retCode)) {
                return null;
            }
            //提取数据，如果数组存在多条，只取第一条
            com.alibaba.fastjson.JSONArray array = rspObj.getJSONObject("UNI_BSS_BODY")
                .getJSONObject("QRY_EFORM_CONTRACT_RSP").getJSONObject("RSP_DETAIL").getJSONArray("list");
            if (ObjectUtil.isEmpty(array)) {
                return null;
            }
            JSONObject infoMap = array.getJSONObject(0);
            //将map转化为对象
            eform.setContractCode(infoMap.getString("contract_code"));
            eform.setContractName(infoMap.getString("contract_name"));
            eform.setCustomerName(infoMap.getString("customer_name"));
            eform.setSubjectName(infoMap.getString("subject_name"));
            eform.setAgentPersonOa(infoMap.getString("creator"));
            eform.setContractAmount(infoMap.getJSONObject("detail").getString("contractAmount"));
            eform.setSignTime(infoMap.getString("create_time"));
            eform.setStartTime(infoMap.getJSONObject("detail").getString("startTime"));
            eform.setEndTime(infoMap.getJSONObject("detail").getString("endTime"));
            eform.setTradeType(infoMap.getString("trade_type"));
        } catch (Exception e) {
            log.error("查询表单电子化合同异常:{}", e.getMessage());
            return null;
        }
        return eform;
    }
}
