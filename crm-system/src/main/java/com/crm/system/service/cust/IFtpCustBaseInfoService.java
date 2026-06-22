package com.crm.system.service.cust;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.domain.dao.FtpCustBaseInfoDo;

import java.util.List;

/**
 * FTP客户基础信息Service接口
 *
 * @author crm
 */
public interface IFtpCustBaseInfoService extends IService<FtpCustBaseInfoDo> {

    /**
     * 备份FTP客户基础信息表
     */
    void backUpFtpCustBaseInfo();

    /**
     * 批量插入FTP客户基础信息
     *
     * @param list 客户基础信息列表
     * @return 插入成功的记录数
     */
    int batchInsert(List<FtpCustBaseInfoDo> list);
}
