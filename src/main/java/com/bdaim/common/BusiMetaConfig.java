package com.bdaim.common;

import java.util.HashMap;
import java.util.Map;

public class BusiMetaConfig {

	private static Map<String, BusiMeta> metas = new HashMap();
	
	public BusiMeta getMeta(String type) {
		return metas.get(type);
	}
	

	class BusiMeta{
		public String type = null;
		public String name = null;
		private Map<String,BusiMetaField> fields = new HashMap();
		
		public BusiMetaField getField(String fieldId) {
			return fields.get(fieldId);
		}
	}
	
	class BusiMetaField{
		String fieldId = null;
		String fieldName = null;
		String fieldType = null;
		Integer fieldLength = null;
		
	}
	
}
