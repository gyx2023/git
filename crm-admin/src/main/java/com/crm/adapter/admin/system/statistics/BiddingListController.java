package com.crm.adapter.admin.system.statistics;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.tree.Tree;
import com.crm.common.annotation.Log;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.system.domain.dto.BiddingListReqDTO;
import com.crm.system.domain.dto.BiddingListRespDTO;
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
    @GetMapping("/list")
    @Log(title = "商机投标清单", businessType = BusinessType.QUERY)
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
        List<BiddingListExportVo> listVo = BeanUtil.copyToList(list, BiddingListExportVo.class);
        ExcelUtil.exportExcel(listVo, "投标清单文件", BiddingListExportVo.class, response);
    }

    /**
     * 获取部门树列表
     */
    @GetMapping("/currentDeptTree")
    public R<List<Tree<Long>>> currentDeptTree() {
        return R.ok(biddingListService.querySysDeptTree());
    }


}
