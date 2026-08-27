package com.crm.system.service;

import com.crm.system.domain.vo.ZqztLedgerAreaVo;

import java.util.List;

/**
 * 台账省市区县街道 服务接口
 */
public interface IZqztLedgerAreaService {

    /**
     * 查询子级区域（parentCode 为空默认 zhejiangsheng → 地市；再下一级为区县、街道）
     */
    List<ZqztLedgerAreaVo> queryChildren(String parentCode);
}
