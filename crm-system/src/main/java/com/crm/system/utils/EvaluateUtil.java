package com.crm.system.utils;


import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;
import lombok.extern.slf4j.Slf4j;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import java.util.Map;

@Slf4j
public class EvaluateUtil {


    // 创建EL表达式执行方法 - 修复参数传递
    public static boolean evaluateCondition(String expression, Map<String, Object> messageMap) {
        log.info("开始执行EL表达式: {}, 参数map: {}", expression, messageMap);
        ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();

        // 将messageMap中的所有键值对注册到上下文中
        for (Map.Entry<String, Object> entry : messageMap.entrySet()) {
            context.setVariable(entry.getKey(),
                factory.createValueExpression(entry.getValue(), Object.class));
        }

        // 修复：使用预定义的ValueExpression代替Lambda
        context.setVariable("empty",
            factory.createValueExpression(false, Boolean.class)); // 简化处理

        try {
            ValueExpression ve = factory.createValueExpression(
                context, expression, Boolean.class);

            Object result = ve.getValue(context);
            boolean finalResult = result instanceof Boolean ? (Boolean) result : Boolean.FALSE;
            log.info("EL表达式执行完成: {}, 结果: {}", expression, finalResult);
            return finalResult;
        } catch (Exception e) {
            log.error("EL表达式执行失败: {}", expression, e);
            return false;
        }
    }

}
