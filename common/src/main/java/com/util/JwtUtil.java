package com.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    public static JwtUtil instance = new JwtUtil() ;

    public static String sercetKey = "!@#$%^&*QWERTYUI" ;

    public static Long keeptime = 1000*60*60*24*30l ;


    /**
     * 产生令牌
     * @param id  用户名
     * @param issuer  签发者
     * @param subject 主体（内容)
     * @return
     */
    public static String generToken(String id, String issuer, String subject) {
        long ttlMillis = keeptime;
        return generToken(id , issuer , subject , ttlMillis) ;
    }

    public static String generToken(String id, String issuer, String subject , long myKeepTime) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(sercetKey);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        JwtBuilder builder = Jwts.builder().setId(id)
                .setIssuedAt(now);
        if (subject != null) {
            builder.setSubject(subject);
        }
        if (issuer != null) {
            builder.setIssuer(issuer);
        }
        builder.signWith(signatureAlgorithm, signingKey);

        if (myKeepTime >= 0) {
            long expMillis = nowMillis + myKeepTime;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    /**
     * 更新令牌
     * @param token
     * @return
     */
    public static String updateToken(String token) {
        try {
            Claims claims = verifyToken(token);
            String id = claims.getId();
            String subject = claims.getSubject();
            String issuer = claims.getIssuer();
            Date date = claims.getExpiration();
            return generToken(id, issuer, subject);
        } catch (Exception ex) {
//              ex.printStackTrace();
            //  ex.printStackTrace();
        }
        return "0";
    }

    /**
     * 获取用户名
     * @param token
     * @return
     */
    public static String getUsername(String token) {

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(sercetKey))
                    .parseClaimsJws(token).getBody();
            return claims.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Claims verifyToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(sercetKey))
                .parseClaimsJws(token).getBody();
        return claims;
    }


}
