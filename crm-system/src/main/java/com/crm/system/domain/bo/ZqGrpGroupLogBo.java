package com.crm.system.domain.bo;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 政企集团客户信息业务对象 zq_grp_group_log
 *
 * @author crm
 * @date 2025-11-24
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ZqGrpGroupLogBo extends BaseEntity {

    /**
     * 集团标识
     */
    @NotNull(message = "集团标识不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long grpId;

    /**
     * 集团名称
     */
    @NotBlank(message = "集团名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grpName;

    /**
     * 正式集团编号（15位编码）
     */
    @NotBlank(message = "正式集团编号（15位编码）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grpCode;

    /**
     * 总部集团编码
     */
    @NotBlank(message = "总部集团编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String hqGrpCode;

    /**
     * 客户id
     */
    @NotBlank(message = "客户id不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custId;

    /**
     * 变更类型：T1-集团基本信息变更,T2-集团客户经理变更
     */
    @NotBlank(message = "变更类型：T1-集团基本信息变更,T2-集团客户经理变更不能为空", groups = { AddGroup.class, EditGroup.class })
    private String updateType;

    /**
     * 1-变更前,2-变更后
     */
    @NotBlank(message = "1-变更前,2-变更后不能为空", groups = { AddGroup.class, EditGroup.class })
    private String dataType;

    /**
     * 集团级别:A-B-C-D-E-F-G-H-I-Y-Z
     */
    @NotBlank(message = "集团级别:A-B-C-D-E-F-G-H-I-Y-Z不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grpLevel;

    /**
     * 集团状态:01-目标,02-潜在,03-正式,04-注销
     */
    @NotBlank(message = "集团状态:01-目标,02-潜在,03-正式,04-注销不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grpStatus;

    /**
     * 地市
     */
    @NotBlank(message = "地市不能为空", groups = { AddGroup.class, EditGroup.class })
    private String areaId;

    /**
     * 县分
     */
    @NotBlank(message = "县分不能为空", groups = { AddGroup.class, EditGroup.class })
    private String countyId;

    /**
     * 行政县
     */
    @NotBlank(message = "行政县不能为空", groups = { AddGroup.class, EditGroup.class })
    private String countyId2;

    /**
     * 所属网格编码
     */
    @NotBlank(message = "所属网格编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String gridId;

    /**
     * 行业大类:01-金融业,02-党政军部门,03-科学教育文化体育卫生出版业,04-旅游饭店娱乐服务业,05-采掘业和加工制造业,06-公共服务业,07-交通运输仓储和邮政业,08-批发零售业,09-房地产业,10-建筑业,11-计算机信息业,12-农林牧副渔,99-其他
     */
    @NotBlank(message = "行业大类:01-金融业,02-党政军部门,03-科学教育文化体育卫生出版业,04-旅游饭店娱乐服务业,05-采掘业和加工制造业,06-公共服务业,07-交通运输仓储和邮政业,08-批发零售业,09-房地产业,10-建筑业,11-计算机信息业,12-农林牧副渔,99-其他不能为空", groups = { AddGroup.class, EditGroup.class })
    private String industryMajorKind;

    /**
     * 行业亚类
     */
    @NotBlank(message = "行业亚类不能为空", groups = { AddGroup.class, EditGroup.class })
    private String industrySubKind;

    /**
     * 行业细类
     */
    @NotBlank(message = "行业细类不能为空", groups = { AddGroup.class, EditGroup.class })
    private String industryDetailKind;

    /**
     * 集团业务分类:00-综合集团,10-移动集团,20-固网集团
     */
    @NotBlank(message = "集团业务分类:00-综合集团,10-移动集团,20-固网集团不能为空", groups = { AddGroup.class, EditGroup.class })
    private String busiType;

    /**
     * 集团客户类别:400-大客户,500-中小企业客户,600-普通客户
     */
    @NotBlank(message = "集团客户类别:400-大客户,500-中小企业客户,600-普通客户不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grpCustType;

    /**
     * 集团客户拓展类型:01-省直拓,02-市直拓,03-县直拓
     */
    @NotBlank(message = "集团客户拓展类型:01-省直拓,02-市直拓,03-县直拓不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grpExpandType;

    /**
     * 集团客户业务部门分类:01-数字政府BU,02-军政（政法）,03-证券保险客户中心,04-银行客户中心,05-军政（军民融合）,06-智慧城市BU,07-智慧园区BU,08-教育行业BU,09-系统集成客户,10-生态环境（生态）,11-政法客户,12-军政客户,13-现代服务（交通）,14-智慧民生客户,15-健康文旅（医疗）,16-公共事业客户中心,17-智算营销中心（云数据）,18-工业互联网BU,19-健康文旅（文旅）,20-智慧制造客户,21-省行业客户部,22-商企客户,23-战略客户,24-生态环境（农业）,25-科创企业客户中心,26-政要客户,27-金融客户,28-阿里中心,29-网易中心,30-产业互联网客户,31-企业服务一部,32-企业服务二部,33-纺织服装行业服务部,34-现代服务（商贸）,35-健康文旅客户中心,36-吉利中心,98-公众客户,99-中小企业
     */
    @NotBlank(message = "集团客户业务部门分类:01-数字政府BU,02-军政（政法）,03-证券保险客户中心,04-银行客户中心,05-军政（军民融合）,06-智慧城市BU,07-智慧园区BU,08-教育行业BU,09-系统集成客户,10-生态环境（生态）,11-政法客户,12-军政客户,13-现代服务（交通）,14-智慧民生客户,15-健康文旅（医疗）,16-公共事业客户中心,17-智算营销中心（云数据）,18-工业互联网BU,19-健康文旅（文旅）,20-智慧制造客户,21-省行业客户部,22-商企客户,23-战略客户,24-生态环境（农业）,25-科创企业客户中心,26-政要客户,27-金融客户,28-阿里中心,29-网易中心,30-产业互联网客户,31-企业服务一部,32-企业服务二部,33-纺织服装行业服务部,34-现代服务（商贸）,35-健康文旅客户中心,36-吉利中心,98-公众客户,99-中小企业不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grpBusiDeptType;

    /**
     * 集团客户省市名单制存增量标识:01-存量,02-增量
     */
    @NotBlank(message = "集团客户省市名单制存增量标识:01-存量,02-增量不能为空", groups = { AddGroup.class, EditGroup.class })
    private String provinceNewoldType;

    /**
     * 服务经理
     */
    @NotBlank(message = "服务经理不能为空", groups = { AddGroup.class, EditGroup.class })
    private String managerId;

    /**
     * 资料创建人
     */
    @NotBlank(message = "资料创建人不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grpCreator;

    /**
     * 资料录入时间
     */
    @NotNull(message = "资料录入时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date grpCreateTime;

    /**
     * 省级客户经理（省分字段）
     */
    @NotBlank(message = "省级客户经理（省分字段）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String provManagerEmpId;

    /**
     * 省级客户经理OA账号（省分字段）
     */
    @NotBlank(message = "省级客户经理OA账号（省分字段）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String provManagerOauser;

    /**
     * 省级客户名字（省分字段）
     */
    @NotBlank(message = "省级客户名字（省分字段）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String provManagerName;

    /**
     * 市级客户经理（省分字段）
     */
    @NotBlank(message = "市级客户经理（省分字段）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String cityManagerEmpId;

    /**
     * 市级客户经理OA账号（省分字段）
     */
    @NotBlank(message = "市级客户经理OA账号（省分字段）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String cityManagerOauser;

    /**
     * 市级客户名字（省分字段）
     */
    @NotBlank(message = "市级客户名字（省分字段）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String cityManagerName;

    /**
     * 县级客户经理（省分字段）
     */
    @NotBlank(message = "县级客户经理（省分字段）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String countyManagerEmpId;

    /**
     * 县级客户经理OA账号（省分字段）
     */
    @NotBlank(message = "县级客户经理OA账号（省分字段）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String countyManagerOauser;

    /**
     * 县级客户名字（省分字段）
     */
    @NotBlank(message = "县级客户名字（省分字段）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String countyManagerName;


}
