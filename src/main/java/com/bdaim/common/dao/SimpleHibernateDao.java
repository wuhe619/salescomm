package com.bdaim.common.dao;

import com.bdaim.common.dto.Page;
import com.bdaim.util.Constant;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.ReflectionUtils;
import com.bdaim.util.StringHelper;
import org.hibernate.*;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 封装Hibernate原生API的DAO泛型基类.
 * <p>
 * 可在Service层直接使用,也可以扩展泛型DAO子类使用.
 * 参考Spring2.5自带的Petlinc例子,取消了HibernateTemplate,直接使用Hibernate原生API.
 */
@SuppressWarnings("unchecked")
@Transactional
public class SimpleHibernateDao<T, PK extends Serializable> extends HibernateDaoSupport {
    protected Logger logger = LoggerFactory.getLogger(getClass());

//    protected SessionFactory sessionFactory;

    @Autowired
    public JdbcTemplate jdbcTemplate;

    @Autowired
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected Class<T> entityClass;

    /**
     * 用于Dao层子类使用的构造函数. 通过子类的泛型定义取得对象类型Class. eg. public class UserDao extends
     * SimpleHibernateDao<User, String>
     */
    public SimpleHibernateDao() {
        this.entityClass = ReflectionUtils.getSuperClassGenricType(getClass());
    }

    /**
     * 用于用于省略Dao层, 在Service层直接使用通用SimpleHibernateDao的构造函数. 在构造函数中定义对象类型Class.
     * eg. SimpleHibernateDao<User, String> userDao = new
     * SimpleHibernateDao<User, String>(sessionFactory, User.class);
     */
//    public SimpleHibernateDao(final SessionFactory sessionFactory,
//                              final Class<T> entityClass) {
//        this.sessionFactory = sessionFactory;
//        this.entityClass = entityClass;
//    }

//    /**
//     * 取得sessionFactory.
//     */
//    public SessionFactory getSessionFactory() {
//        return sessionFactory;
//    }

    /**
     * 采用@javax.annotation.Resource按类型注入SessionFactory,
     * 当有多个SesionFactory的时候Override本函数.
     */
//    @javax.annotation.Resource
//    public void setSessionFactory(final SessionFactory sessionFactory) {
//        this.sessionFactory = sessionFactory;
//    }
    @Autowired
    public void setSessionFactoryOverride(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    /**
     * 取得当前Session.
     */
    public Session getSession() {
        try {
            return super.getSessionFactory().getCurrentSession();
        } catch (HibernateException ex) {
//            ex.printStackTrace();
            logger.error("can not get current session,open new.");
            return getSessionFactory().openSession();
        }
    }

    public void saveOrUpdate(Object entity) {
        getSession().saveOrUpdate(entity);
        getSession().flush();
    }

    /**
     * 保存新增或修改的对象.
     */
    public void save(final T entity) {
        org.springframework.util.Assert.notNull(entity, "entity不能为空");
        getSession().save(entity);
        getSession().flush();
    }

    public Serializable saveReturnPk(final T entity) {
        org.springframework.util.Assert.notNull(entity, "entity不能为空");
        Serializable Pk = getSession().save(entity);
        getSession().flush();
        return Pk;
    }

    public Serializable saveReturnPkNoSession(final T entity) {
        org.springframework.util.Assert.notNull(entity, "entity不能为空");
        Session ses = null;
        try {
            ses = getSession();
            Serializable Pk = ses.save(entity);
            ses.flush();
            return Pk;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }/* finally {
            if (null != ses) {
                ses.close();
            }
        }*/
    }

    /**
     * 保存修改的对象.
     */
    public void update(final T entity) {
        org.springframework.util.Assert.notNull(entity, "entity不能为空");
        // getSession().refresh(entity);
        getSession().merge(entity);
        // getSession().update(entity);
        getSession().flush();
    }

    /**
     * 删除对象.
     *
     * @param entity 对象必须是session中的对象或含id属性的transient对象.
     */
    public void delete(final T entity) {
        org.springframework.util.Assert.notNull(entity, "entity不能为空");
        getSession().delete(entity);
        getSession().flush();
    }

    /**
     * 按id删除对象.
     */
    public void delete(final PK id) {
        org.springframework.util.Assert.notNull(id, "id不能为空");
        delete(get(id));
        getSession().flush();
    }

    /**
     * 按id获取对象.
     */
    public T get(final PK id) {
        org.springframework.util.Assert.notNull(id, "id不能为空");
        return (T) this.getSession().get(entityClass, id);
    }

    public Object get(Class clazz, Serializable id) {
        return this.getSession().get(clazz, id);
    }

    /**
     * 获取全部对象.
     */
    public List<T> getAll() {
        return find();
    }

    /**
     * 获取全部对象,支持排序.
     */
    public List<T> getAll(String orderBy, boolean isAsc) {
        Criteria tempCriteria = this.createCriteria();
        if (isAsc) {
            tempCriteria.addOrder(Order.asc(orderBy));
        } else {
            tempCriteria.addOrder(Order.desc(orderBy));
        }
        return tempCriteria.list();
    }

    /**
     * 按属性查找对象列表,匹配方式为相等.
     */
    public List<T> findBy(final String propertyName, final Object value) {
        org.springframework.util.Assert.hasText(propertyName,
                "propertyName不能为空");
        Criterion criterion = Restrictions.eq(propertyName, value);
        return find(criterion);
    }

    /**
     * 按属性查找唯一对象,匹配方式为相等.
     */
    public T findUniqueBy(final String propertyName, final Object value) {
        org.springframework.util.Assert.hasText(propertyName,
                "propertyName不能为空");
        Criterion criterion = Restrictions.eq(propertyName, value);
        return (T) this.createCriteria(criterion).uniqueResult();
    }

    /**
     * 功能: 根据HQL查询条目总数
     *
     * @param hql
     * @param values
     * @return int 创建时间:2011-5-10 18:09:47
     */
    public int findCount(final String hql, final Object... values) {
        return Integer.parseInt(createQuery(hql, values).uniqueResult()
                .toString());
    }

    /**
     * 按id列表获取对象.
     */
    public List<T> findByIds(List<PK> ids) {
        return find(Restrictions.in(getIdName(), ids));
    }

    /**
     * 按HQL查询对象列表.
     *
     * @param values 数量可变的参数,按顺序绑定.
     */
    public <X> List<X> find(final String hql, final Object... values) {
        return createQuery(hql, values).list();
    }

    public <X> List<X> find(final String hql, final List values) {
        return createQuery(hql, values).list();
    }

    public <X> List<X> findWithPositionalParams(final String hql, final Object... values) {
        return queryWithPositionalParameters(hql, values).list();
    }

    /**
     * 按HQL查询对象列表.
     *
     * @param values 命名参数,按名称绑定.
     */
    public <X> List<X> find(final String hql, final Map<String, ?> values) {
        return createQuery(hql, values).list();
    }

    public Page page(final String hql, final List values, int startIndex, int maxSize) {
        if (startIndex < 0) startIndex = 0;
        if (maxSize < 0 || maxSize > 100) maxSize = 100;

        Page p = new Page();
        p.setTotal(this.findCount(hql, values));
        p.setData(this.createQuery(hql, values).setFirstResult(startIndex).setMaxResults(maxSize).list());
        return p;
    }

    /**
     * pageSize不做限制
     *
     * @param sql
     * @param pageNum
     * @param pageSize
     * @param values
     * @return
     */
    public Page sqlPageQueryByPageSize(String sql, int pageNum, int pageSize, final Object... values) {
        if (pageNum < 0) {
            pageNum = 0;
        }
        if (pageSize < 0) {
            pageSize = 100;
        }
        int total = 0;
        Page p = new Page();
        StringBuilder totalSql = new StringBuilder();
        totalSql.append("select count(*) count from (");
        totalSql.append(sql);
        totalSql.append(") as temp");

        Session session = getSession();
        Query query = session.createSQLQuery(totalSql.toString());
        query.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        // 先查询total
        List<Map<String, Object>> totalList = query.list();
        if (totalList.size() > 0) {
            total = NumberConvertUtil.parseInt(String.valueOf(totalList.get(0).get("count")));
        }
        p.setTotal(total);

        //查询分页数据
        query = session.createSQLQuery(sql);
        query.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        // 先查询total
        query.setFirstResult(pageNum);
        query.setMaxResults(pageSize);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        List rs = query.list();
        p.setData(rs);
        return p;
    }

    public int findCount(final String hql, final List values) {
        String hql_count = null;
        if (hql.trim().startsWith("select")) {
            hql_count = "select count(*) " + hql.substring(hql.indexOf("from"));
        } else {
            hql_count = "select count(*) " + hql;
        }
        Object r = createQuery(hql_count, values).uniqueResult();
        if (r == null)
            return 0;
        return Integer.parseInt(r.toString());
    }

    /**
     * 按HQL查询唯一对象.
     *
     * @param values 数量可变的参数,按顺序绑定.
     */
    public <X> X findUnique(final String hql, final Object... values) {
        return (X) createQuery(hql, values).uniqueResult();
    }

    /**
     * 按HQL查询唯一对象.
     *
     * @param values 命名参数,按名称绑定.
     */
    public <X> X findUnique(final String hql, final Map<String, ?> values) {
        return (X) createQuery(hql, values).uniqueResult();
    }

    /**
     * 执行HQL进行批量修改/删除操作.
     */
    public int batchExecute(final String hql, final Object... values) {
        return createQuery(hql, values).executeUpdate();
    }

    /**
     * 执行HQL进行批量修改/删除操作.
     *
     * @return 更新记录数.
     */
    public int batchExecute(final String hql, final Map<String, ?> values) {
        return createQuery(hql, values).executeUpdate();
    }

    /**
     * 执行HQL进行批量修改/删除操作.
     */
    public int batchExecute(final String hql, final Map<String, ?> map, final Map<String, ?> likeMap) {
        return getQuery(hql, map, likeMap, null).executeUpdate();
    }

    /**
     * 根据查询HQL与参数列表创建Query对象.
     * <p>
     * 本类封装的find()函数全部默认返回对象类型为T,当不为T时使用本函数.
     *
     * @param values 数量可变的参数,按顺序绑定.
     */
    public Query createQuery(final String queryString, final Object... values) {
        org.springframework.util.Assert.hasText(queryString, "queryString不能为空");
        Query query = getSession().createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        return query;
    }

    public Query queryWithPositionalParameters(final String queryString, final Object... values) {
        org.springframework.util.Assert.hasText(queryString, "查询语句不能为空");
        Query query = getSession().createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (i == 0) {
                    query.setParameter("0", values[i]);

                } else if (i == 1) {
                    query.setParameter("1", values[i]);
                }
            }
        }
        return query;
    }

    /**
     * 根据查询HQL与参数列表创建Query对象.
     *
     * @param values 命名参数,按名称绑定.
     */

    public Query createQuery(final String queryString, List values) {
        org.springframework.util.Assert.hasText(queryString, "queryString不能为空");
        Query query = getSession().createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                query.setParameter(i, values.get(i));
            }
        }
        return query;
    }

    /**
     * 根据查询HQL与参数列表创建Query对象.
     *
     * @param values 命名参数,按名称绑定.
     */
    public Query createQuery(final String queryString,
                             final Map<String, ?> values) {
        org.springframework.util.Assert.hasText(queryString, "queryString不能为空");
        Query query = getSession().createQuery(queryString);
        if (values != null) {
            query.setProperties(values);
        }
        return query;
    }

    /**
     * 根据查询HQL与参数列表创建Query对象.
     *
     * @param values 命名参数,按名称绑定.
     */
    public Query getQuery(final String queryString,
                          final Map<String, ?> values, final Map<String, ?> isLike,
                          String orderByName) {
        org.springframework.util.Assert.hasText(queryString, "queryString不能为空");
        StringBuffer sql = new StringBuffer(queryString);
        // 一般查询
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                sql.append(" And ");
                sql.append("t." + key);

                sql.append(" = ");
                sql.append(":");
                sql.append(key);

            }

        }
        // 模糊查询
        for (Map.Entry<String, ?> entry : isLike.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                sql.append(" And ");
                sql.append("t." + key);

                sql.append(" like ");
                sql.append(":");
                sql.append(key);

            }

        }
        if (StringHelper.isNotBlank(orderByName)) {
            sql.append(" ORDER BY t." + orderByName);
        }
        Query query = getSession().createQuery(sql.toString());
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {

                query.setParameter(key, value);
            }
        }
        for (Map.Entry<String, ?> entry : isLike.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                query.setParameter(key, "%" + value + "%");
            }
        }
        return query;
    }

    /**
     * 根据查询HQL与参数列表创建Query对象.
     *
     * @param values 命名参数,按名称绑定.
     */
    public Query getQuery(final String queryString,
                          final Map<String, ?> values, String orderByName) {
        org.springframework.util.Assert.hasText(queryString, "queryString不能为空");
        StringBuffer sql = new StringBuffer(queryString);

        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                sql.append(" And ");
                sql.append("t." + key);

                sql.append("=");
                sql.append(":");
                sql.append(key);

            }

        }
        if (StringHelper.isNotBlank(orderByName)) {
            sql.append(" ORDER BY t." + orderByName);
        }
        Query query = getSession().createQuery(sql.toString());
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                query.setParameter(key, value);
            }
        }

        return query;
    }

    /**
     * 根据查询HQL与参数列表创建Query对象.
     *
     * @param values 命名参数,按名称绑定.
     */
    public Query getSqlQuery(final String queryString,
                             final Map<String, ?> values) {
        org.springframework.util.Assert.hasText(queryString, "queryString不能为空");
        StringBuffer sql = new StringBuffer(queryString);

        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                sql.append(" And ");
                sql.append("t." + key);

                sql.append("=");
                sql.append(":");
                sql.append(key);

            }

        }
        Query query = getSession().createSQLQuery(sql.toString());
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                query.setParameter(key, value);
            }
        }

        return query;
    }

    /**
     * 按Criteria查询对象列表.
     *
     * @param criterions 数量可变的Criterion.
     */
    public List<T> find(final Criterion... criterions) {
        return this.createCriteria(criterions).list();
    }

    /**
     * 按Criteria查询唯一对象.
     *
     * @param criterions 数量可变的Criterion.
     */
    public T findUnique(final Criterion... criterions) {
        return (T) this.createCriteria(criterions).uniqueResult();
    }

    /**
     * 根据Criterion条件创建Criteria.
     * <p>
     * 本类封装的find()函数全部默认返回对象类型为T,当不为T时使用本函数.
     *
     * @param criterions 数量可变的Criterion.
     */
    public Criteria createCriteria(final Criterion... criterions) {
        Criteria criteria = getSession().createCriteria(entityClass);
        for (Criterion tempCriterion : criterions) {
            criteria.add(tempCriterion);
        }
        return criteria;
    }

    /**
     * 初始化对象. 使用load()方法得到的仅是对象Proxy, 在传到View层前需要进行初始化.
     * 只初始化entity的直接属性,但不会初始化延迟加载的关联集合和属性. 如需初始化关联属性,可实现新的函数,执行:
     * Hibernate.initialize(user.getRoles())，初始化User的直接属性和关联集合.
     * Hibernate.initialize
     * (user.getDescription())，初始化User的直接属性和延迟加载的Description属性.
     */
    public void initEntity(T entity) {
        Hibernate.initialize(entity);
    }

    /**
     * @see #initEntity(Object)
     */
    public void initEntity(List<T> entityList) {
        for (T entity : entityList) {
            Hibernate.initialize(entity);
        }
    }

    /**
     * Flush当前Session.
     */
    public void flush() {
        getSession().flush();
    }

    /**
     * 为Query添加distinct transformer.
     */
    public Query distinct(Query query) {
        query.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return query;
    }

    /**
     * 为Criteria添加distinct transformer.
     */
    public Criteria distinct(Criteria criteria) {
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return criteria;
    }

    /**
     * 取得对象的主键名.
     */
    public String getIdName() {
        ClassMetadata meta = getSessionFactory().getClassMetadata(entityClass);
        return meta.getIdentifierPropertyName();
    }

    /**
     * 执行原生的sql语句修改操作
     *
     * @param sqlStr
     * @return
     */
    public int executeUpdateSQL(String sqlStr) {
        Session session = getSession();
        int count = session.createSQLQuery(sqlStr).executeUpdate();
        flush();
        //session.close();
        return count;
    }


    public int executeUpdateSQL(final String sql, final Object... values) {
        Query query = getSession().createSQLQuery(sql);
        int i = 0;
        for (Object v : values)
            query.setParameter(i++, v);
        return query.executeUpdate();
    }

    public Query getSqlQuery(final String queryString,
                             final Map<String, ?> values, final Map<String, ?> isLike) {
        org.springframework.util.Assert.hasText(queryString, "queryString不能为空");
        StringBuffer sql = new StringBuffer(queryString);

        // 一般查询
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                sql.append(" And ");
                sql.append("t." + key);

                sql.append(" =");
                sql.append(":");
                sql.append(key);

            }

        }
        // 模糊查询
        if (null != isLike && !isLike.isEmpty()) {
            boolean is = false;
            for (Map.Entry<String, ?> entry : isLike.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value != null && StringHelper.isNotBlank(value.toString())) {
                    if (is) {
                        sql.append(" Or ");
                        sql.append("t." + key);
                        sql.append(" like ");
                        sql.append(":");
                        sql.append(key);
                    } else {
                        sql.append(" And ");
                        sql.append("(");
                        sql.append("t." + key);
                        sql.append(" like ");
                        sql.append(":");
                        sql.append(key);
                        is = true;
                    }
                }
            }
            sql.append(")");
        }
        Query query = getSession().createSQLQuery(sql.toString());
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {

                query.setParameter(key, value);
            }
        }
        for (Map.Entry<String, ?> entry : isLike.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {

                query.setParameter(key, "%" + value + "%");
            }
        }

        return query;
    }

    /**
     * 获取执行原始sql的query
     *
     * @param sql
     * @return
     */
    public Query getSQLQuery(String sql) {
        return getSession().createSQLQuery(sql);
    }

    public Query getHqlQuery(final String queryString,
                             final Map<String, ?> map, final Map<String, ?> likeMap,
                             String orderBy) {
        org.springframework.util.Assert.hasText(queryString, "queryString不能为空");
        StringBuffer sql = new StringBuffer(queryString);
        // 一般查询
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(Constant.FILTER_KEY_PREFIX))
                continue;
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                if (value.equals("null")) {
                    sql.append(" And ");
                    sql.append("t." + key);
                    sql.append(" is null ");
                } else {
                    sql.append(" And ");
                    sql.append("t." + key);
                    sql.append(" =" + value);
                }
            }
        }
        if (!likeMap.isEmpty()) {
            boolean is = false;
            for (Map.Entry<String, ?> entry : likeMap.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(Constant.FILTER_KEY_PREFIX))
                    continue;
                Object value = entry.getValue();
                if (value != null && StringHelper.isNotBlank(value.toString())) {
                    if (is) {
                        sql.append(" Or ");
                        sql.append("t." + key);
                        sql.append(" like ");
                        sql.append("'%" + value + "%'");
                    } else {
                        sql.append(" And ");
                        sql.append("(");
                        sql.append("t." + key);
                        sql.append(" like ");
                        sql.append("'%" + value + "%'");
                        is = true;
                    }
                }
            }
            sql.append(")");
        }
        if (null != orderBy && (!orderBy.isEmpty())) {
            sql.append(" order by t." + orderBy + " desc");
        }
        Query query = getSession().createQuery(sql.toString());
        return query;
    }

    public Query getHqlQuery(final String queryString,
                             final Map<String, ?> map, final Map<String, ?> orLikeMap,
                             final Map<String, ?> andLikeMap, String orderBy) {
        org.springframework.util.Assert.hasText(queryString, "queryString不能为空");
        StringBuffer sql = new StringBuffer(queryString);
        // 一般查询
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(Constant.FILTER_KEY_PREFIX))
                continue;
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                sql.append(" And ");
                sql.append("t." + key);
                sql.append(" =" + value);
            }
        }
        for (Map.Entry<String, ?> entry : andLikeMap.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(Constant.FILTER_KEY_PREFIX))
                continue;
            Object value = entry.getValue();
            if (value != null && StringHelper.isNotBlank(value.toString())) {
                sql.append(" And ");
                sql.append("t." + key);
                sql.append(" like ");
                if (key.equals("uri")) {
                    sql.append("'" + value + "%'");
                } else {
                    sql.append("'%" + value + "%'");
                }
            }
        }
        if (!orLikeMap.isEmpty()) {
            boolean is = false;
            for (Map.Entry<String, ?> entry : orLikeMap.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(Constant.FILTER_KEY_PREFIX))
                    continue;
                Object value = entry.getValue();
                if (value != null && StringHelper.isNotBlank(value.toString())) {
                    if (is) {
                        sql.append(" Or ");
                        sql.append("t." + key);
                        sql.append(" like ");
                        if (key.equals("uri")) {
                            sql.append("'" + value + "%'");
                        } else {
                            sql.append("'%" + value + "%'");
                        }

                    } else {
                        sql.append(" And ");
                        sql.append("(");
                        sql.append("t." + key);
                        sql.append(" like ");
                        if (key.equals("uri")) {
                            sql.append("'" + value + "%'");
                        } else {
                            sql.append("'%" + value + "%'");
                        }
                        is = true;
                    }
                }
            }
            sql.append(") ");
        }
        if (null != orderBy && (!orderBy.isEmpty())) {
            sql.append(" order by t." + orderBy);
        }
        Query query = getSession().createQuery(sql.toString());
        return query;
    }

    public List<Map<String, Object>> sqlQuery(String sql, final Object... values) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql);
        query.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (values != null)
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        List rs = query.list();
        return rs;
    }

    public Map<String, Object> queryUniqueSql(String sql, Object... params) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql);
        query.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (params != null)
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i, params[i]);
            }
        List rs = query.list();
        return rs != null && rs.size() > 0 ? (Map<String, Object>) rs.get(0) : null;
    }


    /**
     * sql分页
     *
     * @param sql
     * @param pageNum
     * @param pageSize
     * @param values
     * @return
     */
    public Page sqlPageQuery0(String sql, int pageNum, int pageSize, final Object... values) {
        if (pageNum < 0) {
            pageNum = 0;
        }
        if (pageSize < 0 || pageSize > 100) {
            pageSize = 100;
        }
        int total = 0;
        Page p = new Page();
        StringBuilder totalSql = new StringBuilder();
        totalSql.append("select count(*) count from (");
        totalSql.append(sql);
        totalSql.append(") as temp");

        Session session = getSession();
        Query query = session.createSQLQuery(totalSql.toString());
        query.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        // 先查询total
        List<Map<String, Object>> totalList = query.list();
        if (totalList.size() > 0) {
            total = NumberConvertUtil.parseInt(String.valueOf(totalList.get(0).get("count")));
        }
        p.setTotal(total);

        //查询分页数据
        query = session.createSQLQuery(sql);
        query.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        // 先查询total
        query.setFirstResult(pageNum);
        query.setMaxResults(pageSize);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        List rs = query.list();
        p.setData(rs);
        return p;
    }

    public Page sqlPageQuery(String sql, int pageNum, int pageSize, final Object... values) {
        if (pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize < 0 || pageSize > 100) {
            pageSize = 100;
        }
        pageNum = (pageNum - 1) * pageSize;
        int total = 0;
        Page p = new Page();
        StringBuilder totalSql = new StringBuilder();
        totalSql.append("select count(*) count from (");
        totalSql.append(sql);
        totalSql.append(") as temp");

        Session session = getSession();
        Query query = session.createSQLQuery(totalSql.toString());
        query.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        // 先查询total
        List<Map<String, Object>> totalList = query.list();
        if (totalList.size() > 0) {
            total = NumberConvertUtil.parseInt(String.valueOf(totalList.get(0).get("count")));
        }
        p.setTotal(total);

        //查询分页数据
        query = session.createSQLQuery(sql);
        query.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        // 先查询total
        query.setFirstResult(pageNum);
        query.setMaxResults(pageSize);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        List rs = query.list();
        p.setData(rs);
        return p;
    }

    public Page sqlPageQueryByPageSize0(String sql, int pageNum, int pageSize, final Object... values) {
        if (pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 100;
        }
        pageNum = (pageNum - 1) * pageSize;
        int total = 0;
        Page p = new Page();
        StringBuilder totalSql = new StringBuilder();
        totalSql.append("select count(*) count from (");
        totalSql.append(sql);
        totalSql.append(") as temp");

        Session session = getSession();
        Query query = session.createSQLQuery(totalSql.toString());
        query.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        // 先查询total
        List<Map<String, Object>> totalList = query.list();
        if (totalList.size() > 0) {
            total = NumberConvertUtil.parseInt(String.valueOf(totalList.get(0).get("count")));
        }
        p.setTotal(total);

        //查询分页数据
        query = session.createSQLQuery(sql);
        query.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        // 先查询total
        query.setFirstResult(pageNum);
        query.setMaxResults(pageSize);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        List rs = query.list();
        p.setData(rs);
        return p;
    }

    public void batchSaveOrUpdate(final List entityList) {
        org.springframework.util.Assert.notNull(entityList, "entityList不能为空");
        for (Object entity : entityList) {
            getSession().saveOrUpdate(entity);
        }
        getSession().flush();
    }

    /**
     * 根据sql语句查询返回List
     *
     * @param sql
     * @param className
     * @return
     */
    public List queryListBySql(String sql, Class className) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql).addEntity(className);
        List rs = query.list();
        return rs;
    }

    public List queryListBySql(String sql, final Object... values) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql);
        if (values != null)
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        List rs = query.list();
        return rs;
    }

    public List queryListBySql(String sql, Class className, final Object... values) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql).addEntity(className);
        if (values != null)
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        List rs = query.list();
        return rs;
    }

    public T queryUniqueBySql(String sql, Class className, Object... values) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql).addEntity(className);
        if (values != null)
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        List rs = query.list();
        if (!CollectionUtils.isEmpty(rs)) {
            return (T) rs.get(0);
        }
        return null;
    }

    public List<Map<String, Object>> queryListBySql(String sql) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql);
        query.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        List rs = query.list();
        return rs;
    }

    public String queryForObject(String sql, final Object... values) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql);
        if (values != null)
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        List rs = query.list();
        if (rs.size() > 0)
            return String.valueOf(rs.get(0));
        return "";
    }

    public List<Long> queryListForLong(String sql, final Object... values) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql);
        if (values != null)
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        List rs = query.list();
        if (!CollectionUtils.isEmpty(rs)) {
            List<Long> result = new ArrayList<>();
            for (Object obj : rs) {
                result.add(Long.valueOf(String.valueOf(obj)));
            }
            return result;
        } else {
            return null;
        }
    }

    public List<Integer> queryListForInteger(String sql, final Object... values) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql);
        if (values != null)
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        List rs = query.list();
        if (!CollectionUtils.isEmpty(rs)) {
            List<Integer> result = new ArrayList<>();
            for (Object obj : rs) {
                result.add(Integer.parseInt(String.valueOf(obj)));
            }
            return result;
        } else {
            return null;
        }
    }

    public List<String> queryForList(String sql, final Object... values) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql);
        if (values != null)
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        List rs = query.list();
        if (!CollectionUtils.isEmpty(rs)) {
            List<String> result = new ArrayList<>();
            for (Object obj : rs) {
                result.add(String.valueOf(obj));
            }
            return result;
        } else {
            return null;
        }
    }

    public int queryForInt(String sql, final Object... values) {
        Session session = getSession();
        Query query = session.createSQLQuery(sql);
        if (values != null)
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        List rs = query.list();
        if (rs.size() > 0)
            return NumberConvertUtil.parseInt(rs.get(0));
        return 0;
    }

    public List<Map<String, Object>> findMapBySql(String sql, Map<String, Object> params) {
        SQLQuery sqlQuery = getSession().createSQLQuery(sql);
        sqlQuery = getSqlQueryByMap(sqlQuery, params);
        return sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
    }

    private SQLQuery getSqlQueryByMap(SQLQuery sqlQuery, Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                Object obj = params.get(key);
                if (obj instanceof Collection<?>)
                    sqlQuery.setParameterList(key, (Collection<?>) obj);
                else if (obj instanceof Object[])
                    sqlQuery.setParameterList(key, (Object[]) obj);
                else
                    sqlQuery.setParameter(key, obj);

            }
        }
        return sqlQuery;
    }

}
