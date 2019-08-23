package com.bdaim.common.util;



public class SqlKeywordUtils 
{
	private static String[] mysqlkeywords = {"LEVEL"};
	private static String[] oraclekeywords = {"LEVEL"};
	
	
	public static String processKeyword(String sql, String driver)
	{
		if (null == sql)
			return null;
		if ("com.mysql.jdbc.Driver".equals(driver))
		{
			return processKeywordWithKeywords(sql, mysqlkeywords, "`");
		}
		else if ("oracle.jdbc.driver.OracleDriver".equals(driver))
		{
			return processKeywordWithKeywords(sql, oraclekeywords, "\"");
		}
		else
		{
			System.out.println("Unknown db driver," + driver + ",ignore process sql!");
			return sql;
		}
	}
	
	public static String processKeywordWithKeywords(String sql, String[] kws, String quotes)
	{
		for (String keyw : kws)
		{
			sql = replaceKeywords(sql, keyw, quotes);
			
		}
		return sql;
	}
	public static String replaceKeywords(String sql, String keyword, String quotes)
	{
		sql = sql.replaceAll(keyword.toUpperCase(), quotes + keyword + quotes);
		sql = sql.replaceAll("." + keyword.toLowerCase(), "." + quotes + keyword + quotes);
		return sql;
	}
	
	public static void main(String[] args) {
		String sql = "select t.level from label_info t;";
		System.out.println(processKeyword(sql, "com.mysql.jdbc.Driver"));
		System.out.println(processKeyword(sql, "oracle.jdbc.driver.OracleDriver"));
	}
	
}
