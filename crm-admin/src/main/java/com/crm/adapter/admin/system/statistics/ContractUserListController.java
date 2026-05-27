package com.crm.adapter.admin.system.statistics;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import com.crm.common.annotation.Log;
import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.dto.ContractUserReqDTO;
import com.crm.system.domain.dto.ContractUserRespDTO;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.topp.ContractUserListExportVo;
import com.crm.system.service.statistics.ContractUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author pengjun
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/statistics/contractUser")
public class ContractUserListController {

    @Resource
    private ContractUserService contractUserService;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public TableDataInfo<ContractUserRespDTO> list(ContractUserReqDTO reqDTO, PageQuery pageQuery) {
        return contractUserService.selectPageList(reqDTO, pageQuery);
    }


    /**
     * 导出用户列表
     */
    @Log(title = "投标清单导出", businessType = BusinessType.EXPORT)
    @SaCheckPermission("statistics:contractUser:export")
    @PostMapping("/export")
    public void export(ContractUserReqDTO reqDTO, HttpServletResponse response) {
        List<ContractUserRespDTO> list = contractUserService.getContractUserRespDTOS(reqDTO);
        List<ContractUserListExportVo> listVo = BeanUtil.copyToList(list, ContractUserListExportVo.class);
        ExcelUtil.exportExcel(listVo, "投标清单文件", ContractUserListExportVo.class, response);
    }
}
