package com.lavishlife.tools.bulkemailer.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.lavishlife.tools.bulkemailer.model.Contact;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Component
public class EmailContactProcessor implements Processor {

	private static Logger logger = LoggerFactory.getLogger(EmailContactProcessor.class);
	
	@Autowired
	Gmail gmail;
	
	@Autowired
	Configuration freeMarkerConfiguration;
	
	@Autowired 
	Environment env;

	private static final String ORIGIN_EMAIL_ADDRESS = "bulkemailer.email";
	private static final String DEFAULT_EMAIL_SUBJECT = "bulkemailer.subject";

	@SuppressWarnings("unchecked")

	public void process(Exchange exchange) throws Exception {
		List<Contact> contacts = exchange.getIn().getBody(List.class);
		StringBuilder sb = new StringBuilder();
		for (Contact contact : contacts) {
			sb.append(contact);
			 createEmail(contact);
		}
		exchange.getOut().setBody(sb);
	}

	public void createEmail(final Contact contact) throws IOException {
      
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("Contact", contact);
		model.put("timestamp", System.currentTimeMillis());
		Template template = freeMarkerConfiguration.getTemplate("GreetingEmail.ftl");
		String body = null;
		try {
			 body = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
		} catch (TemplateException e1) {
			e1.printStackTrace();
		}
		String subject = env.getProperty(DEFAULT_EMAIL_SUBJECT);
		subject = StringUtils.replace(subject, "%name%", contact.getName());
		String to = "reimaginerei@gmail.com";
		String from = /*"npscholar@gmail.com";*/ env.getProperty(ORIGIN_EMAIL_ADDRESS , "ReImagineRealEstateInvesting@gmail.com");
		String host = "localhost";
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", host);
		Session session = Session.getDefaultInstance(properties);

		// Create a default MimeMessage object.
		MimeMessage mime = new MimeMessage(session);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			// Set From: header field of the header.
			mime.setFrom(new InternetAddress(from));
			// Set To: header field of the header.
			mime.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
			// Set Subject: header field
			mime.setSubject(subject);
			// Now set the actual message
			mime.setText(body);
			logger.debug("Initial MIME content-type: {}",mime.getContentType());
			//mime content type is set to text/plain by default 
			mime.setHeader(HttpHeaders.CONTENT_TYPE, "text/html");
			logger.debug("Modified MIME content-type: {}", mime.getContentType());
			mime.writeTo(baos);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

//		Sending email
		String encodedEmail = Base64.getUrlEncoder().encodeToString(baos.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		try {
			// Using default userid me since we already permission via the oauth token
			message = gmail.users().messages().send("me", message).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.debug("Response: {}", message.toPrettyString());
	}

}
