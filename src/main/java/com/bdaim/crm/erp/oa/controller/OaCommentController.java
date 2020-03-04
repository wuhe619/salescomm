package com.bdaim.crm.erp.oa.controller;

import com.bdaim.crm.entity.LkCrmTaskCommentEntity;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.service.OaCommentService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/comment")
public class OaCommentController extends Controller {

    @Resource
    private OaCommentService commentService;

    /**
     * @param comment 评论对象
     * @author hmb
     * 添加评论或者修改
     */
    @RequestMapping(value = "/setComment")
    public R setComment(@Para("") LkCrmTaskCommentEntity comment) {
        if (comment.getType() == 1) {
            boolean oaAuth = AuthUtil.isOaAuth(OaEnum.TASK_TYPE_KEY.getTypes(), comment.getTypeId());
            if (oaAuth) {
//                renderJson(R.noAuth());
                return R.noAuth();
//                return;
            }

        }
//        renderJson(commentService.setComment(comment));
        return commentService.setComment(comment);
    }

    /**
     * @param comment 评论对象
     * @author hmb
     * 添加项目任务评论或者修改
     */
    @RequestMapping(value = "/setWorkTaskComment")
    public R setWorkTaskComment(@Para("") LkCrmTaskCommentEntity comment) {
//        renderJson(commentService.setComment(comment));
        return commentService.setComment(comment);
    }

    /**
     * 删除评论
     */
    @RequestMapping(value = "/deleteComment")
    public R deleteComment() {
        Integer commentId = getParaToInt("commentId");
//        renderJson(commentService.deleteComment(commentId));
        return commentService.deleteComment(commentId);
    }

    /**
     * @author hmb
     * 查询评论列表
     */
    @RequestMapping(value = "/queryCommentList")
    public R queryCommentList() {
        String typeId = getPara("typeId");
        String type = getPara("type");
        if ("1".equals(type)) {
            boolean oaAuth = AuthUtil.isOaAuth(OaEnum.TASK_TYPE_KEY.getTypes(), Integer.valueOf(typeId));
            if (oaAuth) {
//                renderJson(R.noAuth());
                return R.noAuth();
//                return;
            }
        }
//        renderJson(R.ok().put("data", commentService.queryCommentList(typeId, type)));
        return (R.ok().put("data", commentService.queryCommentList(typeId, type)));
    }

    /**
     * @author hmb
     * 查询评论列表
     */
    @RequestMapping(value = "/queryWorkCommentList")
    public R queryWorkCommentList() {
        String typeId = getPara("typeId");
        String type = getPara("type");
//        renderJson(R.ok().put("data", commentService.queryCommentList(typeId, type)));
        return R.ok().put("data", commentService.queryCommentList(typeId, type));
    }
}
