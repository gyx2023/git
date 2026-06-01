package com.crm.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.domain.DwdRepGwPstD;
import com.crm.system.mapper.DwdRepGwPstDMapper;
import com.crm.system.service.DwdRepGwPstDService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 经分人员职位表Service实现
 */
@Slf4j
@Service
public class DwdRepGwPstDServiceImpl extends ServiceImpl<DwdRepGwPstDMapper, DwdRepGwPstD> implements DwdRepGwPstDService {

    @Resource
    private DwdRepGwPstDMapper dwdRepGwPstDMapper;

    @Override
    public List<Map<String, Object>> getDwdRepGwPstListFromIob() {
        return dwdRepGwPstDMapper.selectDwdRepGwPstListFromIob();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void truncate() {
        dwdRepGwPstDMapper.delete(null);
        log.info("清空dwd_rep_gw_pst_d表数据完成");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertBatch(List<DwdRepGwPstD> list) {
        if (list != null && !list.isEmpty()) {
            log.info("开始插入{}条dwd_rep_gw_pst_d数据", list.size());
            long startTime = System.currentTimeMillis();
            dwdRepGwPstDMapper.insertBatch(list);
            long endTime = System.currentTimeMillis();
            log.info("插入{}条dwd_rep_gw_pst_d数据成功,耗时:{}ms", list.size(), (endTime - startTime));
        }
    }
}
