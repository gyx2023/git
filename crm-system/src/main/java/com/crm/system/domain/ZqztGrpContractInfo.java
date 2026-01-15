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
 * @date 2026-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_grp_contract_info")
public class ZqztGrpContractInfo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 合同编号,由操作员根据合同文本上的合同编号手工录入
     */
    private String contractId;
    /**
     * 对应于集团表中的grp_id
     */
    private Long grpId;
    /**
     * 合同类型（于sys_param_detail.param_name='grp_contract_type_new'定义）
0	普通合同
00	erp主体合同
01	erp其它合同
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
     * 合同带的合同文本，多个合同文本的话用逗号分割
     */
    private String files;
    /**
     * 合同状态，在sys_param_detail.param_name='contract_stat'定义
00	待审核
01	未审核
02	生效
03	注销
10	一审通过
11	一审不通过
20	二审通过
21	二审不通过
30	三审通过
31	三审不通过
     */
    private String contractStat;
    /**
     * 审核流水
     */
    private String auditSn;
    /**
     * 备注
     */
    private String remark;
    /**
     * 合同是首次合同，还是续约合同。在sys_param_detail.param_name='grp_contract_property_new'定义
00	新签合同
01	续签合同
03	补充合同
     */
    private String string1;
    /**
     * 合同erp编码
     */
    private String string2;
    /**
     * 备用
     */
    private String string3;
    /**
     * 本地商机编号
     */
    private String busiOppNo;
    /**
     * 合同商机密级，系统默认"3级"，在sys_param_detail.param_name='busi_opp_sec_lvl'定义
01	一级
02	二级
03	三级
     */
    private String busiOppSecLvl;
    /**
     * 商机名称，系统控制字符不少于５个字
     */
    private String busiOppName;
    /**
     * 客户需求简述，系统控制字符不少于１０个字
     */
    private String custDemandDesc;
    /**
     * 解决方案简述，系统控制字符不少于１０个字
     */
    private String solvePlanDesc;
    /**
     * 是否为招投标项目，选择"是""否"，系统默认"否"，在sys_param_detail.param_name='logic_status'定义
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
     * 合同审核意见
     */
    private String string14;
    /**
     * 合同编号
等于grp_cust_inuse.contract_id
     */
    private String string15;
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
     * 备用
     */
    @TableField(value = "`int8`")
    private Long int8;
    /**
     * 备用
     */
    @TableField(value = "`int9`")
    private Long int9;
    /**
     * 备用
     */
    @TableField(value = "`int10`")
    private Long int10;
    /**
     * 业务类型
     */
    private String busiType;
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
     * 数据库不查询的字段
     */
    @TableField(exist = false)
    private String createBy;

    @TableField(exist = false)
    private String updateBy;

    @TableField(exist = false)
    private Date updateTime;
}
