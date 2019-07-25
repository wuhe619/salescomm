package com.bdaim.slxf.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zheng.liu@baifendian on 2015-08-10.
 */
public class DataNode<T> {
    private T id;
    private String labelId;
    private String name;
    private boolean checked;
    private List<DataNode<T>> children = new ArrayList<DataNode<T>>();

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DataNode<T>> getChildren() {
        return children;
    }

    public void setChildren(List<DataNode<T>> children) {
        this.children = children;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

	public String getLabelId() {
		return labelId;
	}

	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}

}
