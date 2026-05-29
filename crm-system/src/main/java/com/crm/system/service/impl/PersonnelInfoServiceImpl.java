package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.crm.system.domain.PersonnelInfo;
import com.crm.system.mapper.PersonnelInfoMapper;
import com.crm.system.service.PersonnelInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class PersonnelInfoServiceImpl implements PersonnelInfoService {

    @Resource
    private PersonnelInfoMapper baseMapper;

    @Override
    public List<PersonnelInfo> findList(PersonnelInfo personnelInfo) {
        LambdaQueryWrapper<PersonnelInfo> queryWrapper = new LambdaQueryWrapper<PersonnelInfo>()
            .eq(StringUtils.isNotBlank(personnelInfo.getUserId()), PersonnelInfo::getUserId, personnelInfo.getUserId());
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void insertOrUpdate(PersonnelInfo personnelInfo) {
        baseMapper.insertOrUpdate(personnelInfo);
    }

    @Override
    public void insert(PersonnelInfo personnelInfo) {
        baseMapper.insert(personnelInfo);
    }

}
