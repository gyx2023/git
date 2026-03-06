package com.crm.system.listener.client;

import com.crm.system.domain.bo.ZqztGrpContractOtherInfoBo;
import com.crm.system.domain.vo.ContractUserTemplateResultVo;
import com.crm.system.domain.vo.cust.ZqztCustMaintInfoVo;
import com.crm.system.service.IZqztGrpContractOtherInfoService;
import com.crm.system.service.cust.IZqztCustMaintInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.crm.common.enums.InfoTagEnum;

/**
 * 合同关联用户导入监听器
 * 处理合同关联用户数据导入的具体业务逻辑
 */
@Component
@Slf4j
public class ContractUserImportListener {

    @Autowired
    private IZqztGrpContractOtherInfoService contractOtherInfoService;

    @Autowired
    private IZqztCustMaintInfoService custMaintInfoService;

    /**
     * 处理导入数据列表
     *
     * @param dataList     导入的服务号码列表
     * @param contractId   合同ID
     * @param hqGrpCode    总部集团编码
     * @return 导入结果列表
     */
    public List<ContractUserTemplateResultVo> processImportData(List<String> dataList, String contractId, String hqGrpCode) {
        List<ContractUserTemplateResultVo> resultList = new ArrayList<>();

        // 获取未绑定合同的用户列表
        List<ZqztCustMaintInfoVo> uncontractedCustomers = custMaintInfoService.selectUncontractedUsersNoPage(hqGrpCode, contractId, dataList);

        // 提取未绑定合同的服务号码列表
        List<String> validDecryptedSvcNums = uncontractedCustomers.stream().map(ZqztCustMaintInfoVo::getSvcNum).collect(Collectors.toList());
        Map<String, ZqztCustMaintInfoVo> mainInfoMap = uncontractedCustomers.stream().collect(Collectors.toMap(ZqztCustMaintInfoVo::getSvcNum, v -> v));


        // 逐条处理导入的服务号码
        for (String svcNum : dataList) {
            ContractUserTemplateResultVo resultVo = new ContractUserTemplateResultVo();
            resultVo.setSvcNum(svcNum);

            try {
                // 验证服务号码是否为空
                if (!StringUtils.hasText(svcNum)) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("服务号码不能为空");
                    resultList.add(resultVo);
                    continue;
                }

                // 验证服务号码是否在未绑定合同的用户列表中
                if (!validDecryptedSvcNums.contains(svcNum)) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("服务号码不在未绑定合同的用户列表中");
                    resultList.add(resultVo);
                    continue;
                }

                // 构造合同关联用户信息并调用添加接口
                ZqztGrpContractOtherInfoBo bo = new ZqztGrpContractOtherInfoBo();
                bo.setContractId(contractId);
                bo.setInfoTag(InfoTagEnum.GRP_CONTRACT_USER_LIST.getValue());
                bo.setInfoVal(mainInfoMap.get(svcNum).getUserId());
                bo.setInfoVal2(svcNum);
                bo.setIsValid("1");

                boolean success = contractOtherInfoService.insertByBo(bo);

                if (success) {
                    resultVo.setProcessResult("成功");
                } else {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("添加合同关联用户失败");
                }
            } catch (Exception e) {
                resultVo.setProcessResult("失败");
                resultVo.setErrorMessage("处理记录时发生异常: " + e.getMessage());
                log.error("处理记录失败，服务号码: {}", svcNum, e);
            }
            resultList.add(resultVo);
        }

        return resultList;
    }
}
