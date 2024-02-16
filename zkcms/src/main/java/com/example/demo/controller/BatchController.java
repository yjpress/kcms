package com.example.demo.controller;

//import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.batch.CmsUtil;
//import com.example.demo.batch.KcmsBatch;

//@RestController
public class BatchController {
	//@RequestMapping("/")
    public String hello() {
//		KcmsBatch ba = new KcmsBatch();
//		ba.start();
//		
		String dateTime = CmsUtil.getDateTime();
		
		return "Hello kcms "+dateTime;
    }

}
