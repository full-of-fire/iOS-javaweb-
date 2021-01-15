package com.example.demo.utils;

import com.alibaba.fastjson.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HttpUtil {

    public static String sendPostRequest(String url, String token) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String auth = "Bearer " + token;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization",auth)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String sendPostWithJsonRequest(String url, String body,String token) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String auth = "Bearer " + token;
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(body,mediaType);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization",auth)
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String sendGetRequest(String url, MultiValueMap<String, String> params,HttpHeaders headers){
        RestTemplate client = new RestTemplate();

        HttpMethod method = HttpMethod.GET;
        // 以表单的方式提交
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //将请求头部和参数合成一个请求
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
        //执行HTTP请求，将返回的结构使用String 类格式化
        ResponseEntity<String> response = client.exchange(url, method, requestEntity, String.class);

        return response.getBody();
    }
}
