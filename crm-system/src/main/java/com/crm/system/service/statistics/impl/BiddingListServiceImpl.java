package com.crm.system.service.statistics.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.dto.BiddingListReqDTO;
import com.crm.common.core.domain.dto.BiddingListRespDTO;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.helper.LoginHelper;
import com.crm.system.mapper.SysDeptMapper;
import com.crm.system.mapper.topp.ToppBidNewScoreMapper;
import com.crm.system.service.ISysDeptService;
import com.crm.system.service.statistics.BiddingListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 投标列表服务实现类
 */
@Slf4j
@Service
public class BiddingListServiceImpl implements BiddingListService {

    @Resource
    private ToppBidNewScoreMapper scoreMapper;
    @Resource
    private SysDeptMapper sysDeptMapper;
    @Resource
    private ISysDeptService sysDeptService;

    /**
     * 分页查询投标列表数据
     *
     * @param reqDTO    查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @Override
    public TableDataInfo<BiddingListRespDTO> selectPageList(BiddingListReqDTO reqDTO, PageQuery pageQuery) {
        // 获取完整数据列表
        List<BiddingListRespDTO> list = getBiddingListRespDTOS(reqDTO);

        // 创建分页对象
        Page<BiddingListRespDTO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        // 获取总记录数
        int total = list.size();

        // 计算分页起始位置和结束位置
        int startIndex = (pageQuery.getPageNum() - 1) * pageQuery.getPageSize();
        int endIndex = Math.min(startIndex + pageQuery.getPageSize(), total);

        // 分页处理
        List<BiddingListRespDTO> pagedList = new ArrayList<>();
        if (startIndex < total) {
            pagedList = list.subList(startIndex, endIndex);
        }

        // 设置分页数据
        page.setRecords(pagedList);
        page.setTotal(total);
        return TableDataInfo.build(page);
    }

    /**
     * 获取投标列表数据
     *
     * @param reqDTO 查询条件
     * @return 投标列表数据
     */
    @Override
    public List<BiddingListRespDTO> getBiddingListRespDTOS(BiddingListReqDTO reqDTO) {
        // 处理联通类型参数，将其转换为逗号分隔的字符串
        if (ObjectUtil.isNotEmpty(reqDTO.getUnicomType())) {
            String unicomTypeStr = String.join(",", reqDTO.getUnicomType());
            reqDTO.setUnicomTypeT(unicomTypeStr);
        }

        // 处理项目类型参数，将其转换为逗号分隔的字符串
        if (ObjectUtil.isNotEmpty(reqDTO.getProjectType())) {
            String projectTypeStr = String.join(",", reqDTO.getProjectType());
            reqDTO.setProjectTypeT(projectTypeStr);
        }

        // 处理投标主要人员参数，将其转换为逗号分隔的字符串
        if (ObjectUtil.isNotEmpty(reqDTO.getBidMainPersonnel())) {
            String bidMainPersonnelStr = String.join(",", reqDTO.getBidMainPersonnel());
            reqDTO.setBidMainPersonnelT(bidMainPersonnelStr);
        }

        // 处理进展情况下拉框参数，将其转换为逗号分隔的字符串
        if (ObjectUtil.isNotEmpty(reqDTO.getJzqk())) {
            String jzqkStr = String.join(",", reqDTO.getJzqk());
            reqDTO.setJzqkT(jzqkStr);
        }

        // 根据开标时间选项设置时间范围
        setTimeRangeByKbsjOption(reqDTO);

        // 处理自定义时间范围
        if (ObjectUtil.isNotEmpty(reqDTO.getDateRange())) {
            reqDTO.setStartTime(reqDTO.getDateRange()[0]);
            reqDTO.setEndTime(reqDTO.getDateRange()[1]);
        }

        // 如果未指定部门ID，则使用登录用户所属部门的父级部门ID
        if (ObjectUtil.isEmpty(reqDTO.getDeptId())) {
            LoginUser loginUser = LoginHelper.getLoginUser();
            SysDept loginDept = sysDeptMapper.selectById(loginUser.getDeptId());
            SysDept parentDept = sysDeptService.getParentDept(loginDept);
            reqDTO.setDeptId(parentDept.getDeptId().toString());
        }

        // 查询投标列表数据
        List<BiddingListRespDTO> list = scoreMapper.selectBidList(reqDTO);

        // 处理相同oppCode的数据，合并贡献情况信息
        processContributionSituation(list);

        return list;
    }

    /**
     * 根据开标时间选项设置时间范围
     *
     * @param reqDTO 请求参数
     */
    private void setTimeRangeByKbsjOption(BiddingListReqDTO reqDTO) {
        if (ObjectUtil.equals(reqDTO.getKbsj(), "dy")) {
            // 当月
            LocalDate currentDate = LocalDate.now();
            LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
            LocalDate lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
            reqDTO.setStartTime(DateUtil.format(DateUtil.beginOfMonth(DateUtil.parse(firstDayOfMonth.toString())), "yyyy-MM-dd"));
            reqDTO.setEndTime(DateUtil.format(DateUtil.endOfMonth(DateUtil.parse(lastDayOfMonth.toString())), "yyyy-MM-dd"));
        } else if (ObjectUtil.equals(reqDTO.getKbsj(), "jn")) {
            // 今年
            int currentYear = LocalDate.now().getYear();
            LocalDate yearFirstDay = LocalDate.of(currentYear, 1, 1);
            LocalDate yearLastDay = LocalDate.of(currentYear, 12, 31);
            reqDTO.setStartTime(DateUtil.format(DateUtil.beginOfYear(DateUtil.parse(yearFirstDay.toString())), "yyyy-MM-dd"));
            reqDTO.setEndTime(DateUtil.format(DateUtil.endOfYear(DateUtil.parse(yearLastDay.toString())), "yyyy-MM-dd"));
        } else if (ObjectUtil.equals(reqDTO.getKbsj(), "qn")) {
            // 去年
            int lastYear = LocalDate.now().getYear() - 1;
            LocalDate lastYearFirstDay = LocalDate.of(lastYear, 1, 1);
            LocalDate lastYearLastDay = LocalDate.of(lastYear, 12, 31);
            reqDTO.setStartTime(DateUtil.format(DateUtil.beginOfYear(DateUtil.parse(lastYearFirstDay.toString())), "yyyy-MM-dd"));
            reqDTO.setEndTime(DateUtil.format(DateUtil.endOfYear(DateUtil.parse(lastYearLastDay.toString())), "yyyy-MM-dd"));
        }
    }

    /**
     * 处理相同oppCode的数据，合并贡献情况信息
     *
     * @param list 投标列表数据
     */
    private void processContributionSituation(List<BiddingListRespDTO> list) {
        if (ObjectUtil.isEmpty(list)) {
            return;
        }

        // 按照oppCode分组
        Map<String, List<BiddingListRespDTO>> oppCodeMap = list.stream()
            .filter(item -> item.getOppCode() != null)
            .collect(Collectors.groupingBy(BiddingListRespDTO::getOppCode));

        // 处理每个分组
        for (List<BiddingListRespDTO> sameOppCodeList : oppCodeMap.values()) {
            // 只处理有多个相同oppCode的情况
            if (sameOppCodeList.size() > 1) {
                // 构建统一的贡献情况字符串
                String unifiedContribution = sameOppCodeList.stream()
                    .filter(dto -> dto.getSupportManager() != null && dto.getContribution() != null)
                    .map(dto -> dto.getSupportManager() + dto.getContribution())
                    .collect(Collectors.joining(","));

                // 为所有相同oppCode的项设置相同的贡献情况
                for (BiddingListRespDTO dto : sameOppCodeList) {
                    dto.setContributionSituation(unifiedContribution);
                }
            }
        }
    }
}
