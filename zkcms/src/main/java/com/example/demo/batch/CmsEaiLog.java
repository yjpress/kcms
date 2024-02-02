package com.example.demo.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
//import java.sql.ResultSet ;
//import java.util.ArrayList;
//import java.util.HashMap;

/**
 * <PRE>
 * 파일명 : CmsEaiLog.java
 * 기능 : CMS 알람 상태 정보를  테이블에 저장 한다.
 * 설명 : CMS 알람 상태 정보를  로그 테이블에 정보를 저장 및 알람 해제시 삭제한다.
 * 최종수정자 : 정영록
 * 최종수정일자 : 2011-06-10
 * </PRE>
 */

public class CmsEaiLog  {
 

 /**
  * 시보값  전송할 데이터를  시보 로그 테이블에 저장한다
  * @return  void
  */
    public  void  setSiboTransData(Connection  conn_cms) throws Exception {
         
     PreparedStatement pstmt = null;
     
     try {
	       StringBuffer sql = new StringBuffer(); 
	       sql.append("  INSERT INTO  EAI_SIBO_LOG (  ");
	       sql.append("          IDX_PARAMETRE, DH_MESURE, TRANS_FLAG ) ");
	       sql.append("   SELECT IDX_PARAMETRE, DH_MESURE, 'P' "); // --'P' 전송중 값 으로 셋팅 
	       sql.append("     FROM (  SELECT  HT.IDX_PARAMETRE, MAX(DH_MESURE) AS DH_MESURE ");   
	       sql.append("               FROM  HT_PARAMETRE HT, PARAMETRE PR , ALARME AL     ");
	       sql.append("              WHERE  PR.IDX_PARAMETRE  = HT.IDX_PARAMETRE   ");        
	       sql.append("                AND  AL.IDX_PARAMETRE  = HT.IDX_PARAMETRE   ");       
	       sql.append("                AND  AL.ETATMAINT IN ('RAS','ALM','DNG')    "); // 해당 조건에 맞는 자료들만 저장  및 전송한다.      
	       sql.append("                AND  ( PR.NOM  = 'Velocity' OR PR.NOM = 'Acceleration' OR PR.NOM = 'Temperature' ) ");
	       sql.append("                AND  DH_MESURE > SYSDATE - 1 "); 
	       sql.append("             GROUP BY HT.IDX_PARAMETRE ");
	       sql.append("               MINUS  ");  // HT_PARAMETRE 테이블 최근 자료 중에  로그테이블에 없는  자료만 저장 및 보낸다.
	       sql.append("             SELECT  IDX_PARAMETRE , DH_MESURE ");
	       sql.append("               FROM   EAI_SIBO_LOG ");
	       sql.append("              WHERE  DH_MESURE > SYSDATE - 1  "); 
	       sql.append("       ) ");
	
	       pstmt = conn_cms.prepareStatement(sql.toString()); 
           pstmt.executeUpdate(); 
     
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      } finally {
         try {  
            if( pstmt != null)  pstmt.close();
         } catch (Exception e) {}
      }
     
    }
          
    /**
     *  시보 전송 완료 'Y' 플래그 셋팅 
     * @return  void
     */
       public  void  setSiboTransFlag(Connection  conn_cms) throws Exception {
            
        PreparedStatement  pstmt = null;
        
        try {
   	        StringBuffer sql = new StringBuffer(); 
   	        sql.append("  UPDATE  EAI_SIBO_LOG ");
   	        sql.append("     SET  TRANS_FLAG = 'Y'      "); // 완료 
   	        sql.append("         ,TRANS_DATE = SYSDATE  ");
          	sql.append("   WHERE  TRANS_FLAG = 'P'  "); 
   	     	
   	        pstmt = conn_cms.prepareStatement(sql.toString()); 
            pstmt.executeUpdate(); 
        
         } catch (Exception e) {
           e.printStackTrace();
           throw e;
         } finally {
            try {  
               if( pstmt != null)  pstmt.close();
            } catch (Exception e) {}
         }
        
       }
    
 } // end class