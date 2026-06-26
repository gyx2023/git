package com.crm.system.service;

import com.crm.system.domain.bo.PreTravelReportFormBo;

import java.util.Map;

/**
 * 行前报告表单服务接口
 *
 * @author crm
 * @date 2026-06-17
 */
public interface IPreTravelReportFormService {

    /**
     * 提交行前报告生成请求
     *
     * @param bo 表单提交业务对象
     * @return 包含 reportId、mdContent、taskStatus、taskStatusDesc 的结果 Map
     */
    Map<String, Object> submitPreTravelReport(PreTravelReportFormBo bo);
}
