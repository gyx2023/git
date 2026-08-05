package com.crm.system.service.statistics.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.bo.OpportunityStatsBo;
import com.crm.system.domain.dto.OpportunityStatsDTO;
import com.crm.system.domain.vo.OpportunityStatsVo;
import com.crm.system.domain.vo.topp.BiddingListExportVo;
import com.crm.system.mapper.statistics.OpportunityStatsMapper;
import com.crm.system.service.statistics.IOpportunityStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 商机统计Service业务层处理
 *
 * @author system
 * @date 2026-06-12
 */
@Service
@RequiredArgsConstructor
public class OpportunityStatsServiceImpl implements IOpportunityStatsService {

    private final OpportunityStatsMapper opportunityStatsMapper;

    /**
     * 查询商机统计列表
     *
     * @param bo 查询条件
     * @return 商机统计列表
     */
    @Override
    public List<OpportunityStatsDTO> queryList(OpportunityStatsBo bo) {
        // 1. 参数校验和补全
        validateAndFillQuery(bo);

        // 2. 单次查询数据库获取所有子组织数据（模拟旧系统的UNION结果）
        List<OpportunityStatsDTO> retList = opportunityStatsMapper.selectStatsByOrg(bo);

        // 3. 按照旧系统逻辑处理排序和汇总
        return processStatsResult(retList, bo.getOrgId());
    }

    /**
     * 处理统计结果（模拟旧系统逻辑）
     *
     * @param retList 原始查询结果
     * @param orgId 组织ID
     * @return 处理后的结果
     */
    private List<OpportunityStatsDTO> processStatsResult(List<OpportunityStatsDTO> retList, Long orgId) {
        if (retList == null || retList.isEmpty()) {
            return retList;
        }

        // 定义硬编码的排序顺序（与旧系统保持一致）
        LinkedHashMap<Long, String> sortByOrg = new LinkedHashMap<>();
        sortByOrg.put(93L, "浙江省联通本部");
        sortByOrg.put(89L, "舟山市联通分公司");
        sortByOrg.put(88L, "衢州市联通分公司");
        sortByOrg.put(91L, "丽水市联通分公司");
        sortByOrg.put(85L, "湖州市联通分公司");
        sortByOrg.put(86L, "绍兴市联通分公司");
        sortByOrg.put(84L, "嘉兴市联通分公司");
        sortByOrg.put(87L, "金华市联通分公司");
        sortByOrg.put(90L, "台州市联通分公司");
        sortByOrg.put(83L, "温州市联通分公司");
        sortByOrg.put(82L, "宁波市联通分公司");
        sortByOrg.put(81L, "杭州市联通分公司");

        // 按固定顺序提取数据
        LinkedHashSet<OpportunityStatsDTO> getDataBySort = new LinkedHashSet<>();

        // 累加变量
        int oppCount = 0;
        int oppCountBytime = 0;
        int signOpp = 0;
        int signOppBytime = 0;
        int supportOpp = 0;
        int supportOppBytime = 0;
        int singCon = 0;
        int singConBytime = 0;
        BigDecimal singConMoney = BigDecimal.ZERO;
        BigDecimal singConMoneyBytime = BigDecimal.ZERO;
        BigDecimal money = BigDecimal.ZERO;

        // 按硬编码顺序提取数据并累加
        for (Long key : sortByOrg.keySet()) {
            for (OpportunityStatsDTO vo : retList) {
                if (vo.getOrgId() != null && vo.getOrgId().equals(key)) {
                    getDataBySort.add(vo);
                    oppCount += vo.getOpportunityCount() == null ? 0 : vo.getOpportunityCount();
                    oppCountBytime += vo.getNewOpportunityCount() == null ? 0 : vo.getNewOpportunityCount();
                    signOpp += vo.getSignedOpportunityCount() == null ? 0 : vo.getSignedOpportunityCount();
                    signOppBytime += vo.getNewSignedOpportunityCount() == null ? 0 : vo.getNewSignedOpportunityCount();
                    supportOpp += vo.getSupportOpportunityCount() == null ? 0 : vo.getSupportOpportunityCount();
                    supportOppBytime += vo.getNewSupportOpportunityCount() == null ? 0 : vo.getNewSupportOpportunityCount();
                    singCon += vo.getContractCount() == null ? 0 : vo.getContractCount();
                    singConBytime += vo.getNewContractCount() == null ? 0 : vo.getNewContractCount();
                    singConMoney = singConMoney.add(vo.getContractAmount() == null ? BigDecimal.ZERO : vo.getContractAmount());
                    singConMoneyBytime = singConMoneyBytime.add(vo.getNewContractAmount() == null ? BigDecimal.ZERO : vo.getNewContractAmount());
                    money = money.add(vo.getExpectAmount() == null ? BigDecimal.ZERO : vo.getExpectAmount());
                }
            }
        }

        // 查找汇总行（orgId=1）并设置累加值
        for (OpportunityStatsDTO vo : retList) {
            if (vo.getOrgId() != null && vo.getOrgId().equals(1L)) {
                vo.setOpportunityCount(oppCount);
                vo.setNewOpportunityCount(oppCountBytime);
                vo.setSignedOpportunityCount(signOpp);
                vo.setNewSignedOpportunityCount(signOppBytime);
                vo.setSupportOpportunityCount(supportOpp);
                vo.setNewSupportOpportunityCount(supportOppBytime);
                vo.setContractCount(singCon);
                vo.setNewContractCount(singConBytime);
                vo.setContractAmount(singConMoney);
                vo.setNewContractAmount(singConMoneyBytime);
                vo.setExpectAmount(money);
                getDataBySort.add(vo);
                break;
            }
        }

        // 如果查询的是全省（orgId=1），返回反转后的列表
        if (orgId != null && orgId.equals(1L)) {
            // 转换为列表并反转
            List<OpportunityStatsDTO> listReversed = new ArrayList<>(getDataBySort);
            Collections.reverse(listReversed);
            return listReversed;
        }

        // 否则返回原始结果
        return retList;
    }

    /**
     * 导出商机统计报表
     *
     * @param bo 查询条件
     * @param response HTTP响应
     */
    @Override
    public void exportStats(OpportunityStatsBo bo, HttpServletResponse response) {
        List<OpportunityStatsDTO> list = queryList(bo);
        List<OpportunityStatsVo> listVo = BeanUtil.copyToList(list, OpportunityStatsVo.class);

        // 使用EasyExcel导出
        try {
            ExcelUtil.exportExcel(listVo, "商机统计", OpportunityStatsVo.class, response);
        } catch (Exception e) {
            throw new ServiceException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 参数校验和补全
     *
     * @param bo 查询条件
     */
    private void validateAndFillQuery(OpportunityStatsBo bo) {
        if (bo.getOrgId() == null) {
            bo.setOrgId(1L);
        }

        // 日期格式处理：前端传yyyy-MM-dd，转换为yyyyMMdd
        if (StrUtil.isNotBlank(bo.getLeftTimeStr())) {
            bo.setLeftTime(bo.getLeftTimeStr().replace("-", ""));
        }
        if (StrUtil.isNotBlank(bo.getRightTimeStr())) {
            bo.setRightTime(bo.getRightTimeStr().replace("-", ""));
        }
    }
}
