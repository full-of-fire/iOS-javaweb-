package com.example.demo.utils;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import java.util.Date;



public class JWTTokenUtil {//过期时间
    private static final long EXPIRE_TIME = 15 * 60 * 1000;//默认15分钟
    //私钥
    private static final String TOKEN_SECRET = "MHcCAQEEIF9+6o8fNdjrFNS/lH7SDFHwHcrb499UzK50KQNeJfueoAoGCCqGSM49\n" +
            "AwEHoUQDQgAEixKrj/pl4cyow8MJLYk2oFAHS7ZHGrLdECPk/iLOfvHvi2+6ytpl\n" +
            "U9rLCwFD2SJKMg/awXPB23k+WBgEvUCvEg==";

    /**
     * 生成签名，15分钟过期
     * @param **username**
     * @param **password**
     * @return
     */
    public static String createToken() {
        try {
            // 设置过期时间
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            // 私钥和加密算法

            Vertx vertx = Vertx.vertx();

            JWTAuth provider = JWTAuth.create(vertx, new JWTAuthOptions()
                    .addPubSecKey(new PubSecKeyOptions()
                            .setAlgorithm("ES256")
                            .setBuffer(
                                    "-----BEGIN PRIVATE KEY-----\n" +
                                            "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgX37qjx812OsU1L+U\n" +
                                            "ftIMUfAdytvj31TMrnQpA14l+56hRANCAASLEquP+mXhzKjDwwktiTagUAdLtkca\n" +
                                            "st0QI+T+Is5+8e+Lb7rK2mVT2ssLAUPZIkoyD9rBc8HbeT5YGAS9QK8S\n" +
                                            "-----END PRIVATE KEY-----"))
                    );

            JsonObject jsonObject =  new JsonObject();
            jsonObject.put("iss","c7d4c060-3c4c-4b01-8eb8-c97bdde5a451");
            jsonObject.put("exp",date.getTime()/1000);
            System.out.println("timeis" + date.getTime());
            jsonObject.put("aud","appstoreconnect-v1");

            JWTOptions jwtOptions = new JWTOptions();
            jwtOptions.setAlgorithm("ES256");
            JsonObject headerObect =  new JsonObject();
            headerObect.put("alg","ES256");
            headerObect.put("Typ","JWT");
            headerObect.put("kid","89RYF83UBR");
            jwtOptions.setHeader(headerObect);

            String token = provider.generateToken(
                    jsonObject,
                    jwtOptions);

           return token;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}

