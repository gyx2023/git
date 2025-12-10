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
import com.crm.system.service.statistics.BiddingListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BiddingListServiceImpl implements BiddingListService {

    @Resource
    private ToppBidNewScoreMapper scoreMapper;
    @Resource
    private SysDeptMapper sysDeptMapper;


    @Override
    public TableDataInfo<BiddingListRespDTO> selectPageList(BiddingListReqDTO reqDTO, PageQuery pageQuery) {
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

    @Override
    public List<BiddingListRespDTO> getBiddingListRespDTOS(BiddingListReqDTO reqDTO) {
        if (ObjectUtil.equals(reqDTO.getKbsj(), "dy")) {
            LocalDate currentDate = LocalDate.now(); // 当前日期
            LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1); // 当月第一天
            LocalDate lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
            reqDTO.setStartTime(DateUtil.format(DateUtil.beginOfMonth(DateUtil.parse(firstDayOfMonth.toString())), "yyyy-MM-dd"));
            reqDTO.setEndTime(DateUtil.format(DateUtil.endOfMonth(DateUtil.parse(lastDayOfMonth.toString())), "yyyy-MM-dd"));
        }
        if (ObjectUtil.equals(reqDTO.getKbsj(), "jn")) {
            // 获取当前年份
            int currentYear = LocalDate.now().getYear();
            // 设置去年的第一天和最后一天
            LocalDate yearFirstDay = LocalDate.of(currentYear, 1, 1);
            LocalDate yearLastDay = LocalDate.of(currentYear, 12, 31);
            reqDTO.setStartTime(DateUtil.format(DateUtil.beginOfYear(DateUtil.parse(yearFirstDay.toString())), "yyyy-MM-dd"));
            reqDTO.setEndTime(DateUtil.format(DateUtil.endOfYear(DateUtil.parse(yearLastDay.toString())), "yyyy-MM-dd"));
        }
        if (ObjectUtil.equals(reqDTO.getKbsj(), "qn")) {
            // 获取当前年份
            int currentYear = LocalDate.now().getYear();
            // 计算去年的年份
            int lastYear = currentYear - 1;
            // 设置去年的第一天和最后一天
            LocalDate lastYearFirstDay = LocalDate.of(lastYear, 1, 1);
            LocalDate lastYearLastDay = LocalDate.of(lastYear, 12, 31);
            reqDTO.setStartTime(DateUtil.format(DateUtil.beginOfYear(DateUtil.parse(lastYearFirstDay.toString())), "yyyy-MM-dd"));
            reqDTO.setEndTime(DateUtil.format(DateUtil.endOfYear(DateUtil.parse(lastYearLastDay.toString())), "yyyy-MM-dd"));
        }
        if (ObjectUtil.isNotEmpty(reqDTO.getDateRange())) {
            reqDTO.setStartTime(reqDTO.getDateRange()[0]);
            reqDTO.setEndTime(reqDTO.getDateRange()[1]);
        }
        if (ObjectUtil.isEmpty(reqDTO.getDeptId())) {
            LoginUser loginUser = LoginHelper.getLoginUser();
            SysDept loginDept = sysDeptMapper.selectById(loginUser.getDeptId());
            reqDTO.setDeptId(loginDept.getDeptId().toString());
        }
        List<BiddingListRespDTO> list = scoreMapper.selectBidList(reqDTO);
        return list;
    }
}
