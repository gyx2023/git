package com.crm.statistics.service;

import com.crm.common.core.page.TableDataInfo;
import com.crm.statistics.domain.dto.BaikeClickStatDTO;
import com.crm.statistics.domain.vo.BaikeClickStatVO;

import java.util.List;

public interface IBaikeClickStatService {

    /**
     * 分页查询百科点击量统计
     */
    TableDataInfo<BaikeClickStatVO> queryList(BaikeClickStatDTO dto);

    /**
     * 导出百科点击量统计（不分页）
     */
    List<BaikeClickStatVO> exportList(BaikeClickStatDTO dto);
}
