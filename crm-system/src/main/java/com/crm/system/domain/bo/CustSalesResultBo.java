package com.crm.system.domain.bo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 宽带专项营销结果保存请求参数
 * @author crm
 */
@Data
public class CustSalesResultBo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户明细主键集合（ftp_broadband_market_user.ID_） */
    @NotEmpty(message = "请选择需要填写营销结果的用户")
    private List<Integer> userIds;

    /** 营销结果：1-成功 0-失败 */
    @NotNull(message = "请选择营销结果")
    private Integer result;

    /** 营销成功产品编码（营销结果为成功时必填） */
    private String orderProduct;

    /** 营销成功产品（其它）名称，产品选择"其它"时必填 */
    private String otherProductName;
}
