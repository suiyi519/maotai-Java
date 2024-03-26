package org.fuck996.maotai.scheduled;

import org.fuck996.maotai.service.MaoTaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MaoTaiTask {

    @Autowired
    private MaoTaiService maoTaiService;

    @Value("${maotai.reserve.scheduled.enable}")
    private Boolean reserveEnable; // 预约是否开启


    @Value("${maotai.message.scheduled.enable}")
    private Boolean messageEnable; // 消息推送是否开启


    // 如果获取不到, 取冒号后面的默认值
    @Scheduled(cron =  "${maotai.reserve.scheduled.cron:0 30 8 * * *}")
    public void huluwaTask() throws Exception {
        if (reserveEnable) {
            maoTaiService.submit();
        }
    }

    // 如果获取不到, 取冒号后面的默认值
    @Scheduled(cron =  "${maotai.message.scheduled.cron:0 30 8 * * *}")
    public void winningRecordTask() throws Exception {
        if (reserveEnable) {
            maoTaiService.pushMessageForWinningRecord();
        }
    }





}
