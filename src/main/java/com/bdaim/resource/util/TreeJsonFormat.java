package com.bdaim.resource.util;


import com.bdaim.rbac.dto.AbstractTreeResource;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 */
public class TreeJsonFormat {

    public static JSONArray format(AbstractTreeResource tree){
        JSONArray treeJson=new JSONArray();
        if (tree!=null&&tree.getNotes()!=null&&tree.getNotes().size()>0){
            JSONObject jsonObject=null;
            JSONArray children=null;
            for (AbstractTreeResource item:tree.getNotes()){
                jsonObject=new JSONObject();
                jsonObject.put("id",String.valueOf(item.getID()));
                jsonObject.put("text",item.getName());
                jsonObject.put("type", item.getType());
                if ((children=format(item))!=null){
                    jsonObject.put("children",children);
                }else {
                    //jsonObject.put("type","file");
                }
                treeJson.add(jsonObject);
            }
        }
        if (treeJson.size()==0)treeJson=null;
        return treeJson;
    }

}
