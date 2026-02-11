package com.crm.system.service.rpa;


import java.util.Map;

/**
 * rpa单映射Service接口
 *
 * @author crm
 * @date 2026-01-06
 */
public interface IZqztRpaFormMappingService {

    Map<String,Object> formFilling( Map<String, Object> body,String bizCode,String taskCode);

}
