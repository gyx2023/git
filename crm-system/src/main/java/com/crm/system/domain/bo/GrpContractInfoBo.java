package com.crm.system.domain.bo;

import com.crm.common.core.domain.BaseEntity;
import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 集团合同信息业务对象 grp_contract_info
 *
 * @author crm
 * @date 2026-01-23
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class GrpContractInfoBo extends BaseEntity {

    /**
     * 合同ID，序列号
     */
    @NotBlank(message = "合同ID，序列号不能为空", groups = { EditGroup.class })
    private String contractId;

    /**
     * 对应于省分集团客户信息表zqzt_grp_group_info中的GRP_ID
     */
    @NotNull(message = "对应于省分集团客户信息表zqzt_grp_group_info中的GRP_ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long grpId;

    /**
     * 合同ERP编码
     */
    @NotBlank(message = "合同ERP编码不能为空", groups = { EditGroup.class })
    private String string2;

    /**
     * 合同编号，等于grp_cust_inuse.CONTRACT_ID
     */
    @NotBlank(message = "合同编号")
    private String string15;

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
    @NotBlank(message = "合同类型", groups = { AddGroup.class, EditGroup.class })
    private String contractType;

    /**
     * 合同名称
     */
    @NotBlank(message = "合同名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractName;

    /**
     * 合同生效日期
     * */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "合同生效日期不能为空")
    private Date contractEffdate;

    /**
     * 合同失效日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "合同失效日期不能为空")
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
    @NotBlank(message = "缴费方式不能为空")
    private String paytype;

    /**
     * 合同备注
     */
    @NotBlank(message = "合同备注不能为空")
    private String contractNote;

    /**
     * 合同创建人
     */
    @NotBlank(message = "合同创建人不能为空")
    private String creator;

    /**
     * 合同带的合同文本，多个合同文本的话用逗号分割，存放ocs地址
     */
    @NotBlank(message = "合同带的合同文本，多个合同文本的话用逗号分割，存放ocs地址不能为空")
    private String files;

    /**
     * 合同是首次合同，还是续约合同。在SYS_PARAM_DETAIL.PARAM_NAME='GRP_CONTRACT_PROPERTY_NEW'定义
     00	新签合同
     01	续签合同
     03	补充合同
     */
    @NotBlank(message = "合同是首次合同，还是续约合同", groups = { AddGroup.class, EditGroup.class })
    private String contractProperty;

    /**
     * 是否为招投标项目，选择"是""否"，系统默认"否"，在SYS_PARAM_DETAIL.PARAM_NAME='LOGIC_STATUS'定义
     0	否
     1	是
     */
    @NotBlank(message = "是否为招投标项目不能为空")
    private String isBidding;

    /**
     * 我方主体
     */
    @NotBlank(message = "我方主体不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractMain;

    /**
     * 对方全称
     */
    @NotBlank(message = "对方全称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractParty;

    /**
     * 承办部门
     */
    @NotBlank(message = "承办部门不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractDept;

    /**
     * 承办人
     */
    @NotBlank(message = "承办人不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractMan;

    /**
     * 不含增值税合同额(元)
     */
    @NotNull(message = "不含增值税合同额(元)不能为空")
    private BigDecimal contractMoney2;

    /**
     * 是否固定金额 0-是；1-否
     */
    @NotNull(message = "是否固定金额 0-是；1-否不能为空", groups = {  EditGroup.class })
    private Long isFix;

    /**
     * 审批通过日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "审批通过日期不能为空")
    private Date auditDate;

    /**
     * 承办人联系电话
     */
    @NotBlank(message = "承办人联系电话不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractPhone;

    /**
     * 原合同编号
     */
    @NotBlank(message = "原合同编号不能为空")
    private String contractOldnum;

    /**
     * 预计本年完成收入（元）
     */
    @NotNull(message = "预计本年完成收入（元）不能为空")
    private BigDecimal yearIncome;

    /**
     * 服务性金额（元）
     */
    @NotNull(message = "服务性金额（元）不能为空")
    private BigDecimal servAmount;

    /**
     * 含增值税合同额(元)
     */
     @NotNull(message = "含增值税合同额(元)不能为空")
     private BigDecimal contractMoney;

     /**
      * 合同签约日期
      */
     @JsonFormat(pattern = "yyyy-MM-dd")
     @NotNull(message = "合同签约日期不能为空")
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
      15	已定稿16	签订中
      2	审签中
      3	暂停履行
      4	终止履行
      5	办结
      6	变更补充
      7	作废
      9	履行中
      */
     @NotBlank(message = "合同状态不能为空", groups = { AddGroup.class, EditGroup.class })
     private String contractStatus;
     /**
      * 合同分类一级代码
      */
     @NotNull(message = "合同分类一级代码不能为空")
     private Long contractCateL1Code;
     /**
      * 合同分类一级名称
      */
     @NotBlank(message = "合同分类一级名称不能为空", groups = {  EditGroup.class })
     private String contractCateL1Name;

     /**
      * 合同分类二级代码
      */
     @NotNull(message = "合同分类二级代码不能为空")
     private Long contractCateL2Code;

     /**
      * 合同分类二级名称
      */
     @NotBlank(message = "合同分类二级名称不能为空", groups = {  EditGroup.class })
     private String contractCateL2Name;

     /**
      * 合同分类三级代码
      */
     @NotNull(message = "合同分类三级代码不能为空")
     private Long contractCateL3Code;

     /**
      * 合同分类三级名称
      */
     @NotBlank(message = "合同分类三级名称不能为空", groups = { EditGroup.class })
     private String contractCateL3Name;

     /**
      * 备注
      */
     @NotBlank(message = "备注不能为空")
     private String remark;
}

