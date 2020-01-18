package com.bdaim.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码生成工具方法
 *
 * @author Chacker
 */
public class VerifyUtil {
    private static final Logger logger = LoggerFactory.getLogger(VerifyUtil.class);
    public static Map<String, Object> verifyCodes = new HashMap();
    public static Long verifyCodeTimeout = 300000L;

    public static final String randomString(int length) {
        char[] charArray = new char[length];
        int i = 0;
        while (i < length) {
            int f = (int) (Math.random() * 3.0D);
            if (f == 0) {
                charArray[i] = (char) (int) (65.0D + Math.random() * 26.0D);
            } else if (f == 1) {
                charArray[i] = (char) (int) (97.0D + Math.random() * 26.0D);
            } else {
                charArray[i] = (char) (int) (48.0D + Math.random() * 10.0D);
            }
            ++i;
        }
        return new String(charArray);
    }

    public static BufferedImage getLoginVerifyCode(int width, int height, String uuid) {
        BufferedImage image = new BufferedImage(width, height, 1);
        Graphics g = image.getGraphics();
        g.setColor(new Color(14474460));
        g.fillRect(0, 0, width, height);

        g.setColor(Color.lightGray);
        g.drawRect(0, 0, width - 1, height - 1);

        String verifyCode = randomString(4);
        //验证码和uuid存到map中
        VerifyCode codeObject = new VerifyCode();
        codeObject.setVerifyTime(System.currentTimeMillis());
        codeObject.setUuid(uuid);
        codeObject.setVerifyCode(verifyCode);
        verifyCodes.put(uuid, codeObject);
        try {
//            stringRedisTemplate.opsForValue().set(verifyCode.toLowerCase() + cacheKeySuffix, verifyCode, 3L, TimeUnit.MINUTES);
//            LOGGER.debug("cacheKey=" + verifyCode.toLowerCase() + cacheKeySuffix + ",verifyCode=" + verifyCode);
            g.setColor(new Color(1090299));
            g.setFont(new Font("Atlantic Inline", 0, 30));
            String Str = verifyCode.substring(0, 1);
            g.drawString(Str, 8, 25);

            Str = verifyCode.substring(1, 2);
            g.drawString(Str, 28, 30);
            Str = verifyCode.substring(2, 3);
            g.drawString(Str, 48, 27);

            Str = verifyCode.substring(3, 4);
            g.drawString(Str, 68, 32);

            Random random = new Random();
            for (int i = 0; i < 88; ++i) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                g.drawOval(x, y, 0, 0);
            }

            g.dispose();
        } catch (Exception e) {
//            LOGGER.info(e.getMessage());
        }
        return image;

    }
}
