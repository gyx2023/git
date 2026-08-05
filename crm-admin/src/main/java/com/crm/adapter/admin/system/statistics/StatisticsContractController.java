package com.crm.adapter.admin.system.statistics;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.crm.common.annotation.Log;
import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.dto.ContractStatisticsReqDTO;
import com.crm.system.domain.dto.ContractStatisticsRespDTO;
import com.crm.system.domain.dto.ContractStatisticsUserRespDTO;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.common.annotation.InterfaceMonitor;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.topp.SignContractOrgExportVo;
import com.crm.system.domain.vo.topp.SignContractUserExportVo;
import com.crm.system.service.statistics.StatisticsContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/statistics/contract")
public class StatisticsContractController {

    @Resource
    private StatisticsContractService statisticsContractService;

    /**
     * 获取用户列表
     */
    @InterfaceMonitor
    @GetMapping("/list")
    @Log(title = "签约统计", businessType = BusinessType.QUERY)
    public TableDataInfo<ContractStatisticsRespDTO> list(ContractStatisticsReqDTO reqDTO, PageQuery pageQuery) {
        return statisticsContractService.selectPageList(reqDTO, pageQuery);
    }


    /**
     * 获取用户列表
     */
    @GetMapping("/userList")
    public TableDataInfo<ContractStatisticsUserRespDTO> userList(ContractStatisticsReqDTO reqDTO, PageQuery pageQuery) {
        return statisticsContractService.selectUserPageList(reqDTO, pageQuery);
    }


    /**
     * 导出用户列表
     */
    @Log(title = "签约统计按组织导出", businessType = BusinessType.EXPORT)
    @SaCheckPermission("statistics:contract:orgExport")
    @PostMapping("/orgExport")
    public void orgExport(ContractStatisticsReqDTO reqDTO, HttpServletResponse response) {
        List<ContractStatisticsRespDTO> list = statisticsContractService.selectOrgList(reqDTO);
        List<SignContractOrgExportVo> listVo = BeanUtil.copyToList(list, SignContractOrgExportVo.class);
        if (ObjectUtil.isNotEmpty(listVo)) {
            for (SignContractOrgExportVo vo : listVo) {
                vo.setCountQy(ObjectUtil.isEmpty(vo.getCountQy()) ? 0 : vo.getCountQy());
                vo.setYbwQy(ObjectUtil.isEmpty(vo.getYbwQy()) ? 0 : vo.getYbwQy());
                vo.setYbyqQy(ObjectUtil.isEmpty(vo.getYbyqQy()) ? 0 : vo.getYbyqQy());
                vo.setYbwqQy(ObjectUtil.isEmpty(vo.getYbwqQy()) ? 0 : vo.getYbwqQy());
                vo.setWqQy(ObjectUtil.isEmpty(vo.getWqQy()) ? 0 : vo.getWqQy());
                vo.setMoneyQy(ObjectUtil.isEmpty(vo.getMoneyQy()) ? 0 : vo.getMoneyQy());
            }
        }

        ExcelUtil.exportExcel(listVo, "签约统计按组织", SignContractOrgExportVo.class, response);
    }

    /**
     * 导出用户列表
     */
    @Log(title = "签约统计按人员导出", businessType = BusinessType.EXPORT)
    @SaCheckPermission("statistics:contract:userExport")
    @PostMapping("/userExport")
    public void userExport(ContractStatisticsReqDTO reqDTO, HttpServletResponse response) {
        List<ContractStatisticsUserRespDTO> list = statisticsContractService.selectUserList(reqDTO);
        List<SignContractUserExportVo> listVo = BeanUtil.copyToList(list, SignContractUserExportVo.class);
        if (ObjectUtil.isNotEmpty(listVo)) {
            for (SignContractUserExportVo vo : listVo) {
                vo.setCountQy(ObjectUtil.isEmpty(vo.getCountQy()) ? 0 : vo.getCountQy());
                vo.setYbwQy(ObjectUtil.isEmpty(vo.getYbwQy()) ? 0 : vo.getYbwQy());
                vo.setYbyqQy(ObjectUtil.isEmpty(vo.getYbyqQy()) ? 0 : vo.getYbyqQy());
                vo.setYbwqQy(ObjectUtil.isEmpty(vo.getYbwqQy()) ? 0 : vo.getYbwqQy());
                vo.setWqQy(ObjectUtil.isEmpty(vo.getWqQy()) ? 0 : vo.getWqQy());
                vo.setMoneyQy(ObjectUtil.isEmpty(vo.getMoneyQy()) ? 0 : vo.getMoneyQy());
                vo.setGxedMoney(ObjectUtil.isEmpty(vo.getGxedMoney()) ? 0 : vo.getGxedMoney());
            }
        }
        ExcelUtil.exportExcel(listVo, "签约统计按支撑人员", SignContractUserExportVo.class, response);
    }
}
