package com.crm.job.service.grp.impl;

import com.crm.system.domain.grp.TfFCustGroupMDO;
import com.crm.system.mapper.grp.TfFCustGroupMMapper;
import com.crm.job.service.grp.TfFCustGroupMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 客户集团信息服务实现类
 *
 * @author crm
 */
@Slf4j
@Service
public class TfFCustGroupMServiceImpl implements TfFCustGroupMService {

    @Resource
    private TfFCustGroupMMapper tfFCustGroupMMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TfFCustGroupMDO> custGroupList) {
        if (custGroupList == null || custGroupList.isEmpty()) {
            return 0;
        }

        log.info("开始批量插入客户集团信息，数量: {}", custGroupList.size());
        int result = 0;
        // 使用BaseMapper的insert方法逐个插入
        tfFCustGroupMMapper.insertBatch(custGroupList);
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
}
