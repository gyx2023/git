package com.crm.system.mapper;

import com.crm.system.domain.dao.MaintenanceDetailNewDo;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 新维系预警明细 Mapper（写入用）
 *
 * @author crm
 */
@Mapper
public interface MaintenanceDetailNewMapper extends BaseMapper<MaintenanceDetailNewDo> {
}
