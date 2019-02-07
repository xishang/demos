package com.demos.java.poidemo.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureGenerationException;
import com.auth0.jwt.impl.ClaimsHolder;
import com.auth0.jwt.impl.PayloadSerializer;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xishang
 * @version 1.0
 * @date 2017/11/13
 */
public class JwtDemo {

    /**
     * JWT结构:
     * Header: type, algorithm, keyId, contentType, 其他Map
     * payload: issuer, subject, audience, expiresAt, notBefore, issuedAt, jwtId, 其他Claims
     */
    public static String createToken(String secretKey, String data) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            String token = JWT.create()
                    .withKeyId("321")
                    .withIssuer("auth0")
                    .withAudience("james")
                    .withSubject("guest")
                    .withClaim("data", "123456789")
                    .sign(algorithm);
            return token;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JWTCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void decodeToken1(String secretKey, String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            String payload = jwt.getPayload();
            Object data = jwt.getClaim("data").asString();
            System.out.println(data);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JWTVerificationException e) {
            e.printStackTrace();
        }
    }

    public static String createToken(String secretKey, Map<String, Object> params) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTCreator.Builder builder = JWT.create();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    builder.withClaim(entry.getKey(), String.valueOf(value));
                }
                if (value instanceof Long) {
                    builder.withClaim(entry.getKey(), (long) value);
                }
            }
            String token = builder.sign(algorithm);
            return token;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JWTCreationException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> decodeToken(String secretKey, String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            Map<String, Claim> claimMap = jwt.getClaims();
            Map<String, String> resultMap = new HashMap<>();
            for (Map.Entry<String, Claim> entry : claimMap.entrySet()) {
                Claim value = entry.getValue();
                Object obj = value.as(Object.class);
                Map map = value.asMap();
                resultMap.put(entry.getKey(), entry.getValue().asString());
            }
            return resultMap;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JWTVerificationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
//        String secretKey = "AbIJTDMFc7yUa5MhvcP03nJPyCPzZtQcGEp-zWfOkEE";
//        String payload = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJkb2IiOiIiLCJhZGRyZXNzIjoiIiwiaWRlbnRpZmllciI6IjEwMzU2MyIsIm5hbWUiOiJcdTVmMjBcdTY2NjgiLCJpZGVudGlmaWNhdGlvbiI6IiIsImVtYWlsIjoiIiwicGhvbmUiOiIxMzQyMDk4NzAzNSIsInJldHVybl91cmwiOiJodHRwOlwvXC9tLmNodWFuZ3lpbi5uZXQuY24ifQ==.001yQo+H5oOWZtkvgQbDqdrtw8hWQH/eLimn3vRBRGw=";
//        System.out.println(URLEncoder.encode(payload, "utf-8"));
//        decodeToken(secretKey, payload);
//        test();
//        int a = 0x11;
//        int b = -0b110;
//        System.out.println(b);
//
//        int d = 0xffffffff;
//
//        System.out.println("余数:" + (-2 % 4));
//        System.out.println(-4 % 3);
//        System.out.println(-4 % -3);
//
//        System.out.println("余数");
//
//
//        System.out.println(d);
//        System.out.println(Integer.toBinaryString(d));
//        int s = 0b100000001;
//        byte k = (byte) s;
//
//
//        System.out.println("s = " + s + ", binary = " + Integer.toBinaryString(s));
//        System.out.println("k = " + k + ", binary = " + Integer.toBinaryString(k));
        int a = 0x80000000;
        System.out.println(a);
        int b = -2;
        System.out.println(b);
        int c = a + b;
        System.out.println(c);
        System.out.println("a:" + Integer.toBinaryString(a));
        System.out.println("b:" + Integer.toBinaryString(b));
        System.out.println("c:" + Integer.toBinaryString(c));

        int x = -7;
        int y = 2;
        int z = x / y;
        System.out.println("x:" + Integer.toBinaryString(x));
        System.out.println("y:" + Integer.toBinaryString(y));
        System.out.println("z:" + Integer.toBinaryString(z));

        System.out.println(-7/2);


//        Map<String, Object> params = new HashMap<>();
//        params.put("phone", "15012345678");
//        params.put("identifier", "000012");
//        params.put("identification", "422800198605078100");
//        params.put("return_url", "http://10.1.1.1");
//        params.put("name", "cy");
//        params.put("address", "address");
//        params.put("dob", System.currentTimeMillis());
//        params.put("email", "cy@123.com");
//        String jwt = createToken(secretKey, params);
//        System.out.println(jwt);
//        decodeToken(secretKey, jwt);
//        sign();
    }

    public static void test() {
        String secretKey = "AbIJTDMFc7yUa5MhvcP03nJPyCPzZtQcGEp-zWfOkEE";
        Map<String, Object> params = new HashMap<>();
        params.put("phone", "13420987035");
        params.put("identifier", "103563");
        params.put("identification", "");
        params.put("return_url", "http://m.chuangyin.net.cn");
        params.put("name", "张晨");
        params.put("address", "");
        params.put("dob", "1111111");
        params.put("email", "");
        String jwt = createToken(secretKey, params);
        System.out.println(jwt);
    }

    public static void sign() throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ClaimsHolder.class, new PayloadSerializer());
        mapper.registerModule(module);
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);

        Map<String, Object> params = new HashMap<>();
        params.put("phone", "13420987035");
        params.put("identifier", "103563");
        params.put("identification", "");
        params.put("return_url", "http://m.chuangyin.net.cn");
        params.put("name", "张晨");
        params.put("address", "");
        params.put("dob", "");
        params.put("email", "");


        String payloadJson = mapper.writeValueAsString(new ClaimsHolder(params));
        System.out.println(payloadJson);
        byte[] bs = payloadJson.getBytes(StandardCharsets.UTF_8);
        System.out.println(Base64.encodeBase64URLSafeString(payloadJson.getBytes()));
        System.out.println(Base64.encodeBase64URLSafeString(payloadJson.getBytes(StandardCharsets.UTF_8)));

        String payload = Base64.encodeBase64URLSafeString(payloadJson.getBytes(StandardCharsets.UTF_8));
//        String content = String.format("%s.%s", new Object[]{header, payload});
//        byte[] signatureBytes = this.algorithm.sign(content.getBytes(StandardCharsets.UTF_8));
//        String signature = Base64.encodeBase64URLSafeString(signatureBytes);
//        return String.format("%s.%s", new Object[]{content, signature});
    }


    public static void hash() throws Exception {
        String str = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJkb2IiOiIiLCJhZGRyZXNzIjoiIiwiaWRlbnRpZmllciI6IjEwMzU2MyIsIm5hbWUiOiLlvKDmmagiLCJpZGVudGlmaWNhdGlvbiI6IiIsImVtYWlsIjoiIiwicGhvbmUiOiIxMzQyMDk4NzAzNSIsInJldHVybl91cmwiOiJodHRwOi8vbS5jaHVhbmd5aW4ubmV0LmNuIn0%3D.06tTUwBlumIITq40%2BThlXk2boUcJIkeyF26mnTkoRcU%3D";
        str = URLDecoder.decode(str, "utf-8");
        String secretKey = "AbIJTDMFc7yUa5MhvcP03nJPyCPzZtQcGEp-zWfOkEE";
        decodeToken(secretKey, str);
    }

}
