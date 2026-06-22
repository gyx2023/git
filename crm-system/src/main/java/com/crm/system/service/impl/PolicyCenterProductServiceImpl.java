package com.crm.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.domain.dao.PolicyCenterProduct;
import com.crm.system.mapper.PolicyCenterProductMapper;
import com.crm.system.service.PolicyCenterProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 策略中心产品Service实现
 */
@Slf4j
@Service
public class PolicyCenterProductServiceImpl extends ServiceImpl<PolicyCenterProductMapper, PolicyCenterProduct>
        implements PolicyCenterProductService {
}
