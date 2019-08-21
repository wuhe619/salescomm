package com.bdaim.label.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.util.CalendarUtil;
import com.bdaim.common.util.Constant;
import com.bdaim.common.util.HttpUtil;
import com.bdaim.common.util.StringHelper;
import com.bdaim.customgroup.entity.CustomGroupDO;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.label.entity.LabelInfo;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.io.InputStream;
import java.util.*;

@Service("labelInterfaceService")
@Transactional
public class LabelInterfaceService {
	private static Logger log = Logger
			.getLogger(LabelInterfaceService.class);
	private RestTemplate restTemplate;
	@Resource
	private LabelInfoService labelInfoService;
	@Resource
	private CustomGroupService customGroupService;

	@Deprecated
	public String downloadByGidJobSubmission(String _file,
			Map<String, File> files, Map<String, String> texts) {
		Map<String, InputStream> is = new HashMap<String, InputStream>();
		return HttpUtil._postForm(Constant.LABEL_API
				+ "/uplabel/api/pullGroupUp/", _file, files, is, texts);
	}

	
	@Deprecated
	public String downloadByGroupJobSubmission(net.sf.json.JSONObject json) {
		String url = Constant.LABEL_API + "/uplabel/api/pulldata/";
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("data", json.toString());
		/*String result = restTemplate.postForObject(url, map, String.class);
		return result;*/
		return "";
	}

	@Deprecated
	public String downloadByGroupGidSubmission(net.sf.json.JSONObject json) {
		String url = Constant.LABEL_API + "/uplabel/api/downLoadGid/";
		/*String result = restTemplate.postForObject(url, json, String.class);
		return result;*/
		return "";
	}

	@Deprecated
	public String downloadByCidJobSubmission(net.sf.json.JSONObject json) {
		String url = Constant.LABEL_API + "/uplabel/api/pullGroupUp/";
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("data", json.toString());
		/*String result = restTemplate.postForObject(url, map, String.class);
		return result;*/
		return "";
	}

	@Deprecated
	public String getDownloadStatusById(String jobId) {
		String url = Constant.LABEL_API + "/uplabel/api/pullStatus?id={jobId}";
		/*String result = restTemplate.getForObject(url, String.class, jobId);
		return result;*/
		return "";
	}

	/**
	 * 接口ID：BQ0005（固定），
	 *cycle: 0表示all,1表示7天，2表示15天，3表示30天。
	 *terms:人群规则 
	 */
	public String previewByGroupCondition(String groupName, Integer cycle,
			String groupCondition) {
		JSONArray groupTerms = getCustomerGroupTerms(groupCondition);
		MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<String, Object>();
		urlVariables.add("interfaceID", "BQ0005");
		urlVariables.add("cycle", cycle == null ? "0" : cycle.toString());
		urlVariables.add("terms", groupTerms.toJSONString());
		/*String result = restTemplate.postForObject(Constant.LABEL_API
				+ "/labels/rest.do", urlVariables, String.class);
		log.info(result);
		return result;*/
		return "";
	}

	@Deprecated
	public String noticeByGroupCondition(String groupID, JSONArray groupTerms) {
		String result = null;
		JSONObject obj = new JSONObject();
		obj.put("groupID", groupID);
		obj.put("groupTerms", groupTerms);
		String url = Constant.MIC_PIC_URL + "/macro/pullJob";
		/*result = restTemplate.postForObject(url, obj, String.class);
		return result;*/
		return "";
	}


	/**
	 * 查询图谱
	 * interfaceID	接口ID	BQ0006
	 *terms	自定义用户群标签
	 */
	public String getUserGroupGid(CustomGroupDO group, Integer begin,
								  Integer limit, String searchType, String searchValue) {
		String groupName = group.getName();
		JSONArray groupTerms = getCustomerGroupTerms(group.getGroupCondition());
		JSONObject obj = new JSONObject();
		obj.put("groupID", groupName);
		obj.put("groupTerms", groupTerms);
		obj.put("startFrom", begin);
		obj.put("pageSize", limit);
		obj.put("cycle", "0");
		if(searchType!=null&&!searchType.equals("all")){
			obj.put("searchType", searchType);
			obj.put("searchValue", searchValue);
		}
		MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<String, Object>();
		urlVariables.add("interfaceID", "BQ0006");
		urlVariables.add("terms", obj.toJSONString());
		// 获取请求结果
		// 返回画像列表信息
		/*String result = restTemplate.postForObject(Constant.LABEL_API
				+ "/labels/rest.do", urlVariables, String.class);
		log.info(result);
		return result;*/
		return "";
	}

	
	/**
	 * 根据supperid查询用户画像
	 * timelimit，填7就是7天，30是30天。不填或者填别的就是全部
	 */
	public String getMicroscopicPicture(String supperid, Integer cycle) {
		String result = null;
		String url = Constant.MIC_PIC_URL;
		if (cycle == 0)
			cycle = 0;
		else if (cycle == 1)
			cycle = 7;
		else if (cycle == 3)
			cycle = 30;
		url += "&rowkey=" + supperid + "&cycle=" + cycle;
		result = HttpUtil._get(url);
		log.info(result);
		return result;
	}

	
	/**
	 * interfaceID	接口ID	BQ0003
	 *cycle	调度周期（时间段）
	 *terms	组合标签规则
	 */
	public String previewSignatureLabel(LabelInfo label, Integer cycle) {
		// 判断label不能为空
		//AssertHelper.isNotNull(label);
		String content = label.getLabelContent();
		List<Object> list = transform(JSONArray.parseArray(content));
		MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<String, Object>();
		urlVariables.add("interfaceID", "BQ0003");
		urlVariables.add("cycle", cycle == null ? "0" : cycle.toString());
		urlVariables.add("terms", JSON.toJSONString(list));
		String url = Constant.LABEL_API + "/labels/rest.do";
		/*String result = restTemplate.postForObject(url, urlVariables,
				String.class);
		return result;*/
		return "";

	}

	
	/**
	 * interfaceID	接口ID	BQ0002
	 *parentId	二级标签 label_id	
	 *labelName	三级标签值	
	 *labelCode	三级标签labelID	
	 *terms	组合标签规则	
	 */
	public String addSignatureLabel(String parentId, LabelInfo label) {
		//AssertHelper.isNotNull(label);
		String content = label.getLabelContent();
		List<Object> list = transform(JSONArray.parseArray(content));
		MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<String, Object>();
		urlVariables.add("interfaceID", "BQ0002");
		urlVariables.add("parentId", parentId);
		urlVariables.add("labelName", label.getLabelName());
		urlVariables.add("labelCode", label.getLabelId());
		urlVariables.add("terms", JSON.toJSONString(list));
		String result = restTemplate.postForObject(Constant.LABEL_API
				+ "/labels/rest.do", urlVariables, String.class);
		log.info(result);
		return result;

	}

	/*
	*//**
	 * interfaceID	接口ID	BQ0009
	 * 开发录入时调用，新增基础标签
	 *//*
	public String addBasicLabel(LabelInfo label, Config config) {
		MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<String, Object>();
		urlVariables.add("interfaceID", "BQ0009");
		urlVariables.add("second_level_name", config.getSecondLevelName());
		urlVariables.add("third_level_name", config.getThirdLevelName());
		urlVariables.add("third_level_id", config.getThirdLevelId());
		urlVariables.add("hive_table", config.getHiveTable());
		urlVariables.add("mutex", String.valueOf(label.getMutex()));
		urlVariables.add("type", "2");
		String result = restTemplate.postForObject(Constant.LABEL_API
				+ "/labels/rest.do", urlVariables, String.class);
		log.info(result);
		return result;

	}*/

	
	@Deprecated
	/**
	 * 直接获取下载数据流
	 */
	public InputStream downloadGidByCustomerGroup(String groupCondition) {
		JSONArray groupTerms = getCustomerGroupTerms(groupCondition);
		Map<String, Object> urlVariables = new HashMap<String, Object>();
		urlVariables.put("interfaceID", "BQ0004");
		urlVariables.put("cycle", "0");
		urlVariables.put("terms", groupTerms.toJSONString());
		InputStream is = HttpUtil._postForInputStream(Constant.LABEL_API
				+ "/labels/rest.do", urlVariables);
		return is;
	}

	
	/**
	 * interfaceID	接口ID	BQ0007
	 * terms	人群规则
	 * cycle	调度方式	
	 * groupBy	特征标签	
	 */
	public String getCharacteristic(CustomGroupDO group) {
		// 人群条件
		String groupCondition = group.getGroupCondition();
		// 分组条件
		JSONArray groupArr = JSONArray.parseArray(group.getGrouping());
		MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<String, Object>();
		urlVariables.add("interfaceID", "BQ0007");
		if (groupCondition == null) {
			Integer id = group.getId();
			if (id == null || id == 0)
				return null;
			group = customGroupService.getCustomGroupById(id);
			groupCondition = group.getGroupCondition();
		}
		// 请求参数terms
		JSONArray _arr = getCustomerGroupTerms(groupCondition);
		urlVariables.add("terms", JSON.toJSONString(_arr));
		for (int i = 0; i < groupArr.size(); i++) {
			JSONObject groupJson = groupArr.getJSONObject(i);
			groupJson.put("labelID", groupJson.getString("labelId"));
			int type = 2;
			if (groupJson.containsKey("categoryId")) {
				String categoryID = groupJson.getString("categoryId");
				if (!StringHelper.isBlank(categoryID)) {
					groupJson.put("labelID", labelInfoService.getLabelById(groupJson.getIntValue("labelId")).getLabelId());
					groupJson.put("categoryID", categoryID);
					type = 4;
				}
			}
			groupJson.put("type", type);
		}
		urlVariables.add("cycle", group.getCycle().toString());
		urlVariables.add("groupBy", JSON.toJSONString(groupArr));
		// 获取请求结果
		String str = restTemplate.postForObject(Constant.LABEL_API
				+ "/labels/rest.do", urlVariables, String.class);
		return str;
	}

	
	/**
	 * interfaceID	接口ID	BQ0008
	 *cid	渠道id	
	 *cycle 时间周期
	 *	terms	标签数组
	 */
	public String macroPicture(String cid, Integer cycle, String terms) {
		MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<String, Object>();
		urlVariables.add("interfaceID", "BQ0008");
		urlVariables.add("terms", terms);
		urlVariables.add("cycle", cycle == null ? "0" : String.valueOf(cycle));
		urlVariables.add("cid", cid == null ? "global" : cid);
		System.out.println(JSON.toJSONString(urlVariables));
		log.warn("请求rest的参数为："+JSON.toJSONString(urlVariables));
		// 获取请求结果
		String str = restTemplate.postForObject(Constant.LABEL_API
				+ "/labels/rest.do", urlVariables, String.class);
		log.warn("请求rest返回的结果为："+JSON.toJSONString(str));
		return str;
	}

	/**
	 * 将组合标签条件转化为后缀表达式
	 * 
	 * @param arr
	 * @return
	 */
	private List<Object> transform(JSONArray arr) {
		// 结果列表
		List<Object> postfixStack = new ArrayList<Object>();
		// 运算符栈
		Stack<String> opStack = new Stack<String>();
		opStack.push(",");
		int currentIndex = 0;
		// 待处理对象（非运算符及括号）数量
		int count = 0;
		// 当前对象，栈顶对象
		String currentOp, peekOp;
		for (int i = 0; i < arr.size(); i++) {
			currentOp = arr.getJSONObject(i).getString("value");
			if (isOperator(currentOp)) {
				if (count > 0) {
					for (int s = 0; s < count; s++) {
						JSONObject value = arr.getJSONObject(currentIndex + s)
								.getJSONObject("value");
						Integer id = value.getInteger("id");
						String categoryId = null;
						Integer type = 2;
						// 标签、品类对象
						JSONObject tmp = new JSONObject();
						if (value.containsKey("categoryId")
								&& (categoryId = value.getString("categoryId")) != null) {
							LabelInfo parent = labelInfoService
									.getLabelById(value
											.getInteger("firstLabelId"));
							tmp.put("labelID", parent.getLabelId());
							tmp.put("value", categoryId);
							type = 4;
						} else {
							LabelInfo label = labelInfoService.getLabelById(id);
							LabelInfo parent = label.getParent();
							tmp.put("labelID", parent.getLabelId());
							tmp.put("value", label.getLabelName());
						}
						tmp.put("type", Integer.toString(type));
						postfixStack.add(tmp);
					}
				}
				peekOp = opStack.peek();
				if (currentOp.equalsIgnoreCase(")")) {
					while (!opStack.peek().equalsIgnoreCase("(")) {
						postfixStack.add(String.valueOf(opStack.pop()));
					}
					opStack.pop();
				} else {
					while (!currentOp.equalsIgnoreCase("(")
							&& !peekOp.equalsIgnoreCase(",")
							&& compare(currentOp, peekOp)) {
						postfixStack.add(String.valueOf(opStack.pop()));
						peekOp = opStack.peek();
					}
					opStack.push(currentOp);
				}
				count = 0;
				currentIndex = i + 1;
			} else {
				count++;
			}
		}
		if (count > 1
				|| (count == 1 && !isOperator(arr.getString(currentIndex)))) {
			for (int s = 0; s < count; s++) {
				JSONObject tmp = new JSONObject();
				String categoryId = null;
				int type = 2;
				JSONObject value = arr.getJSONObject(currentIndex + s)
						.getJSONObject("value");
				Integer id = value.getInteger("id");
				if (value.containsKey("categoryId")
						&& (categoryId = value.getString("categoryId")) != null) {
					LabelInfo parent = labelInfoService.getLabelById(value
							.getInteger("firstLabelId"));
					tmp.put("labelID", parent.getLabelId());
					tmp.put("value", categoryId);
					type = 4;
				} else {
					LabelInfo label = labelInfoService.getLabelById(id);
					LabelInfo parent = label.getParent();
					tmp.put("labelID", parent.getLabelId());
					tmp.put("value", label.getLabelName());
				}
				tmp.put("type", Integer.toString(type));
				postfixStack.add(tmp);
			}
		}

		while (!opStack.peek().equalsIgnoreCase(",")) {
			postfixStack.add(String.valueOf(opStack.pop()));
		}
		return postfixStack;
	}

	/**
	 * 判断是否为符号
	 * 
	 * @param c
	 * @return
	 */
	private boolean isOperator(String c) {
		return c.equalsIgnoreCase("and") || c.equalsIgnoreCase("or")
				|| c.equalsIgnoreCase("not") || c.equalsIgnoreCase("(")
				|| c.equalsIgnoreCase(")");
	}

	/**
	 * or>not>and
	 * 
	 * @param cur
	 * @param peek
	 * @return
	 */
	public boolean compare(String cur, String peek) {
		boolean result = false;
		if (cur.equalsIgnoreCase("and")) {
			if (peek.equalsIgnoreCase("or")) {
				result = true;
			} else if (peek.equalsIgnoreCase("not")) {
				result = true;
			} else {
				result = false;
			}
		} else if (cur.equalsIgnoreCase("not")) {
			if (peek.equalsIgnoreCase("or")) {
				result = true;
			} else if (peek.equalsIgnoreCase("and")) {
				result = false;
			} else {
				result = false;
			}
		}
		return result;
	}

	/**
	 * 对用户群条件进行处理
	 * 
	 * @param groupCondition
	 * @return
	 */
	private JSONArray getCustomerGroupTerms(String groupCondition) {
		// 处理结果
		JSONArray _arr = new JSONArray();
		JSONArray arr = JSONArray.parseArray(groupCondition);
		// 表示与或逻辑关系，0:and, 1:or
		int symbol = -1;
		for (int i = 0; i < arr.size(); i++) {
			// 字段名转换
			JSONObject _json = new JSONObject();
			JSONObject json = arr.getJSONObject(i);
			String labelId = json.getString("labelId");
			_json.put("labelID", labelId);
			if (json.containsKey("categoryId"))
				_json.put("categoryId", json.getString("categoryId"));
			if (json.containsKey("startTime")) {
				String startTime = json.getString("startTime");
				if (startTime.matches("\\d+$"))
					startTime = CalendarUtil.getDateString(
							new Date(json.getLongValue("startTime") * 1000),
							CalendarUtil.SHORT_DATE_FORMAT);
				_json.put("startTime", startTime);
			}
			if (json.containsKey("endTime")) {
				String endTime = json.getString("endTime");
				if (endTime.matches("\\d+$"))
					endTime = CalendarUtil.getDateString(
							new Date(json.getLongValue("endTime") * 1000),
							CalendarUtil.SHORT_DATE_FORMAT);
				_json.put("endTime", endTime);
			}
			String type = json.getString("type");
			// 0：品类转为4 1：标签转为2
			if (type.equals("1"))
				type = "2";
			else
				type = "4";
			_json.put("type", type);
			// 暂定，后面迁移到constants
			if (symbol != -1)
				_json.put("symbol", symbol);
			if (json.containsKey("symbol"))
				symbol = json.getIntValue("symbol");
			JSONArray leafs = json.getJSONArray("leafs");
			JSONArray vals = new JSONArray();
			for (int j = 0; j < leafs.size(); j++) {
				JSONObject leaf = leafs.getJSONObject(j);
				vals.add(leaf.getString("name"));
			}
			_json.put("values", vals);
			_arr.add(_json);
		}
		return _arr;
	}

	
	/**
	 *下载
	 */
	public String downloadByCustomerGroup(CustomGroupDO group) {
		// 人群条件
		String groupCondition = group.getGroupCondition();
		String groupId = group.getId().toString();
		String downloadType = "csv";
		// 分组条件
		MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<String, Object>();
		urlVariables.add("interfaceID", "BQ0004");
		if (groupCondition == null) {
			Integer id = group.getId();
			if (id == null || id == 0)
				return null;
			group = customGroupService.getCustomGroupById(id);
			groupCondition = group.getGroupCondition();
			groupId = group.getId().toString();
		}
		// 请求参数terms
		JSONArray _arr = getCustomerGroupTerms(groupCondition);
		urlVariables.add("terms", JSON.toJSONString(_arr));
		urlVariables.add("cycle", group.getCycle().toString());
		urlVariables.add("downloadType", downloadType);
		urlVariables.add("id",groupId);
		String result = restTemplate.postForObject(Constant.LABEL_API
				+ "/labels/rest.do", urlVariables, String.class);
		return result;
	}

	
	@Deprecated
	/**
	 * 直接获取下载数据流
	 */
	public InputStream downloadStreamByCustomerGroup(CustomGroupDO group) {
		// 人群条件
		String groupCondition = group.getGroupCondition();
		String downloadType = "stream";
		// 分组条件
		Map<String, Object> urlVariables = new HashMap<String, Object>();
		urlVariables.put("interfaceID", "BQ0004");
		if (groupCondition == null) {
			Integer id = group.getId();
			if (id == null || id == 0)
				return null;
			group = customGroupService.getCustomGroupById(id);
			groupCondition = group.getGroupCondition();
		}
		// 请求参数terms
		JSONArray _arr = getCustomerGroupTerms(groupCondition);
		urlVariables.put("terms", JSON.toJSONString(_arr));
		urlVariables.put("cycle", group.getCycle().toString());
		urlVariables.put("downloadType", downloadType);
		InputStream is = HttpUtil._postForInputStream(Constant.LABEL_API
				+ "/labels/rest.do", urlVariables);
		return is;
	}

	
	/**
	 * interfaceID	接口ID	BQ0010
	 * label	标签
	 */
	public String updateLabelStatus(LabelInfo label) {
		Integer status = label.getStatus();
		String labelID = label.getLabelId();
		Integer type = label.getType();
		//标签上线状态转换，接口上线状态对应为1
		if(status==3)
			status=1;
		else if(status==1)
			status=2;
		MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<String, Object>();
		urlVariables.add("interfaceID", "BQ0010");
		urlVariables.add("labelID", labelID);
		urlVariables.add("type", String.valueOf(type));
		urlVariables.add("status", String.valueOf(status));
		// 获取请求结果
		String str = restTemplate.postForObject(Constant.LABEL_API
				+ "/labels/rest.do", urlVariables, String.class);
		return str;
	}

	
	public String queryByDevelopHiveTable(String hivetablename) 
	{
		// TODO Auto-generated method stub
		MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<String, Object>();
		urlVariables.add("interfaceID", "SBQ0005");
		urlVariables.add("hive_table", hivetablename);
		// 获取请求结果
		String str = restTemplate.postForObject(Constant.LABEL_API + "/labels/rest.do", urlVariables, String.class);
		return str;
	}

	/**
	 * 洋河定制 - 查询用户群画像， 代码与特征发现报告接口基本一致<BR>
	 * 
	 * interfaceID:CustomerGroupPicture
	 * 
	 * @param group
	 * @return
	 */
	
	public String queryCustomerGroupPicture(CustomGroupDO group) {

		// 分组条件，用于在ES进行聚合的条件
		// 普通标签：grouping:[{"labelId":"100010000100001"}]
		// 品类：grouping:[{"labelId":78,"categoryId":"101000002"}]
		JSONArray grouping = JSONArray.parseArray(group.getGrouping());
		log.info("grouping");
		if (grouping == null || grouping.size() == 0) {
			throw new RuntimeException("grouping参数不能为空！");
		} else if (grouping.size() > 1) {
			throw new RuntimeException("grouping 参数大小不能超过1！");
		}

		// 用户群id
		Integer id = group.getId();
		if (id != null) {
			// 根据用户群id查询用户群
			group = customGroupService.getCustomGroupById(id);
		}

		// 人群条件
		String groupCondition = group.getGroupCondition();

		// 请求参数
		MultiValueMap<String, Object> requestParam = new LinkedMultiValueMap<String, Object>();
		requestParam.add("interfaceID", "CustomerGroupPicture");

		// 请求参数中的terms参数
		JSONArray terms = this.getCustomerGroupTerms(groupCondition);
		requestParam.add("terms", JSON.toJSONString(terms));

		for (int i = 0; i < grouping.size(); i++) {
			JSONObject groupingJson = grouping.getJSONObject(i);
			groupingJson.put("labelID", groupingJson.getString("labelId"));
			int type = 2;

			// 如果groupingJson中包含 “品类id”, 此时的labelId参数为label_info表中的id字段
			// grouping:[{"labelId":78,"categoryId":"101000002"}, {"labelId":78,"categoryId":"101000014"}]}
			if (groupingJson.containsKey("categoryId")) {
				String categoryID = groupingJson.getString("categoryId");
				if (!StringHelper.isBlank(categoryID)) {

					// 根据id查询label_info表中的label_id
					String labelId = labelInfoService.getLabelById(groupingJson.getIntValue("labelId")).getLabelId();
					groupingJson.put("labelID", labelId);
					groupingJson.put("categoryID", categoryID);
					type = 4;
				}
			}
			groupingJson.put("type", type);
		}
		requestParam.add("cycle", group.getCycle().toString());
		requestParam.add("groupBy", JSON.toJSONString(grouping));

		// 洋河项目不区分businessType, 默认为user
		requestParam.add("businessType", "user");

		// 获取请求结果
		String esRestURL = Constant.LABEL_API + "/labels/rest.do";
		String result = restTemplate.postForObject(esRestURL, requestParam, String.class);
		return result;
	}
	
}
