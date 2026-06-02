package com.crm.adapter.admin.workflow.oldProject;

import com.crm.common.core.domain.R;
import com.crm.common.utils.StringUtils;

import com.crm.system.domain.bo.ZqztGrpContractInfoBo;
import com.crm.system.domain.dao.DwdContractsDDo;
import com.crm.system.domain.vo.ZqztGrpContractInfoVo;
import com.crm.system.mapper.DwdContractsDMapper;
import com.crm.system.service.IZqztGrpContractInfoService;
import com.crm.system.service.sys.ISysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合同查询 Controller
 * 提供 ERP 合同查询接口，供商机关闭转化表单使用
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/contract")
public class ContractController {

    private final DwdContractsDMapper dwdContractsDMapper;
    private final IZqztGrpContractInfoService grpContractInfoService;
    private final ISysConfigService configService;

    /**
     * 获取分项收入显示阈值（万元）
     * 老系统：sys_param 表按 orgCode 取值，默认 "10"
     */
    @PostMapping("/getMaxTotalMoney")
    public R<String> getMaxTotalMoney() {
        String val = configService.selectConfigByKey("contract.maxTotalMoney");
        if (StringUtils.isBlank(val)) {
            val = "10";
        }
        return R.ok(val);
    }

    /**
     * ERP 合同查询
     * 先查 DWD_CONTRACTS_D，查不到则 fallback 到 zqzt_grp_contract_info
     * 返回格式与老系统 ConOpenContractOne 对齐
     */
    @PostMapping("/addContract")
    public R<Map<String, Object>> addContract(@RequestParam String code) {
        Map<String, Object> result = new HashMap<>();

        // 1. 查询 DWD_CONTRACTS_D（达梦数据库）
        DwdContractsDDo dwd = dwdContractsDMapper.selectByArrairNo(code);
        if (dwd != null) {
            Map<String, Object> contract = new HashMap<>();
            contract.put("contract_code", dwd.getArrairNo());
            contract.put("contract_name", dwd.getContractName());
            contract.put("contract_type", dwd.getContractType());
            contract.put("contract_money", dwd.getArrairAmount());
            contract.put("contract_main", dwd.getRepositoryNameUnicom());
            contract.put("contract_party", dwd.getRepositoryNameVendor());
            contract.put("contract_stat_name", dwd.getContractStatusName());
            contract.put("contract_dept", dwd.getDeptName());
            contract.put("contract_man", dwd.getTakerName());
            contract.put("contract_phone", dwd.getTakerPhone());
            contract.put("contract_signdate", dwd.getEnterperTime());
            contract.put("contract_effdate", dwd.getContractStartDate());
            contract.put("contract_expdate", dwd.getContractEndDate());
            contract.put("audit_date", dwd.getApprovalDate());
            contract.put("contract_nature_name", dwd.getContractChangeTypeName());
            contract.put("contract_note", dwd.getChoseTxt());
            contract.put("contract_oldnum", dwd.getOldArriarNo());
            // 合同三级分类编码（老系统从 contract_type_code 推导一二三级编码）
            if (StringUtils.isNotBlank(dwd.getContractTypeCode())) {
                String c = dwd.getContractTypeCode();
                contract.put("contract_cate_l3_code", c);
                contract.put("contract_cate_l2_code", c.length() >= 3 ? c.substring(0, 3) + "00" : "");
                contract.put("contract_cate_l1_code", c.length() >= 1 ? c.substring(0, 1) + "0000" : "");
            }
            result.put("status", "1");
            result.put("contract", contract);
            return R.ok(result);
        }

        // 2. Fallback：查询 zqzt_grp_contract_info
        ZqztGrpContractInfoBo bo = new ZqztGrpContractInfoBo();
        bo.setErpContractId(code);
        List<ZqztGrpContractInfoVo> grpList = grpContractInfoService.queryList(bo);
        if (grpList != null && !grpList.isEmpty()) {
            ZqztGrpContractInfoVo g = grpList.get(0);
            Map<String, Object> contract = new HashMap<>();
            contract.put("contract_code", g.getErpContractId());
            contract.put("contract_name", g.getContractName());
            contract.put("contract_type", g.getContractType());
            contract.put("contract_money", g.getContractMoney());
            contract.put("contract_main", g.getContractMain());
            contract.put("contract_party", g.getContractParty());
            contract.put("contract_dept", g.getContractDept());
            contract.put("contract_man", g.getContractMan());
            contract.put("contract_signdate", formatDate(g.getContractSigndate()));
            contract.put("contract_effdate", formatDate(g.getContractEffdate()));
            contract.put("contract_expdate", formatDate(g.getContractExpdate()));
            contract.put("audit_date", formatDate(g.getAuditDate()));
            contract.put("contract_note", g.getContractNote());
            contract.put("contract_oldnum", g.getContractOldnum());
            contract.put("contract_stat_name", getContractStatusName(g.getContractStatus()));
            contract.put("contract_nature_name", getNatureName(g.getContractProperty()));
            contract.put("serv_amount", g.getServAmount());
            contract.put("is_fix", g.getIsFix());
            // 分类编码
            contract.put("contract_cate_l1_code", g.getContractCateL1Code());
            contract.put("contract_cate_l1_name", g.getContractCateL1Name());
            contract.put("contract_cate_l2_code", g.getContractCateL2Code());
            contract.put("contract_cate_l2_name", g.getContractCateL2Name());
            contract.put("contract_cate_l3_code", g.getContractCateL3Code());
            contract.put("contract_cate_l3_name", g.getContractCateL3Name());
            result.put("status", "1");
            result.put("contract", contract);
            return R.ok(result);
        }

        result.put("status", "0");
        result.put("msg", "未找到合同信息");
        return R.ok(result);
    }

    /**
     * 合同状态码 → 中文名称
     */
    private String getContractStatusName(String status) {
        if (status == null) return null;
        switch (status) {
            case "0": return "其他";
            case "1": return "已审批";
            case "2": return "审签中";
            case "3": return "暂停履行";
            case "4": return "终止履行";
            case "5": return "办结";
            case "6": return "变更补充";
            case "7": return "作废";
            case "9": return "履行中";
            case "10": return "取消";
            case "11": return "起草";
            case "12": return "终止解除";
            case "13": return "作废申请中";
            case "14": return "删除";
            case "15": return "已定稿";
            case "16": return "签订中";
            default: return status;
        }
    }

    /**
     * 合同性质 → 中文（老系统 ConOpenContractOne 的映射逻辑）
     * 00→新签合同, 01→续签合同, 其他→补充协议
     */
    private String getNatureName(String property) {
        if (property == null) return null;
        switch (property) {
            case "00": return "新签合同";
            case "01": return "续签合同";
            default: return "补充协议";
        }
    }

    private String formatDate(java.util.Date date) {
        if (date == null) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        } catch (Exception e) {
            return null;
        }
    }
}
