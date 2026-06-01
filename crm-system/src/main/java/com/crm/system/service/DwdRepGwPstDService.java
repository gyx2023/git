package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.domain.DwdRepGwPstD;

import java.util.List;
import java.util.Map;

/**
 * 经分人员职位表Service接口
 */
public interface DwdRepGwPstDService extends IService<DwdRepGwPstD> {

    /**
     * 从IOB数据库查询人员职位列表
     */
    List<Map<String, Object>> getDwdRepGwPstListFromIob();

    /**
     * 清空表数据
     */
    void truncate();

    /**
     * 批量插入人员职位数据
     */
    void insertBatch(List<DwdRepGwPstD> list);
}
