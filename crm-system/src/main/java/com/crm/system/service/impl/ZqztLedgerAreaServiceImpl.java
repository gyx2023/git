package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.domain.dao.ZqztLedgerArea;
import com.crm.system.domain.vo.ZqztLedgerAreaVo;
import com.crm.system.mapper.ZqztLedgerAreaMapper;
import com.crm.system.service.IZqztLedgerAreaService;
import com.crm.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 台账省市区县街道 服务实现
 */
@Service
@RequiredArgsConstructor
public class ZqztLedgerAreaServiceImpl implements IZqztLedgerAreaService {

    /** 浙江省组织code（地市下拉的根） */
    private static final String PROVINCE_CODE = "zhejiangsheng";

    private final ZqztLedgerAreaMapper areaMapper;

    @Override
    public List<ZqztLedgerAreaVo> queryChildren(String parentCode) {
        String pc = StringUtils.isBlank(parentCode) ? PROVINCE_CODE : parentCode;
        return areaMapper.selectVoList(Wrappers.<ZqztLedgerArea>lambdaQuery()
            .eq(ZqztLedgerArea::getParentCode, pc)
            .eq(ZqztLedgerArea::getIsValid, "1")
            .orderByAsc(ZqztLedgerArea::getOrgCode));
    }
}
