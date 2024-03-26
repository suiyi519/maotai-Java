package org.fuck996.maotai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "maotai")
public class MaoTaiConfig {

    // cookies
    private Map<String, String> cookies = new HashMap<>();
    // 中文键cookies
    private Map<String, String> cookiesChineseMap = new HashMap<>();

    // appId
    private Map<String,String> appId = new HashMap<>();

    // 中文键appId
    private Map<String, String> appIdChineseMap = new HashMap<>();


    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
        convertToChineseKeys(cookies,cookiesChineseMap); // 转换英文键为中文键
    }

    public Map<String, String> getCookiesChineseMap() {
        return cookiesChineseMap;
    }

    public Map<String, String> getAppId() {
        return appId;
    }

    public void setAppId(Map<String, String> appId) {
        this.appId = appId;
        convertToChineseKeys(appId,appIdChineseMap); // 转换英文键为中文键
    }

    public Map<String, String> getAppIdChineseMap() {
        return appIdChineseMap;
    }


    private Map<String,String> getChineseKey() {
        Map<String,String> map = new HashMap<>();
        map.put("xlth","新联惠购");
        map.put("glyp","贵旅优品");
        map.put("kglg","空港乐购");
        map.put("hlqg","航旅黔购");
        map.put("zhcs","遵航出山");
        map.put("gyqp","贵盐黔品");
        map.put("llsc","乐旅商城");
        map.put("ylqx","驿路黔寻");
        return map;
    }

    /**
     * 转换中文键
     * @param source
     * @param target
     */
    private void convertToChineseKeys(Map<String, String> source, Map<String, String> target) {
        Map<String, String> chineseKeyMap = getChineseKey(); // 获取中文键的映射关系
        for (Map.Entry<String, String> entry : source.entrySet()) {
            String englishKey = entry.getKey();
            String englishValue = entry.getValue();
            // 如果中文键映射关系中包含英文键，则将其转换为对应的中文键
            if (chineseKeyMap.containsKey(englishKey)) {
                String chineseKey = chineseKeyMap.get(englishKey);
                target.put(chineseKey, englishValue);
            }
        }
    }

}
