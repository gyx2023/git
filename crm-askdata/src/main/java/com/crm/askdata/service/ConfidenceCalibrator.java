package com.crm.askdata.service;

import com.crm.askdata.config.HiAgentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * 置信度校准器
 * <p>
 * 陷阱防护：置信度膨胀陷阱
 * AI 模型往往过于自信，需要基于历史数据进行校准
 * <p>
 * 生产环境应加载训练好的校准模型 (如 Platt Scaling)
 * 当前使用简化版规则校准
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfidenceCalibrator {

    private final HiAgentProperties properties;

    /**
     * 校准模型 (生产环境应加载训练好的模型)
     */
    private Object calibrationModel;

    @PostConstruct
    public void init() {
        if (properties.getCalibration().isEnabled()) {
            String modelPath = properties.getCalibration().getModelPath();
            File modelFile = new File(modelPath);

            if (modelFile.exists()) {
                // 加载训练好的模型
                // calibrationModel = loadModel(modelPath);
                log.info("Confidence calibrator initialized with model: {}", modelPath);
            } else {
                log.warn("Calibration model not found at {}, using rule-based calibration", modelPath);
            }
        }
    }

    /**
     * 校准置信度
     *
     * @param rawConfidence 原始置信度
     * @param intentType    意图类型
     * @param queryLength   查询长度
     * @return 校准后的置信度
     */
    public double calibrate(double rawConfidence, String intentType, int queryLength) {
        if (!properties.getCalibration().isEnabled()) {
            return rawConfidence;
        }

        double calibrated = rawConfidence;

        // 1. 短查询惩罚 (短查询往往置信度虚高)
        if (queryLength < properties.getCalibration().getShortQueryLengthThreshold()) {
            calibrated *= properties.getCalibration().getShortQueryPenalty();
            log.debug("Short query penalty applied: {} -> {}", rawConfidence, calibrated);
        }

        // 2. 未知意图上限 (未知意图置信度不应过高)
        if ("unknown".equals(intentType)) {
            calibrated = Math.min(calibrated, 0.5);
            log.debug("Unknown intent cap applied: {} -> {}", rawConfidence, calibrated);
        }

        // 3. 边界值处理
        calibrated = Math.max(
            properties.getCalibration().getMinConfidence(),
            Math.min(properties.getCalibration().getMaxConfidence(), calibrated)
        );

        // 4. 保留 2 位小数
        calibrated = Math.round(calibrated * 100.0) / 100.0;

        return calibrated;
    }

    /**
     * 记录校准指标 (用于后续模型训练)
     *
     * @param rawConfidence        原始置信度
     * @param calibratedConfidence 校准后置信度
     * @param actualCorrect        实际是否正确
     * @param intentType           意图类型
     */
    public void recordCalibrationData(double rawConfidence, double calibratedConfidence,
                                      boolean actualCorrect, String intentType) {
        // 生产环境应记录到数据库，用于定期重新训练校准模型
        log.info("Calibration data: raw={}, calibrated={}, correct={}, intent={}",
            rawConfidence, calibratedConfidence, actualCorrect, intentType);
    }
}
