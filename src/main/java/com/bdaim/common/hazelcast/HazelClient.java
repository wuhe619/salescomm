package com.bdaim.common.hazelcast;


import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

/**
 * 初始化Hazelcast
 *
 */
public class HazelClient {
	
        private static  HazelcastInstance _client = null;
        
        public static synchronized  HazelcastInstance  getInstance(String hazelURL) {
        	
        	if  ( _client == null )  {
        		ClientConfig clientConfig = new ClientConfig();
        		//clientConfig.getGroupConfig().setName("dev").setPassword("dev-pass");
        		clientConfig.getNetworkConfig().addAddress(hazelURL);
        		_client =   HazelcastClient.newHazelcastClient(clientConfig);  
        	}
        	return _client;
        }
}
