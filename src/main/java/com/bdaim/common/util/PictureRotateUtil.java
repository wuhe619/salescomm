package com.bdaim.common.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/11/14 15:49
 */
public class PictureRotateUtil {

    private static Logger logger = LoggerFactory.getLogger(PictureRotateUtil.class);

    public static void main(String args[]){
        String url = "C:\\Users\\123\\Desktop\\微信图片_20181105091348.jpg";
        File img = new File(url);
        //getExif(url);
        int angle =  getAngle( getExif(url));
        System.out.println(angle);

        //-----------------
        InputStream is=null;
        BufferedImage src=null;
        try {
            is = new FileInputStream(img);
            src = ImageIO.read(is);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        //旋转
        BufferedImage bf = getBufferedImg(src,url, getWidth(img), getHeight(img), angle);
        System.out.println(bf.getWidth()+","+bf.getHeight());
        /*BufferedImage bft = ImageTest.draw(url);
        BufferedImage bf = ImageTest.rotate(bft);*/
        try {
            ImageIO.write(bf, "jpg", new File(url));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public static Map<String,Object> getExif(String fileName){
        Map<String,Object> map = new HashMap<String,Object>();
        File file = new File(fileName);
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            map = printExif(file,metadata);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return map;
    }
    //获取exif信息，将旋转角度信息拿到
    private static Map<String,Object> printExif(File file, Metadata metadata){
        Map<String,Object> map = new HashMap<String,Object>();
        String tagName = null;
        String desc = null;
        for(Directory directory : metadata.getDirectories()){
            for(Tag tag : directory.getTags()){
                tagName = tag.getTagName();
                desc = tag.getDescription();
                if(tagName.equals("Orientation")){
                    map.put("Orientation", desc);
                }
            }
        }
        return map;
    }

    public static int getAngle(Map<String,Object> map){
        int ro = 0;
        if(map != null && map.size()>0){
            String ori = map.get("Orientation").toString();
            if(ori.indexOf("90")>=0){
                ro=1;
            }else if(ori.indexOf("180")>=0){
                ro=2;
            }else if(ori.indexOf("270")>=0){
                ro=3;
            }
        }
        return ro;
    }

    public static BufferedImage getBufferedImg(BufferedImage src, String url, int width, int height, int ro){
        int angle = (int)(90*ro);
        int type = src.getColorModel().getTransparency();
        int wid = width;
        int hei = height;
        if(ro%2!=0){
            int temp = width;
            width = height;
            height = temp;
        }
        Rectangle re = new Rectangle(new Dimension(width, height));
        BufferedImage BfImg = null;
        BfImg = new BufferedImage(re.width, re.height, type);
        Graphics2D g2 = BfImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        /*g2.translate((re.width-width)/2, (re.height-height)/2);*/
        g2.rotate(Math.toRadians(angle),re.width/2,re.height/2);
        g2.drawImage(src,(re.width-wid)/2,(re.height-hei)/2,null);
        g2.dispose();
        return BfImg;
    }

    public static int getHeight(File file){
        InputStream is = null;
        BufferedImage src = null;
        int height = -1;
        try {
            is = new FileInputStream(file);
            src = ImageIO.read(is);
            height = src.getHeight();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return height;
    }

    public static int getWidth(File file){
        InputStream is = null;
        BufferedImage src = null;
        int width = -1;
        try {
            is = new FileInputStream(file);
            src = ImageIO.read(is);
            width = src.getWidth();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return width;
    }


}
