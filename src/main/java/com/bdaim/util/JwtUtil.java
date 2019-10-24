package com.bdaim.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/11/15 17:59
 */
public class JwtUtil {
    public static String sercetKey="huokebeijingyouxiangongsi";
    public final static long  keeptime=24*3600*1000;
    //public final static long  keeptime=24*60*1000;

   /* @Value("${token.sercetKey}")
    public  static String sercetKey;
    @Value("${token.keeptime:30000}")
    public static long keeptime;*/

    public static String generToken(String id, String issuer, String subject){
        long ttlMillis=keeptime;
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(sercetKey);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        JwtBuilder builder = Jwts.builder().setId(id)
                .setIssuedAt(now);
        if(subject!=null){
            builder.setSubject(subject);
        }
        if(issuer!=null){
            builder.setIssuer(issuer);
        }
        builder .signWith(signatureAlgorithm, signingKey);

        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    public static String updateToken(String token){
        try {
            Claims claims=verifyToken(token);
            String id=claims.getId();
            String subject=claims.getSubject();
            String issuer=claims.getIssuer();
            Date date = claims.getExpiration();
            return generToken(id, issuer, subject);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "0";
    }


    public static String updateTokenBase64Code(String token)  {
        BASE64Encoder base64Encoder=new  BASE64Encoder();
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            token=new String(decoder.decodeBuffer(token),"utf-8");
            Claims claims=verifyToken(token);
            String id=claims.getId();
            String subject=claims.getSubject();
            String issuer=claims.getIssuer();
            Date date = claims.getExpiration();
            String newToken = generToken(id, issuer, subject);
            return base64Encoder.encode(newToken.getBytes());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "0";
    }


    public static Claims verifyToken(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(sercetKey))
                .parseClaimsJws(token).getBody();
        return  claims;
    }

    public static void main(String args[]){
        //id:custId issuer:customer.username    subject:password
        //String token = generToken("1807170713370004","customer.user1234","aa123456");
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxODA3MTcwNzEzMzcwMDA0IiwiaWF0IjoxNTQyNjI0NjcwLCJzdWIiOiJhYTEyMzQ1NiIsImlzcyI6ImN1c3RvbWVyLnVzZXIxMjM0IiwiZXhwIjoxNTQyNjI0NzMwfQ.MP1O-G_2X64ddN3PzeR9gmJOmH5xLy3Vhx3Cz8NtKfw";
        System.out.println(token);
        Claims claims = verifyToken(token);
        String id = claims.getId();
        String issue = claims.getIssuer();
        String subject = claims.getSubject();
        System.out.println("id:"+id+"\tissue:"+issue+"\tsubject;"+subject);

    }
}
