package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.dao.HDicDao;
import com.bdaim.customs.dao.HMetaDataDefDao;
import com.bdaim.customs.dao.HReceiptRecordDao;
import com.bdaim.customs.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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

    @Autowired
    private ElasticSearchService elasticSearchService;

    /**
     * 保存信息
     * @param mainDan
     * @param user
     */
    public void saveinfo(MainDan mainDan, LoginUser user) {
        List<HBusiDataManager> list = new ArrayList<>();
        buildMain(list, mainDan, user);
        if (list != null && list.size() > 0) {
            for(HBusiDataManager hBusiDataManager:list) {
                Integer id = (Integer) hBusiDataManagerDao.saveReturnPk(hBusiDataManager);
                addDataToES(hBusiDataManager,id);
            }
//            hBusiDataManagerDao.batchSaveOrUpdate(list);
        }
    }


    /**
     * 添加数据到es
     * @param hBusiDataManager
     * @param id
     */
    private void addDataToES(HBusiDataManager hBusiDataManager,Integer id){
        String type = hBusiDataManager.getType();
        if (type.equals(BusiTypeEnum.SZ.getKey())) {
            elasticSearchService.addDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        }else if(type.equals(BusiTypeEnum.SF.getKey())){
            elasticSearchService.addDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        }else if(type.equals(BusiTypeEnum.SS.getKey())){
            elasticSearchService.addDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        }
    }

    /**
     * 查询主单详情
     * @param id
     */
    public JSONObject getMainDetailById(String id,String type){
        JSONObject json = elasticSearchService.getDocumentById(Constants.SF_INFO_INDEX,"haiguan",id);
        if(json==null){
            HBusiDataManager h = hBusiDataManagerDao.get(id);
            if(h!=null && h.getContent()!=null){
                json=JSON.parseObject(h.getContent());
            }
        }
        if(json!=null){
            json.put("id",id);
        }
        return json;
    }

    /**
     * 保存申报单主单信息
     * @param
     * @throws Exception
     */
    public void saveMainDetail(String id,MainDan mainDan) throws Exception {
//         String id = jsonObject.getString("id");
         if(StringUtil.isEmpty(id)){
            log.error("参数id为空");
             throw new Exception("参数错误");
         }
         HBusiDataManager manager = hBusiDataManagerDao.get(id);
         if(manager==null){
             throw new Exception("修改的数据不存在");
         }
         String content = manager.getContent();
         MainDan dbjson = JSON.parseObject(content,MainDan.class);
         BeanUtils.copyProperties(mainDan,dbjson);
         manager.setContent(JSON.toJSONString(dbjson));
         hBusiDataManagerDao.save(manager);
         updateDataToES(manager,Integer.valueOf(id));
    }

    /**
     * 更新索引数据
     * @param hBusiDataManager
     * @param id
     */
    private void updateDataToES(HBusiDataManager hBusiDataManager,Integer id){
        String type = hBusiDataManager.getType();
        if (type.equals(BusiTypeEnum.SZ.getKey())) {
            elasticSearchService.updateDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        }else if(type.equals(BusiTypeEnum.SF.getKey())){
            elasticSearchService.updateDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        }else if(type.equals(BusiTypeEnum.SS.getKey())){
            elasticSearchService.updateDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        }
    }

    /**
     * 组装主单数据
     * @param list
     * @param mainDan
     * @param user
     */
    public void buildMain(List<HBusiDataManager> list,MainDan mainDan,LoginUser user){
        HBusiDataManager dataManager = new HBusiDataManager();
        dataManager.setCreateId(user.getId());
        dataManager.setCreateDate(new Date());
        dataManager.setType(BusiTypeEnum.SZ.getKey());
        JSONObject jsonObject = buildMainContent(mainDan);
        jsonObject.put("type",BusiTypeEnum.SZ.getKey());
        jsonObject.put("commitCangdanStatus","N");
        jsonObject.put("commitBaoDanStatus","N");
        jsonObject.put("create_date",new Date());
        jsonObject.put("create_id",user.getId()+"");
        jsonObject.put("stationId","");//场站id
        jsonObject.put("cust_id",user.getCustId());
        jsonObject.put("idCardNumber",0);
        dataManager.setContent(jsonObject.toJSONString());
        dataManager.setExt_1("N");//commit to cangdan 是否提交仓单 N:未提交，Y：已提交
        dataManager.setExt_2("N");//commit to baogaundan N:未提交，Y：已提交
        dataManager.setExt_3(mainDan.getBill_no());
        list.add(dataManager);
        buildPartyDan(list,mainDan,user);
    }


    /**
     * 1.统计重量
     * 2.统计分单数量
     * 3.是否有低价商品
     * 4.是否短装、溢装
     * 件数  申报分单数  分单总计  申报重量  重量总计
     * 低价商品判断逻辑： 跟当前企业用户历史舱单/报关单商品数据进行比较，
     * 取近3个月的商品均值进行比较。若低于均值，则判断为低价商品
     * 冷启动阶段：商品完税价格
     */
    private static JSONObject buildMainContent(MainDan mainDan){
        log.info(JSON.toJSONString(mainDan));
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(mainDan));
//        String CHARGE_WT = mainDan.getCharge_wt();
        String partynum = mainDan.getSingle_batch_num();
//        String packNo = mainDan.getPack_no();

        List<PartyDan> list = mainDan.getSingles();
        float weightTotal=0;
        for(PartyDan partyDan:list){
            String WEIGHT = partyDan.getWeight();
            if(StringUtil.isEmpty(WEIGHT)){
                WEIGHT="0";
            }
            weightTotal += Float.valueOf(WEIGHT);
        }

        jsonObject.put("weight_total",weightTotal);
        jsonObject.put("party_total",list.size());

        if(Integer.valueOf(partynum)<list.size()){
            jsonObject.put("overWarp","溢装");//溢装
        }else if(Integer.valueOf(partynum)>list.size()){
            jsonObject.put("overWarp","短装");//短装
        }else{
            jsonObject.put("overWarp","正常");//正常
        }

        //todo:低价商品暂时不处理
        System.out.println(jsonObject);

    return jsonObject;

    }

    private JSONObject buildPartyContent(PartyDan partyDan){
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(partyDan));
        return jsonObject;
    }

    private JSONObject buildGoodsContent(Product product){
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(product));
        return jsonObject;
    }



    /**
     * 组装分单
     * @param list
     * @param mainDan
     * @param user
     */
    public void buildPartyDan(List<HBusiDataManager> list, MainDan mainDan,LoginUser user){
        List<PartyDan> partList = mainDan.getSingles();
        if(partList!=null && partList.size()>0){
            for(PartyDan dan:partList){
                List<Product> pList = dan.getProducts();
                buildGoods(list,pList,user);
                HBusiDataManager dataManager=new HBusiDataManager();
                dataManager.setType(BusiTypeEnum.SF.getKey());
                dataManager.setCreateId(user.getId());
                dataManager.setCust_id(Long.valueOf(user.getCustId()));

                dataManager.setCreateDate(new Date());
                dataManager.setExt_3(dan.getBill_NO());//分单号
                dataManager.setExt_4(dan.getMain_bill_NO());//主单号

                JSONObject json=buildPartyContent(dan);
                json.put("type",BusiTypeEnum.SF.getKey());
                json.put("mail_bill_no",mainDan.getBill_no());
                json.put("create_date",dataManager.getCreateDate());
                json.put("create_id",user.getId());
                json.put("cust_id",user.getCustId());
                json.put("check_status","0");
                json.put("idcard_pic_flag","0");
                dataManager.setContent(json.toJSONString());

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
                dataManager.setCreateDate(new Date());
                dataManager.setCreateId(user.getId());
                dataManager.setCust_id(Long.valueOf(user.getCustId()));
                dataManager.setExt_3(product.getCode_ts());//商品编号
                dataManager.setExt_4(product.getParty_No());//分单号
                JSONObject json = buildGoodsContent(product);
                json.put("create_date",new Date());
                json.put("create_id",user.getId());
                json.put("cust_id",user.getCustId());
                json.put("type",BusiTypeEnum.SS);
                dataManager.setContent(json.toJSONString());

                list.add(dataManager);
            }
        }
    }


    public Map<String,List<Map<String,Object>>> getdicList(String type,String propertyName){
        String  hql=" from  HMetaDataDef a where filed_type='array' and type='"+type+"' ";
        if(StringUtil.isNotEmpty(propertyName)){
            hql+="a.property_name='"+propertyName+"'";
        }
        Map<String,List<Map<String,Object>>> m = new HashMap<>();
        List<HMetaDataDef> hMetaDataDeflist = hMetaDataDefDao.find(hql);
        if(hMetaDataDeflist != null && hMetaDataDeflist.size()>0){
            for(int i=0;i<hMetaDataDeflist.size();i++){
                String propertyCode = hMetaDataDeflist.get(i).getProperty_code();
                String property_name_en = hMetaDataDeflist.get(i).getProperty_name_en();
                String sql = "select type,code,name_zh from h_dic where type='"+propertyCode+"'";
                List<Map<String, Object>> list = hMetaDataDefDao.queryListBySql(sql);
                if(list!=null && list.size()>0){
                    for(Map<String,Object> map:list){
                        List<Map<String,Object>> l=null;
                        if(m.containsKey(property_name_en)){
                            l = m.get(property_name_en);
                        }
                        if(l==null){
                            l=new ArrayList<>();
                        }
                        l.add(map);
                        m.put(property_name_en,l);
                    }
                }
            }
        }
        return m;
    }



    public Page  getdicPageList(String dicType,Integer pageSize,Integer pageNo){
        String sql="select * from h_dic where type='"+dicType+ "'";
        Page page = hDicDao.sqlPageQuery(sql,pageNo,pageSize);
        return page;
    }


    public void saveDic(Map<String,String> paramMap){
        HDic dic=new HDic();
        dic.setCode(paramMap.get("code"));
        dic.setName_zh(paramMap.get("name_zh"));
        dic.setName_en(paramMap.get("name_en"));
        dic.setDesc(paramMap.get("desc"));
        if(paramMap.get("status")==null){
            dic.setStatus("Y");
        }else{
            dic.setStatus(paramMap.get("status"));
        }
        dic.setType(paramMap.get("type"));
        hDicDao.saveOrUpdate(dic);
    }



//
//    public MainDan getDetail(){
//
//    }
}
