package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 集团合同信息对象 zqzt_grp_contract_info
 *
 * @author 万超凡
 * @date 2026-01-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_grp_contract_info")
public class ZqztGrpContractInfo extends BaseEntity {

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
     * 合同编号等于grp_cust_inuse.CONTRACT_ID
     */
    private String inuseContractId;
    /**
     * 合同类型（于sys_param_detail.param_name='GRP_CONTRACT_TYPE_NEW'定义）0普通合同 00ERP主体合同 01ERP其它合同 02格式化合同 03关联客户专用 1营销合同 2整体转网协议 3战略合作协议
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
     * 缴费方式 10后付费 12转账支票 13信用卡 14缴费卡 15银行托收 17其他 18轻松如意通 19营业厅话费预缴 20预付费 21银行代扣 22一户通 23银行托收(固网业务) 25原固网营业厅话费预缴
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
     * 合同是首次合同，还是续约合同。在SYS_PARAM_DETAIL.PARAM_NAME='GRP_CONTRACT_PROPERTY_NEW'定义 00新签合同 01续签合同 03补充合同
     */
    private String contractProperty;
    /**
     * 是否为招投标项目，选择"是""否"，系统默认"否"，在SYS_PARAM_DETAIL.PARAM_NAME='LOGIC_STATUS'定义 0否 1是
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
     * 合同状态：0其他 1已审批 10取消 11起草 12终止解除 13作废申请中 14删除 15已定稿 16签订中 2审签中 3暂停履行 4终止履行 5办结 6变更补充 7作废 9履行中
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

}
