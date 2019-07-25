package com.bdaim.slxf.util.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.bdaim.slxf.dto.NodeinfoDTO;


/**
 * 计算当前时间与最后一次插入时间相差值setMap
 * 日期：2017/2/21
 * @author lich@bdcsdk.com
 *
 */
public class ToBeRegisterDB {

	private IMap<String, NodeinfoDTO>  userMap;
	private HazelcastInstance _hazelClient;
	
	public ToBeRegisterDB(String url) {
		 _hazelClient = HazelClient.getInstance(url);
		userMap = _hazelClient.getMap("SessionTable");
	}
	
	public  void setMap(String userId, String verCode, long timeStamp,int num) {
		
		NodeinfoDTO _node = new NodeinfoDTO();
		_node.setTimeStamp(timeStamp);
		_node.setVerCode(verCode);
		_node.setNum(num);
		userMap.put(userId, _node);
	}
	
	public String getVerCodeFromMap(String userId) throws UserIDNotFoundException {
		
		if ( userMap.get(userId) == null ) {
			throw new UserIDNotFoundException("userID not found");
		}
		return userMap.get(userId).getVerCode();
	}
	
	public long  getTimeStampFromMap(String userId) throws UserIDNotFoundException {
		
		if ( userMap.get(userId) == null ) { 
			throw new UserIDNotFoundException("userID not found");
		}
		return userMap.get(userId).getTimeStamp();
	}
	public int  getNum(String userId) throws UserIDNotFoundException {

		if ( userMap.get(userId) == null ) { 
			throw new UserIDNotFoundException("userID not found");
		}
		return userMap.get(userId).getNum();
	}
	public void remove(String userId) {
		userMap.delete(userId);
	}
	public void shutdown() {
		_hazelClient.shutdown();
	}
}
