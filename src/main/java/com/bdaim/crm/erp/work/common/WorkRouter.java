package com.bdaim.crm.erp.work.common;

import com.jfinal.config.Routes;
import com.bdaim.crm.erp.work.controller.*;

public class WorkRouter extends Routes {
    @Override
    public void config() {
        addInterceptor(new WorkInterceptor());
//        add("/work", WorkController.class);
        //add("/task", TaskController.class);
        add("/taskLabel", LabelController.class);
//        add("/workbench", WorkbenchController.class);
        add("/workTrash", TrashController.class);
    }

}
