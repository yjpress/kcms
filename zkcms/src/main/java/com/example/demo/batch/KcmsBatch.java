package com.example.demo.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KcmsBatch{ 
//implements CommandLineRunner  {

	static Properties props = null;
	static Timer timer = null;
	static RunTask_SMS task_sms = null;
	static RunTask_EAI task_eai = null;
	static ArrayList<HashMap<Object, Object>> status_list = null;
	static String cms_home_path = "C:/KCMS";// "C:/KCMS";
	static boolean isSuccess = true; // 성공 여부 test
	
	//@Override
	public void run3(String... args) throws Exception {
		init(); // firstcommit
		
		int polling_time_sms = (int) (Double.parseDouble(props.getProperty("POLLING.TIME.SMS")) * 60 * 1000); // 밀리 sec
		int polling_time_eai = (int) (Double.parseDouble(props.getProperty("POLLING.TIME.EAI")) * 60 * 1000); // 밀리 sec
		// 단위v

		timer = new Timer();
		task_sms = new RunTask_SMS(); // SMS & MAIL
		task_eai = new RunTask_EAI(); // EAI

		timer.schedule(task_sms, 2000, polling_time_sms); // polling time 설정 SMS
		timer.schedule(task_eai, 2000, polling_time_eai); // polling time 설정 EAI
	}

	static void init() {
		InputStream in = null;

		try {
			props = new Properties();
			File file = new File(cms_home_path + "/config/config.properties"); // config

			if (!file.canRead())
				throw new Exception("Can not open config.properties file ");

			in = new FileInputStream(file);

			props.load(in);

			System.out.println(props.getProperty("JUNGYU"));
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	static class RunTask_SMS extends TimerTask {
//		int b=10;
//		@Override
//		public void run() {
//			System.out.println(props.getProperty("EAI_CFG_FILE")+b);
//			b++;
//		}
//	}
//
//	static class RunTask_EAI extends TimerTask {
//		int a=10;
//		
//		@Override
//		public void run() {
//			System.out.println(props.getProperty("SMS_MAIL_SEND_TIME")+a);
//			a++;
//		}
//
//	}

	// SMS , EMAIL 처리
	static class RunTask_SMS extends TimerTask {

		Connection cms_conn = null;
		String file_name = null;
		String dateTime = null;
		String transaction_code = null;

		CmsAlarmCheckStatus alarmCheck = new CmsAlarmCheckStatus();
		SendSmsFile sms = new SendSmsFile();
		SendMail mail = new SendMail();
		CmsAlarmLog almlog = new CmsAlarmLog();

		String sms_mail_weekend_send = props.getProperty("SMS_MAIL_WEEKEND_SEND"); // sms , email 토.일 주말 보낼지 여부
		String sms_mail_send_time = props.getProperty("SMS_MAIL_SEND_TIME"); // sms , email 보내는 시간 설정 정보

		public void run() {
			// 등록된 DB 갯수 만큼 처리
			for (int cnt = 1; props.getProperty("CMS.DB.URL_" + cnt) != null; cnt++) {

				dateTime = CmsUtil.getDateTime();
				transaction_code = props.getProperty("TRANSACTION_CODE_" + cnt); // TRANSACTION_CODE

				System.out.println("---------- SMS Start : #DB START NO: " + cnt + " , DATE: " + dateTime);

				try {
					cms_conn = CmsUtil.getConnection(props.getProperty("CMS.DB.URL_" + cnt),
							props.getProperty("CMS.DB.USER_" + cnt), props.getProperty("CMS.DB.PWD_" + cnt));
					cms_conn.setAutoCommit(false);

					// ########### 1. 알람 정보 MAIL & SMS 처리 #####################

					// 1-1. CMS 알람 상태 정보 확인
					status_list = alarmCheck.checkCmsStatus(cms_conn);
					System.out.println("ALARME COUNT: " + status_list.size());
					file_name = dateTime + ".sms";

					if (status_list.size() > 0) {
						// 1-2. 상태 결과 를 파일로 생성
						sms.setSysInfo(props);
						sms.makeSmsFile(cms_conn, status_list, cms_home_path + "/send_file/sms/" + file_name);

						// SMS ,MAIL 전송 여부를 체크 한다.
						if (smsEmailSendCheck(sms_mail_weekend_send, sms_mail_send_time)) {

							// 1-3. 생성된 파일을 SMS Server 로 전송 (ftp)
							isSuccess = sms.sendSmsFile(cms_home_path + "/send_file/sms/" + file_name);
							System.out.println("[FTP] file SEND : " + isSuccess);

							// 1-4. Mail 전송
							mail.setSysInfo(props);
							isSuccess = mail.send(cms_conn, status_list);
							System.out.println("[MAIL] SEND : " + isSuccess);
						} else {
							System.out.println("[SMS & MAIL] 사용자 설정  전송 안함.");
						}

						// 1-5. 최종 전송 로그 생성
						isSuccess = almlog.insertAlarmLog(cms_conn, status_list);
						System.out.println("[LOG] INSERT : " + isSuccess);

						cms_conn.commit(); // 전송후 COMMIT (SMS 나 메일은 롤백이 안되기 때문에 무조건 COMMIT
					} // end if

					// ################ MAIL & SMS 끝 ##################

					cms_conn.commit();
					cms_conn.close();

					Thread.sleep(1000); // 완료후 1초후 다음 실행

				} catch (Exception e) {
					try {
						cms_conn.rollback();
						e.printStackTrace();
					} catch (Exception ee) {
					}
				} finally {
					try {
						if (cms_conn != null) {
							cms_conn.close();
						}
					} catch (Exception ee) {
					}
				}

			} // END FOR LOOP

			dateTime = CmsUtil.getDateTime();
			System.out.println("---------- SMS End : " + dateTime);

			// System.out.println(props.getProperty("JUNGYU2"));

		}

		public boolean smsEmailSendCheck(String sms_mail_weekend_send, String sms_mail_send_time) {

			boolean flag = true;
			int sms_mail_send_time_start = Integer.parseInt(sms_mail_send_time.substring(0, 4)); // 시작시간
			int sms_mail_send_time_end = Integer.parseInt(sms_mail_send_time.substring(5, 9)); // 끝 시간

			if ("0".equals(sms_mail_weekend_send)) {
				// 주말에 보내지 않을 경우 체크

				Calendar cal = Calendar.getInstance();
				int week = cal.get(Calendar.DAY_OF_WEEK); // 요일을 구한다. 일 = 1

				if (week == Calendar.SATURDAY || week == Calendar.SUNDAY) { // 토/일 요일
					return false;
				}
			}

			int curr_time = Integer.parseInt(CmsUtil.getDateFormat("HHmm")); // 현재 시.분 확인

			if (curr_time >= sms_mail_send_time_start && curr_time <= sms_mail_send_time_end) {
				flag = true;
			} else {
				flag = false;
			}

			return flag;
		}

	} // end inner class RunTask_SMS

	static class RunTask_EAI extends TimerTask {
		Connection cms_conn = null;
		String file_name = null;
		String dateTime = null;
		String transaction_code = null;

		CmsEaiCheckStatus eaiCheck = new CmsEaiCheckStatus();
		SendEaiFile eaiFile = new SendEaiFile();
		CmsEaiLog eailog = new CmsEaiLog();

		public void run() {

			// 등록된 DB 갯수 만큼 처리
			for (int cnt = 1; props.getProperty("CMS.DB.URL_" + cnt) != null; cnt++) {

				dateTime = CmsUtil.getDateTime();
				transaction_code = props.getProperty("TRANSACTION_CODE_" + cnt); // TRANSACTION_CODE

				System.out.println("--------- EAI Start : #DB START NO: " + cnt + " , DATE: " + dateTime);

				try {
					cms_conn = CmsUtil.getConnection(props.getProperty("CMS.DB.URL_" + cnt),
							props.getProperty("CMS.DB.USER_" + cnt), props.getProperty("CMS.DB.PWD_" + cnt));
					cms_conn.setAutoCommit(false);

					// ########### 2. 시보 및 센서 오류 EAI 전송 처리 #################

					eaiFile.setSysInfo(props);

					// 2-1. EAI 전송을 하기 위한 상태값 변경
					// eailog.setSiboTransData(cms_conn); // 전송할 데이터를 로그 테이블에 적재한다.
					// => EAI_SIBO_LOG 테이블 사용 안하는걸로 변경

					// 2-2. EAI 전송 시보 정보 확인
					status_list = eaiCheck.checkSiboInfo(cms_conn);
					System.out.println("SIBO RESULT COUNT: " + status_list.size());

					if (status_list != null && status_list.size() > 0) {
						// 2-3. 시보 정보 결과 를 EAI 전송 파일로 생성후 전송
						eaiFile.makeSiboFile(status_list, transaction_code);
						// eaiFile.sendEaiFile("SIBO");
					}

					// 2-4. EAI 시보 전송 완료 FLAG
					// eailog.setSiboTransFlag(cms_conn); // 전송 'Y' 완료
					// => EAI_SIBO_LOG 테이블 사용 안하는걸로 변경

					cms_conn.commit(); // 시보 전송후 COMMIT

					// -----------------------------------------------------------//

					// 2-5. EAI 전송 센서 이상 정보 확인
					status_list = eaiCheck.checkSensorErrorInfo(cms_conn);
					System.out.println("SENSOR ERROR RESULT COUNT: " + status_list.size());

					if (status_list != null && status_list.size() > 0) {
						// 2-6. 센서 이상 정보 결과 를 EAI 전송 파일로 생성후 전송
						eaiFile.makeSensorErrorFile(status_list, transaction_code);
						eaiFile.sendEaiFile("SENSOR_ERROR");
					}

					// ########### 시보 및 센서 오류 EAI 전송 처리 끝 #################

					cms_conn.commit();
					cms_conn.close();

					Thread.sleep(1000); // 완료후 1초후 다음 실행

				} catch (Exception e) {
					try {
						cms_conn.rollback();
						e.printStackTrace();
					} catch (Exception ee) {
					}
				} finally {
					try {
						if (cms_conn != null) {
							cms_conn.close();
						}
					} catch (Exception ee) {
					}
				}

			} // END FOR LOOP

			dateTime = CmsUtil.getDateTime();
			System.out.println("--------- EAI End : " + dateTime);

		}

	} // end inner class RunTask_EAI
}
