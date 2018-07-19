package com.atguigu.gmall.passport.util;

import io.jsonwebtoken.*;

import java.util.Map;

public class JwtUtil {
    // 加密 key：自己定义的字符串，param：userInfo对象 slat：ip
    public static String encode(String key,Map<String,Object> param,String salt){
        if(salt!=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256,key);
        // 存放userInfo信息
        jwtBuilder = jwtBuilder.setClaims(param);
        // token = 密钥
        String token = jwtBuilder.compact();
        return token;

    }

    // 解密
    public  static Map<String,Object> decode(String token , String key, String salt){
        Claims claims=null;
        if (salt!=null){
            key+=salt;
        }
        try {
            claims= Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch ( JwtException e) {
            return null;
        }
        return  claims;
    }

}
