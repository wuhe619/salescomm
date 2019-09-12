package com.bdaim.common.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dao.ChartConfigDao;
import com.bdaim.common.entity.ChartConfig;
import com.bdaim.label.dao.LabelInfoDao;
import com.bdaim.label.service.LabelCategoryService;
import com.bdaim.label.service.LabelInfoService;
import com.bdaim.label.service.LabelInterfaceService;
import com.bdaim.rbac.entity.User;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service("chartConfigService")
@Transactional
public class ChartConfigService  {
	@Resource
	private ChartConfigDao chartConfigDao;
	@Resource
	private LabelInterfaceService labelInterfaceService;
	@Resource
	private LabelInfoService labelInfoService;
	@Resource
	private LabelCategoryService labelCategoryService;
	@Resource
	private LabelInfoDao labelInfoDao;
	@Resource
	private ChartConfigSingleChartService chartConfigSingleChartService;
	
	
	
	public Integer addChartConfig(ChartConfig config) {
		if(null!=config.getId()){
			StringBuilder sql = new StringBuilder("update chart_config set title='");
			sql.append(config.getTitle()).append("', content='").append(config.getContent())
				.append("', show_channel=").append(config.getShowChannel()).append(", show_cycle=").append(config.getShowCycle())
				.append(" where id=").append(config.getId());
			chartConfigDao.executeUpdateSQL(sql.toString());
			return config.getId();
		}else{
			//judge is the title already exists...
			String sql = "select title FROM chart_config where title like '" + config.getTitle() + "%' ";
//			Query query = chartConfigDao.createQuery(sql, config.getTitle());
			Query query = chartConfigDao.getSQLQuery(sql);
			List lst = query.list();
			if (lst.size() > 0)
			{
				//if exists, then return -2
				return -2;
			}
			//if not exists, then insert...
			return (Integer)chartConfigDao.saveReturnPk(config);
		}
	}

	
	public void updateChartConfig(ChartConfig config) {
		chartConfigDao.update(config);
	}

	
	public ChartConfig getChartConfigById(Integer id) {
		return chartConfigDao.get(id);
	}

//	
//	public List<ChartConfig> getAllChartConfig() {
//		return chartConfigDao.getAll();
//	}

	
	public List<Map<String, Object>> getAllChartConfigTree() {
		
		List<ChartConfig> lst = chartConfigDao.createQuery("From ChartConfig where type=0 order by title", new Object[]{}).list();
		
		return ListToMap(lst);
	}

	private List<Map<String, Object>> ListToMap(List<ChartConfig> list) {
		Map<String, List<Map<String, Object>>> m = new HashMap<String, List<Map<String, Object>>>();
		int index = 1;
		for (ChartConfig cc : list) {
			try{
				String str = cc.getTitle();
				if (!str.contains(","))
					continue;
				String[] ss = str.split(",");
				String pre = "";
				String key = null;
				for (int i = 0; i < ss.length; i++) {
					if (pre.isEmpty()) {
						key = "root";
						pre = "root";
					} else {
						key = pre + "-" + ss[i - 1];
						pre=key;
					}
					List<Map<String, Object>> ll = new ArrayList<Map<String,Object>>();
					Map<String, Object> m1 = new HashMap<String, Object>();
					m1.put("key", key+"-"+ss[i] );
					m1.put("name", ss[i] );
					m1.put("index", index++);
					if(i==ss.length-1){
						m1.put("id", cc.getId());
						m1.put("content", JSONArray.parse(cc.getContent()));
						m1.put("showCycle", cc.getShowCycle());
						m1.put("showChannel", cc.getShowChannel());
					}
					if(m.containsKey(key))
						ll=m.get(key);
					
					boolean b = false;
					for(Map<String, Object> map : ll){
						String key1 = map.get("key").toString();
						String key2 = m1.get("key").toString();
						if(key1.equals(key2))
							b = true;
					}
					if(b) continue;
					ll.add(m1);
					m.put(key, ll);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		for (Entry<String, List<Map<String, Object>>> entry : m.entrySet()) {
			List<Map<String, Object>> ll = entry.getValue();
			for(Map<String, Object> mm:ll){
				mm.put("children", m.get(mm.get("key")));
				mm.remove("key");
			}
		}
		return m.get("root");
	}
	
	
	public List<ChartConfig> getAllChartConfigByUser(User user) {
		return null;
	}

	
	public String getMacroPicture(String cid, Integer cycle, String labelId
			, String categoryId, String interfaceId, String charttype
			) {
		if ("wordcloud".equals(charttype) || "map".equals(charttype))
		{//处理标签云图，地图
			try {
				Class cls = chartConfigSingleChartService.getClass();
				Method method = cls.getMethod(interfaceId, null);
				List<Map<String, String>> ret = (List<Map<String, String>>)method.invoke(this.chartConfigSingleChartService, null);
				JSONObject jo = new JSONObject();
				jo.put("data", ret);
				return jo.toJSONString();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else //所有其他的类型都套用以前的业务逻辑，即假设他是条，线，饼之类的图("bar".equals(charttype) || "line".equals(charttype) || "pie".equals(charttype))
		{//处理柱状图，饼状图，线形图
			Map<String, String> lstMap = getAllLabelChannel();
			String terms = "[{\"labelID\":\""+labelId+"\",\"type\":\"2\"}]";
			if("10003".equals(labelId)){
				terms = "[{\"labelID\":\""+labelId+"\",\"type\":\"4\"}]";
			}
			if(!StringUtils.isEmpty(categoryId)){
				terms = "{\"labelID\":\""+labelId+"\",\"categoryID\":\""+categoryId+"\",\"type\":\"4\"}";
			}
			
			StringBuilder cidsb = new StringBuilder("{\"labelID\":\"100170000100001\",\"values\":[\"");
			if(cid.indexOf(",")>0){
				String[] cidArr = cid.split(",");
				for(int i=0;i<cidArr.length;i++) {
					String id = cidArr[i];
					cidsb.append(lstMap.get(id));
					if(i==cidArr.length-1){
						cidsb.append("\"");
					}else{
						cidsb.append("\",\"");
					}
				}
				
				cidsb.append("],\"type\":\"2\"}");
			}else{
				cidsb.append(lstMap.get(cid))
					.append("\"],\"type\":\"2\"}");
			}
			
	 		String str = labelInterfaceService.macroPicture(cidsb.toString(), cycle, terms);
			
			return str;
		}
		return null;
	}
	
	
	public Map<String, String> getAllLabelChannel() {
		Map<String, String> result = new HashMap<String, String>();
		 List<Map<String,Object>> list = chartConfigDao.getSQLQuery("select t.id as \"id\",t.cid_cn as \"channel\" from label_channel t  ").setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
		 for(Map<String,Object> m:list){
			 result.put(m.get("id").toString(),m.get("channel").toString());
		 }
		 return result;
	}

	
	public List<Map<String, Object>> getLabelChannel(Long userId) {
		return chartConfigDao.getSQLQuery("select t.id as \"id\",t.cid_cn as \"channel\" from label_channel t left join t_user_channel_rel t1 on t.id = t1.CHANNEL_ID where t1.USER_ID="+userId).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
	}

	
	public void deleteMacroPicture(Integer id) {
		chartConfigDao.delete(id);
	}

	
	public List<ChartConfig> getAllChartConfig() {
		return chartConfigDao.createQuery("From ChartConfig order by title ", new Object[]{}).list();
	}

	
	public String getContentByName(String title) {
		Query query = chartConfigDao.createQuery("select new map(showChannel as showChannel ,content as content) from ChartConfig where title=?", new Object[]{title});
		Map<String, Object> map = (Map<String, Object>) query.uniqueResult();
		if(null!=map){
			Object content = JSONArray.parse(map.get("content").toString());
			map.put("content", content);
			return JSONObject.toJSONString(map);
		}
		return null;
	}

}
