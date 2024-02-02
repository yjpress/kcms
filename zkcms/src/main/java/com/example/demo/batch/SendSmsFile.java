package com.example.demo.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Properties;
import java.util.ArrayList;
import org.apache.commons.net.ftp.*;



/**
 * <PRE>
 * 파일명 : SendSmsFile.java
 * 기능 : SMS 전송파일을  생성 및  FTP 전송한다.
 * 설명 : CMS 상태정보를  SMS 처리를 위한 FTP 전송한다.
 * 최종수정자 : 정영록
 * 최종수정일자 : 2010-11-23
 * </PRE>
 */
public class SendSmsFile  {

	
   Properties  props  = null;// Properties
   String   FTP_IP    = null;// IP
   String   FTP_USER  = null;// 계정
   String   FTP_PWD   = null;// passwd
   String   FTP_PATH  = null;// 전송될 경로 위치   
   String   SMS_FILE_DELETE = "1"; // 전송파일 삭제 여부  TRUE: 1 , FALSE: 0 
   
   boolean  isSuccess = true; //성공 여부 
   
   public  void  setSysInfo(Properties  p) {	  
	   props = p;
	   FTP_IP   = props.getProperty("SMS.FTP.IP");
	   FTP_USER = props.getProperty("SMS.FTP.USER");
	   FTP_PWD  = props.getProperty("SMS.FTP.PWD");
	   FTP_PATH = props.getProperty("SMS.FTP.PATH");
	   SMS_FILE_DELETE = props.getProperty("SMS_FILE_DELETE");
   }
   
   
    /**
     * <PRE>
     * SMS 전송할 파일을 생성한다 .
     * @param    file_name  생성할 파일(FULL PATH) 
     * @return   status     true(성공) false(실패) 
     * </PRE>
     */
    public  boolean  makeSmsFile(Connection conn, ArrayList<HashMap<Object, Object>> status_array, String  file_name)  throws  Exception {
        
      	 BufferedWriter out        = null;
      	 StringBuffer   message    = null;
      	 HashMap<Object, Object>        hmap       = null;      	
      	
      	 //String         admin_name  = null;
      	 ArrayList<HashMap<Object, Object>>      admin_array = null;
      	 HashMap<Object, Object>        admin_hmap  = null;
      	 CmsAlarmCheckStatus  alarmCheck  = new CmsAlarmCheckStatus();
      	 
      	 try {

	  	      out = new BufferedWriter(new FileWriter(file_name)); //해당 일자 
	  	      	 
	  	      // 상태 결과값  건수 만큼  메세지 생성 
	  	      for (int i=0 ; i < status_array.size() ; i++ ) { 
	  	    	  
	  	    	  hmap = (HashMap<Object, Object>)status_array.get(i);
	  	          
	  	    	  message = new StringBuffer();
	  	    	 //if(Integer.parseInt(String.valueOf(hmap.get("CNT"))) == 2 ) {
	  	    	 // message.append("[금일최종]");
	  	    	 //}
		  	      message.append("CMS,");
		  	      message.append(String.valueOf(hmap.get("EMP_ABG")) );//단축 공장명
		  	      message.append(",");
		  	      message.append(String.valueOf(hmap.get("EQ_NOM")) );//설비명
		  	      message.append(",");
		  	      message.append(String.valueOf(hmap.get("POINT_ABG")) );//포인트 약어 
		  	      message.append(",");
		  	      message.append(String.valueOf(hmap.get("DT_MESURE")) );//측정일자

		  	     if("Velocity".equals(String.valueOf(hmap.get("PARAM_NAME")))) {
		  	      message.append(",속도:");//속도
		  	     } else if("Temperature".equals(String.valueOf(hmap.get("PARAM_NAME")))) { // 온도추가 (2020.11.21)
		  	      message.append(",온도:");//온도
		  	     } else {
		  	      message.append(",가속도:");//가속도
		  	     }

		  	      message.append(CmsUtil.format(Double.valueOf(String.valueOf(hmap.get("CURR_VALUE")))) ); //측정 값 
		  	      message.append(String.valueOf(hmap.get("ID_UNITE")) );// 단위
		  	      message.append(",주의:");
		  	      message.append(CmsUtil.format(Double.valueOf(String.valueOf(hmap.get("SEUILALHAUTMAINT")))) );// 주의값
		  	      message.append(",위험:");
		  	      message.append(CmsUtil.format(Double.valueOf(String.valueOf(hmap.get("SEUILDGHAUTMAINT")))) );// 위험값
	 		  	   
	  	          // 해당 POINT 관리자  확인 		  	       
		  	       admin_array = alarmCheck.getCmsAdminInfo(conn, String.valueOf(hmap.get("IDX_POINT")) );

		  	        // 등록된 관리자  건수 만큼  메세지 생성 
			  	    for (int k=0 ; k < admin_array.size() ; k++ ) { 
			  	    	
			  	    	admin_hmap = (HashMap<Object, Object>)admin_array.get(k);
			  	    	
		  	        	//out.write("IMC1216");//부서코드
		  	        	out.write("CM");
				        out.newLine();
				        out.write( CmsUtil.toKor(props.getProperty("CMS.NAME").trim()) );
				        out.newLine();
				        out.write( props.getProperty("CMS.PHONE").trim() );
				        out.newLine();
				        out.write( String.valueOf(admin_hmap.get("ADMIN_NAME")) );
				        out.newLine();
				        out.write( String.valueOf(admin_hmap.get("ADMIN_PHONE")) );
				        out.newLine();
				        out.write( message.toString() );
				        out.newLine();
				        out.write( CmsUtil.getDateFormat("yyyyMMddHHmmss"));
				        out.newLine();
				        out.write("*");
				        out.newLine();
			  	    }
			  	     
	  	      } // end for
    	  } catch (Exception e) {
    		 isSuccess = false;
    		 throw e;
    	  } finally {
    	     if(out != null) out.close();  	 
    	  }     
    	      
       return  isSuccess;
    		    
    }
	
	
	
    /**
     * <PRE>
     * 등록 파일을 해당  FTP 서버로 송신한다.
     * @param    fileNm     전송할  파일명(FULL PATH)
     * @return   status     true(전송성공) false(전송실패) 
     * </PRE>
     */
    public  boolean  sendSmsFile(String  fileNm)  throws  Exception {

      FTPClient  ftpClient = null;    	
        
       try {
	    	     	   
	    	  ftpClient = new FTPClient();
              ftpClient.setControlEncoding("euc-kr"); // 한글파일명 때문에 디폴트 인코딩을 euc-kr 
              ftpClient.connect(FTP_IP); // SMS Server FTP로 연결 
              int reply = ftpClient.getReplyCode(); // 서버접속 상태 유무

              if (!FTPReply.isPositiveCompletion(reply)) { // 응답코드가 비정상이면 종료 
                    ftpClient.disconnect();
                    throw  new  Exception("FTP 접속 실패!");
              } else {

                    boolean login_State = ftpClient.login(FTP_USER, FTP_PWD); // FTP서버 로그인
                    
                    if (!login_State) {
                         throw  new  Exception("FTP 로그인 실패!");
                    }

                    // 파일
	                if ( fileNm == null) {
	                    throw  new  Exception("전송할 파일  없음!");
	                } 
                    
	                File  file = null;
	                BufferedInputStream  bi = null;
                    boolean      fileResult = true;

	              	file = new File(fileNm);
	              	
	              	if(file.length() > 0 ) {
	                  bi = new BufferedInputStream(new FileInputStream(file.getPath()));
	                  //ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	                  ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
	                
	                  fileResult = ftpClient.storeFile(FTP_PATH + file.getName(), bi);	  
	
	                  System.out.println("[FILE_NAME] " + FTP_PATH + file.getName() );
	                  bi.close();
	                  if( !fileResult )  isSuccess = false;
	              	} else {
	              	  System.out.println("파일 전송 안함  size: " + file.length());
	              	  isSuccess = false;
	              	}
	              	
	                if( (isSuccess && "1".equals(SMS_FILE_DELETE)) || (file.length() == 0 && "1".equals(SMS_FILE_DELETE))  ) { // 1: true	   
	              		file.delete(); // 기존 파일 삭제 
	              	}
              }
        } catch (Exception e) {
           isSuccess =  false;
           System.out.println("FTP Connect Exception!! ");
           //e.printStackTrace();
           throw e;
        } finally {
              try {
                    ftpClient.logout();
                    if (ftpClient != null && ftpClient.isConnected()) {
                          try {
                                ftpClient.disconnect();
                          } catch (Exception ioe) {
                                ioe.printStackTrace();
                          }
                    }
              } catch (Exception e) {
                    e.printStackTrace();
              } 
        }
        
        return  isSuccess;
  }


   
 
}
