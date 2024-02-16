package com.example.demo.batch.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.batch.CmsAlarmCheckStatus;
import com.example.demo.batch.CmsUtil;

@Component
public class KcsmBatchTest implements CommandLineRunner{

		
	static Properties props = null;
	static Timer timer = null;
	static ArrayList<HashMap<Object, Object>> status_list = null;
	static String cms_home_path = "C:/KCMS";// "C:/KCMS";
	static boolean isSuccess = true; // 성공 여부
	
	static RunTask_SMS task_sms = null;
	//static RunTask_EAI task_eai = null;
	
	
	
		@Override
		public void run(String... args) throws Exception {
			InputStream in = null;
			
			
				props = new Properties();
				File file = new File(cms_home_path + "/config/config.properties"); // config   

				if (!file.canRead())
					throw new Exception("Can not open config.properties file ");

				in = new FileInputStream(file);

				props.load(in);

				System.out.println(props.getProperty("JUNGYU"));
				
				in.close();

			
			
			int polling_time_sms = (int) (Double.parseDouble(props.getProperty("POLLING.TIME.SMS")) * 1000); // 밀리 sec
			//int polling_time_eai = (int) (Double.parseDouble(props.getProperty("POLLING.TIME.EAI")) * 60 * 1000); // 밀리 sec
			// 단위v

			timer = new Timer();
			task_sms = new RunTask_SMS(); // SMS & MAIL
			//task_eai = new RunTask_EAI(); // EAI

			timer.schedule(task_sms, 2000, polling_time_sms); // polling time 설정 SMS
			//timer.schedule(task_eai, 2000, polling_time_eai); // polling time 설정 EAI
			
			System.out.println(props.getProperty("JUNGYU3"));
			
			
		}
		
		
		static class RunTask_SMS extends TimerTask {

			String file_name = null;
			String dateTime = null;
			String transaction_code = null;
			
			CmsAlarmCheckStatus alarmCheck = new CmsAlarmCheckStatus();
			
			
			HashMap<Object, Object> hmap  = null;
			
			public void run() {
				// 등록된 DB 갯수 만큼 처리

					dateTime = CmsUtil.getDateTime();
					//transaction_code = props.getProperty("TRANSACTION_CODE_" + cnt); // TRANSACTION_CODE

					System.out.println("---------- SMS Start : #DB START NO: " + "" + " , DATE: " + dateTime);

						file_name = dateTime + ".sms";

						System.out.println("---------- file_name: " + file_name);

						// ################ MAIL & SMS 끝 ##################


						//Thread.sleep(1000); // 완료후 1초후 다음 실행

					
					}

				} // END FOR LOOP
}

