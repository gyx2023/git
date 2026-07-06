package com.crm.system.mapper.zbzq;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.dao.zbzq.VisitAppointmentDo;
import com.crm.system.domain.dao.zbzq.VisitRecordDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@DS("slave_1")
public interface VisitAppointmentQueryMapper {

    List<VisitAppointmentDo> selectUnvisitedAppointments(@Param("startTime") String startTime,
                                                         @Param("endTime") String endTime);

    VisitRecordDo selectLastVisitRecord(@Param("customerId") String customerId,
                                        @Param("oaAccount") String oaAccount);

    Map<String, Object> selectCustomerInfo(@Param("customerId") String customerId);
}
