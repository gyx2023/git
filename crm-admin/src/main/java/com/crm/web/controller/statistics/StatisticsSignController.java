package com.crm.web.controller.statistics;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.Log;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.domain.dto.SignStatisticsReqDTO;
import com.crm.common.core.domain.dto.SignStatisticsRespDTO;
import com.crm.common.core.domain.dto.SignStatisticsUserRespDTO;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.topp.StatisticsOrgExportVo;
import com.crm.system.domain.vo.topp.StatisticsUserExportVo;
import com.crm.system.service.statistics.StatisticsSignService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * @author pengjun
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/statistics/sign")
public class StatisticsSignController extends BaseController {

    @Resource
    private StatisticsSignService statisticsSignService;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public TableDataInfo<SignStatisticsRespDTO> list(SignStatisticsReqDTO reqDTO, PageQuery pageQuery) {
        return statisticsSignService.selectPageList(reqDTO, pageQuery);
    }

    /**
     * 获取用户列表
     */
    @PostMapping("/isLastOrg")
    public R<Boolean> isLastOrg(@RequestParam("deptId") String deptId) {
        Boolean result = statisticsSignService.isLastOrg(deptId);
        return R.ok(result);
    }


    /**
     * 获取用户列表
     */
    @GetMapping("/userList")
    public TableDataInfo<SignStatisticsUserRespDTO> userList(SignStatisticsReqDTO reqDTO, PageQuery pageQuery) {
        return statisticsSignService.selectUserPageList(reqDTO, pageQuery);
    }


    /**
     * 导出用户列表
     */
    @Log(title = "投标统计按组织导出", businessType = BusinessType.EXPORT)
    @SaCheckPermission("statistics:sign:orgExport")
    @PostMapping("/orgExport")
    public void orgExport(SignStatisticsReqDTO reqDTO, HttpServletResponse response) {
        List<SignStatisticsRespDTO> list = statisticsSignService.selectOrgList(reqDTO);
        List<StatisticsOrgExportVo> listVo = BeanUtil.copyToList(list, StatisticsOrgExportVo.class);
        if (ObjectUtil.isNotEmpty( listVo)) {
            for (StatisticsOrgExportVo vo : listVo) {
                vo.setCountTb(ObjectUtil.isEmpty(vo.getCountTb()) ? 0: vo.getCountTb());
                vo.setCountZb(ObjectUtil.isEmpty(vo.getCountZb()) ? 0: vo.getCountZb());
                vo.setYbwTb(ObjectUtil.isEmpty(vo.getYbwTb()) ? 0 : vo.getYbwTb());
                vo.setYbyqTb(ObjectUtil.isEmpty(vo.getYbyqTb()) ? 0 : vo.getYbyqTb());
                vo.setYbwqTb(ObjectUtil.isEmpty(vo.getYbwqTb()) ? 0 : vo.getYbwqTb());
                vo.setWqTb(ObjectUtil.isEmpty(vo.getWqTb()) ? 0 : vo.getWqTb());
                vo.setMoneyTb(ObjectUtil.isEmpty(vo.getMoneyTb()) ? 0 : vo.getMoneyTb());
                vo.setYbwZb(ObjectUtil.isEmpty(vo.getYbwZb()) ? 0 : vo.getYbwZb());
                vo.setYbyqZb(ObjectUtil.isEmpty(vo.getYbyqZb()) ? 0 : vo.getYbyqZb());
                vo.setYbwqZb(ObjectUtil.isEmpty(vo.getYbwqZb()) ? 0 : vo.getYbwqZb());
                vo.setWqZb(ObjectUtil.isEmpty(vo.getWqZb()) ? 0 : vo.getWqZb());
                vo.setMoneyZb(ObjectUtil.isEmpty(vo.getMoneyZb()) ? 0 : vo.getMoneyZb());
                vo.setZbl(ObjectUtil.isEmpty(vo.getZbl()) ? "0" : vo.getZbl());
            }
        }

        ExcelUtil.exportExcel(listVo, "投标统计按组织", StatisticsOrgExportVo.class, response);
    }

    /**
     * 导出用户列表
     */
    @Log(title = "投标统计按人员导出", businessType = BusinessType.EXPORT)
    @SaCheckPermission("statistics:sign:userExport")
    @PostMapping("/userExport")
    public void userExport(SignStatisticsReqDTO reqDTO, HttpServletResponse response) {
        List<SignStatisticsUserRespDTO> list = statisticsSignService.selectUserList(reqDTO);
        List<StatisticsUserExportVo> listVo = BeanUtil.copyToList(list, StatisticsUserExportVo.class);
        if (ObjectUtil.isNotEmpty( listVo)) {
            for (StatisticsUserExportVo vo : listVo) {
                vo.setCountTb(ObjectUtil.isEmpty(vo.getCountTb()) ? 0: vo.getCountTb());
                vo.setCountZb(ObjectUtil.isEmpty(vo.getCountZb()) ? 0: vo.getCountZb());
                vo.setYbwTb(ObjectUtil.isEmpty(vo.getYbwTb()) ? 0 : vo.getYbwTb());
                vo.setYbyqTb(ObjectUtil.isEmpty(vo.getYbyqTb()) ? 0 : vo.getYbyqTb());
                vo.setYbwqTb(ObjectUtil.isEmpty(vo.getYbwqTb()) ? 0 : vo.getYbwqTb());
                vo.setWqTb(ObjectUtil.isEmpty(vo.getWqTb()) ? 0 : vo.getWqTb());
                vo.setMoneyTb(ObjectUtil.isEmpty(vo.getMoneyTb()) ? 0 : vo.getMoneyTb());
                vo.setYbwZb(ObjectUtil.isEmpty(vo.getYbwZb()) ? 0 : vo.getYbwZb());
                vo.setYbyqZb(ObjectUtil.isEmpty(vo.getYbyqZb()) ? 0 : vo.getYbyqZb());
                vo.setYbwqZb(ObjectUtil.isEmpty(vo.getYbwqZb()) ? 0 : vo.getYbwqZb());
                vo.setWqZb(ObjectUtil.isEmpty(vo.getWqZb()) ? 0 : vo.getWqZb());
                vo.setMoneyZb(ObjectUtil.isEmpty(vo.getMoneyZb()) ? 0 : vo.getMoneyZb());
                vo.setGxedZb(ObjectUtil.isEmpty(vo.getGxedZb()) ? 0 : vo.getGxedZb());
            }
        }
        ExcelUtil.exportExcel(listVo, "投标统计按支撑人员", StatisticsUserExportVo.class, response);
    }


}
