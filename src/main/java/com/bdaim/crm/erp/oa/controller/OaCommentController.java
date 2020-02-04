package com.bdaim.crm.erp.oa.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.service.OaCommentService;
import com.bdaim.crm.erp.work.entity.TaskComment;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;

public class OaCommentController extends Controller {

    @Inject
    private OaCommentService commentService;
    /**
     * @author hmb
     * 添加评论或者修改
     * @param comment 评论对象
     */
    public void setComment(@Para("") TaskComment comment){
        if(comment.getType() == 1){
            boolean oaAuth = AuthUtil.isOaAuth(OaEnum.TASK_TYPE_KEY.getTypes(), comment.getTypeId());
            if(oaAuth){renderJson(R.noAuth());return;}
        }
        renderJson(commentService.setComment(comment));
    }

    /**
     * @author hmb
     * 添加项目任务评论或者修改
     * @param comment 评论对象
     */
    public void setWorkTaskComment(@Para("") TaskComment comment){
        renderJson(commentService.setComment(comment));
    }

    /**
     * 删除评论
     */
    public void deleteComment(){
        Integer commentId = getParaToInt ( "commentId" );
        renderJson(commentService.deleteComment (commentId));
    }

    /**
     * @author hmb
     * 查询评论列表
     */
    public void queryCommentList(){
        String typeId = getPara("typeId");
        String type = getPara("type");
        if("1".equals(type)){
            boolean oaAuth = AuthUtil.isOaAuth(OaEnum.TASK_TYPE_KEY.getTypes(), Integer.valueOf(typeId));
            if(oaAuth){renderJson(R.noAuth());return;}
        }
        renderJson(R.ok().put("data",commentService.queryCommentList(typeId,type)));
    }

    /**
     * @author hmb
     * 查询评论列表
     */
    public void queryWorkCommentList(){
        String typeId = getPara("typeId");
        String type = getPara("type");
        renderJson(R.ok().put("data",commentService.queryCommentList(typeId,type)));
    }
}
