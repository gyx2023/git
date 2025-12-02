package com.crm.common.annotation;

import java.lang.annotation.*;

/**
 * @author wuxiaoxu 20131012
 * @see
 * 	required 字段是否为必填项注解
 * 	index 字段的下标 从0开始
 */
@Documented//说明该注解将被包含在javadoc中
@Target(ElementType.FIELD)//字段、枚举的常量
@Retention(RetentionPolicy.RUNTIME)//编译后保存在字节码中
public @interface FieldAnnotaion {

	/**DataValidation
	 * @author wuxiaoxu 20131012
	 * @see 用于校验属性是否为null
	 * 	true  必填项
	 * 	false 非必填
	 * @return
	 */
	boolean required() default false; // 定义"作者"参数

	/**
	 * @author wuxiaoxu 20131012
	 * @see 字段的排列数序，下标从0开始 小于0时不会插入数据。
	 * @return
	 */
	int index() default -1;

	/**
	 * @author wuxiaoxu 20131022
	 * @see 当Field为Date类型时，dateFormat作为format转换为Date
	 * 例子：“yyyyMMddHHmmss”
	 * @return
	 */
	String dateFormat() default "";
}
