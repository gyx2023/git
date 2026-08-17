package com.crm.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.bo.ZqztLedgerInfoBo;
import com.crm.system.domain.dto.ZqztCloudConfigRespDTO;
import com.crm.system.domain.vo.ZqztLedgerInfoExportVo;
import com.crm.system.domain.vo.ZqztLedgerInfoVo;

import java.util.Collection;
import java.util.List;

/**
 * 台账数据主表 服务接口
 */
public interface IZqztLedgerInfoService {

    /** 分页查询（省市县 + 客户经理数据权限） */
    TableDataInfo<ZqztLedgerInfoVo> queryPageList(ZqztLedgerInfoBo bo, PageQuery pageQuery);

    /** 详情（敏感字段脱敏） */
    ZqztLedgerInfoVo queryById(Long id);

    /** 修改回显（不脱敏） */
    ZqztLedgerInfoVo queryForEdit(Long id);

    /** 工单号是否已存在（不过滤 is_valid，与数据库唯一键 uk_order_no 保持一致） */
    Boolean existsByOrderNo(String orderNo);

    /** 新增（主表 + 子表事务） */
    Boolean insertByBo(ZqztLedgerInfoBo bo);

    /** 修改（工单号不可变，子表先删后插） */
    Boolean updateByBo(ZqztLedgerInfoBo bo);

    /** 删除（主表 + 子表 is_valid=0 逻辑删除） */
    Boolean deleteWithValidByIds(Collection<Long> ids);

    /** 导出分页（job-server 无登录态，数据权限由 areaCode/custManagerOauser 显式传入） */
    List<ZqztLedgerInfoExportVo> exportPageList(ZqztLedgerInfoBo bo, Page<ZqztLedgerInfoExportVo> page,
        String areaCode, String custManagerOauser);

    /** 按摄像头账号查询台账及云智眼设备配置（供外部系统同步，未找到返回 null） */
    ZqztCloudConfigRespDTO queryCameraConfig(String cameraAccount);
}
