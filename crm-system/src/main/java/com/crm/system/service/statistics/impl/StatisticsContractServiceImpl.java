package com.crm.system.service.statistics.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.dto.ContractStatisticsReqDTO;
import com.crm.common.core.domain.dto.ContractStatisticsRespDTO;
import com.crm.common.core.domain.dto.ContractStatisticsUserRespDTO;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.helper.LoginHelper;
import com.crm.system.mapper.SysDeptMapper;
import com.crm.system.mapper.topp.ToppBidNewMapper;
import com.crm.system.service.ISysDeptService;
import com.crm.system.service.statistics.StatisticsContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 签约统计服务实现类
 */
@Slf4j
@Service
public class StatisticsContractServiceImpl implements StatisticsContractService {


    @Resource
    private ToppBidNewMapper topBidNewMapper;

    @Resource
    private SysDeptMapper sysDeptMapper;

    @Resource
    private ISysDeptService sysDeptService;

    /**
     * 设置查询时间范围
     *
     * @param reqDTO 查询条件
     */
    private void setDateRange(ContractStatisticsReqDTO reqDTO) {
        // 当前日期相关的快捷时间范围设置
        if (ObjectUtil.equals(reqDTO.getKbsj(), "dy")) {
            // 当月
            LocalDate currentDate = LocalDate.now();
            LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
            LocalDate lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
            reqDTO.setStartTime(DateUtil.format(DateUtil.beginOfMonth(DateUtil.parse(firstDayOfMonth.toString())), "yyyy-MM-dd"));
            reqDTO.setEndTime(DateUtil.format(DateUtil.endOfMonth(DateUtil.parse(lastDayOfMonth.toString())), "yyyy-MM-dd"));
        }
        if (ObjectUtil.equals(reqDTO.getKbsj(), "jn")) {
            // 今年
            int currentYear = LocalDate.now().getYear();
            LocalDate yearFirstDay = LocalDate.of(currentYear, 1, 1);
            LocalDate yearLastDay = LocalDate.of(currentYear, 12, 31);
            reqDTO.setStartTime(DateUtil.format(DateUtil.beginOfYear(DateUtil.parse(yearFirstDay.toString())), "yyyy-MM-dd"));
            reqDTO.setEndTime(DateUtil.format(DateUtil.endOfYear(DateUtil.parse(yearLastDay.toString())), "yyyy-MM-dd"));
        }
        if (ObjectUtil.equals(reqDTO.getKbsj(), "qn")) {
            // 去年
            int currentYear = LocalDate.now().getYear();
            int lastYear = currentYear - 1;
            LocalDate lastYearFirstDay = LocalDate.of(lastYear, 1, 1);
            LocalDate lastYearLastDay = LocalDate.of(lastYear, 12, 31);
            reqDTO.setStartTime(DateUtil.format(DateUtil.beginOfYear(DateUtil.parse(lastYearFirstDay.toString())), "yyyy-MM-dd"));
            reqDTO.setEndTime(DateUtil.format(DateUtil.endOfYear(DateUtil.parse(lastYearLastDay.toString())), "yyyy-MM-dd"));
        }
        // 自定义时间范围
        if (ObjectUtil.isNotEmpty(reqDTO.getDateRange())) {
            reqDTO.setStartTime(reqDTO.getDateRange()[0]);
            reqDTO.setEndTime(reqDTO.getDateRange()[1]);
        }
    }

    /**
     * 设置查询部门条件
     *
     * @param reqDTO 查询条件
     */
    private void setDeptCondition(ContractStatisticsReqDTO reqDTO) {
        // 如果未指定部门，则默认为当前登录用户所在部门的父级部门
        if (ObjectUtil.isEmpty(reqDTO.getDeptId())) {
            LoginUser loginUser = LoginHelper.getLoginUser();
            SysDept loginDept = sysDeptMapper.selectById(loginUser.getDeptId());
            SysDept parentDept = sysDeptService.getParentDept(loginDept);
            reqDTO.setDeptId(parentDept.getDeptId().toString());
        }
    }


    /**
     * 分页查询部门签约统计列表
     *
     * @param reqDTO    查询条件
     * @param pageQuery 分页参数
     * @return 部门签约统计分页数据
     */
    @Override
    public TableDataInfo<ContractStatisticsRespDTO> selectPageList(ContractStatisticsReqDTO reqDTO, PageQuery pageQuery) {
        // 处理多个联通类型参数
        if (ObjectUtil.isNotEmpty(reqDTO.getUnicomType())) {
            String unicomTypeStr = String.join(",", reqDTO.getUnicomType());
            reqDTO.setUnicomTypeT(unicomTypeStr);
        }

        // 处理多个项目类型参数
        if (ObjectUtil.isNotEmpty(reqDTO.getProjectType())) {
            String projectTypeStr = String.join(",", reqDTO.getProjectType());
            reqDTO.setProjectTypeT(projectTypeStr);
        }

        // 处理多个主要人员参数
        if (ObjectUtil.isNotEmpty(reqDTO.getBidMainPersonnel())) {
            String mainPersonnelStr = String.join(",", reqDTO.getBidMainPersonnel());
            reqDTO.setBidMainPersonnelT(mainPersonnelStr);
        }

        // 处理多个进展状态参数
        if (ObjectUtil.isNotEmpty(reqDTO.getJzqk())) {
            String jzqkStr = String.join(",", reqDTO.getJzqk());
            reqDTO.setJzqkT(jzqkStr);
        }

        // 设置查询时间和部门条件
        setDateRange(reqDTO);
        setDeptCondition(reqDTO);

        List<ContractStatisticsRespDTO> list = new ArrayList<>();
        SysDept sysDept = sysDeptMapper.selectById(reqDTO.getDeptId());

        // 获取所有子部门
        List<SysDept> deptList = sysDeptMapper.selectList(
            new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, reqDTO.getDeptId()));
        if (!sysDept.getDeptId().equals(1L)) {
            deptList.add(sysDept);
        }
        reqDTO.setAreaCode(sysDept.getDeptId());

        // 计算分页参数
        int total = deptList.size();
        int startIndex = (pageQuery.getPageNum() - 1) * pageQuery.getPageSize();
        int endIndex = Math.min(startIndex + pageQuery.getPageSize(), total);

        // 分页处理
        List<SysDept> pagedDeptList = deptList.subList(startIndex, endIndex);

        // 遍历部门列表，获取各部门统计数据
        for (SysDept dept : pagedDeptList) {
            ContractStatisticsRespDTO respDTO = new ContractStatisticsRespDTO();

            // 判断是否为当前查询的根部门
            if (dept.getDeptId().equals(sysDept.getDeptId())) {
                respDTO = topBidNewMapper.selectContractStatisticsBj(reqDTO);
            } else {
                reqDTO.setTempDeptId(dept.getDeptId());
                respDTO = topBidNewMapper.selectContractStatisticsRespDTO(reqDTO);
            }

            respDTO = ObjectUtil.isEmpty(respDTO) ? new ContractStatisticsRespDTO() : respDTO;
            respDTO.setDeptId(dept.getDeptId().toString());
            respDTO.setDeptName(dept.getDeptName());
        }

        TableDataInfo<ContractStatisticsRespDTO> tableDataInfo = TableDataInfo.build(list);
        tableDataInfo.setTotal(total);
        return tableDataInfo;
    }


    /**
     * 分页查询用户签约统计列表
     *
     * @param reqDTO    查询条件
     * @param pageQuery 分页参数
     * @return 用户签约统计分页数据
     */
    @Override
    public TableDataInfo<ContractStatisticsUserRespDTO> selectUserPageList(ContractStatisticsReqDTO reqDTO, PageQuery pageQuery) {
        // 处理多个联通类型参数
        if (ObjectUtil.isNotEmpty(reqDTO.getUnicomType())) {
            String unicomTypeStr = String.join(",", reqDTO.getUnicomType());
            reqDTO.setUnicomTypeT(unicomTypeStr);
        }

        // 处理多个项目类型参数
        if (ObjectUtil.isNotEmpty(reqDTO.getProjectType())) {
            String projectTypeStr = String.join(",", reqDTO.getProjectType());
            reqDTO.setProjectTypeT(projectTypeStr);
        }

        // 处理多个主要人员参数
        if (ObjectUtil.isNotEmpty(reqDTO.getBidMainPersonnel())) {
            String mainPersonnelStr = String.join(",", reqDTO.getBidMainPersonnel());
            reqDTO.setBidMainPersonnelT(mainPersonnelStr);
        }

        // 处理多个进展状态参数
        if (ObjectUtil.isNotEmpty(reqDTO.getJzqk())) {
            String jzqkStr = String.join(",", reqDTO.getJzqk());
            reqDTO.setJzqkT(jzqkStr);
        }

        // 设置查询时间和部门条件
        setDateRange(reqDTO);
        setDeptCondition(reqDTO);

        // 查询所有用户统计数据
        List<ContractStatisticsUserRespDTO> list = topBidNewMapper.selectContractStatisticsUserRespDTO(reqDTO);

        // 创建分页对象并设置分页参数
        Page<ContractStatisticsUserRespDTO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        int total = list.size();
        int startIndex = (pageQuery.getPageNum() - 1) * pageQuery.getPageSize();
        int endIndex = Math.min(startIndex + pageQuery.getPageSize(), total);

        // 分页处理
        List<ContractStatisticsUserRespDTO> pagedList = new ArrayList<>();
        if (startIndex < total) {
            pagedList = list.subList(startIndex, endIndex);
        }

        // 设置分页数据
        page.setRecords(pagedList);
        page.setTotal(total);
        return TableDataInfo.build(page);
    }

    /**
     * 查询部门签约统计列表（不分页）
     *
     * @param reqDTO 查询条件
     * @return 部门签约统计列表
     */
    @Override
    public List<ContractStatisticsRespDTO> selectOrgList(ContractStatisticsReqDTO reqDTO) {
        // 设置查询时间和部门条件
        setDateRange(reqDTO);
        setDeptCondition(reqDTO);

        List<ContractStatisticsRespDTO> list = new ArrayList<>();
        SysDept sysDept = sysDeptMapper.selectById(reqDTO.getDeptId());

        // 获取所有子部门
        List<SysDept> deptList = sysDeptMapper.selectList(
            new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, reqDTO.getDeptId()));
        if (!sysDept.getDeptId().equals(1L)) {
            deptList.add(sysDept);
        }
        reqDTO.setAreaCode(sysDept.getDeptId());

        // 遍历部门列表，获取各部门统计数据
        for (SysDept dept : deptList) {
            ContractStatisticsRespDTO respDTO = new ContractStatisticsRespDTO();

            // 判断是否为当前查询的根部门
            if (dept.getDeptId().equals(sysDept.getDeptId())) {
                respDTO = topBidNewMapper.selectContractStatisticsBj(reqDTO);
            } else {
                reqDTO.setTempDeptId(dept.getDeptId());
                respDTO = topBidNewMapper.selectContractStatisticsRespDTO(reqDTO);
            }

            respDTO = ObjectUtil.isEmpty(respDTO) ? new ContractStatisticsRespDTO() : respDTO;
            respDTO.setDeptId(dept.getDeptId().toString());
            respDTO.setDeptName(dept.getDeptName());

            list.add(respDTO);
        }
        return list;
    }

    /**
     * 查询用户签约统计列表（不分页）
     *
     * @param reqDTO 查询条件
     * @return 用户签约统计列表
     */
    @Override
    public List<ContractStatisticsUserRespDTO> selectUserList(ContractStatisticsReqDTO reqDTO) {
        // 设置查询时间和部门条件
        setDateRange(reqDTO);
        setDeptCondition(reqDTO);
        return topBidNewMapper.selectContractStatisticsUserRespDTO(reqDTO);
    }
}
