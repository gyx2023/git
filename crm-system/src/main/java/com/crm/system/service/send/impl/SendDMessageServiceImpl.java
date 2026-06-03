package com.crm.system.service.send.impl;

import com.crm.system.domain.dao.DingMessage;
import com.crm.system.domain.vo.SysUsersVo;
import com.crm.system.service.IDingMessageService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.service.ISysUsersService;
import com.crm.system.service.send.ISendDMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
@Slf4j
public class SendDMessageServiceImpl implements ISendDMessageService {
    @Autowired
    protected ISysUsersService sysUsersService;
    @Autowired
    protected ISysParamService sysParamService;
    @Autowired
    protected IDingMessageService dingMessageService;
    // 短信积压预警提醒字典类型
    private static final String MESSAGE_OVERSTOCK_WARNING_TYPE = "message_overstock_warning";
    @Override
    public void initNoticeMessage(List<Integer> userIds, String message) {
        //防止重复发送
        HashSet<Integer> set = new HashSet<>(userIds);
        userIds.clear();
        userIds.addAll(set);
        if ((userIds != null) && (userIds.size() > 0)) {
            HashSet<Integer> h = new HashSet<>();
            for (Integer userId : userIds){
                h.add(userId);
            }
            for (Integer userId : h) {
                SysUsersVo user = sysUsersService.queryById(Long.valueOf(userId));
                if (user != null) {
                    if (StringUtils.isNotEmpty(user.getRelationTel())) {
                        DingMessage dm = new DingMessage();
                        dm.setContent(message);
                        dm.setCreateTime(new Date());
                        dm.setUserIds(user.getId() + "");
                        //设定预设发送时间
                        dm.setSettingSendTime(getSmsSendTimeSetting(user));
                        dm.setMsgType("text");
                        dingMessageService.saveDingMessage(dm);
                    }
                }
            }
        }
    }

    private Date getSmsSendTimeSetting(SysUsersVo user) {
        Calendar calendar = Calendar.getInstance();
        String  crurrentDay=new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        String smsSendBeginTime=crurrentDay+ " "+(user.getRecSmsBeginTime()!=null?user.getRecSmsBeginTime():"08:00")+":00";
        String smsSendEndTime=crurrentDay+ " "+(user.getRecSmsEndTime()!=null?user.getRecSmsEndTime():"20:00")+":59";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //判断三种情况
        Date date=new Date();
        try {
            //当前时间在指定时间之前的，则返回当天的日期+用户预设的开始时间
            if(date.before(simpleDateFormat.parse(smsSendBeginTime))){
                return simpleDateFormat.parse(smsSendBeginTime);
            }else if(date.after(simpleDateFormat.parse(smsSendEndTime))){
                Calendar c = Calendar.getInstance();
                c.setTime(simpleDateFormat.parse(smsSendBeginTime));
                //日期加1;
                c.add(Calendar.DAY_OF_MONTH, 1);
                return c.getTime();
            }else {
                return date;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
