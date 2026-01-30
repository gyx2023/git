package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 集团合同信息日志对象 zqzt_grp_contract_info_log
 *
 * @author 万超凡
 * @date 2026-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_grp_contract_info_log")
public class ZqztGrpContractInfoLog extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 合同ID，序列号
     */
    @TableId(value = "contract_id")
    private String contractId;
    /**
     * 对应于省分集团客户信息表zqzt_grp_group_info中的GRP_ID
     */
    private Long grpId;
    /**
     * 合同ERP编码
     */
    private String erpContractId;
    /**
     * 合同编号，等于grp_cust_inuse.CONTRACT_ID
     */
    private String inuseContractId;
    /**
     * 合同类型（于sys_param_detail.param_name='GRP_CONTRACT_TYPE_NEW'定义）
     0	普通合同
     00	ERP主体合同
     01	ERP其它合同
     02	格式化合同
     03	关联客户专用
     1	营销合同
     2	整体转网协议
     3	战略合作协议
     */
    private String contractType;
    /**
     * 合同名称
     */
    private String contractName;
    /**
     * 合同生效日期
     */
    private Date contractEffdate;
    /**
     * 合同失效日期
     */
    private Date contractExpdate;
    /**
     * 缴费方式
     10	后付费
     12	转账支票
     13	信用卡
     14	缴费卡
     15	银行托收
     17	其他
     18	轻松如意通
     19	营业厅话费预缴
     20	预付费
     21	银行代扣
     22	一户通
     23	银行托收(固网业务)
     25	原固网营业厅话费预缴
     */
    private String paytype;
    /**
     * 合同备注
     */
    private String contractNote;
    /**
     * 合同创建人
     */
    private String creator;
    /**
     * 合同带的合同文本，多个合同文本的话用逗号分割，存放ocs地址
     */
    private String files;
    /**
     * 合同是首次合同，还是续约合同。在SYS_PARAM_DETAIL.PARAM_NAME='GRP_CONTRACT_PROPERTY_NEW'定义
     00	新签合同
     01	续签合同
     03	补充合同
     */
    private String contractProperty;
    /**
     * 是否为招投标项目，选择"是""否"，系统默认"否"，在SYS_PARAM_DETAIL.PARAM_NAME='LOGIC_STATUS'定义
     0	否
     1	是
     */
    private String isBidding;
    /**
     * 我方主体
     */
    private String contractMain;
    /**
     * 对方全称
     */
    private String contractParty;
    /**
     * 承办部门
     */
    private String contractDept;
    /**
     * 承办人
     */
    private String contractMan;
    /**
     * 不含增值税合同额(元)
     */
    private BigDecimal contractMoney2;
    /**
     * 是否固定金额 0-是；1-否
     */
    private Long isFix;
    /**
     * 审批通过日期
     */
    private Date auditDate;
    /**
     * 承办人联系电话
     */
    private String contractPhone;
    /**
     * 原合同编号
     */
    private String contractOldnum;
    /**
     * 预计本年完成收入（元）
     */
    private BigDecimal yearIncome;
    /**
     * 服务性金额（元）
     */
    private BigDecimal servAmount;
    /**
     * 含增值税合同额(元)
     */
    private BigDecimal contractMoney;
    /**
     * 合同签约日期
     */
    private Date contractSigndate;
    /**
     * 合同状态：
     0	其他
     1	已审批
     10	取消
     11	起草
     12	终止解除
     13	作废申请中
     14	删除
     15	已定稿
     16	签订中
     2	审签中
     3	暂停履行
     4	终止履行
     5	办结
     6	变更补充
     7	作废
     9	履行中
     */
    private String contractStatus;
    /**
     * 合同分类一级代码
     */
    private Long contractCateL1Code;
    /**
     * 合同分类一级名称
     */
    private String contractCateL1Name;
    /**
     * 合同分类二级代码
     */
    private Long contractCateL2Code;
    /**
     * 合同分类二级名称
     */
    private String contractCateL2Name;
    /**
     * 合同分类三级代码
     */
    private Long contractCateL3Code;
    /**
     * 合同分类三级名称
     */
    private String contractCateL3Name;
    /**
     * 备注
     */
    private String remark;

    /**
     * 数据库不查询的字段
     */
    @TableField(exist = false)
    private String createBy;

    @TableField(exist = false)
    private String updateBy;

    @Override
    public void setCreateBy(String createBy) {
        // 不执行任何操作，防止自动填充
    }

    @Override
    public void setUpdateBy(String updateBy) {
        // 不执行任何操作，防止自动填充
    }

}

