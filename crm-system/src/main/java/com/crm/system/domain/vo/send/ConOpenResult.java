package com.crm.system.domain.vo.send;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ConOpenResult {
    private String code;
    private String msg;
    @JsonIgnore
    private String jsonStr;

    @Override
    public String toString() {
        return "ConOpenResult [code=" + code + ", msg=" + msg + ", jsonStr=" + jsonStr + "]";
    }

}
