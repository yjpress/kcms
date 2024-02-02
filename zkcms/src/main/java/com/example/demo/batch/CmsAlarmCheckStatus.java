package com.example.demo.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <PRE>
 * 파일명 : CmsAlarmCheckStatus.java
 * 기능 : CMS 상태 정보를 체크한다.
 * 설명 : CMS 상태 정보를 체크하여 해당 내역을 SMS/MAIL 로 전송한다.
 * 최종수정자 : 정영록
 * 최종수정일자 : 2010-10-12
 * </PRE>
 */

public class CmsAlarmCheckStatus  {
	

	/**
	 *  CMS 알람  테이블을 통해   각 측정된  설비  상태를 확인한다.
     * @return  void 
	 */
    public  ArrayList<HashMap<Object, Object>>  checkCmsStatus(Connection  conn_cms) {
     	  	
    	PreparedStatement   pstmt        =  null;
    	ResultSet           rs           =  null;
    	ArrayList<HashMap<Object, Object>>           status_array =  null;
    	
     try {
	        StringBuffer  cms_sb = new StringBuffer();

	        cms_sb.append(" SELECT  TAB.IDX_EQUIPEMENT ");     
	        cms_sb.append("        , EMP_NOM           "); 
	        cms_sb.append("        , EMP_ABG           "); 
	        cms_sb.append("        , EQ_NOM            "); 
	        cms_sb.append("        , EQ_ABG            ");             
	        cms_sb.append("        , TAB.IDX_POINT     ");     
	        cms_sb.append("        , POINT_NOM         ");
	        cms_sb.append("        , POINT_ABG         ");
	        cms_sb.append("        , SEUILDGHAUTMAINT  "); 
	        cms_sb.append("        , SEUILALHAUTMAINT  "); 
	        cms_sb.append("        , PARAM_NAME        ");
	        cms_sb.append("        , DT_MESURE         ");
	        cms_sb.append("        , DH_MESURE         ");
	        cms_sb.append("        , ID_UNITE          ");      
	        cms_sb.append("        , ROUND(TAB.VALEUR , 2)  AS CURR_VALUE ");                  
	        cms_sb.append("        , CG.VALEUR              AS PRIV_VALUE ");             
	        cms_sb.append("        , NVL(ROUND(( (ROUND(TAB.VALEUR , 2) - CG.VALEUR)/ CG.VALEUR) * 100, 2), 0) AS RATIO ");  // 이전 측정치 대비  상승률
	        cms_sb.append("        , NVL(CG.CNT, 0 ) AS  CNT  ");  
	        cms_sb.append(" FROM (  SELECT   EQ.IDX_EQUIPEMENT, EQ.EMP_NOM, EQ.EMP_ABG, EQ.EQ_NOM, EQ.EQ_ABG, PM.IDX_POINT  ");         
	        cms_sb.append("                , PM.NOM  AS POINT_NOM  "); 
	        cms_sb.append("                , PM.ABG  AS POINT_ABG  "); 
	        cms_sb.append("                , AL.SEUILDGHAUTMAINT   ");
	        cms_sb.append("                , AL.SEUILALHAUTMAINT   "); 
	        cms_sb.append("                , PR.NOM  AS PARAM_NAME "); 
	        cms_sb.append("                , TO_CHAR(HT.DH_MESURE , 'MM-DD HH24:MI')         AS DT_MESURE ");   
	        cms_sb.append("                , TO_CHAR(HT.DH_MESURE , 'YYYY-MM-DD HH24:MI:SS') AS DH_MESURE ");   
	        cms_sb.append("                , REPLACE(HT.ID_UNITE, 'UNIT_', '')   AS ID_UNITE              ");   
	        cms_sb.append("                , HT.VALEUR                                                    ");    
	        cms_sb.append("          FROM  (  SELECT  LEV, ARB_NOM, EQ.ABG AS EQ_ABG, EQ.NOM AS EQ_NOM, EQ.IDX_EQUIPEMENT, ICON, IDX_NODE, IDX_PARENT, IDX_ELEMENT, EMP_ABG, EMP_NOM ");
	        cms_sb.append("                     FROM (  SELECT  LEVEL AS LEV, A.LABEL AS ARB_NOM, ICON , IDX_NODE , IDX_PARENT, IDTYPEELEMENT, IDX_ELEMENT "); 
	        cms_sb.append("                                   , IDX_EMPLACEMENT, CONNECT_BY_ROOT ABG AS EMP_ABG , CONNECT_BY_ROOT  NOM  AS  EMP_NOM  ");     
	        cms_sb.append("                              FROM  ARBRE A, EMPLACEMENT E  ");             
	        cms_sb.append("                             WHERE  A.IDX_ELEMENT  = E.IDX_EMPLACEMENT(+) "); // outer join 으로  ARBRE 가 모두 나오게
	        cms_sb.append("                             START WITH IDX_PARENT = 0                    "); // IDX_PARENT = 0 이 최상위 임  level = 1
	        cms_sb.append("                           CONNECT BY PRIOR IDX_NODE = IDX_PARENT         "); // ARBRE 테이블로  계층형 sql 로  최상위 를 찾고  EMPLACEMENT 테이블의  이름을 가져온다. 
	        cms_sb.append("                          )  T , EQUIPEMENT EQ                 ");          
	        cms_sb.append("                    WHERE  T.IDX_ELEMENT   = EQ.IDX_EQUIPEMENT ");
	        cms_sb.append("                      AND  T.IDTYPEELEMENT = 'EQ'              ");
	        cms_sb.append("                  ) EQ                                         ");
	        cms_sb.append("                 , ALARME  AL , POINT_MESURE PM , PARAMETRE PR , HT_PARAMETRE  HT  ");                        
	        cms_sb.append("                 , (SELECT  IDX_PARAMETRE, MAX(DH_MESURE) AS DH_MESURE             ");                       
	        cms_sb.append("                      FROM  HT_PARAMETRE                ");                                                  
	        cms_sb.append("                     GROUP BY IDX_PARAMETRE ) HP        ");
	        cms_sb.append("          WHERE  EQ.IDX_EQUIPEMENT = AL.IDX_EQUIPEMENT  ");                   
	        cms_sb.append("            AND  AL.IDX_PARAMETRE  = PR.IDX_PARAMETRE   ");                   
	        cms_sb.append("            AND  PR.IDX_POINT      = PM.IDX_POINT       ");                   
	        cms_sb.append("            AND  PR.IDX_PARAMETRE  = HP.IDX_PARAMETRE   ");                   
	        cms_sb.append("            AND  HP.IDX_PARAMETRE  = HT.IDX_PARAMETRE   ");                   
	        cms_sb.append("            AND  HP.DH_MESURE      = HT.DH_MESURE       ");                   
	        cms_sb.append("            AND  AL.ETATMAINT IN ('ALM', 'DNG')         ");                   
	        cms_sb.append("            AND  ( PR.NOM  = 'Velocity' OR PR.NOM = 'Acceleration' OR PR.NOM = 'Temperature') "); // 온도:'Temperature' 추가 (2020.11.21)
	        cms_sb.append("      ) TAB  ");   // 요기--------- 알람,위험 측정 값 결과 테이블          
	        cms_sb.append("     , (SELECT CT.IDX_EQUIPEMENT, CT.IDX_POINT, CT.LOG_DATE, CT.CNT , CL.VALEUR, CL.PARAMETRE_NAME  ");             
	        cms_sb.append("         FROM ( SELECT IDX_EQUIPEMENT, IDX_POINT, PARAMETRE_NAME , MAX(LOG_DATE) AS LOG_DATE,  SUM (COUNT(*)) OVER (PARTITION BY IDX_EQUIPEMENT, IDX_POINT) AS CNT ");
	        cms_sb.append("                 FROM CMS_ALARM_LOG                                                "); //위 : POINT 별로 보낸 COUNT 합계를 구한다.(속도/가속도 별 아님)
	        cms_sb.append("                WHERE TO_CHAR(LOG_DATE,'YYYYMMDD') = TO_CHAR(SYSDATE, 'YYYYMMDD')  ");
	        cms_sb.append("                GROUP BY IDX_EQUIPEMENT, IDX_POINT, PARAMETRE_NAME                 ");
	        cms_sb.append("              ) CT , CMS_ALARM_LOG  CL                 ");       
	        cms_sb.append("         WHERE  CT.IDX_EQUIPEMENT = CL.IDX_EQUIPEMENT  ");       
	        cms_sb.append("           AND  CT.IDX_POINT      = CL.IDX_POINT       ");       
	        cms_sb.append("           AND  CT.LOG_DATE       = CL.LOG_DATE        ");
	        cms_sb.append("           AND  CT.PARAMETRE_NAME = CL.PARAMETRE_NAME  ");
	        cms_sb.append("       ) CG  ");   // 요기---------- CMS_ALARM_LOG 보낸 결과 테이블      
	        cms_sb.append(" WHERE  CG.IDX_EQUIPEMENT(+)  = TAB.IDX_EQUIPEMENT     "); // 알람 테이블 결과와 보낸 로그 테이블을 각각 OUTER JOIN 해서  총 2건 이하 (즉 3번보냄) 인것만 조회            
	        cms_sb.append("   AND  CG.IDX_POINT(+)       = TAB.IDX_POINT          ");
	        cms_sb.append("   AND  CG.PARAMETRE_NAME(+)  = TAB.PARAM_NAME         ");
	        cms_sb.append("   AND  ( CG.CNT IS  NULL  OR  CG.CNT <= 2 )           ");            
	        cms_sb.append(" ORDER BY   IDX_EQUIPEMENT,  IDX_POINT, DH_MESURE  ASC ");   
	              
	    	
	        HashMap<Object, Object>  hmap  = null;
	        
			pstmt = conn_cms.prepareStatement(cms_sb.toString()); //  상태 조회 
			rs = pstmt.executeQuery();
			
			status_array  = new ArrayList<>();
			
		       while ( rs.next() ) {
		    	  
		    	  hmap = new HashMap<Object, Object>();
		    	  hmap.put( "IDX_EQUIPEMENT" , rs.getString("IDX_EQUIPEMENT") ); // EQUIP ID
		    	  hmap.put( "IDX_POINT"      , rs.getString("IDX_POINT") ); // POINT ID

		    	  hmap.put( "EMP_NOM"        , rs.getString("EMP_NOM") ); // 공장 명
		    	  hmap.put( "EMP_ABG"        , rs.getString("EMP_ABG") ); // 단축 공장명
		    	  hmap.put( "EQ_NOM"         , rs.getString("EQ_NOM") ); // 설비 명
		    	  hmap.put( "EQ_ABG"         , rs.getString("EQ_ABG") ); // 단축 설비명		    	  
		          hmap.put( "POINT_NOM"      , rs.getString("POINT_NOM") ); //포인트 명
		          hmap.put( "POINT_ABG"      , rs.getString("POINT_ABG") ); //단축 포인트명
		          
		          hmap.put( "SEUILDGHAUTMAINT" , rs.getDouble("SEUILDGHAUTMAINT") ); // 위험값		          
		          hmap.put( "SEUILALHAUTMAINT" , rs.getDouble("SEUILALHAUTMAINT") ); // 주의값
		          hmap.put( "PARAM_NAME"  , rs.getString("PARAM_NAME") ); // 속도/가속도 여부 
		          hmap.put( "DT_MESURE"   , rs.getString("DT_MESURE") );  //측정일자 (date 맞춤)
		          hmap.put( "DH_MESURE"   , rs.getString("DH_MESURE") );  //원 측정일자
		          hmap.put( "CURR_VALUE"  , rs.getDouble("CURR_VALUE") ); //측정 값 
		          hmap.put( "PRIV_VALUE"  , rs.getDouble("PRIV_VALUE") ); // 이전 측정값
		          hmap.put( "RATIO"       , rs.getDouble("RATIO") ); // 이전 측정치 대비 상승률
		          hmap.put( "CNT"         , rs.getInt("CNT") ); // 금일 발생 횟수 
		          hmap.put( "ID_UNITE"    , rs.getString("ID_UNITE") ); // 단위
//
//		          hmap.put( "IDX_EQUIPEMENT" , rs.getString("IDX_EQUIPEMENT") ); // EQUIP ID
//		    	  hmap.put( "IDX_POINT"      , rs.getString("IDX_POINT") ); // POINT ID
//
//		    	  hmap.put( "EMP_NOM"        , rs.getString("EMP_NOM") ); // 공장 명
//		    	  hmap.put( "EMP_ABG"        , rs.getString("EMP_ABG") ); // 단축 공장명
//		    	  hmap.put( "EQ_NOM"         , rs.getString("EQ_NOM") ); // 설비 명
//		    	  hmap.put( "EQ_ABG"         , rs.getString("EQ_ABG") ); // 단축 설비명		    	  
//		          hmap.put( "POINT_NOM"      , rs.getString("POINT_NOM") ); //포인트 명
//		          hmap.put( "POINT_ABG"      , rs.getString("POINT_ABG") ); //단축 포인트명
//		          
//		          hmap.put( "SEUILDGHAUTMAINT" , rs.getDouble("SEUILDGHAUTMAINT") ); // 위험값		          
//		          hmap.put( "SEUILALHAUTMAINT" , rs.getDouble("SEUILALHAUTMAINT") ); // 주의값
//		          hmap.put( "PARAM_NAME"  , rs.getString("PARAM_NAME") ); // 속도/가속도 여부 
//		          hmap.put( "DT_MESURE"   , rs.getString("DT_MESURE") );  //측정일자 (date 맞춤)
//		          hmap.put( "DH_MESURE"   , rs.getString("DH_MESURE") );  //원 측정일자
//		          hmap.put( "CURR_VALUE"  , rs.getDouble("CURR_VALUE") ); //측정 값 
//		          hmap.put( "PRIV_VALUE"  , rs.getDouble("PRIV_VALUE") ); // 이전 측정값
//		          hmap.put( "RATIO"       , rs.getDouble("RATIO") ); // 이전 측정치 대비 상승률
//		          hmap.put( "CNT"         , rs.getInt("CNT") ); // 금일 발생 횟수 
//		          hmap.put( "ID_UNITE"    , rs.getString("ID_UNITE") ); // 단위
//		          
		          status_array.add(hmap); 
		       } 

	     }	catch (Exception e) {
	    	   e.printStackTrace();
	     } finally {
	    	  try {
	    	    if(rs    != null)  rs.close(); 
	    	    if(pstmt != null)  pstmt.close(); 	    	    
	    	  } catch(Exception ee) { ee.printStackTrace(); }  
	     }
	     
	       return  status_array;
    }
     
     
	/**
	 *  CMS 관리자 테이블에 등록되어 있는 해당 point 담당자 를 조회 한다.
     * @return  void 
	 */
    public  ArrayList<HashMap<Object, Object>>  getCmsAdminInfo(Connection  conn_cms, String  idx_point) {
     	  	
    	PreparedStatement pstmt        =  null;
    	ResultSet         rs           =  null;
    	ArrayList<HashMap<Object, Object>>         admin_array =  null;
    	
     try {
	    	StringBuffer  cms_sb = new StringBuffer(); 	   	
	    		    	
	    	cms_sb.append("  SELECT IDX_CMS_ADMIN, IDX_POINT, POINT_TYPE, ");
	        cms_sb.append("         TRIM(ADMIN_NAME) AS ADMIN_NAME , TRIM(ADMIN_PHONE) AS ADMIN_PHONE, ");
	        cms_sb.append("         TRIM(ADMIN_MAIL) AS ADMIN_MAIL  ");
	        cms_sb.append("   FROM  CMS_ADMIN_INFO  ");
	        cms_sb.append("  WHERE  IDX_POINT = ?   "); //해당 point 담당자

	        HashMap<Object, Object> hmap  = null;

			pstmt = conn_cms.prepareStatement(cms_sb.toString());
			pstmt.setString(1, idx_point); 

			rs = pstmt.executeQuery();
			
			admin_array  = new ArrayList<>();
			
		       while ( rs.next() ) {
		    	  
		    	  hmap = new HashMap<Object, Object>();
		    	  
		    	  hmap.put( "IDX_POINT"   , rs.getString("IDX_POINT") );
		    	  hmap.put( "ADMIN_NAME"  , rs.getString("ADMIN_NAME") );
		    	  hmap.put( "ADMIN_PHONE" , rs.getString("ADMIN_PHONE") );
		    	  hmap.put( "ADMIN_MAIL"  , rs.getString("ADMIN_MAIL") );

		    	  
//		    	  hmap.put( "IDX_POINT"   , rs.getString("IDX_POINT") );
//		    	  hmap.put( "ADMIN_NAME"  , rs.getString("ADMIN_NAME") );
//		    	  hmap.put( "ADMIN_PHONE" , rs.getString("ADMIN_PHONE") );
//		    	  hmap.put( "ADMIN_MAIL"  , rs.getString("ADMIN_MAIL") );
//		    	  
		    	  admin_array.add(hmap); 
		       }

	     }	catch (Exception e) {
	    	   e.printStackTrace();
	     } finally {
	    	  try {
	    	    if(rs    != null)  rs.close(); 
	    	    if(pstmt != null)  pstmt.close(); 	    	    
	    	  } catch(Exception ee) { ee.printStackTrace(); }  
	     }
	     
	       return  admin_array;
    }
    
 } // end class