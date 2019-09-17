package com.bdaim.customs.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customs.entity.HReceiptRecord;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class HReceiptRecordDao extends SimpleHibernateDao<HReceiptRecord, Serializable> {

}
