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

    /** 修改回显 */
    ZqztLedgerInfoVo queryForEdit(Long id);

    /** 工单号是否已存在（不过滤 is_valid，与数据库唯一键 uk_order_no 保持一致） */
    Boolean existsByOrderNo(String orderNo);

    /** 新增（主表 + 子表事务）：全流程状态由完成时间推导 */
    Boolean insertByBo(ZqztLedgerInfoBo bo);

    /**
     * 新增并指定全流程状态与备注
     *
     * @param status 指定全流程状态；为 null 时仍按完成时间推导
     * @param remark 指定备注；为非空白时覆盖表单备注
     * @return 是否成功
     */
    Boolean insertByBo(ZqztLedgerInfoBo bo, String status, String remark);

    /** 修改（工单号不可变，子表先删后插） */
    Boolean updateByBo(ZqztLedgerInfoBo bo);

    /** 删除（主表 + 子表 is_valid=0 逻辑删除） */
    Boolean deleteWithValidByIds(Collection<Long> ids);

    /**
     * 同步火焰卫士（RPA 失败重处理）：清空回调结果字段，可选置「待同步」
     *
     * @param id                   台账ID
     * @param syncToFlameGuardian  是否同时置 flame_guardian_syn='1' 由定时任务重推
     * @return 是否成功
     */
    Boolean syncFlameGuardian(Long id, Boolean syncToFlameGuardian);

    /** 导出分页（job-server 无登录态，数据权限由 areaCode/custManagerOauser 显式传入） */
    List<ZqztLedgerInfoExportVo> exportPageList(ZqztLedgerInfoBo bo, Page<ZqztLedgerInfoExportVo> page,
        String areaCode, String custManagerOauser);

    /** 按摄像头账号查询台账及云智眼设备配置（供外部系统同步，未找到返回 null） */
    ZqztCloudConfigRespDTO queryCameraConfig(String cameraAccount);

    /** 发送云智眼账户改密短信验证码（验证码发到当前登录用户手机） */
    void sendCloudAccountSmsCode(Long ledgerId);

    /** 修改云智眼账户密码（短信验证码校验通过后，仅更新 cloud_password） */
    Boolean updateCloudAccountPassword(Long ledgerId, String newPassword, String smsCode);

    /** 查看云智眼账户密码原值（短信验证码校验通过后返回明文） */
    String viewCloudAccountPassword(Long ledgerId, String smsCode);

    /** 当前登录用户脱敏手机号（供改密弹窗直接展示） */
    String getCurrentUserMaskedPhone();
}
