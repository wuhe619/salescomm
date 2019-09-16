package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.dao.HDicDao;
import com.bdaim.customs.dao.HMetaDataDefDao;
import com.bdaim.customs.dao.HReceiptRecordDao;
import com.bdaim.customs.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CustomsService {
    private static Logger log = LoggerFactory.getLogger(CustomsService.class);

    @Autowired
    private HBusiDataManagerDao hBusiDataManagerDao;

    @Autowired
    private HDicDao hDicDao;

    @Autowired
    private HMetaDataDefDao hMetaDataDefDao;

    @Autowired
    private HReceiptRecordDao hReceiptRecordDao;


    public void saveinfo(MainDan mainDan, LoginUser user) {
        List<HBusiDataManager> list = new ArrayList<>();
        buildMain(list, mainDan, user);
        if (list != null && list.size() > 0) {
            hBusiDataManagerDao.batchSaveOrUpdate(list);
        }
    }

    /**
     * 组装主单数据
     * @param list
     * @param mainDan
     * @param user
     */
    public void buildMain(List<HBusiDataManager> list,MainDan mainDan,LoginUser user){
        HBusiDataManager dataManager=new HBusiDataManager();
        dataManager.setCreateId(user.getId());
        dataManager.setCreateTime(new Date());
        dataManager.setType(BusiTypeEnum.SZ.getKey());
        dataManager.setContent(JSON.toJSONString(mainDan));
        dataManager.setExt_1("N");//commit to cangdan 是否提交仓单 N:未提交，Y：已提交
        dataManager.setExt_2("N");//commit to baogaundan N:未提交，Y：已提交
        dataManager.setExt_3(mainDan.getBILL_NO());
        list.add(dataManager);
        buildPartyDan(list,mainDan.getSINGLES(),user);
    }


    /**
     * 组装分单
     * @param list
     * @param partList
     * @param user
     */
    public void buildPartyDan(List<HBusiDataManager> list, List<PartyDan> partList,LoginUser user){
        if(partList!=null && partList.size()>0){
            for(PartyDan dan:partList){
                List<Product> pList = dan.getPRODUCTS();
                buildGoods(list,pList,user);
                HBusiDataManager dataManager=new HBusiDataManager();
                dataManager.setType(BusiTypeEnum.SF.getKey());
                dataManager.setCreateId(user.getId());
                dataManager.setCust_id(Long.valueOf(user.getCustId()));
                dataManager.setContent(JSON.toJSONString(dan));
                dataManager.setCreateTime(new Date());
                dataManager.setExt_3(dan.getBILL_NO());//分单号
                dataManager.setExt_4(dan.getMain_bill_NO());//主单号
                list.add(dataManager);
            }
        }
    }

    /**
     * 组装商品
     * @param list
     * @param pList
     * @param user
     */
    public void buildGoods(List<HBusiDataManager> list, List<Product> pList,LoginUser user){
        if(pList!=null && pList.size()>0){
            for(Product product:pList){
                HBusiDataManager dataManager=new HBusiDataManager();
                dataManager.setType(BusiTypeEnum.SS.getKey());
                dataManager.setCreateTime(new Date());
                dataManager.setCreateId(user.getId());
                dataManager.setCust_id(Long.valueOf(user.getCustId()));
                dataManager.setContent(JSON.toJSONString(product));
                dataManager.setExt_3(product.getCODE_TS());//商品编号
                dataManager.setExt_4(product.getParty_No());//分单号
                list.add(dataManager);
            }
        }
    }

    public Map<String,List<Map<String,Object>>> getdicList(String type,String dicType){
        String sql = "select type,code,name_zh from h_dic where type='"+dicType+"'";
        List<Map<String, Object>> list = hMetaDataDefDao.queryListBySql(sql);
        Map<String,List<Map<String,Object>>> m = new HashMap<>();
        if(list!=null && list.size()>0){
            for(Map<String,Object> map:list){
                List<Map<String,Object>> l=null;
                String _type = (String) map.get("type");
//                String code = (String) map.get("code");
//                String name_zh = (String) map.get("name_zh");
                if(m.containsKey(_type)){
                    l = m.get(_type);
                }
                if(l==null){
                    l=new ArrayList<>();
                }
                l.add(map);
                m.put(_type,l);
            }
        }
        return m;
    }
//
//    public MainDan getDetail(){
//
//    }
}
