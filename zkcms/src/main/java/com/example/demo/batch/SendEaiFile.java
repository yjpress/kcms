package com.example.demo.batch;

import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileInputStream;
//import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Properties;
import java.util.ArrayList;
import com.posdata.eai.adapter.EAIHandler;

/**
 * <PRE>
 * 파일명 : SendEaiFile.java
 * 기능 : EAI 전송파일을  생성 및   전송한다.
 * 설명 : CMS 상태정보를  파일로 생성하여  EAI 전송한다.
 * 최종수정자 : 정영록
 * 최종수정일자 : 2011-06-02
 * </PRE>
 */
public class SendEaiFile  {

 
   Properties  props  = null;// Properties
   String   TRANSACTION_CODE = null; // TRANSACTION_CODE
   String   INTERFACE_ID     = null; // INTERFACE_ID
   String   EAI_FILE_PATH    = null; // 파일 생성 위치 
   String   EAI_CFG_FILE     = null; // EAI CFG 파일    
   
   String   EAI_SIBO_FILE         = null; // EAI 전송 시보 파일
   String   EAI_SENSOR_ERROR_FILE = null; // EAI 전송 센서 오류 파일 
   
   String   EAI_FILE_DELETE  = "1"; // 전송파일 삭제 여부  TRUE: 1 , FALSE: 0 
   
   public  void  setSysInfo(Properties  p) {   
     props = p;
     INTERFACE_ID     = props.getProperty("INTERFACE_ID");
     EAI_FILE_PATH    = props.getProperty("EAI_FILE_PATH");
     EAI_CFG_FILE     = props.getProperty("EAI_CFG_FILE");
     EAI_FILE_DELETE  = props.getProperty("EAI_FILE_DELETE");
   }
   
   
    /**
     * <PRE>
     * EAI 전송할 파일을 생성한다 .(시보파일)
     * @param    status_array  cms 상태정보 array
     * @param    makeType  생성할 파일 유형 (시보,알람)
     * </PRE>
     */
    public  void  makeSiboFile(ArrayList<HashMap<Object, Object>> status_array, String transaction_code)  throws  Exception {
        
        BufferedWriter out        = null;
        StringBuffer   message    = null;
        HashMap<Object, Object>        hmap       = null;
        String         dateTimeMs = null;           
        
     try {

          dateTimeMs = CmsUtil.getDateFormat("yyyyMMddHHmmssSSS");
          TRANSACTION_CODE = transaction_code; // TR 코드를 받아온다.
          
         //시보 파일 생성일 경우  
          EAI_SIBO_FILE  = TRANSACTION_CODE + "_" + dateTimeMs+"_0" + ".DAT";
          out = new BufferedWriter(new FileWriter(EAI_FILE_PATH+"/"+EAI_SIBO_FILE)); 
    
          // 상태 결과값  건수 만큼  메세지 생성 
          // 보낸 자료는  다시 조회 안되게 쿼리 변경 필요 
 
          for (int i=0 ; i < status_array.size() ; i++ ) { 
           
           hmap = (HashMap<Object, Object>)status_array.get(i);

           message = new StringBuffer();
      
           message.append(CmsUtil.convertBlankFormat(String.valueOf(hmap.get("EQUIP_CODE")), 9) ); // 설비공정코드 (694425610) (9자리)
           message.append(String.valueOf(hmap.get("DATA_TYPE")) );                            // 유형구분 =진동(VV) 또는  온도(TT) (2자리) 
           message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("SENSOR_NO")), 5) );  // 일련번호 (센서 번호  5자리) 
           message.append("H"); // CMS 타입 ( H: 시보 , S: 센서 이상정보   1자리)
           message.append(String.valueOf(hmap.get("HTP_DH_MESURE"))); // 발생시간(DATA 수집 시간) (14자리) 
           message.append("1"); // 발생상태 ( 시보의 경우는 1, 센서 이상 정보는 2,3   1자리)
 
           if("VV".equals( String.valueOf(hmap.get("DATA_TYPE"))) ) { 
             // 진송 데이터 일경우  
              message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("VELO_VALUE")), 8) );    //속도 측정값  8자리
              message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("VELO_AL_VALUE")), 8) ); //속도 알람값  8자리
              message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("VELO_DG_VALUE")), 8) ); //속도 위험값  8자리
            
              message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("ACCE_VALUE")), 8) );    //가속도 측정값  8자리
              message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("ACCE_AL_VALUE")), 8) ); //가속도 알람값  8자리
              message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("ACCE_DG_VALUE")), 8) ); //가속도 위험값  8자리
           } else {
             // 온도 일경우 
              message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("TEMP_VALUE")), 8) );    //온도 측정값  8자리
              message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("TEMP_AL_VALUE")), 8) ); //온도 알람값  8자리
              message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("TEMP_DG_VALUE")), 8) ); //온도 위험값  8자리
             // 동일하게 1번더 채움 (요청)
              message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("TEMP_VALUE")), 8) );    //온도 측정값  8자리
              message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("TEMP_AL_VALUE")), 8) ); //온도 알람값  8자리
              message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("TEMP_DG_VALUE")), 8) ); //온도 위험값  8자리
          }

          message.append(CmsUtil.convertBlankFormat("", 50) ); // 공백  50자리
           
          out.write( message.toString() );
          out.newLine();
       
         } // end for
          
         System.out.println("SIBO File Create OK!");  
        
      } catch (Exception e) {
           e.printStackTrace();
           throw e;
      } finally {
          if(out != null) out.close();    
      }   
          
  }


    /**
     * <PRE>
     * EAI 전송할 파일을 생성한다 .(시보파일)
     * @param    status_array  cms 상태정보 array
     * @param    makeType  생성할 파일 유형 (시보,알람)
     * </PRE>
     */
    public  void  makeSensorErrorFile(ArrayList<HashMap<Object, Object>> status_array, String transaction_code)  throws  Exception {
        
        BufferedWriter out        = null;
        StringBuffer   message    = null;
        HashMap<Object, Object>        hmap       = null;
        String         dateTimeMs = null;           
        
     try {

          dateTimeMs = CmsUtil.getDateFormat("yyyyMMddHHmmssSSS");
          TRANSACTION_CODE = transaction_code; // TR 코드를 받아온다.
          
          //센서 오류 전송 파일 생성  
          EAI_SENSOR_ERROR_FILE  = TRANSACTION_CODE + "_" + dateTimeMs+"_9" + ".DAT";
          out = new BufferedWriter(new FileWriter(EAI_FILE_PATH+"/"+EAI_SENSOR_ERROR_FILE));  
                
          for (int i=0 ; i < status_array.size() ; i++ ) { 
           
           hmap = (HashMap<Object, Object>)status_array.get(i);
           message = new StringBuffer();
           
           message.append(CmsUtil.convertBlankFormat(String.valueOf(hmap.get("EQUIP_CODE")), 9) ); // 설비공정코드 (694425610) (9자리)
           message.append(String.valueOf(hmap.get("DATA_TYPE")) );                            // 유형구분 =진동(VV) 또는  온도(TT) (2자리) 
           message.append(CmsUtil.convertFormat(String.valueOf(hmap.get("SENSOR_NO")), 5) );  // 일련번호 (센서 번호  5자리) 
           message.append("S");                                     // CMS 타입 ( H: 시보 , S: 센서 이상정보   1자리)
           message.append(String.valueOf(hmap.get("DH_CREATION"))); // 발생시간(DATA 수집 시간) (14자리) 
           message.append(String.valueOf(hmap.get("ERROR_TYPE")));  // 센서 이상 정보는  1자리    단락 '3'   단선'2'  로 해서 보냄 )
  
           message.append(CmsUtil.convertFormat("", 8) ); //'0'  8자리
           message.append(CmsUtil.convertFormat("", 8) ); //'0'  8자리
           message.append(CmsUtil.convertFormat("", 8) ); //'0'  8자리
            
           message.append(CmsUtil.convertFormat("", 8) ); //'0'  8자리
           message.append(CmsUtil.convertFormat("", 8) ); //'0'  8자리
           message.append(CmsUtil.convertFormat("", 8) ); //'0'  8자리
        
           message.append(CmsUtil.convertBlankFormat("", 50) ); // 공백  50자리

           out.write( message.toString() );
           out.newLine();
       
         } // end for
          
          System.out.println("SENSOR ERROR File Create OK!");  
        
      } catch (Exception e) {
           e.printStackTrace();
           throw e;
      } finally {
          if(out != null) out.close();    
      }   
          
  }
 
 
    /**
     * <PRE>
     * 생성된  파일을  최종  EAI 전송  한다.
     * @param    fileType   전송 파일 유형 (시보,알람)
     * @return   isSuccess  true(전송성공) false(전송실패) 
     * </PRE>
     */
    public  boolean  sendEaiFile(String  fileType)  throws  Exception {
  
      EAIHandler eaiHandler = null;
      String    file_name   = null;
      boolean   isSuccess   = false;
      
      File   file = null;
      
      int returnValue = 0;      
      
       try {
           
           if("SIBO".equals(fileType) ) {
            //시보파일 전송
             file_name = EAI_SIBO_FILE;
             file = new File(EAI_FILE_PATH+"/"+file_name);
          
           } else {
            //센서 이상 정보  파일 전송    
             file_name = EAI_SENSOR_ERROR_FILE;
             file = new File(EAI_FILE_PATH+"/"+file_name);
           }
        
           
           if(file.exists()) {             
               if(file.length() <= 0 ) {
                  System.out.println(fileType+" File Size 0");
                  if("1".equals(EAI_FILE_DELETE)) { // 1: true 
                     file.delete();
                  }
                  return  true; // 삭제후 정상 종료( 알람 데이터가 존재하지 않는 정상 ) 
               }
               
           } else {
              throw new Exception(fileType+" File not Exists!!! !");
           }
      
      
           eaiHandler  = new EAIHandler();   
           returnValue = eaiHandler.initialize(EAI_CFG_FILE);
        
           if (returnValue == 0) {
             System.out.println("u-CUBE Engine과의 연결을 성공했습니다.");
           } else {
             System.out.println("u-CUBE Engine과의 연결을 실패했습니다. 결과= "+ returnValue);
             eaiHandler.onClose();
             return  false;
           }
    
           returnValue = -1;                        
           returnValue = eaiHandler.sendFile( INTERFACE_ID, EAI_FILE_PATH, file_name);
          
         
           switch (returnValue) {
            case 0 :
                 System.out.println(returnValue+" :파일송신 성공. ");
                 isSuccess = true;
                 break;
            case 1 :
                 System.out.println(returnValue+" :송신실패, 로컬큐잉 되었습니다. ");
                 break;
            case -1 :
                 System.out.println(returnValue+" :송신실패, Interface ID 확인 필요 ");
                 break;
            case -2 :
                 System.out.println(returnValue+" :송신실패");
                 break;
            case -3 :
                 System.out.println(returnValue+" :송신실패, 파일이 존재하지 않습니다.");
                 break;
            default :
                 System.out.println(returnValue+" :송신실패, CFG, IFD 파일설정 오류.");
                 break;
           }
         
            isSuccess = true;
            if(isSuccess && "1".equals(EAI_FILE_DELETE) ) { // 1: true
          		file.delete(); // 기존 파일 삭제 
          	}
          	
          	
        } catch (Exception e) {
           isSuccess =  false;
           System.out.println("EAI File Send Exception!! ");
           e.printStackTrace();
           throw e;
        } finally {
            if( eaiHandler != null) {
            	eaiHandler.onClose();
            }
        }
        
        return  isSuccess;
    }
  
 
}
