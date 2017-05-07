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
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.Message;
import com.lavishlife.tools.bulkemailer.model.Contact;

@Component
public class EmailContactProcessor implements Processor {

	@Autowired
	private VelocityEngine velocityEngine;
	
	@Autowired
	Gmail gmail;

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

//	Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		email.writeTo(baos);
//		String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());
//		Message message = new Message();
//		message.setRaw(encodedEmail);
//		return message;
//	}

	public void createEmail(final Contact contact) throws IOException {

        // Print the labels in the user's account.
        String user = "me";
        ListLabelsResponse listResponse =
            gmail.users().labels().list(user).execute();
        List<Label> labels = listResponse.getLabels();
        if (labels.size() == 0) {
            System.out.println("No labels found.");
        } else {
            System.out.println("Labels:");
            for (Label label : labels) {
                System.out.printf("- %s\n", label.getName());
            }
        }
        
        
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("user.name", contact.getName());
		model.put("user.email", contact.getEmail());
		String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "src/main/resources/email.vm", model);
		

		String to = "jonbon1992@aol.com";
		String from = "npscholar@gmail.com";
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
			mime.setSubject("I think this message is going to be bland");
			// Now set the actual message
			mime.setText("I told you... "+ text);
			mime.writeTo(baos);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

//		Sending email
		String encodedEmail = Base64.getUrlEncoder().encodeToString(baos.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		try {
			message = gmail.users().messages().send("me", message).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Response: " +message.toPrettyString());

	}

}
