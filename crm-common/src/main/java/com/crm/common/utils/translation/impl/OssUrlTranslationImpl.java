package com.crm.common.utils.translation.impl;

import com.crm.common.annotation.TranslationType;
import com.crm.common.constant.TransConstant;
import com.crm.common.core.service.OssService;
import com.crm.common.utils.translation.TranslationInterface;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * OSS翻译实现
 *
 * @author Lion Li
 */
@Component
@AllArgsConstructor
@TranslationType(type = TransConstant.OSS_ID_TO_URL)
public class OssUrlTranslationImpl implements TranslationInterface<String> {

    private final OssService ossService;

    @Override
    public String translation(Object key, String other) {
        return ossService.selectUrlByIds(key.toString());
    }
}
