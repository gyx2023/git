package com.crm.system.domain.vo;

import lombok.Data;
import java.util.List;

@Data
public class SysAreaTreeVo {

    private String id;

    private String label;

    private List<SysAreaTreeVo> children;
}
