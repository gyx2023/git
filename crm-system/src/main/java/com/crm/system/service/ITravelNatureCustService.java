package com.crm.system.service;

import com.crm.system.domain.vo.NatureCustVo;

import java.util.List;

/**
 * 自然客户查询服务接口
 *
 * @author crm
 * @date 2026-06-17
 */
public interface ITravelNatureCustService {

    /**
     * 根据关键字模糊搜索浙江省自然客户（名称或编码）
     *
     * @param keyword 搜索关键字
     * @return 匹配的自然客户列表（最多50条）
     */
    List<NatureCustVo> searchNatureCust(String keyword);
}
