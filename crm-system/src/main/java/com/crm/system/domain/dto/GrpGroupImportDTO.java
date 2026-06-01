package com.crm.system.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装后三个字段的DTO类
 */
@Data  // 自动生成getter、setter、toString等方法
@NoArgsConstructor  // 无参构造器
@AllArgsConstructor // 全参构造器
public class GrpGroupImportDTO {
    private String managerid;
    // 对应列表的第二个字段
    private String empId;
    // 对应列表的第三个字段
    private String oaUserName;
    // 对应列表的第四个字段
    private String fullName;
}
