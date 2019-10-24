/*  1:   */ package com.bdaim.common.spring;
/*  2:   */ 
/*  3:   */

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/*  4:   */
/*  5:   */
/*  6:   */
/*  7:   */
/*  8:   */
/*  9:   */
/* 10:   */

/* 11:   */
/* 12:   */ public class ConfigPropertiesHolder
/* 13:   */   extends PropertyPlaceholderConfigurer
/* 14:   */ {
/* 15:19 */   private static Map<String, Object> conf = new HashMap();
/* 16:   */   
/* 17:   */   protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
/* 18:   */     throws BeansException
/* 19:   */   {
/* 20:23 */     super.processProperties(beanFactoryToProcess, props);
/* 21:24 */     Enumeration e = props.keys();
/* 22:25 */     String key = "";
/* 23:26 */     while (e.hasMoreElements())
/* 24:   */     {
/* 25:27 */       key = e.nextElement().toString();
/* 26:28 */       if (!"".equals(key)) {
/* 27:   */         try
/* 28:   */         {
/* 29:30 */           conf.put(key, new String(props.getProperty(key).getBytes("ISO-8859-1"), "utf-8"));
/* 30:   */         }
/* 31:   */         catch (UnsupportedEncodingException e1)
/* 32:   */         {
/* 33:32 */           e1.printStackTrace();
/* 34:   */         }
/* 35:   */       }
/* 36:   */     }
/* 37:   */   }
/* 38:   */   
/* 39:   */   public static Object getConf(String confKey)
/* 40:   */   {
/* 41:43 */     return conf.get(confKey);
/* 42:   */   }
/* 43:   */ }



/* Location:           C:\Users\max\.m2\repository\com\ztwj\ext\1.0.1.RELEASE\ext-1.0.1.RELEASE.jar

 * Qualified Name:     com.bdaim.spring.ConfigPropertiesHolder

 * JD-Core Version:    0.7.0.1

 */