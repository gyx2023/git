package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.crm.system.domain.cust.TfFCustGroupDDo;
import com.crm.system.mapper.cust.TfFCustGroupDMapper;
import com.crm.system.service.TfFCustGroupDService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class TfFCustGroupDServiceImpl implements TfFCustGroupDService {

    @Resource
    private TfFCustGroupDMapper tfFCustGroupDMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TfFCustGroupDDo> tfFCustGroupDList) {
        if (tfFCustGroupDList == null || tfFCustGroupDList.isEmpty()) {
            return 0;
        }

        log.info("开始批量插入集团实体客户增量信息，数量: {}", tfFCustGroupDList.size());
        int result = 0;
        // 使用BaseMapper的insert方法逐个插入
        tfFCustGroupDMapper.insertBatch(tfFCustGroupDList, 200);
        log.info("批量插入集团实体客户增量信息完成，成功数量: {}", tfFCustGroupDList.size());
        return tfFCustGroupDList.size();
    }

    @Override
    public int delByDateId(Date targetDate) {
        log.info("开始清空tf_f_cust_group_d表数据");
        String etlDataCycle = new SimpleDateFormat("yyyyMMdd").format(targetDate);
        QueryWrapper<TfFCustGroupDDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("etl_data_cycle", etlDataCycle);
        int result = tfFCustGroupDMapper.delete(queryWrapper);
        log.info("清空tf_f_cust_group_d表数据完成，删除数量: {}", result);
        return result;
    }
}
