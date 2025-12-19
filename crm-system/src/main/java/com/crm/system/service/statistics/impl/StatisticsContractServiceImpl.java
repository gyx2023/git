package com.crm.system.service.statistics.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.dto.*;
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
        // 设置查询时间和部门条件
        setDateRange(reqDTO);
        setDeptCondition(reqDTO);

        SysDept sysDept = sysDeptMapper.selectById(reqDTO.getDeptId());

        // 获取所有子部门
        List<SysDept> deptList = sysDeptMapper.selectList(
            new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, reqDTO.getDeptId()));
        deptList.add(sysDept);
        reqDTO.setAreaCode(sysDept.getDeptId());

        // 合计数据
        ContractStatisticsRespDTO totalDTO = new ContractStatisticsRespDTO();
        totalDTO.setDeptName("合计");
        int totalCountQy = 0;
        double totalMoneyQy = 0;
        int totalYbwQy = 0;
        int totalYbyqQy = 0;
        int totalYbwqQy = 0;
        int totalWqQy = 0;


        // 先查询出所有数据
        List<ContractStatisticsRespDTO> allList = new ArrayList<>();
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
            // 累加统计数据
            if (ObjectUtil.isNotEmpty(respDTO.getCountQy())) {
                totalCountQy += Integer.parseInt(respDTO.getCountQy());
            }
            if (ObjectUtil.isNotEmpty(respDTO.getMoneyQy())) {
                totalMoneyQy += Double.parseDouble(respDTO.getMoneyQy());
            }
            if (ObjectUtil.isNotEmpty(respDTO.getYbwQy())) {
                totalYbwQy +=  Integer.parseInt(respDTO.getYbwQy());
            }
            if (ObjectUtil.isNotEmpty(respDTO.getYbyqQy())) {
                totalYbyqQy +=  Integer.parseInt(respDTO.getYbyqQy());
            }
            if (ObjectUtil.isNotEmpty(respDTO.getYbwqQy())) {
                totalYbwqQy +=  Integer.parseInt(respDTO.getYbwqQy());
            }
            if (ObjectUtil.isNotEmpty(respDTO.getWqQy())) {
                totalWqQy +=  Integer.parseInt(respDTO.getWqQy());
            }

            allList.add(respDTO);
        }
        // 设置合计行数据
        totalDTO.setDeptId(reqDTO.getDeptId());
        totalDTO.setCountQy(String.valueOf(totalCountQy));
        totalDTO.setMoneyQy(String.valueOf(totalMoneyQy));
        totalDTO.setYbwQy(String.valueOf(totalYbwQy));
        totalDTO.setYbyqQy(String.valueOf(totalYbyqQy));
        totalDTO.setYbwqQy(String.valueOf(totalYbwqQy));
        totalDTO.setWqQy(String.valueOf(totalWqQy));
        allList.add(totalDTO);

        // 再进行分页处理
        int total = allList.size();
        int startIndex = (pageQuery.getPageNum() - 1) * pageQuery.getPageSize();
        int endIndex = Math.min(startIndex + pageQuery.getPageSize(), total);

        // 分页数据
        List<ContractStatisticsRespDTO> pagedList = new ArrayList<>();
        if (startIndex < total) {
            pagedList = allList.subList(startIndex, endIndex);
        }

        TableDataInfo<ContractStatisticsRespDTO> tableDataInfo = TableDataInfo.build(pagedList);
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
        // 设置查询时间和部门条件
        setDateRange(reqDTO);
        setDeptCondition(reqDTO);
        // 设置合计行数据

        ContractStatisticsUserRespDTO totalDTO = new ContractStatisticsUserRespDTO();
        totalDTO.setUserName("合计");
        int totalCountQy = 0;
        double totalMoneyQy = 0;
        int totalYbwQy = 0;
        int totalYbyqQy = 0;
        int totalYbwqQy = 0;
        int totalWqQy = 0;
        double totalGxedZb = 0;
        // 查询所有用户统计数据
        List<ContractStatisticsUserRespDTO> list = topBidNewMapper.selectContractStatisticsUserRespDTO(reqDTO);
        for (ContractStatisticsUserRespDTO respDTO : list) {
            // 累加统计数据
            if (ObjectUtil.isNotEmpty(respDTO.getCountQy())) {
                totalCountQy += Integer.parseInt(respDTO.getCountQy());
            }
            if (ObjectUtil.isNotEmpty(respDTO.getMoneyQy())) {
                totalMoneyQy += Double.parseDouble(respDTO.getMoneyQy());
            }
            if (ObjectUtil.isNotEmpty(respDTO.getYbwQy())) {
                totalYbwQy +=  Integer.parseInt(respDTO.getYbwQy());
            }
            if (ObjectUtil.isNotEmpty(respDTO.getYbyqQy())) {
                totalYbyqQy +=  Integer.parseInt(respDTO.getYbyqQy());
            }
            if (ObjectUtil.isNotEmpty(respDTO.getYbwqQy())) {
                totalYbwqQy +=  Integer.parseInt(respDTO.getYbwqQy());
            }
            if (ObjectUtil.isNotEmpty(respDTO.getWqQy())) {
                totalWqQy +=  Integer.parseInt(respDTO.getWqQy());
            }
            if (ObjectUtil.isNotEmpty(respDTO.getGxedMoney())) {
                totalGxedZb += Double.parseDouble(respDTO.getGxedMoney());
            }
        }
        // 设置合计行数据
        totalDTO.setCountQy(String.valueOf(totalCountQy));
        totalDTO.setMoneyQy(String.valueOf(totalMoneyQy));
        totalDTO.setYbwQy(String.valueOf(totalYbwQy));
        totalDTO.setYbyqQy(String.valueOf(totalYbyqQy));
        totalDTO.setYbwqQy(String.valueOf(totalYbwqQy));
        totalDTO.setWqQy(String.valueOf(totalWqQy));
        totalDTO.setGxedMoney(String.valueOf(totalGxedZb));
        list.add(totalDTO);

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
