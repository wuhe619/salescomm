package com.bdaim.batch.service;


import java.util.LinkedList;

public interface BatchService {
	/**
	 * 调用失联修复接口，根据联调调整入参
	 */
	String sendtofile(LinkedList<String> certilist, LinkedList<String> cususerIdlist , String repairMode,String batchId);

	/**
	 * 文件内容条目是否重复
	 */
	Boolean repeatIdCardStatus(String batchId);

	Boolean repeateEntrpriseIdStatus(String batchId);

	String batchNameGet(String batchId);

	int uploadNumGet(String compId);
}
