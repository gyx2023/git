package com.crm.common.utils;

import com.crm.common.annotation.FieldAnnotaion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FieldAnnotaionUtil {

    /**
     * 反射操作耗时 缓存
     */
    private static ConcurrentHashMap<Class<?>, Field[]> classFieldsMap = new ConcurrentHashMap<>();

    /**
     * 向指定对象的属性中插入值
     */
    @SuppressWarnings("rawtypes")
    private static boolean setFieldValueFromObject(Object obj, Field field, String val) {
        boolean flag = true;
        try {
            boolean accessFlag = field.isAccessible();
            if (!accessFlag) {
                field.setAccessible(true);
            }
            Class t = field.getType();
            //包装类型
            if (t == Character.class) {
                //ok
                field.set(obj, (isBlank(val) ? null : val.charAt(0)));
            } else if (t == Boolean.class) {
                //自动支持 "" "asds" null
                field.set(obj, Boolean.valueOf(val));
            } else if (t == Byte.class) {
                field.set(obj, (isBlank(val) ? null : Byte.valueOf(val)));
            } else if (t == Short.class) {
                field.set(obj, (isBlank(val) ? null : Short.valueOf(val)));
            } else if (t == Integer.class) {
                field.set(obj, (isBlank(val) ? null : Integer.valueOf(val)));
            } else if (t == Long.class) {
                field.set(obj, (isBlank(val) ? null : Long.valueOf(val)));
            } else if (t == Double.class) {
                field.set(obj, (isBlank(val) ? null : Double.valueOf(val)));
            } else if (t == Float.class) {
                field.set(obj, (isBlank(val) ? null : Float.valueOf(val)));
            } else if (t == BigDecimal.class) {
                //大数据类型
                field.set(obj, (isBlank(val) ? null : new BigDecimal(val)));
            } else if (t == Date.class) {
                FieldAnnotaion dv = field.getAnnotation(FieldAnnotaion.class);
                if (dv == null) {
                    // 不使用注解时 慎用yyyyMMdd放在最后
                    if(StringUtils.isNotEmpty(val)){
                        Date date = DateUtils.parseDate(val, "yyyyMMddHHmmss", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss");
                        field.set(obj, date);
                    }else{
                        field.set(obj, null);
                    }
                } else {
                    String format = dv.dateFormat();
                    if (isBlank(val) || isBlank(format)) {
                        field.set(obj, null);
                    } else {
                        Date date = parseDate(val, format);
                        field.set(obj, date);
                    }
                }
                //基本类型
            } else if (t == char.class) {
                field.setChar(obj, (val == null ? null : val.charAt(0)));
            } else if (t == boolean.class) {
                //自动支持 "" "asds" null
                field.setBoolean(obj, Boolean.parseBoolean(val));
            } else if (t == byte.class) {
                field.setByte(obj, Byte.parseByte(val));
            } else if (t == short.class) {
                field.setShort(obj, Short.parseShort(val));
            } else if (t == int.class) {
                field.setInt(obj, Integer.parseInt(val));
            } else if (t == long.class) {
                field.setLong(obj, Long.parseLong(val));
            } else if (t == double.class) {
                field.setDouble(obj, Double.parseDouble(val));
            } else if (t == float.class) {
                field.setFloat(obj, Float.parseFloat(val));
            } else {//其他对象类型要再次追加 把val转换为 相应对象
                field.set(obj, val);
            }

            if (!accessFlag) {
                field.setAccessible(false);
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    private static boolean isBlank(String val) {
        return StringUtils.isBlank(val);
    }

    public static Date parseDate(String dateStr, String format) {
        try {
            if ((dateStr == null) || dateStr.isEmpty()) {
                return null;
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            return simpleDateFormat.parse(dateStr);
        } catch (Exception e) {
            return new Date();
        }
    }

    /**
     * 按自然顺序赋值
     *
     * @param obj    目标对象
     * @param values 值数组
     * @return 是否成功
     */
    public static boolean setFiledValFromValuesByOrder(Object obj, String[] values) {
        return setFiledValFromValuesByOrder(obj, values, 0);
    }

    /**
     * 按自然顺序赋值(跳过前extraColumn个字段)
     *
     * @param obj         目标对象
     * @param values      值数组
     * @param extraColumn 需要跳过的字段数(如id、创建时间等额外字段)
     * @return 是否成功
     */
    public static boolean setFiledValFromValuesByOrder(Object obj, String[] values, Integer extraColumn) {
        if (null == obj) return false;
        if (null == values || values.length < 1) return false;
        if (extraColumn == null) extraColumn = 0;

        boolean flag = true;
        Class<?> clazz = obj.getClass();
        Field[] fields = classFieldsMap.get(clazz);
        if (fields == null) {
            fields = clazz.getDeclaredFields();
            classFieldsMap.put(clazz, fields);
        }
        if (fields.length < 1) return false;

        // 验证字段数与values个数是否匹配(考虑跳过的字段)
        if (fields.length != values.length + extraColumn) {
            log.error("字段数与values个数不一致, fields.length={}, values.length={}, extraColumn={}",
                    fields.length, values.length, extraColumn);
            return false;
        }

        log.info("===开始赋值===");
        for (int i = 0; i < values.length; i++) {
            Field f = fields[i];
            if (!setFieldValueFromObject(obj, f, values[i])) {
                log.error("value在插入属性时因类型不一致导致异常, field={}, value={}", f.getName(), values[i]);
                return false;
            }
        }
        log.info("===赋值结束===");
        return flag;
    }
}
