package com.crm.system.service.statistics.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.dto.SignStatisticsReqDTO;
import com.crm.common.core.domain.dto.SignStatisticsRespDTO;
import com.crm.common.core.domain.dto.SignStatisticsUserRespDTO;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.helper.LoginHelper;
import com.crm.system.mapper.SysDeptMapper;
import com.crm.system.mapper.topp.ToppBidNewMapper;
import com.crm.system.service.statistics.StatisticsSignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class StatisticsSignServiceImpl implements StatisticsSignService {

    @Resource
    private ToppBidNewMapper topBidNewMapper;

    @Resource
    private SysDeptMapper sysDeptMapper;

    /**
     * 设置查询时间范围
     *
     * @param reqDTO 查询条件
     */
    private void setDateRange(SignStatisticsReqDTO reqDTO) {
        if (ObjectUtil.equals(reqDTO.getKbsj(), "dy")) {
            LocalDate currentDate = LocalDate.now();
            LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
            LocalDate lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
            reqDTO.setStartTime(DateUtil.format(DateUtil.beginOfMonth(DateUtil.parse(firstDayOfMonth.toString())), "yyyy-MM-dd"));
            reqDTO.setEndTime(DateUtil.format(DateUtil.endOfMonth(DateUtil.parse(lastDayOfMonth.toString())), "yyyy-MM-dd"));
        }
        if (ObjectUtil.equals(reqDTO.getKbsj(), "jn")) {
            int currentYear = LocalDate.now().getYear();
            LocalDate yearFirstDay = LocalDate.of(currentYear, 1, 1);
            LocalDate yearLastDay = LocalDate.of(currentYear, 12, 31);
            reqDTO.setStartTime(DateUtil.format(DateUtil.beginOfYear(DateUtil.parse(yearFirstDay.toString())), "yyyy-MM-dd"));
            reqDTO.setEndTime(DateUtil.format(DateUtil.endOfYear(DateUtil.parse(yearLastDay.toString())), "yyyy-MM-dd"));
        }
        if (ObjectUtil.equals(reqDTO.getKbsj(), "qn")) {
            int currentYear = LocalDate.now().getYear();
            int lastYear = currentYear - 1;
            LocalDate lastYearFirstDay = LocalDate.of(lastYear, 1, 1);
            LocalDate lastYearLastDay = LocalDate.of(lastYear, 12, 31);
            reqDTO.setStartTime(DateUtil.format(DateUtil.beginOfYear(DateUtil.parse(lastYearFirstDay.toString())), "yyyy-MM-dd"));
            reqDTO.setEndTime(DateUtil.format(DateUtil.endOfYear(DateUtil.parse(lastYearLastDay.toString())), "yyyy-MM-dd"));
        }
        if (ObjectUtil.isNotEmpty(reqDTO.getDateRange())) {
            reqDTO.setStartTime(reqDTO.getDateRange()[0]);
            reqDTO.setEndTime(reqDTO.getDateRange()[1]);
        }
    }

    /**
     * 设置查询部门
     *
     * @param reqDTO 查询条件
     */
    private void setDeptCondition(SignStatisticsReqDTO reqDTO) {
        if (ObjectUtil.isEmpty(reqDTO.getDeptId())) {
            LoginUser loginUser = LoginHelper.getLoginUser();
            SysDept loginDept = sysDeptMapper.selectById(loginUser.getDeptId());
            reqDTO.setDeptId(loginDept.getDeptId().toString());
        }
    }

    /**
     * 处理统计数据计算占比
     *
     * @param respDTO 统计数据
     */
    private void calculateProportion(SignStatisticsRespDTO respDTO) {
        if (ObjectUtil.isNotEmpty(respDTO.getCountTb()) && ObjectUtil.isNotEmpty(respDTO.getCountZb())) {
            respDTO.setZbl(String.valueOf(Double.parseDouble(respDTO.getCountTb()) / Double.parseDouble(respDTO.getCountZb()) * 100));
        }
    }

    @Override
    public TableDataInfo<SignStatisticsRespDTO> selectPageList(SignStatisticsReqDTO reqDTO, PageQuery pageQuery) {
        setDateRange(reqDTO);
        setDeptCondition(reqDTO);

        List<SignStatisticsRespDTO> list = new ArrayList<>();
        List<SysDept> deptList = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, reqDTO.getDeptId()));

        // 计算分页起始位置和结束位置
        int total = deptList.size();
        int startIndex = (pageQuery.getPageNum() - 1) * pageQuery.getPageSize();
        int endIndex = Math.min(startIndex + pageQuery.getPageSize(), total);

        // 分页处理
        List<SysDept> pagedDeptList = deptList.subList(startIndex, endIndex);

        for (SysDept dept : pagedDeptList) {
            reqDTO.setTempDeptId(dept.getDeptId());
            SignStatisticsRespDTO respDTO = topBidNewMapper.selectSignStatisticsRespDTO(reqDTO);
            respDTO = ObjectUtil.isEmpty(respDTO) ? new SignStatisticsRespDTO() : respDTO;
            calculateProportion(respDTO);
            respDTO.setDeptId(dept.getDeptId().toString());
            respDTO.setDeptName(dept.getDeptName());
            list.add(respDTO);
        }

        TableDataInfo<SignStatisticsRespDTO> tableDataInfo = TableDataInfo.build(list);
        tableDataInfo.setTotal(total);
        return tableDataInfo;
    }

    @Override
    public Boolean isLastOrg(String deptId) {
        List<SysDept> list = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, deptId));
        return ObjectUtil.isEmpty(list);
    }

    @Override
    public TableDataInfo<SignStatisticsUserRespDTO> selectUserPageList(SignStatisticsReqDTO reqDTO, PageQuery pageQuery) {
        setDateRange(reqDTO);
        setDeptCondition(reqDTO);

        List<SignStatisticsUserRespDTO> list = topBidNewMapper.selectSignStatisticsUserRespDTO(reqDTO);
        // 创建分页对象
        Page<SignStatisticsUserRespDTO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        // 获取总记录数
        int total = list.size();
        // 计算分页起始位置和结束位置
        int startIndex = (pageQuery.getPageNum() - 1) * pageQuery.getPageSize();
        int endIndex = Math.min(startIndex + pageQuery.getPageSize(), total);
        // 分页处理
        List<SignStatisticsUserRespDTO> pagedList = new ArrayList<>();
        if (startIndex < total) {
            pagedList = list.subList(startIndex, endIndex);
        }
        // 设置分页数据
        page.setRecords(pagedList);
        page.setTotal(total);
        return TableDataInfo.build(page);
    }

    @Override
    public List<SignStatisticsRespDTO> selectOrgList(SignStatisticsReqDTO reqDTO) {
        setDateRange(reqDTO);
        setDeptCondition(reqDTO);

        List<SignStatisticsRespDTO> list = new ArrayList<>();
        List<SysDept> deptList = sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, reqDTO.getDeptId()));

        for (SysDept dept : deptList) {
            reqDTO.setTempDeptId(dept.getDeptId());
            SignStatisticsRespDTO respDTO = topBidNewMapper.selectSignStatisticsRespDTO(reqDTO);
            respDTO = ObjectUtil.isEmpty(respDTO) ? new SignStatisticsRespDTO() : respDTO;
            calculateProportion(respDTO);
            respDTO.setDeptId(dept.getDeptId().toString());
            respDTO.setDeptName(dept.getDeptName());
            list.add(respDTO);
        }
        return list;
    }

    @Override
    public List<SignStatisticsUserRespDTO> selectUserList(SignStatisticsReqDTO reqDTO) {
        setDateRange(reqDTO);
        setDeptCondition(reqDTO);

        return topBidNewMapper.selectSignStatisticsUserRespDTO(reqDTO);
    }


}
