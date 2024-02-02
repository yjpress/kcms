package com.example.demo.batch;

import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


/**
 * <PRE>
 * 파일명 : CmsUtil
 * 기능 : 공통 UTIL 함수 제공 
 * 설명 : 공통 UTIL 함수 제공
 * 최종수정자 : 정영록
 * 최종수정일자 : 2010-12-01
 * </PRE>
 */

public class CmsUtil {

 
   /**
    * DB 접속을 위한 객체를 얻는다.
    * @return  Connection
    * @throws  Exception
    */
  public  static Connection getConnection(String dbUrl, String user , String pwd) throws Exception {
     
     Connection conn   = null;        
        
       /** CMS DB 서버 url */
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            //Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            
            conn = DriverManager.getConnection(dbUrl, user, pwd);
        } catch (Exception ex) {         
            ex.printStackTrace();
        }
        
        return conn;
    }
     
     
   /**
    * 한글변환 함수.
    * @param    String
    * @return   String
    */
   public static String toKor(String str) {
    if(isNull(str))   return "";
    try {
         return  new String(str.getBytes("8859_1"),"euc-kr");
     } catch (Exception e) {
         e.printStackTrace();
      return str;
     }
   }
     
   
  /**
   * 변환 함수.
   * @param    String
   * @return   String
   */
   public static String toConvert(String str) {
     if(isNull(str))   return "";
      try {
      return  new String(str.getBytes(),"8859_1");
      } catch (Exception e) {
           e.printStackTrace();
        return str;
      }
   }
     
   /**
    * null 또는 값의 유효 여부확인 함수
    * @param    String
    * @return   boolean
    */
    public static boolean isNull(String str) {

       if(str == null || "".equals(str) || "null".equals(str))
          return true;
       else
          return false;
    }
       
    /**
     * oracle의 nvl(...)과 동일한 기능을 하는 함수 
     * @param   src    String  type의 데이터
     * @return  변환된    String
     */
      public static String nvl(String src ){
         return  nvl(src, ""); // default  ""
      }
    
    /**
     * oracle의 nvl(...)과 동일한 기능을 하는 함수 
     * @param   src    String  type의 데이터
     * @param   ret    String  type의 리턴되어질 테이터
     * @return  변환된    String
     */
      public static String nvl( String src, String ret ){
        if ( src == null || src.equals("null") || "".equals(src) )
            return ret;
        else
            return src;
      }
    
    /**
     * 공백 제거 함수 
     * @param   src    String  type의 데이터
     * @param   ret    String  type의 리턴되어질 테이터
     * @return  변환된  String
     */
    public static String trim(String src){
   
      if(src != null && src.length()>0)
        return src.trim();
      else
        return ""; 
    }
  
   /**
    * 숫자를 지정된 포맷으로 변환한다.
    * <pre>
    *  String strValue = new DisplayUtil().convert(34515.332, "#,###.#"); //34,515.3
    * </pre>
    * @param doubleObj 숫자
    * @param format 숫자포맷
    * @return 포맷이 적용된 문자열
    */
   public static  String format(double doubleObj, String format) {
       DecimalFormat formatter = new DecimalFormat(format);
       return formatter.format(doubleObj).toString();
   }
   
   /**
    *  숫자를 지정된 default  포맷으로 변환한다.
    * @param doubleObj 숫자
    * @param format 숫자포맷
    * @return 포맷이 적용된 문자열
    */
   public static  String  format(double doubleObj ) {
      DecimalFormat formatter = new DecimalFormat("###,###.#####");
      return formatter.format(doubleObj).toString();
   }
   
   /**
    * 날짜 관련  함수
    * @return   String
    */
    public static String getDate() {
      SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd", java.util.Locale.KOREA);
 
      return formatter.format(new Date());
    }
     
    public static String getDateTime() {
	   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", java.util.Locale.KOREA);
	
	   return formatter.format(new Date());
    }	   
    public static String getDateFormat(String str) {
       SimpleDateFormat formatter = new SimpleDateFormat(str, java.util.Locale.KOREA);
 
       return formatter.format(new Date());
    }
     
    public static String htmlToTxt(String comment) {
  
      StringBuffer sbuf = new StringBuffer();
      char c = ' ';
      String s = comment;  
	  try {   
	     for (int i = 0; i < s.length(); i++) {
	       c = s.charAt(i);     
	       switch (c){ 
	           case '<' :
	             sbuf.append("&lt;");
	             break; 
	           case '>' :
	             sbuf.append("&gt;");
	             break; 
	           default :
	             sbuf.append(c);
	             break;
	       }
	   }

         return  sbuf.toString();   

    } catch (Exception e) {
      return comment;
    }  
 } 
    
 

 /**
  * 입력된 문자를 지정한 크기 만큼  '0'을 붙여서 반환한다.
  * <pre>
  * String strValue = new StringUtil().convertFormat("23", "5"); // '00023'
  * </pre>
  * @param strValue 문자열
  * @param cnt  문자길이
  * @return 포맷이 적용된 문자열
  */
  public static String convertFormat(String strValue,  int  cnt){
  
    String resultStr = "";
  
    int strLen = strValue.length();
    int strLoopCnt = cnt - strLen;
  
    String formatChar = "";  
  
    for(int i = 0; i < strLoopCnt; i++){
      formatChar += "0";// '0' 을 붙인다.
    }
  
    resultStr = formatChar + strValue;
    return resultStr;
  }   
 
  /**
   * 입력된 문자를 지정한 크기 만큼  공백 을 붙여서 반환한다.
   * <pre>
   * String strValue = new StringUtil().convertFormat("23", "5"); // '   23'
   * </pre>
   * @param strValue 문자열
   * @param cnt  문자길이
   * @return 포맷이 적용된 문자열
   */
   public static String convertBlankFormat(String strValue,  int  cnt){
   
     String resultStr = "";
   
     int strLen = strValue.length();
     int strLoopCnt = cnt - strLen;
   
     String formatChar = "";  
   
     for(int i = 0; i < strLoopCnt; i++){
       formatChar += " ";// 공백 을 붙인다.
     }
   
     resultStr = formatChar + strValue;
     return resultStr;
   }   
  
  
}

