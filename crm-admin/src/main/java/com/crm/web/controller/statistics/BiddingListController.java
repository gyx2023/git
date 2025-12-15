package com.crm.web.controller.statistics;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.crm.common.annotation.Log;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.dto.BiddingListReqDTO;
import com.crm.common.core.domain.dto.BiddingListRespDTO;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.topp.BiddingListExportVo;
import com.crm.system.service.statistics.BiddingListService;
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
@RequestMapping("/statistics/biddingList")
public class BiddingListController {
    @Resource
    private BiddingListService biddingListService;

    /**
     * 获取用户列表
     */
    @SaCheckPermission("statistics:biddingList:list")
    @GetMapping("/list")
    public TableDataInfo<BiddingListRespDTO> list(BiddingListReqDTO reqDTO, PageQuery pageQuery) {
        return biddingListService.selectPageList(reqDTO, pageQuery);
    }


    /**
     * 导出用户列表
     */
    @Log(title = "投标清单导出", businessType = BusinessType.EXPORT)
    @SaCheckPermission("statistics:biddingList:export")
    @PostMapping("/export")
    public void export(BiddingListReqDTO reqDTO, HttpServletResponse response) {
        List<BiddingListRespDTO> list = biddingListService.getBiddingListRespDTOS(reqDTO);
        List<BiddingListExportVo> listVo =BeanUtil.copyToList(list, BiddingListExportVo.class);
        ExcelUtil.exportExcel(listVo, "投标清单文件", BiddingListExportVo.class, response);
    }

}
