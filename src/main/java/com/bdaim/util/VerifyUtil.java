package com.bdaim.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class VerifyUtil {
    private static final Logger logger = LoggerFactory.getLogger(VerifyUtil.class);

    public static final String randomString(int length) {
        char[] charArry = new char[length];
        int i = 0;
        while (i < length) {
            int f = (int) (Math.random() * 3.0D);
            if (f == 0)
                charArry[i] = (char) (int) (65.0D + Math.random() * 26.0D);
            else if (f == 1)
                charArry[i] = (char) (int) (97.0D + Math.random() * 26.0D);
            else
                charArry[i] = (char) (int) (48.0D + Math.random() * 10.0D);
            ++i;
        }
        return new String(charArry);
    }

    public static BufferedImage getLoginVerifyCode(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, 1);
        Graphics g = image.getGraphics();
        g.setColor(new Color(14474460));
        g.fillRect(0, 0, width, height);

        g.setColor(Color.lightGray);
        g.drawRect(0, 0, width - 1, height - 1);

        String verifyCode = randomString(4);
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
