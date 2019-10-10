package com.bdaim.common;

import java.util.HashMap;
import java.util.Map;

public class BusiMetaConfig {

    private static Map<String, BusiMeta> metas = new HashMap();

    public BusiMeta getMeta(String type) {
        return metas.get(type);
    }


    class BusiMeta {
        public String type = null;
        public String name = null;
        public String idxSr = null;
        private Map<String, BusiMetaField> fields = new HashMap();

        public BusiMetaField getField(String fieldId) {
            return fields.get(fieldId);
        }
    }

    class BusiMetaField {
        String fieldId = null;
        String fieldName = null;
        String fieldType = null;
        Integer fieldLength = null;
        String fieldSave = null;
    }

    public static String getFieldIndex(String type, String field) {
        String newField = null;
        if ("sbd_f".equals(type) && "pid".equals(field)) {
            newField = "ext_4";
        } else if ("cust_id".equals(field) || "cust_user_id".equals(field) || "cust_group_id".equals(field)) {
            newField = field;
        } else if (field.startsWith("_g_")) {
            newField = "JSON_EXTRACT(content, '$." + field.substring(3) + "') ";
        } else if (field.startsWith("_ge_")) {
            newField = "JSON_EXTRACT(content, '$." + field.substring(4) + "') ";
        } else if (field.startsWith("_l_")) {
            newField = "JSON_EXTRACT(content, '$." + field.substring(3) + "') ";
        } else if (field.startsWith("_le_")) {
            newField = "JSON_EXTRACT(content, '$." + field.substring(4) + "') ";
        } else if (field.startsWith("_eq_")) {
            newField = "JSON_EXTRACT(content, '$." + field.substring(4) + "') ";
        } else {
            newField = "JSON_EXTRACT(content, '$." + field + "')";
        }

        return newField;
    }
}
