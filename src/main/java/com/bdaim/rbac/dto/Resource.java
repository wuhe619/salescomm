package com.bdaim.rbac.dto;

/**
 */
public interface Resource {
    /**
     * 获得一个Resource的ID
     * @return
     */
    public Long getID();

    /**
     * 获得一个对象的URI，这里的URI指的是一个资源的连接、标识或访问路径
     * @return
     */
    public String getUri();

    /**
     * 因为所有资源的处理方式是根据类型来进行定制的，所以类型采用的枚举的方式进行定义
     * @return
     */
    public String getType();

    /**
     * 获得当前Resource的名称
      * @return
     */
    public String getName();
}
