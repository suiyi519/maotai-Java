package org.fuck996.maotai.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HttpUtils {


    /**
     * 计算digest算法
     *
     * @param body
     * @param sk
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static String calculateDigest(String body, String sk) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(sk.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKey);
        byte[] hmacBytes = hmac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    /**
     * 计算signature算法
     *
     * @param method
     * @param url
     * @param ak
     * @param sk
     * @param date
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static String calculateSignature(String method, String url, String ak, String sk, String date) throws Exception {
        String strToSign = method.toUpperCase() + "\n" + url + "\n\n" + ak + "\n" + date + "\n";
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(sk.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKey);
        byte[] hmacBytes = hmac.doFinal(strToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    /**
     * 构建请求头
     *
     * @param method
     * @param url
     * @param body
     * @param ak
     * @param sk
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static Map<String, String> buildHeader(String method, String url, String body, String ak, String sk) throws Exception {

        // 当前时间减8小时
        LocalDateTime localDateTime = LocalDateTime.now().minusHours(8);
        // 格式化指定的时间为 UTC 时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'",Locale.ENGLISH);
        String date = localDateTime.atZone(ZoneOffset.UTC).format(formatter);

        String signature = calculateSignature(method, url, ak, sk, date);
        String digest = calculateDigest(body, sk);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-HMAC-SIGNATURE", signature);
        headers.put("X-HMAC-ACCESS-KEY", ak);
        headers.put("X-HMAC-ALGORITHM", "hmac-sha256");
        headers.put("X-HMAC-DIGEST", digest);
        headers.put("X-HMAC-Date", date);
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36 MicroMessenger/7.0.20.1781(0x6700143B) NetType/WIFI MiniProgramEnv/Windows WindowsWechat/WMPF XWEB/6945");

        return headers;
    }

    /**
     * GET请求
     * @param url https://baidu.com
     * @return
     * @throws IOException
     */
    public static String sendGet(String url) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder result = new StringBuilder();

        try {
            URL getUrl = new URL(url);
            connection = (HttpURLConnection) getUrl.openConnection();
            connection.setRequestMethod("GET");

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result.toString();
    }

    /**
     * POST请求
     * @param url https://baidu.com
     * @param body 格式: String body = "{\"key\": \"value\"}";
     *         代码示例:
     *         Map<String,String> body = new HashMap<>();
     *         body.put("id","123");
     *
     *         ObjectMapper objectMapper = new ObjectMapper();
     *         String bodyStr = objectMapper.writeValueAsString(body);
     *         依赖包:
     *         <dependency>
     *             <groupId>com.fasterxml.jackson.core</groupId>
     *             <artifactId>jackson-databind</artifactId>
     *             <version>2.13.0</version>
     *         </dependency>
     * @return
     * @throws IOException
     */
    public static String sendPost(String url, String body) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder result = new StringBuilder();

        try {
            URL postUrl = new URL(url);
            connection = (HttpURLConnection) postUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            OutputStream os = connection.getOutputStream();
            os.write(body.getBytes());
            os.flush();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result.toString();
    }


    /**
     * @param url https://baidu.com
     * @param headers Map<String, String> headers
     * @param body 格式: String body = "{\"key\": \"value\"}";
     *         代码示例:
     *         Map<String,String> body = new HashMap<>();
     *         body.put("id","123");
     *
     *         ObjectMapper objectMapper = new ObjectMapper();
     *         String bodyStr = objectMapper.writeValueAsString(body);
     *         依赖包:
     *         <dependency>
     *             <groupId>com.fasterxml.jackson.core</groupId>
     *             <artifactId>jackson-databind</artifactId>
     *             <version>2.13.0</version>
     *         </dependency>
     * @return
     * @throws IOException
     */
    public static String sendPost(String url, Map<String, String> headers, String body) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder result = new StringBuilder();

        try {
            URL postUrl = new URL(url);
            connection = (HttpURLConnection) postUrl.openConnection();

            // 设置请求方法为 POST
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // 设置请求头部
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 设置请求体数据
            OutputStream os = connection.getOutputStream();
            os.write(body.getBytes());
            os.flush();

            // 打开连接
            connection.connect();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result.toString();
    }

}
