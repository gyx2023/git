package com.crm.job.service.oppo;

import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppContractBo;
import com.crm.system.domain.bo.topp.ToppContractEformBo;
import com.crm.system.domain.bo.topp.ToppOpportunityBo;
import com.crm.system.domain.bo.zbzq.SalesupCollOppoPoolsBo;
import com.crm.system.domain.vo.topp.ToppContractEformVo;
import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.domain.vo.zbzq.SalesupCollOppoPoolsVo;
import com.crm.system.domain.vo.zbzq.SalesupCollOppoSignVo;
import com.crm.system.service.topp.IToppContractEformService;
import com.crm.system.service.topp.IToppContractService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.zbzq.ISalesupCollOppoPoolsService;
import com.crm.system.service.zbzq.ISalesupCollOppoSignService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * 同步总部政企中台合同数据定时任务
 * 查询总部政企中台合同商机绑定表，同步省份表数据
 *
 * @author fpp
 */
@Slf4j
@Component
public class OppoContractSyncJob {

    @Autowired
    private ISalesupCollOppoSignService oppoSignService;

    @Autowired
    private ISalesupCollOppoPoolsService oppoPoolsService;

    @Autowired
    private IToppOpportunityService toppOpportunityService;

    @Autowired
    private IToppContractService toppContractService;

    @Autowired
    private IToppContractEformService toppContractEformService;

    @XxlJob("oppoContractSyncJob")
    public void sync() {
        log.info("开始同步合同数据");
        String param = XxlJobHelper.getJobParam();
        if(StringUtils.isBlank(param)){
            //默认取7天内数据
            param = "7";
        }
        List<SalesupCollOppoSignVo> list = oppoSignService.queryContractSignData(Integer.parseInt(param));
        log.info("总部政企中台合同绑定数据量：" + list.size());
        for(SalesupCollOppoSignVo signVo : list) {
            ToppOpportunityVo toppOpportunity = new ToppOpportunityVo();
            ToppOpportunityBo toppOpportunityBo = new ToppOpportunityBo();
            toppOpportunityBo.setOppCode(signVo.getOppoNumber().substring(3));
            List<ToppOpportunityVo> toppOpportunityVos = toppOpportunityService.queryList(toppOpportunityBo);
            if (toppOpportunityVos != null && !toppOpportunityVos.isEmpty()) {
                toppOpportunity = toppOpportunityVos.get(0);
            }else{
                log.error("oppo_number为：" + signVo.getOppoNumber() + "的商机表不存在");
            }
            if(toppOpportunity.getId() != null){
                //判断if_sign类型，为合同还是电子表单
                try {
                    if (signVo.getIfSign().equals("合同")) {
                        syncContractData(signVo, toppOpportunity);
                    } else if (signVo.getIfSign().equals("电子表单")) {
                        syncContractEformData(signVo, toppOpportunity);
                    }
                }catch (Exception e){
                    log.error("ContractId为：" + signVo.getContractId() + "的合同或电子表单保存失败");
                }
            }
        }
    }
    private void syncContractData(SalesupCollOppoSignVo signVo, ToppOpportunityVo toppOpportunity) throws ParseException {
        ToppContractBo toppContract = new ToppContractBo();
        toppContract.setProjectId(toppOpportunity.getId());
        toppContract.setContractNo(signVo.getContractId());
        List<ToppContractVo> toppContractVos = toppContractService.queryList(toppContract);
        if(toppContractVos == null || toppContractVos.isEmpty()){
            //新增toppContract表
            ToppContractBo toppContractBo = new ToppContractBo();
            toppContractBo.setProjectId(toppOpportunity.getId());
            toppContractBo.setContractNo(signVo.getContractId());
            toppContractBo.setContractName(signVo.getSignName());
            // 设置我方签约主体
            toppContractBo.setOurCompany(signVo.getSignSub());
            // 设置对方签约主体和最终客户名称
            toppContractBo.setOtherCompany(signVo.getClientoName());
            toppContractBo.setFinalCustName(signVo.getClientoName());
            // 设置合同类型
            if (signVo.getContractType() != null && signVo.getContractType().contains("ICT")) {
                toppContractBo.setContractType("contract_type_ICT");
            } else {
                toppContractBo.setContractType("contract_type_tf");
            }
            // 设置合同币种
            toppContractBo.setContractCurrency("currency_rmb");
            // 设置合同总金额
            toppContractBo.setTotalMoney(signVo.getContractIncome());
            // 设置合同签订时间
            if (StringUtils.isNotBlank(signVo.getSignDate())) {
                toppContractBo.setSignDate(DateUtils.parseDate(signVo.getSignDate(),"yyyy-MM-dd HH:mm:ss"));
            }
            // 设置合同履行开始日期和结束日期
            if (StringUtils.isNotBlank(signVo.getEffectDate())) {
                toppContractBo.setStartDate(DateUtils.parseDate(signVo.getEffectDate(),"yyyy-MM-dd HH:mm:ss"));
            }
            if (StringUtils.isNotBlank(signVo.getEndDate())) {
                toppContractBo.setEndDate(DateUtils.parseDate(signVo.getEndDate(),"yyyy-MM-dd HH:mm:ss"));
            }
            // 计算合同期限（月数）
            if (StringUtils.isNotBlank(signVo.getEffectDate()) && StringUtils.isNotBlank(signVo.getEndDate())) {
                Date startDate = DateUtils.parseDate(signVo.getEffectDate(),"yyyy-MM-dd HH:mm:ss");
                Date endDate = DateUtils.parseDate(signVo.getEndDate(),"yyyy-MM-dd HH:mm:ss");
                if (startDate != null && endDate != null) {
                    long months = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24 * 30L);
                    toppContractBo.setValidityTime(String.valueOf(months));
                }
            }
            // 设置操作类型为新增
            toppContractBo.setOperationType("1");
            // 设置合同类别 2合同
            toppContractBo.setContractAddType("2");
            toppContractBo.setCreateTime(new Date());
            toppContractService.insertByBo(toppContractBo);
            log.info("新增合同数据成功");
            if(toppOpportunity.getContractId() == null){
                ToppOpportunityBo toppOpportunityBo = new ToppOpportunityBo();
                toppOpportunityBo.setId(toppOpportunity.getId());
                toppOpportunityBo.setContractId(toppContractBo.getId());
                toppOpportunityService.updateByBo(toppOpportunityBo);
            }
        }
    }
    private void syncContractEformData(SalesupCollOppoSignVo signVo, ToppOpportunityVo toppOpportunity) throws ParseException {
        ToppContractEformBo toppContract = new ToppContractEformBo();
        toppContract.setProjectId(toppOpportunity.getId());
        toppContract.setEformNo(signVo.getContractId());
        List<ToppContractEformVo> toppContractVos = toppContractEformService.queryList(toppContract);
        if(toppContractVos == null || toppContractVos.isEmpty()){
            ToppContractEformBo eformBo = new ToppContractEformBo();
            eformBo.setProjectId(toppOpportunity.getId());
            eformBo.setEformNo(signVo.getContractId());
            eformBo.setEformName(signVo.getSignName());
            eformBo.setCustName(signVo.getClientoName());
            eformBo.setOurCompany(signVo.getSignSub());
            eformBo.setAgentPersonOa(signVo.getExecutorContractLogin());
            eformBo.setAgentPersonName(signVo.getExecutorContractName());
            //signVo表单位为万元，ToppContractEformBo为元
            eformBo.setTotalMoney(signVo.getContractIncome() != null ? signVo.getContractIncome().multiply(new java.math.BigDecimal(10000)) : null);

            if (StringUtils.isNotBlank(signVo.getSignDate())) {
                eformBo.setSignTime(DateUtils.parseDate(signVo.getSignDate(),"yyyy-MM-dd HH:mm:ss"));
            }

            if (StringUtils.isNotBlank(signVo.getEffectDate())) {
                eformBo.setStartDate(DateUtils.parseDate(signVo.getEffectDate(),"yyyy-MM-dd HH:mm:ss"));
            }

            if (StringUtils.isNotBlank(signVo.getEndDate())) {
                eformBo.setEndDate(DateUtils.parseDate(signVo.getEndDate(),"yyyy-MM-dd HH:mm:ss"));
            }
            eformBo.setBusiType("订购");
            eformBo.setCreateTime(new Date());

            toppContractEformService.insertByBo(eformBo);
            log.info("新增电子表单数据成功");
        }
    }

}
