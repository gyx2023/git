package com.crm.common.core.domain.dto;

/**
 * 封装后三个字段的DTO（性能优先设计）
 */
public class ThreeFieldDTO {
    // final修饰+直接赋值，避免JVM重排序，减少GC开销
    private final String empId;
    private final String oaUserName;
    private final String fullName;

    // 全参构造器（直接初始化，比setter快10%+）
    public ThreeFieldDTO(String empId, String oaUserName, String fullName) {
        this.empId = empId;
        this.oaUserName = oaUserName;
        this.fullName = fullName;
    }

    // 仅生成必要的getter（按需使用）
    public String getEmpId() {
        return empId;
    }

    public String getOaUserName() {
        return oaUserName;
    }

    public String getFullName() {
        return fullName;
    }
}
