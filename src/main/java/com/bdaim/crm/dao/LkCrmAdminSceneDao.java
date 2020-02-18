package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminSceneEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmAdminSceneDao extends SimpleHibernateDao<LkCrmAdminSceneEntity, Integer> {

    public List queryScene(int type, long userId) {
        String sql = "select a.scene_id,a.data,a.name,if(b.default_id is null,0,1) as is_default,a.is_system,a.bydata\n" +
                "    from lkcrm_admin_scene as a left join lkcrm_admin_scene_default as b on a.scene_id = b.scene_id\n" +
                "    where a.type = ? and a.user_id = ? and is_hide = 0 order by a.sort asc";
        return sqlQuery(sql, type, userId);
    }

    public List queryHideScene(int type, long userId) {
        String sql = "select scene_id,name,data from lkcrm_admin_scene where type = ? and user_id = ? and is_hide = 1" ;
        return sqlQuery(sql, type, userId);
    }

    public Map queryIsHideSystem(List ids) {
        String sql = "select count(scene_id) as number from 72crm_admin_scene where scene_id in (?) and is_system = 1" ;
        return sqlQuery(sql, ids).get(0);
    }
}
