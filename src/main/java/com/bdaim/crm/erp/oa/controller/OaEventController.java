package com.bdaim.crm.erp.oa.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.annotation.ClassTypeCheck;
import com.bdaim.crm.entity.LkCrmOaEventEntity;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.entity.OaEventRelation;
import com.bdaim.crm.erp.oa.service.OaEventService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.core.paragetter.Para;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/OaEvent")
public class OaEventController extends BasicAction {

    @Resource
    private OaEventService oaEventService;

    /**
     * @author wyq
     * 查询日程列表
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.POST)
    public R queryList(@Para("") LkCrmOaEventEntity oaEvent){
        return(R.ok().put("data",oaEventService.queryList(oaEvent)));
    }

    /**
     * @author wyq
     * 查询日程列表
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.POST)
    public R queryById(@Para("eventId") Integer eventId){
        return(R.ok().put("data",oaEventService.queryById(eventId)));
    }

    /**
     * @author wyq
     * 新增日程
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public R add(@Para("")LkCrmOaEventEntity oaEvent){
        return(oaEventService.add(oaEvent));
    }

    /**
     * @author wyq
     * 更新日程
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public R update(@Para("")LkCrmOaEventEntity oaEvent){
        boolean oaAuth = AuthUtil.isOaAuth(OaEnum.EVENT_TYPE_KEY.getTypes(), oaEvent.getEventId());
        if(oaAuth){
            return(R.noAuth());
            //return;
        }
        return(oaEventService.update(oaEvent));
    }

    /**
     * @author wyq
     * 删除日程
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public R delete(@Para("eventId") Integer eventId){
        boolean oaAuth = AuthUtil.isOaAuth(OaEnum.EVENT_TYPE_KEY.getTypes(), eventId);
        if(oaAuth){
            return(R.noAuth());
            //return;
        }
        return(oaEventService.delete(eventId));
    }

    /**
     * @author wyq
     * crm查询日程
     */
    @RequestMapping(value = "/queryEventRelation", method = RequestMethod.POST)
    @ClassTypeCheck(classType = OaEventRelation.class)
    public R queryEventRelation(BasePageRequest<OaEventRelation> basePageRequest){
        return(oaEventService.queryEventRelation(basePageRequest));
    }
}
