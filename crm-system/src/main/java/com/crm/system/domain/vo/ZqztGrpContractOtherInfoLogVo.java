package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 集团合同扩展信息日志视图对象 zqzt_grp_contract_other_info_log
 *
 * @author 万超凡
 * @date 2026-01-15
 */
@Data
@ExcelIgnoreUnannotated
public class ZqztGrpContractOtherInfoLogVo {


    private static final long serialVersionUID = 1L;

    /**
     * 序号，自增
     */
    @ExcelProperty(value = "序号，自增")
    private Long id;

    /**
     * 集团合同序号，对应GRP_CONTRACT_INFO.CONTRACT_ID
     */
    @ExcelProperty(value = "集团合同序号，对应GRP_CONTRACT_INFO.CONTRACT_ID")
    private String contractId;

    /**
     * 信息标识，详细看tab_column_config.module_name='GRP_CONTRACT_INFO'
     */
    @ExcelProperty(value = "信息标识，详细看tab_column_config.module_name='GRP_CONTRACT_INFO'")
    private String infoTag;

    /**
     * 信息值
     */
    @ExcelProperty(value = "信息值")
    private String infoVal;

    /**
     * 填写服务号码
     */
    @ExcelProperty(value = "填写服务号码")
    private String infoVal2;

    /**
     * 是否有效：1有效，0无效，99迁移失效
     */
    @ExcelProperty(value = "是否有效：1有效，0无效，99迁移失效")
    private String isValid;
}
