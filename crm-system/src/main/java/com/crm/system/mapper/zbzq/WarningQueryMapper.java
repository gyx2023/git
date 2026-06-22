package com.crm.system.mapper.zbzq;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface WarningQueryMapper {

    List<Map<String, Object>> selectCutOverWarnings(@Param("provinceCustIds") List<String> provinceCustIds,
                                                     @Param("oaId") String oaId);

    List<Map<String, Object>> selectContractDueWarnings(@Param("provinceCustIds") List<String> provinceCustIds,
                                                         @Param("userDbId") String userDbId);

    List<Map<String, Object>> selectRedListWarnings(@Param("provinceCustIds") List<String> provinceCustIds,
                                                     @Param("userDbId") String userDbId);

    List<Map<String, Object>> selectReceiptWarnings(@Param("provinceCustIds") List<String> provinceCustIds,
                                                     @Param("userDbId") String userDbId);

    List<Map<String, Object>> selectGroupBuyWarnings(@Param("provinceCustIds") List<String> provinceCustIds,
                                                      @Param("userDbId") String userDbId);

    List<Map<String, Object>> selectOweWarnings(@Param("provinceCustIds") List<String> provinceCustIds,
                                                 @Param("userDbId") String userDbId);

    List<Map<String, Object>> selectOpportunities(@Param("custNo") String custNo,
                                                   @Param("userDbId") String userDbId);
}
