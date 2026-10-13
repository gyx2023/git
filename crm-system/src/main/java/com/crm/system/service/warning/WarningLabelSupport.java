package com.crm.system.service.warning;

import com.crm.system.service.sys.ISysCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 预警明细「闭环标识 / 预警等级」中文标签转换（字典驱动）。
 * <p>
 * 这两个字段的标签口径只属于<b>客户级续约类预警</b>一族（旧系统 000030 红名单到期、
 * 000031 团购折扣、000033 终端合约），只有它们的列表页带这两列：
 * <ul>
 *   <li>000030 → {@code userCustWarningHMDList.jsp}</li>
 *   <li>000031 → {@code userCustWarningGbwList.jsp}</li>
 *   <li>000033 → {@code userCustWarningTcList.jsp}</li>
 * </ul>
 * 特殊活动(000032)、沃TV(C00024)、宽带装机地址(C00023) 的列表页没有这两列，不需要用本类。
 * </p>
 * <p>
 * 文案取自 {@code sys_code} 字典（{@code type_='cycle_flag'} / {@code type_='warning_level'}，
 * 显示值在 {@code name_}）—— 与旧系统同表同源：旧列表页走 {@code dict:codeLabel}，
 * 旧导出走 {@code sysCodeService.findNameByCode(type, code)}，读的都是这张表。
 * 改文案只需改字典，无需改代码或重启（缓存 5 分钟自动过期）。
 * </p>
 * <p>
 * 取值优先级：<b>字典 name_ &gt; 内置兜底文案 &gt; 原始码</b>。
 * 旧系统字典查不到时渲染空串，本类改为逐级回退，避免页面出现空白或裸码；
 * 内置兜底仅覆盖已知码（0~3 / 1~3），越界码最终原样展示，便于排查脏数据。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarningLabelSupport {

    private static final String DICT_CYCLE_FLAG = "cycle_flag";
    private static final String DICT_WARNING_LEVEL = "warning_level";

    /** 字典缓存有效期（毫秒）：到期后下次调用重新加载，避免改字典必须重启 */
    private static final long DICT_TTL_MS = 5 * 60 * 1000L;

    /**
     * 闭环标识兜底文案：0:未闭环 1:续约 2:离网 3:超时。
     * <p>依据旧页面 {@code userCustWarningHMDList.jsp:70-72} 与 {@code UserCustWarningAction:617}
     * 的 CASE 表达式（旧页面 0 无分支、渲染为空，此处补「未闭环」以免整列空白）。</p>
     */
    private static final Map<String, String> CYCLE_FLAG_DEFAULTS = new LinkedHashMap<String, String>() {{
        put("0", "未闭环");
        put("1", "续约");
        put("2", "离网");
        put("3", "超时");
    }};

    /**
     * 预警等级兜底文案：1:Ⅰ级 2:Ⅱ级 3:Ⅲ级。
     * <p>依据旧页面 {@code userCustWarningHMDList.jsp:75-77} 与 {@code UserCustWarningAction:617}。</p>
     */
    private static final Map<String, String> WARNING_LEVEL_DEFAULTS = new LinkedHashMap<String, String>() {{
        put("1", "Ⅰ级");
        put("2", "Ⅱ级");
        put("3", "Ⅲ级");
    }};

    private final ISysCodeService sysCodeService;

    private volatile Map<String, String> cycleFlagDict;
    private volatile Map<String, String> warningLevelDict;
    private volatile long dictLoadedAt;

    /**
     * 闭环标识 → 中文标签。
     *
     * @param cycleFlag 原始码，可为 null
     * @return 中文标签；null/空返回 ""
     */
    public String cycleFlagText(Object cycleFlag) {
        return toText(cycleFlagDict(), CYCLE_FLAG_DEFAULTS, toCode(cycleFlag));
    }

    /**
     * 预警等级 → 中文标签。
     *
     * @param warningLevel 原始码，可为 null
     * @return 中文标签；null/空返回 ""
     */
    public String warningLevelText(Object warningLevel) {
        return toText(warningLevelDict(), WARNING_LEVEL_DEFAULTS, toCode(warningLevel));
    }

    /**
     * 一次性把闭环标识/预警等级写入明细记录 Map，键名与前端表格、导出 VO 的约定一致：
     * {@code cycleFlag / cycleFlagText / warningLevel / warningLevelText}。
     *
     * @param record       明细记录 Map（SQL 行转换后的结果）
     * @param cycleFlag    预警行的 CYCLE_FLAG 原始值
     * @param warningLevel 预警行的 WARNING_LEVEL 原始值
     */
    public void putLabels(Map<String, Object> record, Object cycleFlag, Object warningLevel) {
        String cycleFlagCode = toCode(cycleFlag);
        String levelCode = toCode(warningLevel);
        record.put("cycleFlag", cycleFlagCode);
        record.put("cycleFlagText", toText(cycleFlagDict(), CYCLE_FLAG_DEFAULTS, cycleFlagCode));
        record.put("warningLevel", levelCode);
        record.put("warningLevelText", toText(warningLevelDict(), WARNING_LEVEL_DEFAULTS, levelCode));
    }

    // ==================== 字典加载 ====================

    private Map<String, String> cycleFlagDict() {
        refreshIfExpired();
        return cycleFlagDict;
    }

    private Map<String, String> warningLevelDict() {
        refreshIfExpired();
        return warningLevelDict;
    }

    /** 缓存为空或已过期则重新加载。并发下可能重复加载，结果一致，不做加锁。 */
    private void refreshIfExpired() {
        long now = System.currentTimeMillis();
        if (cycleFlagDict != null && warningLevelDict != null && now - dictLoadedAt <= DICT_TTL_MS) {
            return;
        }
        cycleFlagDict = loadDict(DICT_CYCLE_FLAG);
        warningLevelDict = loadDict(DICT_WARNING_LEVEL);
        dictLoadedAt = System.currentTimeMillis();
    }

    /** 加载失败返回空 Map，走兜底文案，不影响主流程 */
    private Map<String, String> loadDict(String type) {
        try {
            Map<String, String> dict = sysCodeService.getCodeMapByType(type);
            return dict != null ? dict : Collections.<String, String>emptyMap();
        } catch (Exception e) {
            log.warn("【预警标签】加载 sys_code 字典失败, type={}", type, e);
            return Collections.emptyMap();
        }
    }

    // ==================== 取值 ====================

    private String toCode(Object v) {
        return v == null ? null : v.toString();
    }

    /** 字典 name_ > 内置兜底 > 原始码；null/空返回空串 */
    private String toText(Map<String, String> dict, Map<String, String> defaults, String code) {
        if (code == null || code.isEmpty()) {
            return "";
        }
        String name = dict.get(code);
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return defaults.getOrDefault(code, code);
    }
}
