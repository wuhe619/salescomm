package com.bdaim.pay.config;

public class AlipayConstants {

    //沙箱环境网关
    public static final String SERVER_URL_DEV = "https://openapi.alipaydev.com/gateway.do";
    //正式环境网关
    public static final String SERVER_URL_PRO = "https://openapi.alipay.com/gateway.do";

    // APP_ID (沙箱应用)
    public static final String APP_ID_DEV = "2016101700708049";
    // APP_ID (正式环境)
    public static final String APP_ID_PRO = "";

    // 签名方式
    public static final String SIGN_TYPE = "RSA2";

    //沙箱环境私钥
    public static final String APP_PRIVATE_KEY_DEV = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCT6FzOYVsEmHmwxXrD0XqOT3ntslynEhDNoJaoDK6Kwn72ZL+4uWSVCDjwc4EwXlG9wKGAcPa7evpj6ahDI6b3WqrUEmbWnLR1oczQ5C55sZv9RYhk3qnuNqHNVxT8kGSbJT8yp44SRlTW82Xhjxpwv3tIE64FavMGAhrnoaIuDgVyGPbqnZkClgTm1x8EvOI2ZrFwGJ3jcH6NIlLbP8yxIwSbskBzFeEXBxPKCxUmShjQDBG6hT0z+v+LD8vccwx+/X9TxlArPfnZh41mUx6+pnnNuRitw613KUlQ+cYYKTjThK4Q2vdYai8XbPwPmeBCSXk2bWP/4JDCOo7kg1NNAgMBAAECggEAZrdi5Cx2Mi3VrO6oAB+VFgmZqnpIn/oK01Kn7dLI0DKcS27SOm39rQYFzO7aFAYcjEOfpPxOTQrtor9dDCtRQ8yigB67bmVraZnRDGy63eZ7ZxuHyk1cA8PLADhuqat5QslOJ1Z3creHKbPk2A0yloRUEE0ieD66wTxrBNuaLpKWw9kBcP3TACaf07ZUW0k8fyOcXBfF0+5qR6qtlJPT0/DlGNZQ9XPd3O79scYnb/Pfwhbu7PYHSndM2/Ts+hSQAuonbCUvXITFcb1S6ls2nDOAniggC7oNRO/NSAqOEczbKaw5eziirY9Xb9NjUU9DMWfZJ1B8dFrcqEKP+nG/XQKBgQD02MF+KFxEmKlRpjprCdwty2248RjHy9TslkwjLvbds+LhWVdfw6m7dE6rbcAmQdKsa/LfSTFf2PKmBZa3fE8LoDqU1FNR/8bZ+3KtEwpDLYN5EH2A0iuur7zR1BcEP+wF0TkvZRKcdZq3PglD1XI5F+KRoMES4ARGNHvCl6DAGwKBgQCapSqOa3gcAMzB3ReddHh6o12oJeE7fdYiUVhTNuY7xjVIHqtNPpoTu62h+H+7Tp73lloRQGRzYGgU1f4iQeeMQqxWF6KUxkq6ePu5aMxxk775GkjlBljXIr3LRQWBos9QY8f7NNuHyZJtn1ZZSGYvsoyZQkVm3uX3O7wnL24AtwKBgQDmy6sYkFLpFj4EiPxoCVNSl39F5X3GV4zNtp7uS0bIlNg4M075JibIXEoLrdiCN2muvdKiRwwTnRydAoefTb3054RQ5hPkJ4X509u06U2zJSPY6oJi+7nRYDZMU++eQeLpWN7enhd9BB5ivEdfecPBrZIUU8OrVx6wTqiaNPOCIQKBgQCJXO6JJPLCRP+br+x7AurFXHkjk8oV0vey8fh3qgslyzCmx/83FMU9g/Z+KcIjuXlAyL9mFYFkbpzk7RPYkBvBnbOAoWvbpKraKf7GuUzoEemPUQaw0Et3hTd65+s1NE3bxVXsUA4z9htn3iGQGtwRwBJdS91ju9gfHGNLlzipzwKBgEOA0AtUZvaw0vOpRPlJA0ijhUIv944vpXx90LU2f7sFx8R6CKj2Kjz+DqVxm5NIZ3Pu7IInDeeTOe2K+VRDeCnOEu1esNar+jFV0piqiJyAcqwn7xYWl+kWLzSkQXkiu7UJ/Oejk5w5BjMR1VdiN7K3AWLvRzDX3QqPVZsdB42+";
    //正式环境私钥
    public static final String APP_PRIVATE_KEY_PRO = "";

    //沙箱环境 支付宝公钥
    public static final String PUBLIC_KEY_DEV = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgU6A6ID6p7KbONrl55Wws6b6R7TfSwTEzUSigEcJJEty09/pXpAr9QdIXQFB+zV6cT8wNcQ9JhL+aVwNsDoCszIVRAZ3MRVyz+9HIxKmPub9VFLBkbqklf4178On2aBzOsWVKIJFeHbL7DoZmzG68kI0Up/uRA+/g3/2hC/Bld2UobWbRhC1z2azkqDu0kqNMIrgN2XamxC7FUKZkW0JxRfRvdyWXvmID1+GSDiYYDWKBSLhohTh3S2Tc6KoIxiuvibvLf+NKteCn+DFIyAooaGOo+En5aEKDLqcbPYQDtZ/97wYUKzEEeDBavf0leVTpYFpY5AbQzG+PlirOMdwzwIDAQAB";
    //正式环境 公钥 查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APP_ID下的支付宝公钥
    public static final String PUBLIC_KEY_PRO = "";

    //支付完成的跳转链接地址 测试环境
    public static final String RETURN_URL_DEV = "http://m.idearclass.com/#/course/paymentresult";
    //支付完成的跳转链接地址 正式环境
    public static final String RETURN_URL_PRO = "";

    //支付结果异步通知地址(必须是外网可以访问的地址)
    public static final String NOTIFY_URL_DEV = "http://bp.online.datau.top/packages/getAliPayResult";
    public static final String NOTIFY_URL_PRO = "";


}
