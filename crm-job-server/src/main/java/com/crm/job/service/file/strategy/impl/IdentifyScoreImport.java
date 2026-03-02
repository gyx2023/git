package com.crm.job.service.file.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.common.utils.WordTextExtractor;
import com.crm.job.service.file.strategy.AbstractImportStrategy;
import com.crm.system.domain.SysOss;
import com.crm.system.domain.bo.identify.IdentifyScoreBo;
import com.crm.system.domain.bo.identify.IdentifyScoreCustomerBo;
import com.crm.system.domain.file.FileAsyncTaskDo;
import com.crm.system.domain.identify.IdentifyScoreCustomerDo;
import com.crm.system.domain.identify.IdentifyScoreDo;
import com.crm.system.mapper.SysOssMapper;
import com.crm.system.service.ISysOperLogService;
import com.crm.system.service.ISysOssService;
import com.crm.system.service.ISysUserService;
import com.crm.system.service.identify.IIdentifyScoreCustomerService;
import com.crm.system.service.identify.IIdentifyScoreService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 三清单评分导入策略实现类
 * 核心功能：从OSS读取Word文件 → 调用智能体接口解析评分 → 更新数据库评分数据
 *
 * @author 开发者
 * @date 2026-02-04
 */
@Component
@Slf4j
public class IdentifyScoreImport extends AbstractImportStrategy {

    // ========== 常量定义（抽离硬编码，便于维护） ==========
    /** 智能体接口地址 */
    @Value("${identify.score.api.url:https://132.151.50.33:15605/api/proxy/api/v1/sync_run_app_workflow}")
    private String identifyScoreApiUrl;

    /** 智能体接口API Key */
    @Value("${identify.score.api.key:d4ja6vea6n5ndl3ehi1g}")
    private String identifyScoreApiKey;

    /** OKHttp超时时间（秒） */
    private static final int OKHTTP_TIMEOUT_SECONDS = 120;

    /** 合计得分字段名 - 实际得分 */
    private static final String FIELD_ACTUAL_SCORE = "实际得分";

    /** 合计得分字段名 - 合计得分 */
    private static final String FIELD_TOTAL_SCORE = "合计得分";

    /** 合计字段名 */
    private static final String FIELD_TOTAL = "8.合计";

    /** 接口返回状态成功值 */
    private static final String API_STATUS_SUCCESS = "success";

    /** 节点类型-结束节点 */
    private static final String NODE_TYPE_END = "end";

    /** 操作日志-业务类型 */
    private static final int OPER_LOG_BIZ_TYPE = 3;

    /** 评分状态-成功 */
    private static final String SCORE_STATUS_SUCCESS = "1";

    /** 评分状态-失败 */
    private static final String SCORE_STATUS_FAILED = "2";

    // ========== 依赖注入 ==========
    @Resource
    private ISysOssService sysOssService;

    @Resource
    private SysOssMapper sysOssMapper;

    @Resource
    private ISysOperLogService sysOperLogService;

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private IIdentifyScoreService identifyScoreService;

    @Resource
    private IIdentifyScoreCustomerService identifyScoreCustomerService;

    // ========== OKHttp客户端（单例复用，避免重复创建） ==========
    private final OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
        .connectTimeout(OKHTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(OKHTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(OKHTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build();

    @Override
    protected boolean doImportTask(FileAsyncTaskDo task) {
        String fileUrl = task.getFileUrl();
        log.info("开始三清单导入任务，文件URL: {}", fileUrl);

        // 1. 获取OSS文件信息
        SysOss sysOss = getSysOssByFileUrl(fileUrl);
        if (sysOss == null) {
            String errorMsg = "处理失败：未找到对应的OSS文件记录，文件URL: " + fileUrl;
            task.setRemark(errorMsg);
            log.error(errorMsg);
            return false;
        }

        try {
            // 2. 获取创建人信息
            SysUser sysUser = sysUserService.selectUserById(Long.valueOf(sysOss.getCreateBy()));
            if (sysUser == null) {
                String errorMsg = "处理失败：未找到OSS文件创建人，创建人ID: " + sysOss.getCreateBy();
                task.setRemark(errorMsg);
                log.error(errorMsg);
                return false;
            }

            // 3. 读取文件内容
            InputStream fileContent = sysOssService.getFileContent(sysOss.getOssId());
            StringBuilder wordText = WordTextExtractor.readWordText((FileInputStream) fileContent, sysOss.getFileName());
            if (ObjectUtil.isEmpty(wordText)) {
                String errorMsg = "处理失败：Word文件内容为空，OSS ID: " + sysOss.getOssId();
                task.setRemark(errorMsg);
                log.error(errorMsg);
                return false;
            }

            // 4. 查询客户和评分数据
            IdentifyScoreCustomerDo customerDo = identifyScoreCustomerService.queryByFileId(sysOss.getOssId(), sysUser.getUserId());
            IdentifyScoreDo scoreDo = identifyScoreService.queryByCustomerId(customerDo.getId(), task.getTaskId());
            if (scoreDo == null) {
                String errorMsg = "处理失败：未找到评分记录，客户ID: " + customerDo.getId() + "，任务ID: " + task.getTaskId();
                task.setRemark(errorMsg);
                log.error(errorMsg);
                return false;
            }

            // 5. 调用智能体接口解析评分
            Map<String, Object> scoreResultMap = queryFileByScore(wordText.toString(), sysUser);

            // 6. 更新评分数据
            updateScoreData(customerDo, scoreDo, scoreResultMap);
            log.info("三清单导入任务完成，OSS ID: {}，客户ID: {}", sysOss.getOssId(), customerDo.getId());
            return true;

        } catch (Exception e) {
            String errorMsg = "三清单导入任务异常，文件URL: " + fileUrl;
            log.error(errorMsg, e);
            task.setRemark(errorMsg + "，异常信息：" + e.getMessage());

            // 异常时更新评分状态为失败
            try {
                IdentifyScoreCustomerDo customerDo = identifyScoreCustomerService.queryByFileId(sysOss.getOssId(),
                    Long.valueOf(sysOss.getCreateBy()));
                IdentifyScoreDo scoreDo = identifyScoreService.queryByCustomerId(customerDo.getId(), task.getTaskId());
                if (scoreDo != null) {
                    scoreDo.setScoreStatus(SCORE_STATUS_FAILED);
                    scoreDo.setUpdateTime(new Date());
                    identifyScoreService.updateByBo(BeanCopyUtils.copy(scoreDo, IdentifyScoreBo.class));
                }
            } catch (Exception ex) {
                log.error("更新评分失败状态异常", ex);
            }
            return false;
        }
    }

    @Override
    public String getBusinessType() {
        return "identify_score_import";
    }

    /**
     * 根据文件URL获取OSS文件信息
     * 1. 精确匹配URL
     * 2. 若包含查询参数，去除后再次匹配
     * 3. 匹配失败则打印所有存在的URL便于调试
     */
    private SysOss getSysOssByFileUrl(String fileUrl) {
        // 1. 精确匹配
        SysOss sysOss = sysOssMapper.selectOne(new LambdaQueryWrapper<SysOss>()
            .eq(SysOss::getUrl, fileUrl));

        // 2. 去除查询参数后匹配
        if (sysOss == null && StringUtils.hasText(fileUrl) && fileUrl.contains("?")) {
            String baseUrl = fileUrl.substring(0, fileUrl.indexOf("?"));
            log.info("完整URL未找到记录，尝试使用基础URL查询: {}", baseUrl);
            sysOss = sysOssMapper.selectOne(new LambdaQueryWrapper<SysOss>()
                .eq(SysOss::getUrl, baseUrl));
        }

        // 3. 匹配失败，打印调试信息
        if (sysOss == null) {
            List<SysOss> allOssRecords = sysOssMapper.selectList(new LambdaQueryWrapper<SysOss>().isNotNull(SysOss::getUrl));
            log.info("未找到对应的OSS文件记录，文件URL: {}，系统中存在的URL记录数量: {}",
                fileUrl, allOssRecords.size());
            // 批量打印，避免循环打印过多日志
            log.info("系统中存在的URL列表: {}", allOssRecords.stream()
                .map(SysOss::getUrl));
        }

        return sysOss;
    }

    /**
     * 调用智能体接口解析文件评分
     */
    public Map<String, Object> queryFileByScore(String inputContent, SysUser sysUser) {
        Map<String, Object> resultMap = new HashMap<>(2); // 初始容量2，减少扩容

        try {
            // 1. 调用智能体接口
            String responseStr = syncReadFileFlowRequest(inputContent, sysUser);
            if (ObjectUtil.isEmpty(responseStr)) {
                throw new RuntimeException("智能体接口返回为空");
            }

            // 2. 解析接口返回结果
            JSONObject responseJson = JSON.parseObject(responseStr);
            String status = responseJson.getString("status");
            if (!API_STATUS_SUCCESS.equals(status)) {
                throw new RuntimeException("接口返回状态异常，status: " + status);
            }

            // 3. 提取end节点的output内容
            JSONObject nodes = responseJson.getJSONObject("nodes");
            if (nodes == null) {
                throw new RuntimeException("接口返回的nodes为空");
            }

            String content = extractEndNodeOutput(nodes);
            if (StrUtil.isBlank(content)) {
                throw new RuntimeException("未从end节点提取到有效内容");
            }

            // 4. 解析评分数据
            JSONObject scoreJson = JSON.parseObject(content);
            log.info("三清单返回内容: {}", scoreJson);
            resultMap.put("content", scoreJson);

            // 5. 提取合计得分
            String totalScore = extractTotalScore(scoreJson);
            resultMap.put("totalScore", totalScore);

            return resultMap;

        } catch (Exception e) {
            log.error("解析文件评分失败", e);
            throw new RuntimeException("解析文件评分失败：" + e.getMessage(), e);
        }
    }

    /**
     * 从end节点提取output内容
     */
    private String extractEndNodeOutput(JSONObject nodes) {
        Set<String> keySet = nodes.keySet();
        for (String key : keySet) {
            JSONObject nodeObj = nodes.getJSONObject(key);
            String nodeType = nodeObj.getString("nodeType");
            if (NODE_TYPE_END.equals(nodeType)) {
                String inputJsonStr = nodeObj.getString("input");
                if (StrUtil.isNotBlank(inputJsonStr)) {
                    JSONObject inputObj = JSON.parseObject(inputJsonStr);
                    return inputObj.getString("output");
                }
            }
        }
        return null;
    }

    /**
     * 提取合计得分（优先实际得分，其次合计得分）
     */
    private String extractTotalScore(JSONObject scoreJson) {
        String totalFieldValue = scoreJson.getString(FIELD_TOTAL);
        if (StrUtil.isBlank(totalFieldValue)) {
            throw new RuntimeException("未找到合计字段：" + FIELD_TOTAL);
        }

        JSONObject totalJson = JSON.parseObject(totalFieldValue);
        String totalScore = totalJson.getString(FIELD_ACTUAL_SCORE);
        if (ObjectUtil.isEmpty(totalScore)) {
            totalScore = totalJson.getString(FIELD_TOTAL_SCORE);
        }

        if (ObjectUtil.isEmpty(totalScore)) {
            throw new RuntimeException("未找到合计得分，实际得分/合计得分均为空");
        }
        return totalScore;
    }

    /**
     * 更新评分数据到数据库
     */
    private void updateScoreData(IdentifyScoreCustomerDo customerDo, IdentifyScoreDo scoreDo, Map<String, Object> scoreResultMap) {
        // 更新客户表
        customerDo.setContent(Objects.toString(scoreResultMap.get("content"), null));
        customerDo.setTotalScore(Objects.toString(scoreResultMap.get("totalScore"), null));
        customerDo.setUpdateTime(new Date());
        identifyScoreCustomerService.updateByBo(BeanCopyUtils.copy(customerDo, IdentifyScoreCustomerBo.class));

        // 更新评分表
        scoreDo.setContent(Objects.toString(scoreResultMap.get("content"), null));
        scoreDo.setTotalScore(Objects.toString(scoreResultMap.get("totalScore"), null));
        scoreDo.setScoreStatus(SCORE_STATUS_SUCCESS);
        scoreDo.setUpdateTime(new Date());
        identifyScoreService.updateByBo(BeanCopyUtils.copy(scoreDo, IdentifyScoreBo.class));
    }

    /**
     * 调用智能体接口
     */
    public String syncReadFileFlowRequest(String inputContent, SysUser sysUser) throws Exception {
        Long startTime = System.currentTimeMillis();
        String reqBodyJson = buildRequestJson(inputContent, sysUser);

        // 构建请求
        Request request = buildRequest(reqBodyJson);

        try (Response response = okHttpClient.newCall(request).execute()) {
            // 读取响应
            String respStr = Objects.requireNonNull(response.body()).string();
            log.info("三清单智能评分返回报文：{}", respStr);

            // 记录成功日志
            recordOperLog(startTime, reqBodyJson, respStr, null, 0, sysUser);
            return respStr;

        } catch (Exception e) {
            // 记录失败日志
            recordOperLog(startTime, reqBodyJson, "调用三清单智能评分接口异常", e.getMessage(), 1, sysUser);
            log.error("三清单智能评分调用异常", e);
            throw e;
        }
    }

    /**
     * 构建接口请求JSON
     */
    private String buildRequestJson(String inputContent, SysUser sysUser) {
        Map<String, Object> reqBodyMap = new HashMap<>(2);
        reqBodyMap.put("UserID", sysUser.getUserId());

        Map<String, Object> inputDataMap = new HashMap<>(1);
        inputDataMap.put("uploadFileContext", inputContent);
        reqBodyMap.put("InputData", JSON.toJSONString(inputDataMap));

        return JSON.toJSONString(reqBodyMap);
    }

    /**
     * 构建OKHttp请求
     */
    private Request buildRequest(String reqBodyJson) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, reqBodyJson);

        return new Request.Builder()
            .url(identifyScoreApiUrl)
            .post(requestBody)
            .addHeader("Apikey", identifyScoreApiKey)
            .addHeader("Content-Type", "application/json")
            .build();
    }

    /**
     * 统一记录操作日志
     */
    private void recordOperLog(Long startTime, String reqParam, String respContent,
                               String errorMsg, int status, SysUser sysUser) {
        sysOperLogService.recordOperLog(
            "三清单智能评分调用",
            identifyScoreApiKey,
            "POST",
            identifyScoreApiUrl,
            reqParam,
            startTime,
            status,
            respContent,
            errorMsg,
            OPER_LOG_BIZ_TYPE,
            sysUser
        );
    }
}
