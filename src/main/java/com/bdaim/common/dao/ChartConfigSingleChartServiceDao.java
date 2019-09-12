package com.bdaim.common.dao;

import org.hibernate.Query;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class ChartConfigSingleChartServiceDao extends SimpleHibernateDao<Map, Serializable>
{
	public List<Map<String, Integer>> getLabelCoverTopn(int topn)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(" select tli.labelId, tli.labelName, tlc.coverNum ");
		sb.append(" from label_info tli, label_cover tlc  ");
		sb.append(" where tli.id = tlc.id and (tli.`LEVEL`=3 or tli.`LEVEL`=4) ");
		sb.append("order by tlc.cover_num desc ;");
		Query query = super.getHqlQuery(sb.toString(), null, null, null);
		query.setFirstResult(1).setFetchSize(topn);
		List<Object[]> qur = query.list();
		List<Map<String, Integer>> rets = new ArrayList<Map<String, Integer>>();
		for (Object[] oneo : qur)
		{
			Map<String, Integer> onemmp = new HashMap<String, Integer>();
			onemmp.put((String)oneo[1], (Integer)oneo[2]);
		}
		return rets;
	}
	
	public List<Map<String, Integer>> getProvinceCoverNum() 
	{
		
		return null;
	}
	
}
