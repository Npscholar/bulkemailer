package com.lavishlife.tools.bulkemailer.util;
//package com.lavishlife.tools.bulkemailer.util;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.client.ClientHttpResponse;
//import org.springframework.web.client.ResponseErrorHandler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//
//public class MyResponseErrorHandler implements ResponseErrorHandler {
//	
//	private static Logger logger = LoggerFactory.getLogger(MyResponseErrorHandler.class);
//	
//	@Autowired
//	ObjectMapper mapper;
//
//	@Override
//	public boolean hasError(ClientHttpResponse response) throws IOException {
//		HttpStatus.Series series = response.getStatusCode().series();
//		return (HttpStatus.Series.CLIENT_ERROR.equals(series)) || (HttpStatus.Series.SERVER_ERROR.equals(series));
//	}
//
//	@Override
//	public void handleError(ClientHttpResponse response) throws IOException {
//		StringBuilder sb = new StringBuilder();
//		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody()));
//		while(reader.ready()){
//			sb.append(reader.readLine());
//		}
//		logger.error(sb.toString());
//
//	}
//
//}
