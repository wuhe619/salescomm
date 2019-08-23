package com.bdaim.rbac.dto;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

/** 这种主要是生成一个树状结构
 */
public abstract class AbstractTreeResource implements Resource {

    private Long pid;
    private List<AbstractTreeResource> notes;
    private Integer sn;
    private Long ID;
    private String Uri;
    private String name;
    private String type;
    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public List<AbstractTreeResource> getNotes() {
        return notes;
    }

    public void setNotes(List<AbstractTreeResource> notes) {
        this.notes = notes;
    }

    public Integer getSn() {
        return sn;
    }

    public void setSn(Integer sn) {
        this.sn = sn;
    }

    @Override
    public Long getID() {
        return this.ID;
    }

    public void setID(Long ID){
        this.ID=ID;
    }

    @Override
    public String getUri() {
        return this.Uri;
    }

    public void setUri(String uri) {
        Uri = uri;
    }

    @Override
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public JSONObject getTree(){
//        JSONObject root=new JSONObject();
//        root.put("name",this.getName());
//        root.put("id",getID());
//        root.put("uri",getUri());
//        root.put("order",getSn());
//        root.put("type",getType().getId());
//        if (notes!=null&&notes.size()>0)root.put("note",toJArray(notes));
//        return root;
//    }
    
    public JSONArray toJArray(List<AbstractTreeResource> list){
        JSONArray rs=new JSONArray();
        if (list==null||list.size()==0)return null;
        JSONObject note=null;
        for (AbstractTreeResource r:list){
            note=new JSONObject();
            note.put("name",r.getName());
            note.put("id", r.getID());
            note.put("uri", r.getUri());
            note.put("order",r.getSn());
            note.put("type",r.getType());
            if (r.getNotes()!=null&&r.getNotes().size()>0)note.put("notes",toJArray(r.getNotes()));
            rs.add(note);
        }
        return rs;
    }
}
