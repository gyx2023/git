package com.crm.system.mapper;

import com.crm.system.domain.dao.MaintenanceDetailDo;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 维系预警明细 Mapper（写入用）
 *
 * @author crm
 */
@Mapper
public interface MaintenanceDetailMapper extends BaseMapper<MaintenanceDetailDo> {
}
