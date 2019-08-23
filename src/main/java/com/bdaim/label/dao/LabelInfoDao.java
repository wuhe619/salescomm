package com.bdaim.label.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.label.entity.LabelInfo;
import com.bdaim.label.vo.LabelPriceSumVO;
import com.bdaim.label.vo.LabelPriceVO;
import org.hibernate.Query;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
public class LabelInfoDao extends SimpleHibernateDao<LabelInfo, Serializable> {

    public List<LabelPriceVO> getLabelAllPrice(Integer sourceId, String labelsId, Integer industryPoolId) {
        StringBuilder sb = new StringBuilder();
		/*sb.append(
				"SELECT a.industry_pool_id as industryPoolId, b.source_id as sourceId, a.label_id as labelId, d.label_name as labelName, b.price as salePrice, c.price as sourcePrice FROM	t_industry_label a LEFT JOIN t_label_sale_price b ON a.label_id = b.industry_label_id  and b.source_id = ");
		sb.append(sourceId);
		sb.append(" LEFT JOIN t_label_source_price c ON a.label_id = c.label_id and c.source_id = ");
		sb.append(sourceId);
		sb.append(" LEFT JOIN label_info d on a.label_id = d.label_id where a.industry_pool_id = ");
		sb.append(industryPoolId);
		sb.append(" and a.label_id in (");
		sb.append(labelsId);
		sb.append(")");*/
        //modify
        sb.append(" SELECT ");
        sb.append(" 	b.industry_pool_id AS industryPoolId,");
        sb.append(" 	c.source_id AS sourceId,");
        sb.append(" 	CAST(b.industry_label_id AS CHAR) AS labelId,");
        sb.append(" 	d.label_name AS labelName,");
        sb.append(" 	b.price AS salePrice,");
        sb.append(" 	c.price AS sourcePrice");
        sb.append(" FROM ");
        sb.append(" 	t_industry_label b ");
        sb.append(" LEFT JOIN t_label_source_price c ON b.industry_label_id = c.label_id ");
        sb.append(" AND c.source_id=" + sourceId);
        sb.append(" LEFT JOIN label_info d ON b.industry_label_id = d.label_id ");
        sb.append(" WHERE ");
        sb.append(" 	b.industry_pool_id =" + industryPoolId);
        //sb.append(" AND c.source_id =" + sourceId);
        sb.append(" AND b.label_id IN (" + labelsId + ")");
        Query query = super.getSQLQuery(sb.toString());
        query.setResultTransformer(Transformers.aliasToBean(LabelPriceVO.class));
        List<LabelPriceVO> list = query.list();
        return list;
    }

    public LabelPriceSumVO getLabelSumPrice(Integer sourceId, String labelsId, Integer industryPoolId) {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT ");
        sb.append(" 	SUM(b.price) AS salePrice, ");
        sb.append(" 	SUM(c.price) AS sourcePrice ");
        sb.append(" FROM ");
        sb.append(" 	t_industry_label b ");
        sb.append(" LEFT JOIN t_label_source_price c ON b.industry_label_id = c.label_id ");
        sb.append(" AND c.source_id=" + sourceId);
        sb.append(" WHERE ");
        sb.append(" 	b.industry_pool_id =" + industryPoolId);
        //sb.append(" AND c.source_id =" + sourceId);
        sb.append(" AND b.label_id IN (" + labelsId + ")");
        Query query = super.getSQLQuery(sb.toString());
        query.setResultTransformer(Transformers.aliasToBean(LabelPriceSumVO.class));
        List<LabelPriceSumVO> list = query.list();
        LabelPriceSumVO sumPrice = null;
        if (list.size() > 0) {
            sumPrice = list.get(0);
        }
        return sumPrice;
    }

}
