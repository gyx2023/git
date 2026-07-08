package com.crm.framework.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import com.crm.common.annotation.DataColumn;
import com.crm.common.annotation.DataPermission;
import com.crm.common.core.domain.dto.RoleDTO;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.enums.DataScopeType;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.helper.DataPermissionHelper;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.common.utils.StreamUtils;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.spring.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据权限过滤
 *
 * @author Lion Li
 * @version 3.5.0
 */
@Slf4j
public class PlusDataPermissionHandler {

    /**
     * 方法或类(名称) 与 注解的映射关系缓存
     */
    private final Map<String, DataPermission> dataPermissionCacheMap = new ConcurrentHashMap<>();

    /**
     * 无效注解方法缓存用于快速返回
     */
    private final Set<String> invalidCacheSet = new ConcurrentHashSet<>();

    /**
     * spel 解析器
     */
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParserContext parserContext = new TemplateParserContext();
    /**
     * bean解析器 用于处理 spel 表达式中对 bean 的调用
     */
    private final BeanResolver beanResolver = new BeanFactoryResolver(SpringUtils.getBeanFactory());


    public Expression getSqlSegment(Expression where, String mappedStatementId, boolean isSelect) {
        DataColumn[] dataColumns = findAnnotation(mappedStatementId);
        if (ArrayUtil.isEmpty(dataColumns)) {
            invalidCacheSet.add(mappedStatementId);
            return where;
        }
        LoginUser currentUser = null;
        try {
            currentUser = DataPermissionHelper.getVariable("user");
        } catch (Exception e) {
            // 非Web上下文，忽略
        }
        if (ObjectUtil.isNull(currentUser)) {
            try {
                currentUser = LoginHelper.getLoginUser();
                DataPermissionHelper.setVariable("user", currentUser);
            } catch (Exception e) {
                // 非Web上下文（定时任务等场景），无法获取登录信息，跳过数据权限控制
                return where;
            }
        }
        // 如果没有登录信息，则不进行数据权限控制（定时任务等场景）
        if (ObjectUtil.isNull(currentUser)) {
            return where;
        }
        // 如果是超级管理员，则不过滤数据
        if (LoginHelper.isAdmin()) {
            return where;
        }
        String dataFilterSql = buildDataFilter(dataColumns, isSelect);
        if (StringUtils.isBlank(dataFilterSql)) {
            return where;
        }
        try {
            Expression expression = CCJSqlParserUtil.parseExpression(dataFilterSql);
            // 数据权限使用单独的括号 防止与其他条件冲突
            Parenthesis parenthesis = new Parenthesis(expression);
            if (ObjectUtil.isNotNull(where)) {
                return new AndExpression(where, parenthesis);
            } else {
                return parenthesis;
            }
        } catch (JSQLParserException e) {
            throw new ServiceException("数据权限解析异常 => " + e.getMessage());
        }
    }

    /**
     * 构造数据过滤sql
     */
    private String buildDataFilter(DataColumn[] dataColumns, boolean isSelect) {
        // 更新或删除需满足所有条件
        String joinStr = isSelect ? " OR " : " AND ";
        LoginUser user = DataPermissionHelper.getVariable("user");
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver(beanResolver);
        DataPermissionHelper.getContext().forEach(context::setVariable);
        Set<String> conditions = new HashSet<>();

        // 先检查是否有客户经理OA账号数据权限或通用特殊名单数据权限
        boolean hasCustManagerOaUserPermission = false;
        boolean hasSpecialListPermission = false;
        for (RoleDTO role : user.getRoles()) {
            DataScopeType type = DataScopeType.findCode(role.getDataScope());
            if (type == DataScopeType.CUST_MANAGER_OAUSER) {
                hasCustManagerOaUserPermission = true;
            }
            if (type == DataScopeType.SPECIAL_LIST) {
                hasSpecialListPermission = true;
            }
        }

        for (RoleDTO role : user.getRoles()) {
            user.setRoleId(role.getRoleId());
            // 获取角色权限泛型
            DataScopeType type = DataScopeType.findCode(role.getDataScope());
            if (ObjectUtil.isNull(type)) {
                throw new ServiceException("角色数据范围异常 => " + role.getDataScope());
            }
            // 全部数据权限直接返回(但如果存在客户经理OA账号权限或特殊名单权限则不返回)
            if (type == DataScopeType.ALL && !hasCustManagerOaUserPermission && !hasSpecialListPermission) {
                return "";
            }
            // 如果是全部数据权限但存在客户经理权限或特殊名单权限,跳过该角色继续处理
            if (type == DataScopeType.ALL && (hasCustManagerOaUserPermission || hasSpecialListPermission)) {
                continue;
            }
            // 通用特殊名单数据权限 → 看全部，直接返回
            if (type == DataScopeType.SPECIAL_LIST) {
                return "";
            }
            boolean isSuccess = false;
            for (DataColumn dataColumn : dataColumns) {
                if (dataColumn.key().length != dataColumn.value().length) {
                    throw new ServiceException("角色数据范围异常 => key与value长度不匹配");
                }
                // 不包含 key 变量 则不处理
                if (!StringUtils.containsAny(type.getSqlTemplate(),
                    Arrays.stream(dataColumn.key()).map(key -> "#" + key).toArray(String[]::new)
                )) {
                    continue;
                }
                // 设置注解变量 key 为表达式变量 value 为变量值
                for (int i = 0; i < dataColumn.key().length; i++) {
                    context.setVariable(dataColumn.key()[i], dataColumn.value()[i]);
                }

                // 解析sql模板并填充
                String sql = parser.parseExpression(type.getSqlTemplate(), parserContext).getValue(context, String.class);
                conditions.add(joinStr + sql);
                isSuccess = true;
            }
            // 未处理成功则填充兜底方案
            if (!isSuccess && StringUtils.isNotBlank(type.getElseSql())) {
                conditions.add(joinStr + type.getElseSql());
            }
        }

        if (CollUtil.isNotEmpty(conditions)) {
            // 如果只有兜底方案(如 1=0)，且用户没有全部/特殊名单权限，则用 create_by 兜底
            boolean allElseSql = conditions.stream().allMatch(c -> c.contains("1 = 0"));
            if (allElseSql && !hasSpecialListPermission) {
                for (DataColumn dataColumn : dataColumns) {
                    if ("createBy".equals(dataColumn.key()[0])) {
                        String defaultFilter = dataColumn.value()[0] + " = " + user.getUserId();
                        conditions.clear();
                        conditions.add(joinStr + defaultFilter);
                        break;
                    }
                }
            }
            String sql = StreamUtils.join(conditions, Function.identity(), "");
            return sql.substring(joinStr.length());
        }
        // 没有条件且无全部/特殊名单权限 → 默认仅看自己创建的
        if (!hasSpecialListPermission) {
            for (DataColumn dataColumn : dataColumns) {
                if ("createBy".equals(dataColumn.key()[0])) {
                    return dataColumn.value()[0] + " = " + user.getUserId();
                }
            }
        }
        return "";
    }

    private DataColumn[] findAnnotation(String mappedStatementId) {
        StringBuilder sb = new StringBuilder(mappedStatementId);
        int index = sb.lastIndexOf(".");
        String clazzName = sb.substring(0, index);
        String methodName = sb.substring(index + 1, sb.length());
        Class<?> clazz = ClassUtil.loadClass(clazzName);
        List<Method> methods = Arrays.stream(ClassUtil.getDeclaredMethods(clazz))
            .filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());
        DataPermission dataPermission;
        // 获取方法注解
        for (Method method : methods) {
            dataPermission = dataPermissionCacheMap.get(mappedStatementId);
            if (ObjectUtil.isNotNull(dataPermission)) {
                return dataPermission.value();
            }
            if (AnnotationUtil.hasAnnotation(method, DataPermission.class)) {
                dataPermission = AnnotationUtil.getAnnotation(method, DataPermission.class);
                dataPermissionCacheMap.put(mappedStatementId, dataPermission);
                return dataPermission.value();
            }
        }
        dataPermission = dataPermissionCacheMap.get(clazz.getName());
        if (ObjectUtil.isNotNull(dataPermission)) {
            return dataPermission.value();
        }
        // 获取类注解
        if (AnnotationUtil.hasAnnotation(clazz, DataPermission.class)) {
            dataPermission = AnnotationUtil.getAnnotation(clazz, DataPermission.class);
            dataPermissionCacheMap.put(clazz.getName(), dataPermission);
            return dataPermission.value();
        }
        return null;
    }

    /**
     * 是否为无效方法 无数据权限
     */
    public boolean isInvalid(String mappedStatementId) {
        return invalidCacheSet.contains(mappedStatementId);
    }
}
