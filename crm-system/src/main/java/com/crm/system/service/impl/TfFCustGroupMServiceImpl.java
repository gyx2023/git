package com.crm.system.service.impl;

import com.crm.system.domain.dao.TfFCustGroupMDo;
import com.crm.system.mapper.grp.TfFCustGroupMMapper;
import com.crm.system.service.ITfFCustGroupMService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class TfFCustGroupMServiceImpl implements ITfFCustGroupMService {

    @Resource
    private TfFCustGroupMMapper tfFCustGroupMMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TfFCustGroupMDo> custGroupList) {
        if (custGroupList == null || custGroupList.isEmpty()) {
            return 0;
        }

        log.info("开始批量插入客户集团信息，数量: {}", custGroupList.size());
        int result = 0;
        // 使用BaseMapper的insert方法逐个插入
        tfFCustGroupMMapper.insertBatch(custGroupList,200);
        log.info("批量插入客户集团信息完成，成功数量: {}", custGroupList.size());
        return custGroupList.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int truncateTable() {
        log.info("开始清空tf_f_cust_group_m表数据");
        int result = tfFCustGroupMMapper.delete(null);
        log.info("清空tf_f_cust_group_m表数据完成，删除数量: {}", result);
        return result;
    }

    @Override
    public TfFCustGroupMDo selectByGroupId(String groupId) {
        LambdaQueryWrapper<TfFCustGroupMDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TfFCustGroupMDo::getGroupId, groupId);
        List<TfFCustGroupMDo> list = tfFCustGroupMMapper.selectList(queryWrapper);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }
}
