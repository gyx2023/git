package com.crm.system.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.crm.system.domain.Sequence;
import com.crm.system.mapper.SequenceMapper;
import com.crm.system.service.ISequenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class SequenceServiceImpl implements ISequenceService {
    @Resource
    private SequenceMapper mapper;


    @Transactional
    @Override
    public String getCode(String seqName) {
        String searchDate = DateUtil.formatDate(new Date()).replaceAll("-", "");
        mapper.update(null, new LambdaUpdateWrapper<Sequence>()
                .set(Sequence::getIncrementVal, 1)
                .eq(Sequence::getSeqName, seqName));
        Sequence seq = mapper.selectOne(new LambdaUpdateWrapper<Sequence>()
                .eq(Sequence::getSeqName, seqName));
        int currentVal = seq.getCurrentVal();
        if (searchDate.equals(seq.getSearchDate())) {
            currentVal = currentVal + 1;
            mapper.update(null, new LambdaUpdateWrapper<Sequence>()
                    .set(Sequence::getCurrentVal, currentVal)
                    .eq(Sequence::getSeqName, seqName));
        } else {
            currentVal = 1;
            mapper.update(null, new LambdaUpdateWrapper<Sequence>()
                    .set(Sequence::getCurrentVal, currentVal)
                    .set(Sequence::getSearchDate, searchDate)
                    .eq(Sequence::getSeqName, seqName));
        }
        String str = String.format("%04d", currentVal);
        return seqName + "-" + searchDate + "-" + str;
    }
}
