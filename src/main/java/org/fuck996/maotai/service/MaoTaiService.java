package org.fuck996.maotai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.fuck996.maotai.config.MaoTaiConfig;
import org.fuck996.maotai.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MaoTaiService {

    @Autowired
    private MaoTaiConfig maoTaiConfig;

    @Value("${maotai.message.pushKey}")
    private String pushKey; // pushKey

    @Value("${maotai.message.pushUrl}")
    private String pushUrl; // pushUrl

    private String HOST = "https://gw.huiqunchina.com";
    private String AK = "00670fb03584fbf44dd6b136e534f495";
    private String SK = "0d65f24dbe2bc1ede3c3ceeb96ef71bb";


    /**
     * pushDeer发送消息
     * @param text 标题
     * @param desp 内容
     * @throws Exception
     */
    private void pushMessage(String text, String desp) throws Exception {

        StringBuilder body = new StringBuilder();
        body.append("pushkey=").append(pushKey)
                .append("&text=").append(text)
                .append("&desp=").append(desp)
                .append("&type=markdown");

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type","application/x-www-form-urlencoded");

        String response = HttpUtils.sendPost(pushUrl,headers,body.toString());
        JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();
        if (0 != responseJson.get("code").getAsInt()) {
            System.out.println("消息发送失败");
        }
        // String phone = userInfoJson.getAsJsonObject("data").get("phone").getAsString();
        System.out.println(response);
    }

    private String getWinningRecord(String appId, String cookie) throws Exception {
        String url = "/front-manager/api/customer/promotion/queryLotteryRecord";
        Map<String,String> body = new HashMap<>();
        body.put("appId",appId);

        ObjectMapper objectMapper = new ObjectMapper();
        String bodyStr = objectMapper.writeValueAsString(body);
        Map<String, String> headers = HttpUtils.buildHeader("post", url, bodyStr, AK, SK);
        headers.put("X-access-token",cookie);

        String response = HttpUtils.sendPost(HOST + url,headers,bodyStr);

        return response;
    }



    /**
     * 提交预约
     * @throws Exception
     */
    public void submit() throws Exception {
        System.out.println("--------" + LocalDateTime.now() + "--------");

        StringBuilder message = new StringBuilder();

        Map<String, String> appIdChineseMap = maoTaiConfig.getAppIdChineseMap();
        Map<String, String> cookiesChineseMap = maoTaiConfig.getCookiesChineseMap();

        for (Map.Entry<String, String> entry : appIdChineseMap.entrySet()) {
            String name = entry.getKey();
            String appId = entry.getValue();
            String cookie = cookiesChineseMap.get(name);

            System.out.println("--------" + name + "预约开始" + "--------");
            message.append(name).append("预约开始");

            // 获取用户信息
            String userInfo = getUserInfo(appId, cookie);
            // 使用JsonParser解析JSON字符串
            JsonObject userInfoJson = JsonParser.parseString(userInfo).getAsJsonObject();
            if (!userInfoJson.get("success").getAsBoolean()) {
                message.append("----用户未登陆----");
                continue;
            }
            String phone = userInfoJson.getAsJsonObject("data").get("phone").getAsString();
            System.out.println("当前用户: " + phone);
            message.append("----当前用户: ").append(phone);

            // 获取活动信息
            String channelId = getChannelId(name);
            String activity = getChannelActivity(channelId, cookie);
            JsonObject activityJson = JsonParser.parseString(activity).getAsJsonObject();
            if (!activityJson.get("success").getAsBoolean()) {
                continue;
            }
            String activityId = activityJson.getAsJsonObject("data").get("id").getAsString();
            String activityName = activityJson.getAsJsonObject("data").get("name").getAsString();
            System.out.println("活动名称: " + activityName);
            message.append("----活动名称: ").append(activityName);

            // 检查抢购信息
            String qiangGou = checkCustomerInQiangGou(activityId, channelId, cookie);
            JsonObject qiangGouJson = JsonParser.parseString(qiangGou).getAsJsonObject();
            if (!qiangGouJson.get("success").getAsBoolean()) {
                continue;
            }
            System.out.println("预约结果: " + "预约成功");
            message.append("----预约结果: ").append("预约成功----");

        }
        // 发送消息
        pushMessage("葫芦娃预约",message.toString());
    }

    private String getUserInfo(String appId, String cookie) throws Exception {
        String url = "/front-manager/api/customer/queryById/token";
        Map<String,String> body = new HashMap<>();
        body.put("appId",appId);

        ObjectMapper objectMapper = new ObjectMapper();
        String bodyStr = objectMapper.writeValueAsString(body);
        Map<String, String> headers = HttpUtils.buildHeader("post", url, bodyStr, AK, SK);
        headers.put("X-access-token",cookie);

        String response = HttpUtils.sendPost(HOST + url,headers,bodyStr);

        return response;
    }

    private String getChannelActivity(String channelId, String cookie) throws Exception {
        String url = "/front-manager/api/customer/promotion/channelActivity";
        Map<String,String> body = new HashMap<>();
        body.put("id",channelId);

        ObjectMapper objectMapper = new ObjectMapper();
        String bodyStr = objectMapper.writeValueAsString(body);
        Map<String, String> headers = HttpUtils.buildHeader("post", url, bodyStr, AK, SK);
        headers.put("X-access-token",cookie);

        String response = HttpUtils.sendPost(HOST + url,headers,bodyStr);

        return response;
    }

    private String checkCustomerInQiangGou(String activityId, String channelId, String cookie) throws Exception {
        String url = "/front-manager/api/customer/promotion/checkCustomerInQianggou";
        Map<String,String> body = new HashMap<>();
        body.put("activityId",activityId);
        body.put("channelId",channelId);

        ObjectMapper objectMapper = new ObjectMapper();
        String bodyStr = objectMapper.writeValueAsString(body);
        Map<String, String> headers = HttpUtils.buildHeader("post", url, bodyStr, AK, SK);
        headers.put("X-access-token",cookie);

        String response = HttpUtils.sendPost(HOST + url,headers,bodyStr);

        return response;
    }

    private String getChannelId(String name) {

        Map<String, String> map = new HashMap<>();
        map.put("新联惠购","8");
        map.put("贵旅优品","7");
        map.put("空港乐购","2");
        map.put("航旅黔购","6");
        map.put("遵航出山","5");
        map.put("贵盐黔品","3");
        map.put("乐旅商城","1");
        map.put("驿路黔寻","9");

        return map.get(name);

    }

    /**
     * 发送中奖记录消息
     */
    public void pushMessageForWinningRecord() throws Exception {

        StringBuilder message = new StringBuilder();

        Map<String, String> appIdChineseMap = maoTaiConfig.getAppIdChineseMap();
        Map<String, String> cookiesChineseMap = maoTaiConfig.getCookiesChineseMap();

        for (Map.Entry<String, String> entry : appIdChineseMap.entrySet()) {
            String name = entry.getKey();
            String appId = entry.getValue();
            String cookie = cookiesChineseMap.get(name);

            message.append(name);

            // 获取用户信息
            String userInfo = getUserInfo(appId, cookie);
            JsonObject userInfoJson = JsonParser.parseString(userInfo).getAsJsonObject();
            if (!userInfoJson.get("success").getAsBoolean()) {
                message.append("----用户未登陆----");
                continue;
            }
            String phone = userInfoJson.getAsJsonObject("data").get("phone").getAsString();
            System.out.println("当前用户: " + phone);
            message.append("----当前用户: ").append(phone);

            // 获取中奖记录
            String record = getWinningRecord(appId, cookie);
            JsonObject recordJson = JsonParser.parseString(record).getAsJsonObject();
            if (!recordJson.get("success").getAsBoolean()) {
                continue;
            }
            int total = recordJson.getAsJsonObject("data").get("total").getAsInt();

            System.out.println(record);
            message.append("----中奖记录: ").append(total).append("条----");
        }

        // 发送pushDeer消息
        pushMessage("中奖记录",message.toString());
    }
}
