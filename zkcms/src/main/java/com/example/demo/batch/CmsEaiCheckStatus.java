package com.example.demo.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet ;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <PRE>
 * 파일명 : CmsEaiCheckStatus.java
 * 기능 : EAI 전송을 위한 CMS 상태 정보를 체크한다.
 * 설명 : CMS 상태 정보를 체크하여 해당 내역을  EAI 로 전송한다.
 * 최종수정자 : 정영록
 * 최종수정일자 : 2011-05-24
 * </PRE>
 */

public class CmsEaiCheckStatus  {
 

 /**
  *  EAI 전송될   시보 정보를 확인한다.
  * @return  ArrayList 
  */
    public  ArrayList<HashMap<Object, Object>>  checkSiboInfo(Connection  conn_cms)  throws  Exception {
         
     PreparedStatement pstmt        =  null;
     ResultSet         rs           =  null;
     ArrayList<HashMap<Object, Object>>         status_array =  null;
     
     try {
          StringBuffer  cms_sb = new StringBuffer();
           
	      cms_sb.append(" SELECT   EQUIP_CODE, SENSOR_NO, TAB.IDX_POINT,                       "); 
	      cms_sb.append("          TO_CHAR(SYSDATE,'YYYYMMDDHH24MISS') AS SEND_DATE,           ");
	      //cms_sb.append("          TO_CHAR(TAB.DH_MESURE,'YYYYMMDDHH24MISS') AS HTP_DH_MESURE, ");
	      cms_sb.append("          TO_CHAR(SYSDATE - (1/(24*12)) ,'YYYYMMDDHH24MI')||'00' AS HTP_DH_MESURE, "); //측정시간을 SYSDATE 5분 전으로  일괄 변경
	      cms_sb.append("          VELO_VALUE, VELO_AL_VALUE, VELO_DG_VALUE,                   ");
	      cms_sb.append("          ACCE_VALUE, ACCE_AL_VALUE, ACCE_DG_VALUE,                   ");
	      cms_sb.append("          TEMP_VALUE, TEMP_AL_VALUE, TEMP_DG_VALUE,                   ");
	      cms_sb.append("          TAB.DATA_TYPE, STATUS                                       ");
	      cms_sb.append("  FROM (                                                              ");
	      cms_sb.append("         SELECT EQUIP_CODE, SENSOR_NO , IDX_POINT ,                   ");
	      cms_sb.append("                MAX(DH_MESURE ) AS DH_MESURE ,                        ");
	      cms_sb.append("                MAX(DECODE(PARAM_NAME , 'VELO' , VALEUR , '')) AS  VELO_VALUE ,                 ");
	      cms_sb.append("                MAX(DECODE(PARAM_NAME , 'VELO' , SEUILALHAUTMAINT , '')) AS  VELO_AL_VALUE ,    ");
	      cms_sb.append("                MAX(DECODE(PARAM_NAME , 'VELO' , SEUILDGHAUTMAINT , '')) AS  VELO_DG_VALUE ,    ");
	      cms_sb.append("                MAX(DECODE(PARAM_NAME , 'ACCE' , VALEUR , '')) AS  ACCE_VALUE ,                 ");
	      cms_sb.append("                MAX(DECODE(PARAM_NAME , 'ACCE' , SEUILALHAUTMAINT , '')) AS  ACCE_AL_VALUE ,    ");
	      cms_sb.append("                MAX(DECODE(PARAM_NAME , 'ACCE' , SEUILDGHAUTMAINT , '')) AS  ACCE_DG_VALUE ,    ");
	      cms_sb.append("                MAX(DECODE(PARAM_NAME , 'TEMP' , VALEUR , '')) AS  TEMP_VALUE ,                 ");
	      cms_sb.append("                MAX(DECODE(PARAM_NAME , 'TEMP' , SEUILALHAUTMAINT , '')) AS  TEMP_AL_VALUE ,    ");
	      cms_sb.append("                MAX(DECODE(PARAM_NAME , 'TEMP' , SEUILDGHAUTMAINT , '')) AS  TEMP_DG_VALUE ,    ");
	      cms_sb.append("                MAX(DECODE(PARAM_NAME , 'TEMP' , 'TT' , 'VV')) AS  DATA_TYPE ,                  ");
	      cms_sb.append("                MIN(DECODE(ETATMAINT  , 'RAS'  , 'NORML' , 'ALAM'))  AS  STATUS                 ");
	      cms_sb.append("          FROM (                                                               ");
	      cms_sb.append("             SELECT EQ.IDX_EQUIPEMENT, KE.EQUIP_CODE ,                         ");
	      cms_sb.append("                    PM.IDX_POINT     , KP.SENSOR_NO ,                          ");
	      cms_sb.append("                    HP.IDX_PARAMETRE , HP.DH_MESURE ,                          ");
	      cms_sb.append("                    AL.ETATMAINT,                                              ");
	      cms_sb.append("                    ROUND(HT.VALEUR, 2) * 100           AS VALEUR ,            ");
	      cms_sb.append("                    ROUND(AL.SEUILALHAUTMAINT, 2) * 100 AS SEUILALHAUTMAINT ,  ");
	      cms_sb.append("                    ROUND(AL.SEUILDGHAUTMAINT, 2) * 100 AS SEUILDGHAUTMAINT ,  ");
	      cms_sb.append("                    CASE                                                       ");
	      cms_sb.append("                        WHEN  PR.NOM = 'Velocity'     THEN 'VELO'  ");
	      cms_sb.append("                        WHEN  PR.NOM = 'Acceleration' THEN 'ACCE'  ");
	      cms_sb.append("                        WHEN  PR.NOM = 'Temperature'  THEN 'TEMP'  ");
	      cms_sb.append("                    END  AS  PARAM_NAME                            ");
	      cms_sb.append("             FROM (                                                ");
	      cms_sb.append("                    SELECT  HT.IDX_PARAMETRE, MAX(DH_MESURE) AS DH_MESURE ");
	      cms_sb.append("                      FROM  HT_PARAMETRE HT, PARAMETRE PR , ALARME AL     ");   
	      cms_sb.append("                     WHERE  PR.IDX_PARAMETRE  = HT.IDX_PARAMETRE          ");   
	      cms_sb.append("                       AND  AL.IDX_PARAMETRE  = HT.IDX_PARAMETRE          ");
	      cms_sb.append("                       AND  AL.ETATMAINT IN ('RAS','ALM','DNG')           ");
	      cms_sb.append("                       AND  ( PR.NOM  = 'Velocity' OR PR.NOM = 'Acceleration' OR PR.NOM = 'Temperature' ) ");
	      cms_sb.append("                       AND  DH_MESURE > SYSDATE - 30 ");
	      cms_sb.append("                     GROUP BY HT.IDX_PARAMETRE       ");         
	      cms_sb.append("                    ) HP ,                           "); // EAI_SIBO_LOG 테이블에서  직접 조회로 변경함 2013.11.30
	      cms_sb.append("                    EQUIPEMENT  EQ , KCMS_EQUIPEMENT KE, KCMS_PARAMETRE KP, POINT_MESURE  PM ,            ");
	      cms_sb.append("                    PARAMETRE   PR , HT_PARAMETRE  HT ,            ");
	      cms_sb.append("                    ALARME      AL                                 ");
	      cms_sb.append("             WHERE  EQ.IDX_EQUIPEMENT = PM.IDX_EQUIPEMENT          ");
	      cms_sb.append("               AND  PM.IDX_POINT      = PR.IDX_POINT               ");
	      cms_sb.append("               AND  PR.IDX_PARAMETRE  = HT.IDX_PARAMETRE           ");
	      cms_sb.append("               AND  HT.IDX_PARAMETRE  = HP.IDX_PARAMETRE           ");
	      cms_sb.append("               AND  HT.DH_MESURE      = HP.DH_MESURE               ");
	      cms_sb.append("               AND  AL.IDX_PARAMETRE  = HT.IDX_PARAMETRE           ");
	      
	      cms_sb.append("               AND  EQ.IDX_EQUIPEMENT  =  KE.IDX_EQUIPEMENT        ");
	      cms_sb.append("               AND  PR.IDX_PARAMETRE  = KP.IDX_PARAMETRE           ");
	      
	      
          cms_sb.append("          )    ");
          cms_sb.append("            GROUP BY  EQUIP_CODE, SENSOR_NO, IDX_POINT, DECODE(PARAM_NAME, 'TEMP', 'TT', 'VV') "); // 온도, 진동 별  gropu by 추가 
          cms_sb.append("    ) TAB      ");
          cms_sb.append("  ORDER BY  IDX_POINT  ASC  ");
               
         HashMap<Object, Object> hmap  = null;
         
         pstmt = conn_cms.prepareStatement(cms_sb.toString()); //  상태 조회 
         rs = pstmt.executeQuery();
   
         status_array  = new ArrayList<>();
   
         while ( rs.next() ) {
         
            hmap = new HashMap<>();
            hmap.put( "EQUIP_CODE"    , CmsUtil.nvl(rs.getString("EQUIP_CODE")) );
            hmap.put( "SENSOR_NO"     , CmsUtil.nvl(rs.getString("SENSOR_NO")) );
            hmap.put( "IDX_POINT"     , rs.getString("IDX_POINT") );
            hmap.put( "SEND_DATE"     , rs.getString("SEND_DATE") );
            hmap.put( "HTP_DH_MESURE" , rs.getString("HTP_DH_MESURE") );
          
            hmap.put( "VELO_VALUE"    , rs.getString("VELO_VALUE") );
            hmap.put( "VELO_AL_VALUE" , rs.getString("VELO_AL_VALUE") );
            hmap.put( "VELO_DG_VALUE" , rs.getString("VELO_DG_VALUE") );
            
            hmap.put( "ACCE_VALUE"    , rs.getString("ACCE_VALUE") ); 
            hmap.put( "ACCE_AL_VALUE" , rs.getString("ACCE_AL_VALUE") );
            hmap.put( "ACCE_DG_VALUE" , rs.getString("ACCE_DG_VALUE") ); 
            
            hmap.put( "TEMP_VALUE"    , rs.getString("TEMP_VALUE") ); 
            hmap.put( "TEMP_AL_VALUE" , rs.getString("TEMP_AL_VALUE") );
            hmap.put( "TEMP_DG_VALUE" , rs.getString("TEMP_DG_VALUE") );
            
            hmap.put( "DATA_TYPE"   , rs.getString("DATA_TYPE") );
            hmap.put( "STATUS"      , rs.getString("STATUS") );
//            
//            hmap.put( "EQUIP_CODE"    , CmsUtil.nvl(rs.getString("EQUIP_CODE")) );
//            hmap.put( "SENSOR_NO"     , CmsUtil.nvl(rs.getString("SENSOR_NO")) );
//            hmap.put( "IDX_POINT"     , rs.getString("IDX_POINT") );
//            hmap.put( "SEND_DATE"     , rs.getString("SEND_DATE") );
//            hmap.put( "HTP_DH_MESURE" , rs.getString("HTP_DH_MESURE") );
//          
//            hmap.put( "VELO_VALUE"    , rs.getString("VELO_VALUE") );
//            hmap.put( "VELO_AL_VALUE" , rs.getString("VELO_AL_VALUE") );
//            hmap.put( "VELO_DG_VALUE" , rs.getString("VELO_DG_VALUE") );
//            
//            hmap.put( "ACCE_VALUE"    , rs.getString("ACCE_VALUE") ); 
//            hmap.put( "ACCE_AL_VALUE" , rs.getString("ACCE_AL_VALUE") );
//            hmap.put( "ACCE_DG_VALUE" , rs.getString("ACCE_DG_VALUE") ); 
//            
//            hmap.put( "TEMP_VALUE"    , rs.getString("TEMP_VALUE") ); 
//            hmap.put( "TEMP_AL_VALUE" , rs.getString("TEMP_AL_VALUE") );
//            hmap.put( "TEMP_DG_VALUE" , rs.getString("TEMP_DG_VALUE") );
//            
//            hmap.put( "DATA_TYPE"   , rs.getString("DATA_TYPE") );
//            hmap.put( "STATUS"      , rs.getString("STATUS") );             
        
            status_array.add(hmap); 
         } 

      } catch (Exception e) {
         e.printStackTrace();
         throw  e;
      } finally {
        try {
          if(rs    != null)  rs.close(); 
          if(pstmt != null)  pstmt.close();           
        } catch(Exception ee) { ee.printStackTrace(); }  
      }
      
        return  status_array;
    }
     
     

    /**
     *  EAI 전송될   센서 이상  정보를 확인한다.
     * @return  ArrayList 
     */
       public  ArrayList<HashMap<Object, Object>>  checkSensorErrorInfo(Connection  conn_cms)  throws  Exception {
            
        PreparedStatement pstmt        =  null;
        ResultSet         rs           =  null;
        ArrayList<HashMap<Object, Object>>         status_array =  null;
        
        try {
            StringBuffer  cms_sb = new StringBuffer();
              
            cms_sb.append("  SELECT  EQUIP_CODE, SENSOR_NO , IDX_POINT,                            ");
            cms_sb.append("          TAB3.IDX_ELEMENT_CAT AS MVX_NO, TAB3.CHANNEL_NO,  DATA_TYPE , ");
            //cms_sb.append("          TO_CHAR(TAB2.DH_CREATION,'YYYYMMDDHH24MISS') AS DH_CREATION , ");
            cms_sb.append("          TO_CHAR(SYSDATE - (1/(24*12)) ,'YYYYMMDDHH24MI')||'00' AS DH_CREATION , "); //측정시간을 SYSDATE 5분 전으로  일괄 변경
            cms_sb.append("          DECODE( TAB2.ERROR_TYPE ,'1' , '3' , '2') AS ERROR_TYPE       "); //-- 1: 단락 '3'  2:단선'2'  로 해서 보냄  
            cms_sb.append("    FROM  ( SELECT  STATUS, ETIQUETTE ,                                 ");
            cms_sb.append("                    SUBSTR(ETIQUETTE, 4 , (INSTR( ETIQUETTE, 'Ch',1 ,1 ) - 4) ) AS MVX_NO , ");
            cms_sb.append("                    SUBSTR(ETIQUETTE, INSTR( ETIQUETTE, 'Ch',1 ,1 )+2  ) AS CHANNEL_NO      ");
            cms_sb.append("              FROM  FEUILLE_DATASOURCE   ");
            cms_sb.append("             WHERE  STATUS = '34'        "); //-- 센서 이상 상태 의미
            cms_sb.append("          ) TAB1,                        "); //-- FEUILLE_DATASOURCE 테이블 (오류가 발생되면 해당 테이블에 status 가 '34' 표시되고  정상일경우 '2'로 update 됨  
            cms_sb.append("          ( SELECT  IDX_ELEMENT_CAT, LIST_ERREURS,     ");
            cms_sb.append("                    MAX(DH_CREATION)  AS DH_CREATION , ");
            cms_sb.append("                    SUBSTR(LIST_ERREURS,  LENGTH(LIST_ERREURS), 1 ) AS ERROR_TYPE, ");
            cms_sb.append("                    SUBSTR(LIST_ERREURS, INSTR( LIST_ERREURS, ';',1 ,1 )+1  , INSTR( LIST_ERREURS, ';',1 ,2 ) - (INSTR( LIST_ERREURS, ';',1 ,1 )+1)  ) AS CHANNEL_NO ");
            cms_sb.append("              FROM EVENEMENTS  A                  ");
            cms_sb.append("             WHERE LIST_ERREURS LIKE 'NUM_VOIE%'  ");
            cms_sb.append("               AND SUBSTR(LIST_ERREURS,  LENGTH(LIST_ERREURS), 1 ) IN ('1','2') "); //-- 1:단락 ,  2:단선  의미 
            cms_sb.append("               AND LENGTH(LIST_ERREURS)  > 25         ");
            cms_sb.append("              GROUP BY IDX_ELEMENT_CAT, LIST_ERREURS  ");
            cms_sb.append("          ) TAB2 ,                                    "); //-- EVENEMENTS 테이블은 센서 이상일 경우 지속적으로   값이 insert 됨  mvx , channel 값 확인 
            cms_sb.append("          ( SELECT  DISTINCT  KE.EQUIP_CODE, PM.IDX_POINT, KP.SENSOR_NO, PR.IDX_ELEMENT_CAT, KP.CHANNEL_NO, ");
            cms_sb.append("                    CASE   ");                                                    
            cms_sb.append("                      WHEN  PR.NOM = 'Velocity' OR  PR.NOM = 'Acceleration' THEN 'VV' "); //-- 진동
            cms_sb.append("                      WHEN  PR.NOM = 'Temperature'  THEN 'TT'                         "); //-- 온도
            cms_sb.append("                    END  AS  DATA_TYPE                              ");
            cms_sb.append("              FROM  EQUIPEMENT  EQ, POINT_MESURE PM, PARAMETRE  PR, KCMS_EQUIPEMENT  KE , KCMS_PARAMETRE  KP  ");
            cms_sb.append("             WHERE  EQ.IDX_EQUIPEMENT = PM.IDX_EQUIPEMENT           ");
            cms_sb.append("               AND  PM.IDX_POINT      = PR.IDX_POINT                ");
            cms_sb.append("               AND  ( PR.NOM  = 'Velocity' OR PR.NOM = 'Acceleration' OR PR.NOM = 'Temperature' ) ");
            cms_sb.append("          ) TAB3                                 ");
            cms_sb.append("  WHERE  TAB1.MVX_NO     = TAB2.IDX_ELEMENT_CAT  ");
            cms_sb.append("    AND  TAB1.CHANNEL_NO = TAB2.CHANNEL_NO       ");
            cms_sb.append("    AND  TAB3.IDX_ELEMENT_CAT = TAB1.MVX_NO      ");
            cms_sb.append("    AND  TAB3.CHANNEL_NO      = TAB1.CHANNEL_NO  ");
            
            cms_sb.append("    AND  EQ.IDX_EQUIPEMENT = KE.IDX_EQUIPEMENT  ");
            cms_sb.append("    AND  PR.IDX_PARAMETRE = KP.IDX_PARAMETRE  ");
            
            cms_sb.append("  ORDER BY MVX_NO, CHANNEL_NO ASC                ");
         
            
            HashMap<Object, Object> hmap  = null;
            
            pstmt = conn_cms.prepareStatement(cms_sb.toString()); //  상태 조회 
            rs = pstmt.executeQuery();
      
            status_array  = new ArrayList<>();
      
            while ( rs.next() ) {
            
               hmap = new HashMap<>();
               hmap.put( "EQUIP_CODE"  , CmsUtil.nvl(rs.getString("EQUIP_CODE")) );
               hmap.put( "SENSOR_NO"   , CmsUtil.nvl(rs.getString("SENSOR_NO")) );
               hmap.put( "IDX_POINT"   , rs.getString("IDX_POINT") ); 
               hmap.put( "DH_CREATION" , rs.getString("DH_CREATION") );
               hmap.put( "DATA_TYPE"   , rs.getString("DATA_TYPE") );
               hmap.put( "ERROR_TYPE"  , rs.getString("ERROR_TYPE") ); 
 
               status_array.add(hmap); 
            } 

         } catch (Exception e) {
            e.printStackTrace();
            throw  e;
         } finally {
           try {
             if(rs    != null)  rs.close(); 
             if(pstmt != null)  pstmt.close();           
           } catch(Exception ee) { ee.printStackTrace(); }  
         }
         
           return  status_array;
       }
        

    
 } // end class
