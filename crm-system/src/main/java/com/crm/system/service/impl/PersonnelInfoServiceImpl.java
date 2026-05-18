package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.domain.PersonnelInfo;
import com.crm.system.mapper.PersonnelInfoMapper;
import com.crm.system.service.PersonnelInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 人员信息Service实现
 */
@Slf4j
@Service
public class PersonnelInfoServiceImpl extends ServiceImpl<PersonnelInfoMapper, PersonnelInfo>
        implements PersonnelInfoService {

    @Resource
    private PersonnelInfoMapper personnelInfoMapper;

    @Override
    public PersonnelInfo getByUserId(String userId) {
        LambdaQueryWrapper<PersonnelInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(PersonnelInfo::getUserId, userId);
        return personnelInfoMapper.selectOne(lqw);
    }
}
