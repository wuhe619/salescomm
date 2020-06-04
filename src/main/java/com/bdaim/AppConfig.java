package com.bdaim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@DependsOn("systemConfig")
public class AppConfig {
    private final static Logger LOG = LoggerFactory.getLogger(AppConfig.class);

    private static String app = null;
    private static String hbase_audio_url = null;
    private static String restPath = null;
    private static String finance_h5_host = null;
    private static String hazelcast_HOST = null;
    private static String location = null;
    private static String destpath = null;
    private static String file_path = null;
    private static String zfb_HOST = null;
    private static String audiolocation = null;
    private static String destaudiolocation = null;
    private static String ytx_spuid = null;
    private static String ytx_sppwd = null;
    private static String host = null;
    private static String port = null;
    private static String username = null;
    private static String password = null;
    private static String online_host = null;
    private static String es_rest = null;
    private static String ds_es_index_0 = null;
    private static String ds_es_type_0 = null;
    private static String KeyGeneratorRule = null;
    private static Integer oper_log_queue_size = 1000;
    private static Integer oper_log_thread_num = 2;
    private static Boolean is_enable_oper_log = false;
    private static Integer oper_log_insert_timeout = 1000;
	private static String xz_call_api = "http://api.salescomm.net:8017";

	private static String ent_data_index = null;
	private static String ent_data_type = null;
	private static String ent_data_url = null;

	private static String email_username = null;

	public static String getApp() {
		return app;
	}

	public static String getHbase_audio_url() {
		return hbase_audio_url;
	}

	public static String getRestPath() {
		return restPath;
	}
	@Value("${restPath:/}")
	public void setRestPath(String restPath) {
		AppConfig.restPath = restPath;
	}

	public static String getFinance_h5_host() {
		return finance_h5_host;
	}
	@Value("${finance.h5.host:localhost}")
	public void setFinance_h5_host(String finance_h5_host) {
		AppConfig.finance_h5_host = finance_h5_host;
	}

	public static String getHazelcast_HOST() {
		return hazelcast_HOST;
	}
	@Value("${hazelcast_HOST:localhost}")
	public void setHazelcast_HOST(String hazelcast_HOST) {
		AppConfig.hazelcast_HOST = hazelcast_HOST;
	}

	public static String getLocation() {
		return location;
	}
	@Value("${location:/}")
	public void setLocation(String location) {
		AppConfig.location = location;
	}

	public static String getDestpath() {
		return destpath;
	}
	@Value("${destpath:/}")
	public void setDestpath(String destpath) {
		AppConfig.destpath = destpath;
	}

	public static String getFile_path() {
		return file_path;
	}
	@Value("${file.file_path:/}")
	public void setFile_path(String file_path) {
		AppConfig.file_path = file_path;
	}

	public static String getZfb_HOST() {
		return zfb_HOST;
	}
	@Value("${zfb_HOST:localhost}")
	public void setZfb_HOST(String zfb_HOST) {
		AppConfig.zfb_HOST = zfb_HOST;
	}

	public static String getAudiolocation() {
		return audiolocation;
	}
	@Value("${audiolocation:/}")
	public void setAudiolocation(String audiolocation) {
		AppConfig.audiolocation = audiolocation;
	}

	public static String getDestaudiolocation() {
		return destaudiolocation;
	}
	@Value("${destaudiolocation:/}")
	public void setDestaudiolocation(String destaudiolocation) {
		AppConfig.destaudiolocation = destaudiolocation;
	}

	public static String getYtx_spuid() {
		return ytx_spuid;
	}
	@Value("${ytx.spuid:0}")
	public void setYtx_spuid(String ytx_spuid) {
		AppConfig.ytx_spuid = ytx_spuid;
	}

	public static String getYtx_sppwd() {
		return ytx_sppwd;
	}
	@Value("${ytx.sppwd:0}")
	public void setYtx_sppwd(String ytx_sppwd) {
		AppConfig.ytx_sppwd = ytx_sppwd;
	}

	public static String getHost() {
		return host;
	}
	@Value("${host:localhost}")
	public void setHost(String host) {
		AppConfig.host = host;
	}

	public static String getPort() {
		return port;
	}
	@Value("${port:22}")
	public void setPort(String port) {
		AppConfig.port = port;
	}

	public static String getUsername() {
		return username;
	}
	@Value("${username:0}")
	public void setUsername(String username) {
		AppConfig.username = username;
	}

	public static String getPassword() {
		return password;
	}
	@Value("${password:0}")
	public void setPassword(String password) {
		AppConfig.password = password;
	}

	public static String getOnline_host() {
		return online_host;
	}
	@Value("${online.host:localhost}")
	public void setOnline_host(String online_host) {
		AppConfig.online_host = online_host;
	}

	public static String getEs_rest() {
		return es_rest;
	}
	@Value("${es.rest:localhost}")
	public void setEs_rest(String es_rest) {
		AppConfig.es_rest = es_rest;
	}

	public static String getDs_es_index_0() {
		return ds_es_index_0;
	}
	@Value("${ds.es.index.0:tags}")
	public void setDs_es_index_0(String ds_es_index_0) {
		AppConfig.ds_es_index_0 = ds_es_index_0;
	}

	public static String getDs_es_type_0() {
		return ds_es_type_0;
	}
	@Value("${ds.es.type.0:tag}")
	public void setDs_es_type_0(String ds_es_type_0) {
		AppConfig.ds_es_type_0 = ds_es_type_0;
	}

	public static String getKeyGeneratorRule() {
		return KeyGeneratorRule;
	}
	@Value("${KeyGeneratorRule:,}")
	public void setKeyGeneratorRule(String keyGeneratorRule) {
		KeyGeneratorRule = keyGeneratorRule;
	}

	public static Logger getLog() {
		return LOG;
	}

	@Value("${app:BP}")
	public void setApp(String app) {
		AppConfig.app = app;
	}

	@Value("${hbase_audio_url:/}")
	public void setHbase_audio_url(String hbase_audio_url) {
		AppConfig.hbase_audio_url = hbase_audio_url;
	}

	public static Integer getOper_log_queue_size() {
		return oper_log_queue_size;
	}

	@Value("${oper_log_queue_size:1000}")
	public void setOper_log_queue_size(Integer oper_log_queue_size) {
		AppConfig.oper_log_queue_size = oper_log_queue_size;
	}

	public static Integer getOper_log_thread_num() {
		return oper_log_thread_num;
	}
	@Value("${oper_log_thread_num:2}")
	public void setOper_log_thread_num(Integer oper_log_thread_num) {
		AppConfig.oper_log_thread_num = oper_log_thread_num;
	}

	public static Boolean getIs_enable_oper_log() {
		return is_enable_oper_log;
	}
	@Value("${is_enable_oper_log:false}")
	public void setIs_enable_oper_log(Boolean is_enable_oper_log) {
		AppConfig.is_enable_oper_log = is_enable_oper_log;
	}

	public static Integer getOper_log_insert_timeout() {
		return oper_log_insert_timeout;
	}
	@Value("${oper_log_insert_timeout:1000}")
	public void setOper_log_insert_timeout(Integer oper_log_insert_timeout) {
		AppConfig.oper_log_insert_timeout = oper_log_insert_timeout;
	}

	public static String getXz_call_api() {
		return xz_call_api;
	}
	@Value("${xz_call_api:http://api.salescomm.net:8017}")
	public void setXz_call_api(String xz_call_api) {
		AppConfig.xz_call_api = xz_call_api;
	}

	public static String getEnt_data_index() {
		return "retrieve";
	}

	public void setEnt_data_index(String ent_data_index) {
		AppConfig.ent_data_index = "retrieve";
	}

	public static String getEnt_data_type() {
		return ent_data_type;
	}
	@Value("${ent_data_type:tag}")
	public void setEnt_data_type(String ent_data_type) {
		AppConfig.ent_data_type = ent_data_type;
	}

	public static String getEnt_data_url() {
		return ent_data_url;
	}
	@Value("${spring.elasticsearch.jest.uris:http://ll6:9200}")
	public void setEnt_data_url(String ent_data_url) {
		AppConfig.ent_data_url = ent_data_url;
	}

	public static String getEmail_username() {
		return email_username;
	}
	@Value("${spring.mail.username}")
	public void setEmail_username(String email_username) {
		AppConfig.email_username = email_username;
	}
}
