package com.crm.adapter.admin.servicehall;

import cn.dev33.satoken.annotation.SaIgnore;
import com.alibaba.fastjson2.JSONObject;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.utils.signutil.RpaRSAUtil;
import com.crm.system.domain.bo.SysCommonReq;
import com.crm.system.domain.dao.rpa.RpaErrorSaveDo;
import com.crm.system.service.rpa.IRpaErrorSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;


/**
 * rpa错误信息Controller
 *
 * @author ruoyi
 * @date 2025-09-25
 */
@RestController
@RequestMapping("/system/rpa")
@Slf4j
public class RpaErrorSaveController extends BaseController
{
    @Resource
    private IRpaErrorSaveService rpaErrorSaveService;

    private static String SYSTEM_CODE = "rpaCode";

    @Value("${rpa.error-save.rsa.private-key}")
    private String privateKey;

    @Value("${rpa.error-save.rsa.public-key}")
    private String publicKey;


    /**
     * 接收rpa错误信息
     */
    @PostMapping("/rpaErrorSave")
    @SaIgnore
    public R<Void> rpaErrorSave(@RequestBody SysCommonReq sysCommonReq)
    {
        try {
            if (!sysCommonReq.getAppCode().equals(SYSTEM_CODE)){
                return R.fail("appCode异常！");
            }
            Long timestamp = sysCommonReq.getTimestamp();

            String requestObj = sysCommonReq.getRequestObj();
            String rObj = RpaRSAUtil.decrypt(requestObj, privateKey);
            RpaErrorSaveDo rpaErrorSave = JSONObject.parseObject(rObj, RpaErrorSaveDo.class);
            Long ts = rpaErrorSave.getTs();

            //校验时间戳是否异常，有效期三分钟
            if (!timestamp.equals(ts) || new Date().getTime() - ts > 3 * 60 * 1000){
                return R.fail(401,"请求过期，时间戳超过三分钟");
            }

            //校验参数完整性
            if (rpaErrorSave.getSceneName() == null || rpaErrorSave.getSceneUser() == null || rpaErrorSave.getErrorInfo() == null){
                log.error("参数错误：" + rpaErrorSave);
                return R.fail(400,"参数错误！");
            }

            rpaErrorSave.setCreateBy("rpa错误信息接收接口录入");

            int i =rpaErrorSaveService.insertRpaErrorSave(rpaErrorSave);

            return toAjax(i);
        }catch (Exception e){

            return R.fail();
        }
    }

//    public static void main(String[] args) throws Exception {
//        JSONObject token = new JSONObject();
//        Long ts = new Date().getTime();
//        token.put("ts",ts);
//        token.put("sceneName","sceneNamesceneNamesceneNamesceneNamesceneName");
//        token.put("sceneUser","sceneUsersceneUsersceneUsersceneUser");
//        token.put("errorInfo","errorInfoerrorInfoerrorInfoerrorInfoerrorInfoerrorInfoerrorInfoerrorInfo");
//        token.put("errorId","errorIderrorIderrorId");
//        token.put("type","1");
//        token.put("message","messagemessagemessagemessage");
//        token.put("extraInfo","extraInfoextraInfoextraInfo");
//        token.put("runningStack","runningStackrunningStackrunningStackrunningStack");
//        token.put("methodName","methodNamemethodNamemethodNamemethodNamemethodName");
//        token.put("bak1","bak1bak1bak1bak1");
//        token.put("bak2","bak2bak1bak1bak1");
//        token.put("bak3","bak3bak3bak3bak3");
//        token.put("bak4","bak4bak4bak4bak4bak4");
//        token.put("bak5","bak5bak5bak5bak5");
//        token.put("bak6","bak6bak6bak6bak6");
//        token.put("bak7","bak7bak7bak7bak7bak7");
//        token.put("bak8","bak8bak8bak8bak8bak8");
//        String tokenEncrypt = RSAUtils.encryptByPublicKey(token.toJSONString(), PUBLIC_KEY);
//        System.out.println(ts);
//        System.out.println(tokenEncrypt);
//
//    }
}
