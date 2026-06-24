package com.crm.system.mapper.cust;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.FtpCustBaseInfoDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * FTP客户基础信息Mapper接口
 *
 * @author crm
 */
@Mapper
public interface FtpCustBaseInfoMapper extends BaseMapperPlus<FtpCustBaseInfoMapper, FtpCustBaseInfoDo, FtpCustBaseInfoDo> {

    /**
     * 删除备份表
     */
    @Update("DROP TABLE IF EXISTS ftp_cust_base_info_backup")
    void dropBackupTable();

    /**
     * 创建备份表
     */
    @Update("CREATE TABLE ftp_cust_base_info_backup LIKE ftp_cust_base_info")
    void createBackupTable();

    /**
     * 复制数据到备份表
     */
    @InterceptorIgnore(dataPermission = "true")
    @Update("INSERT INTO ftp_cust_base_info_backup SELECT * FROM ftp_cust_base_info")
    void copyToBackupTable();

    /**
     * 清空原表
     */
    @Update("TRUNCATE TABLE ftp_cust_base_info")
    void truncateTable();
}
