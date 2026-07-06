package com.crm.system.service.cust;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.domain.dao.FtpCustBaseInfoDo;
import com.crm.system.mapper.cust.FtpCustBaseInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * FTP客户基础信息Service实现
 *
 * @author crm
 */
@Slf4j
@Service
public class FtpCustBaseInfoServiceImpl extends ServiceImpl<FtpCustBaseInfoMapper, FtpCustBaseInfoDo>
        implements IFtpCustBaseInfoService {

    @Resource
    private FtpCustBaseInfoMapper ftpCustBaseInfoMapper;

    /**
     * 备份FTP客户基础信息表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void backUpFtpCustBaseInfo() {
        log.info("开始备份FTP客户基础信息表");
        try {
            // 删除旧备份表
            ftpCustBaseInfoMapper.dropBackupTable();
            // 创建备份表结构
            ftpCustBaseInfoMapper.createBackupTable();
            // 复制数据到备份表
            ftpCustBaseInfoMapper.copyToBackupTable();
            // 清空原表
            ftpCustBaseInfoMapper.truncateTable();
            log.info("FTP客户基础信息表备份完成");
        } catch (Exception e) {
            log.error("备份FTP客户基础信息表失败", e);
            throw new RuntimeException("备份FTP客户基础信息表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量插入FTP客户基础信息
     *
     * @param list 客户基础信息列表
     * @return 插入成功的记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<FtpCustBaseInfoDo> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        int count = list.size();

        log.info("开始批量插入{}条FTP客户基础信息", list.size());
        long startTime = System.currentTimeMillis();

        try {
            ftpCustBaseInfoMapper.insertBatch(list);
            long endTime = System.currentTimeMillis();
            log.info("成功插入{}条FTP客户基础信息，耗时: {}ms", count, (endTime - startTime));
            return count;
        } catch (Exception e) {
            log.error("批量插入FTP客户基础信息失败", e);
            throw new RuntimeException("批量插入FTP客户基础信息失败: " + e.getMessage(), e);
        }
    }
}
