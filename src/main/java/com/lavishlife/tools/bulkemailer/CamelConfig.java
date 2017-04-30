package com.lavishlife.tools.bulkemailer;

import java.io.InputStream;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		System.out.println("Completed reading resource....");
		from("file:/Users/npscholar/Desktop?fileName=input-customer3.xls&move=.done").convertBodyTo(InputStream.class)
				.process("excelConverterBean").process("emailContactProcessor")
				.log("received message[${exchange}, [${body}]]").end();
		
	}

}
