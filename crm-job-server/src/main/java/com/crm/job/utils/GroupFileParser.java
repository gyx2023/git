package com.crm.job.utils;

import com.crm.common.utils.file.FieldAnnotaionUtil;
import com.crm.system.domain.grp.GrpGroupInfoDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件解析通用工具类
 * 用于将文件内容解析为指定类型的对象列表
 *
 * @author crm
 */
@Slf4j
@Component
public class GroupFileParser {

    /**
     * 通用文件解析方法
     * 支持解析不同类型的DO对象
     *
     * @param fileContent 文件内容
     * @param clazz       要解析成的对象类型
     * @param delimiter   字段分隔符
     * @param <T>         泛型类型
     * @return 解析后的对象列表
     */
    public <T> List<T> parseFile(String fileContent, Class<T> clazz, String delimiter) {
        List<T> resultList = new ArrayList<>();

        if (fileContent == null || fileContent.trim().isEmpty()) {
            log.warn("文件内容为空，无法解析");
            return resultList;
        }

        String[] lines = fileContent.split("\n");
        log.info("开始解析文件，总行数: {}", lines.length);

        // 处理文件的每一行数据
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                T obj = parseLine(line, clazz, delimiter);
                if (obj != null) {
                    resultList.add(obj);
                }
            } catch (Exception e) {
                log.error("解析第{}行数据失败: {}", i + 1, line, e);
            }
        }

        log.info("文件解析完成，成功解析对象数量: {}", resultList.size());
        return resultList;
    }



    /**
     * 解析单行数据为指定类型的对象
     *
     * @param line      单行数据
     * @param clazz     要解析成的对象类型
     * @param delimiter 字段分隔符
     * @param <T>       泛型类型
     * @return 解析后的对象
     */
    private <T> T parseLine(String line, Class<T> clazz, String delimiter) {
        try {
            // 创建目标对象实例
            T obj = clazz.getDeclaredConstructor().newInstance();

            // 使用FieldAnnotaionUtil工具类根据字段索引设置对应的值
            String[] values = line.split(delimiter, -1);
            FieldAnnotaionUtil.setFiledValFromValuesByOrder(obj, values, 0);

            return obj;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("创建对象实例失败: {}", clazz.getName(), e);
            throw new RuntimeException("创建对象实例失败", e);
        } catch (Exception e) {
            log.error("解析字段值失败", e);
            throw new RuntimeException("解析字段值失败", e);
        }
    }
}
