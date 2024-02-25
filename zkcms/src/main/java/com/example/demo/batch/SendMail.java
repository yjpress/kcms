package com.example.demo.batch;

import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.ArrayList;
import java.sql.Connection;
import javax.mail.Message;
//import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
//import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
//import javax.mail.internet.MimeMultipart;
//import javax.mail.Authenticator;
//import javax.mail.PasswordAuthentication; 

/**
 * <PRE>
 * 파일명 : SendMail.java
 * 기능 : MAIL 전송 기능. ....
 * 설명 : CMS 상태정보를   담당자에게 MAIL 전송 한다.
 * 최종수정자 : 정영록
 * 최종수정일자 : 2010-12-01
 * </PRE>
 */

public class SendMail {

	Properties props = null;// Properties
	String MAIL_SMTP_HOST = null;// smtp server
	String MAIL_SMTP_PORT = null;// smtp port
	String MAIL_MIME_TYPE = null;// mime type
	String MAIL_SMTP_USER = null;
	String MAIL_CHARSET = null;
	String MAIL_TRANSPORT_PROTOCOL = null;

	String subject = null; // 제목
	String content = null; // 내용
	String from_email = null;// 보내는 사람 mail
	String from_name = null;// 보내는 사람 이름

	int to_mail_cnt = 0; // 받는사람 수

	boolean isSuccess = false; // 성공 여부

	public void setSysInfo(Properties p) {
		props = p;
		MAIL_SMTP_HOST = props.getProperty("MAIL.SMTP.HOST");
		MAIL_SMTP_PORT = props.getProperty("MAIL.SMTP.PORT");

		MAIL_MIME_TYPE = props.getProperty("MAIL.MIME.TYPE");
		MAIL_SMTP_USER = props.getProperty("MAIL.SMTP.USER");
		MAIL_CHARSET = props.getProperty("MAIL.CHARSET");
		MAIL_TRANSPORT_PROTOCOL = props.getProperty("MAIL.TRANSPORT.PROTOCOL");
	}

	/**
	 * 메일을 전송한다. mail.properties 에서 mail host 의 정보를 가져와서 세션을 통해 보낼 메세지를 작성한다. <br>
	 * 
	 * @throws Exception
	 */
	public boolean send(Connection conn, ArrayList<HashMap<Object, Object>> status_array) throws Exception {

		HashMap<Object, Object> hmap = null;
		StringBuffer content = null;

		try {

			Properties prop = new Properties();

			prop.put("mail.transport.protocol", MAIL_TRANSPORT_PROTOCOL);
			prop.put("mail.smtp.host", MAIL_SMTP_HOST);
			prop.put("mail.smtp.port", MAIL_SMTP_PORT);
			prop.put("mail.smtp.debug", "true");
			Session session = Session.getDefaultInstance(prop, null);

			/*
			 * //이건 TEST G-MAIL prop.put("mail.transport.protocol",
			 * MAIL_TRANSPORT_PROTOCOL); prop.put("mail.smtp.host", MAIL_SMTP_HOST);
			 * prop.put("mail.smtp.port", MAIL_SMTP_PORT);
			 * prop.put("mail.smtp.starttls.enable", "true");
			 * prop.put("mail.smtp.socketFactory.port", "465");
			 * prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			 * prop.put("mail.smtp.socketFactory.fallback", "false");
			 * prop.put("mail.smtp.auth", "true");
			 * 
			 * Authenticator auth = new SMTPAuthenticator(); Session session =
			 * Session.getDefaultInstance(prop, auth);
			 */
			// -----------------------------------

			// create a message
			MimeMessage msg = new MimeMessage(session);

			subject = "CMS 경보 발생 내역 ";
			from_email = props.getProperty("CMS.MAIL"); // 보내는 사람 mail
			from_name = CmsUtil.toKor(props.getProperty("CMS.NAME")); // 보내는 사람 이름

			msg.setFrom(new InternetAddress(from_email, from_name, "euc-kr"));

			// header 에 날짜 삽입
			msg.setSentDate(new Date());

			ArrayList<InternetAddress> toAddrList = null;
			InternetAddress[] toAddress = null; // 받는 당당자 adress

			String admin_name = null;
			ArrayList<HashMap<Object, Object>> admin_array = null;
			HashMap<Object, Object> admin_hmap = null;
			CmsAlarmCheckStatus alarmCheck = new CmsAlarmCheckStatus();

			// 내용 - 상태 결과값 건수 만큼 메일 전송
			for (int i = 0; i < status_array.size(); i++) {

				hmap = (HashMap<Object, Object>) status_array.get(i);
				content = new StringBuffer();
				toAddrList = new ArrayList<>();

				// 해당 POINT 관리자 확인
				admin_array = alarmCheck.getCmsAdminInfo(conn, String.valueOf(hmap.get("IDX_POINT")));

				// 등록된 관리자 건수 만큼 메세지 생성
				for (int k = 0; k < admin_array.size(); k++) {

					admin_hmap = (HashMap<Object, Object>) admin_array.get(k);

					admin_name = String.valueOf(admin_hmap.get("ADMIN_NAME"));
					System.out.println("TO: " + String.valueOf(admin_hmap.get("ADMIN_MAIL")));
					toAddrList.add(
							new InternetAddress(String.valueOf(admin_hmap.get("ADMIN_MAIL")), admin_name, "euc-kr"));
				}

				// 수신자
				if (toAddrList.size() <= 0)
					continue; // 수신자 없을경우 제외

				toAddress = new InternetAddress[toAddrList.size()];
				for (int k = 0; k < toAddrList.size(); k++) {
					toAddress[k] = (InternetAddress) toAddrList.get(k);
				}

				msg.setRecipients(Message.RecipientType.TO, toAddress);

				// 제목
				if (Integer.parseInt(String.valueOf(hmap.get("CNT"))) == 2) {
					// msg.setSubject("[금일최종 송신] " + subject , "euc-kr");
					msg.setSubject(subject, "euc-kr"); // 2021.03.21 "[금일최종 송신]" 삭제
				} else {
					msg.setSubject(subject, "euc-kr");
				}

				content.append("<table width='400' border='0' cellspacing='0' cellpadding='1'>");
				content.append("<tr>");
				content.append("<td align='left'>****CMS 경보발생내역 [");
				content.append(String.valueOf(hmap.get("EMP_NOM")) + "]"); // 공장 명
				content.append("</td>");
				content.append("</tr>");
				content.append("</table>");

				content.append(
						"<table width='400' border='1' cellspacing='0' cellpadding='1' style='border-collapse:collapse' bordercolor='gray'>");
				content.append("<tr>");
				content.append("<td  width='80'> 설비정보</td>");
				content.append("<td>");
				content.append(String.valueOf(hmap.get("EQ_NOM")));
				content.append("-");
				content.append(String.valueOf(hmap.get("POINT_NOM")));
				content.append("</td>");
				content.append("</tr>");

				content.append("<tr>");
				content.append("<td  width='80'> 발생일시</td>");
				content.append("<td>");
				content.append(String.valueOf(hmap.get("DH_MESURE")));
				content.append("</td>");
				content.append("</tr>");

				content.append("<tr>");
				content.append("<td  width='80'> 측정항목</td>");
				content.append("<td> 진동: ");

				if ("Velocity".equals(String.valueOf(hmap.get("PARAM_NAME")))) {
					content.append("속도");
				} else if ("Temperature".equals(String.valueOf(hmap.get("PARAM_NAME")))) { // 온도추가 (2020.11.21)
					content.append("온도");// 온도
				} else {
					content.append("가속도");
				}
				content.append("</td>");
				content.append("</tr>");

				content.append("<tr>");
				content.append("<td  width='80'> 기준치</td>");
				content.append("<td> 주의: ");
				content.append(CmsUtil.format(Double.valueOf(String.valueOf(hmap.get("SEUILALHAUTMAINT")))));
				content.append(String.valueOf(hmap.get("ID_UNITE")));
				content.append(" , 위험: ");
				content.append(CmsUtil.format(Double.valueOf(String.valueOf(hmap.get("SEUILDGHAUTMAINT")))));// 위험값
				content.append(String.valueOf(hmap.get("ID_UNITE")));
				content.append("</td>");
				content.append("</tr>");

				content.append("<tr>");
				content.append("<td  width='80'> 측정치</td>");
				content.append("<td>");
				content.append(CmsUtil.format(Double.valueOf(String.valueOf(hmap.get("CURR_VALUE")))));
				content.append(String.valueOf(hmap.get("ID_UNITE")));
				content.append("</td>");
				content.append("</tr>");

				content.append("<tr>");
				content.append("<td  width='80'> 상승률</td>");
				content.append("<td>");
				content.append(CmsUtil.format(Double.valueOf(String.valueOf(hmap.get("RATIO")))));
				content.append("%</td>");
				content.append("</tr>");

				content.append("</table>");

				msg.setContent(content.toString(), MAIL_MIME_TYPE + ";charset=" + MAIL_CHARSET);

				// send the message
				Transport.send(msg);
				isSuccess = true;
			} // end for

		} catch (SendFailedException e) {
			isSuccess = false;
			e.printStackTrace();
		} catch (Exception e) {
			isSuccess = false;
			e.printStackTrace();
		} finally {
			return isSuccess;
		}
	}

	/**
	 * 구글 MAIL 테스트
	 */
	/*
	 * class SMTPAuthenticator extends Authenticator {
	 * 
	 * public PasswordAuthentication getPasswordAuthentication() { String username =
	 * "dandyhary@gmail.com"; String password = "jyr"; return new
	 * PasswordAuthentication(username, password); } }
	 */

}
