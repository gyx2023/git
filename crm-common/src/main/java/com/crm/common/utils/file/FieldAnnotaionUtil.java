package com.crm.common.utils.file;


import com.crm.common.annotation.FieldAnnotaion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class FieldAnnotaionUtil {

    private static final Logger log = LoggerFactory.getLogger(FieldAnnotaionUtil.class);

    /**
     * 验证FieldValidation注解字段的正确性
     *
     * @param obj
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean fieldValidationCheck(Object obj) {
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();//获得所有字段
        for (Field f : fields) {
            if (f.isAnnotationPresent(FieldAnnotaion.class)) {
                FieldAnnotaion dv = f.getAnnotation(FieldAnnotaion.class);
                if (dv.required()) {//必填字段
                    if (null == getFieldValueFromObject(obj, f)) {//判断必填项是否为null
                        log.info("存在必填字段为空，字段名称：" + f.getName());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 从指定对象中获取属性的值
     *
     * @param obj
     * @param field
     * @return
     * @author wuxiaoxu 20131012
     */
    private static Object getFieldValueFromObject(Object obj, Field field) {
        Object val = null;
        try {
            if (field.isAccessible()) {
                val = field.get(obj);
            } else {
                field.setAccessible(true);
                val = field.get(obj);
                field.setAccessible(false);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return val;
    }


    /**
     * 向指定对象的属性中插入值
     *
     * @param obj
     * @param field
     * @return
     * @author wuxiaoxu 20131012
     */
    @SuppressWarnings("rawtypes")
    private static boolean setFieldValueFromObject(Object obj, Field field, String val) {
//		if(StringUtils.isBlank(val))return true;//当为null 或 "  "时不修改数据暂不使用此句
        boolean flag = true;
        try {
            boolean accessFlag = field.isAccessible();
            if (!accessFlag) {
                field.setAccessible(true);
            }
            Class t = field.getType();
            //包装类型
            if (t == Character.class) {//ok
                field.set(obj, (isBlank(val) ? null : val.charAt(0)));
            } else if (t == Boolean.class) {
                field.set(obj, Boolean.valueOf(val));//自动支持 "" "asds" null
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
            } else if (t == BigDecimal.class) {//大数据类型
                field.set(obj, (isBlank(val) ? null : new BigDecimal(val)));
            } else if (t == Date.class) {//
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
                        Date date = DateUtils.parseDate(val, format);
                        field.set(obj, date);
                    }
                }
                //基本类型
            } else if (t == char.class) {
                field.setChar(obj, (val == null ? null : val.charAt(0)));
            } else if (t == boolean.class) {//自动支持 "" "asds" null
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


    /**
     * 把valuse根据其下标插入到存在注解Field的域中且values的下标跟注解中的index向对应
     *
     * @param obj
     * @param values
     * @return
     * @author wuxiaoxu 20131012
     */
    @SuppressWarnings("rawtypes")
    public static boolean setFiledValFromValuesByIndex(Object obj, String[] values) {
        if (null == obj) return false;
        if (null == values || values.length < 1) return false;

        boolean flag = true;
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();//获得所有字段
        if (fields.length < 1) return false;

        Map<Integer, Field> indexFieldMap = new HashMap<Integer, Field>();
        for (Field f : fields) {
            if (f.isAnnotationPresent(FieldAnnotaion.class)) {
                FieldAnnotaion dv = f.getAnnotation(FieldAnnotaion.class);
                int i = dv.index();
                if (i >= 0) {
                    if (null != indexFieldMap.get(i)) {
                        System.out.println("error---------------注解index重复");
                        return false;
                    } else {
                        indexFieldMap.put(i, f);
                    }
                }
            }
        }

        if (indexFieldMap.size() != values.length) {
            System.out.println("error---------------字段数与values个数不一致");
            return false;
        }
        for (int i = 0; i < values.length; i++) {
            Field f = indexFieldMap.get(i);
            if (!setFieldValueFromObject(obj, f, values[i])) {
                System.out.println("error---------------value在插入属性时因类型不一致导致异常");
                return false;
            }
        }
        return flag;
    }


    private static boolean isBlank(String val) {
        return StringUtils.isBlank(val);
    }

    /**
     * 按自然顺序赋值
     *
     * @param obj
     * @param values
     * @return
     */
    public static boolean setFiledValFromValuesByOrder(Object obj, String[] values) {
        return setFiledValFromValuesByOrder(obj, values, 0);
    }


    /**
     * 按自然顺序赋值
     *
     * @param obj
     * @param values
     * @param extraColumn 数据库额外字段数 如id、创建时间
     * @return
     */
    public static boolean setFiledValFromValuesByOrder(Object obj, String[] values, Integer extraColumn) {
        if (null == obj) return false;
        if (null == values || values.length < 1) return false;
        boolean flag = true;
        Class<?> clazz = obj.getClass();
        Field[] fields = classFieldsMap.get(clazz);
        if (fields == null) {
            fields = clazz.getDeclaredFields();//获得所有字段
            classFieldsMap.put(clazz, fields);
        }
        if (fields.length < 1) return false;
//        if (fields.length != values.length + extraColumn) {
//            System.out.println("error---------------字段数与values个数不一致");
//            return false;
//        }
        log.info("开始赋值==============");
        for (int i = 0; i < values.length; i++) {
            log.info("第" + i + " 个字段赋值:" +values[i]);
            Field f = fields[i];
            if (!setFieldValueFromObject(obj, f, values[i])) {
                log.info("error---------------value在插入属性时因类型不一致导致异常");
                return false;
            }
        }
        log.info("赋值完毕==============");
        return flag;
    }

    /**
     * 反射操作耗时 缓存
     */
    private static ConcurrentHashMap<Class<?>, Field[]> classFieldsMap = new ConcurrentHashMap<>();
}
