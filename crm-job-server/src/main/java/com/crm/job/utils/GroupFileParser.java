package com.crm.job.utils;

import com.crm.common.utils.file.FieldAnnotaionUtil;
import com.crm.system.domain.grp.GrpGroupInfoDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 集团文件解析工具类
 * 用于将文件内容解析为GrpGroupInfoDO对象列表
 *
 * @author crm
 */
@Slf4j
@Component
public class GroupFileParser {

    /**
     * 解析CSV格式的集团信息文件
     * 假设文件格式为：grpId,grpName,grpCode,custid,grpLevel,grpStat,isFocus,grpDept,areaid,countyid,...
     *
     * @param fileContent 文件内容
     * @return 集团信息列表
     */
    public List<GrpGroupInfoDO> parseCsvFile(String fileContent) {
        List<GrpGroupInfoDO> groupInfoList = new ArrayList<>();

        if (fileContent == null || fileContent.trim().isEmpty()) {
            log.warn("文件内容为空，无法解析");
            return groupInfoList;
        }

        String[] lines = fileContent.split("\n");
        log.info("开始解析集团信息文件，总行数: {}", lines.length);

        // 处理txt文件的每一行数据，不跳过第一行
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                GrpGroupInfoDO groupInfo = parseLine(line);
                if (groupInfo != null) {
                    groupInfoList.add(groupInfo);
                }
            } catch (Exception e) {
                log.error("解析第{}行数据失败: {}", i + 1, line, e);
            }
        }

        log.info("文件解析完成，成功解析集团信息数量: {}", groupInfoList.size());
        return groupInfoList;
    }

    /**
     * 解析单行数据
     *
     * @param line 单行数据
     * @return 集团信息对象
     */
    private GrpGroupInfoDO parseLine(String line) {


        GrpGroupInfoDO groupInfo = new GrpGroupInfoDO();
        // 根据字段索引设置对应的值
        try {
            FieldAnnotaionUtil.setFiledValFromValuesByOrder(groupInfo, line.split("\\x1b", -1), 0);
        } catch (Exception e) {
            log.error("解析字段值失败", e);
            throw e;
        }
        return groupInfo;
    }

    /**
     * 批量处理集团信息，用于分批插入
     *
     * @param groupInfoList 完整的集团信息列表
     * @param batchSize     每批大小
     * @return 分批后的集团信息列表
     */
    public List<List<GrpGroupInfoDO>> batchProcess(List<GrpGroupInfoDO> groupInfoList, int batchSize) {
        List<List<GrpGroupInfoDO>> batches = new ArrayList<>();

        for (int i = 0; i < groupInfoList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, groupInfoList.size());
            List<GrpGroupInfoDO> batch = groupInfoList.subList(i, endIndex);
            batches.add(new ArrayList<>(batch));
        }

        return batches;
    }
}
