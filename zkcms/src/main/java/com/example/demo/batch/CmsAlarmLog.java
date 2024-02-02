package com.example.demo.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
//import java.sql.ResultSet ;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <PRE>
 * 파일명 : CmsAlarmLog.java
 * 기능 : CMS 상태 정보를 로그테이블에 저장한다.
 * 설명 : CMS 상태 정보 를 SMS/MAIL 로 전송후  로그테이블에 정보를 저장한다.
 * 최종수정자 : 정영록
 * 최종수정일자 : 2010-12-08
 * </PRE>
 */

public class CmsAlarmLog  {
	

	/**
	 *  CMS 알람  테이블을 통해   각 측정된  설비  상태를 확인한다.
     * @return  void 
	 */
    public  boolean  insertAlarmLog(Connection  conn_cms , ArrayList<HashMap<Object, Object>>  status_array) {
     	  	
    	PreparedStatement pstmt = null;
    	HashMap<Object, Object>           hmap  = null;	
    	boolean           isSuccess  = false;
    	
     try {
	    	StringBuffer sb = new StringBuffer(); 	    	
	    	
	    	sb.append("  INSERT  INTO  CMS_ALARM_LOG ( ");
	    	sb.append("          IDX_EQUIPEMENT, IDX_POINT, LOG_DATE,  ");
	    	sb.append("          EMP_NAME , EQUIP_NAME, POINT_NAME,    ");
	    	sb.append("          PARAMETRE_NAME, SEUILDGHAUTMAINT, SEUILALHAUTMAINT, ");
	    	sb.append("          VALEUR, RATIO, ID_UNITE, ");
	    	sb.append("          MESURE_DATE  ");
	    	sb.append("  ) VALUES ( ?, ?, SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ");	
	    	sb.append("            TO_DATE( ?, 'yyyy-mm-dd hh24:mi:ss') )  "); 
	    	
	        pstmt = conn_cms.prepareStatement(sb.toString()); 
	        int i = 1;
	          
	  	    for (int idx=0 ; idx < status_array.size() ; idx++ ) { 
	  	    	  
	  	        hmap = (HashMap<Object, Object>)status_array.get(idx);
	  	        i = 1;
	  	        
			    pstmt.setString(i++, String.valueOf(hmap.get("IDX_EQUIPEMENT")) );
			    pstmt.setString(i++, String.valueOf(hmap.get("IDX_POINT")) );
			    pstmt.setString(i++, String.valueOf(hmap.get("EMP_NOM")) );
			    pstmt.setString(i++, String.valueOf(hmap.get("EQ_NOM")) );
			    pstmt.setString(i++, String.valueOf(hmap.get("POINT_NOM")) );
			    
			    pstmt.setString(i++, String.valueOf(hmap.get("PARAM_NAME")) );			    
			    pstmt.setDouble(i++, Double.valueOf(String.valueOf(hmap.get("SEUILDGHAUTMAINT"))) );
			    pstmt.setDouble(i++, Double.valueOf(String.valueOf(hmap.get("SEUILALHAUTMAINT"))) );
			    pstmt.setDouble(i++, Double.valueOf(String.valueOf(hmap.get("CURR_VALUE"))) );
			    pstmt.setDouble(i++, Double.valueOf(String.valueOf(hmap.get("RATIO"))) );
			    
			    pstmt.setString(i++, String.valueOf(hmap.get("ID_UNITE")) );
			    pstmt.setString(i++, String.valueOf(hmap.get("DH_MESURE")) );
		    
		        pstmt.executeUpdate(); 
		        pstmt.clearParameters(); 
	  	    }   
		   
	  	    isSuccess = true;
		    
	     }	catch (Exception e) {
	    	 e.printStackTrace();
	     } finally {
	    	try {  
	    	  if( pstmt != null)  pstmt.close();
	        } catch (Exception e) {}
	    	
	     }
	     
	     return   isSuccess;
    	
    }
     
     
       
    
 } // end class

