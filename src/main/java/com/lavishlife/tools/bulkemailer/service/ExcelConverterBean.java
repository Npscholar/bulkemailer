package com.lavishlife.tools.bulkemailer.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lavishlife.tools.bulkemailer.model.Contact;
import com.lavishlife.tools.bulkemailer.util.ReaderConstants;

@Component
public class ExcelConverterBean implements Processor{
	private static final Logger logger = LoggerFactory.getLogger(ExcelConverterBean.class);
	private Integer nameIndex = null;
	private Integer emailIndex = null;
	private Integer locationIndex = null;
	private List<com.lavishlife.tools.bulkemailer.model.Contact> newContactsList = new ArrayList<Contact>();

	public List<Contact> read(@Body InputStream in) throws IOException {
		logger.info("Made it in ExcelConverterBean");
		HSSFWorkbook wb;
		try {
			wb = new HSSFWorkbook(in);
			if (wb.getNumberOfSheets() > 1) {
				logger.info("Number of pages greater than 1. Reading only the first");
			}
			HSSFSheet sheet = wb.getSheetAt(0);
			int subsetBeginIndex = 0;
			// Get important column indices for row loops
			HSSFRow firstRow = sheet.getRow(sheet.getFirstRowNum());
			for (int j = 0; j < firstRow.getLastCellNum() - 1; j++) {
				HSSFCell cell = firstRow.getCell(j);
				if (cell.getRichStringCellValue().getString().equalsIgnoreCase(ReaderConstants.NAME_COLUMN)) {
					nameIndex = j;
				} else if (cell.getRichStringCellValue().getString()
						.equalsIgnoreCase(ReaderConstants.LOCATION_COLUMN)) {
					locationIndex = j;
				} else if (cell.getRichStringCellValue().getString().equalsIgnoreCase(ReaderConstants.EMAIL_COLUMN)) {
					emailIndex = j;
				}
			}
			// got index values loop through the rest of rows
			// check if location is empty or complete
			int bodyRows = sheet.getFirstRowNum() + 1;
			int lastRowNum =  sheet.getLastRowNum();
			for (int i = bodyRows;  i <= lastRowNum; i++) {
				HSSFRow row = sheet.getRow(i);
				if (row.getCell(locationIndex) == null) {
					// record row to be start of new subset to be emailed loop
					// should be recursive or reverse loop 
					subsetBeginIndex = i + 1;
					break;
				}
			}
				for (int cnt = subsetBeginIndex; cnt <=lastRowNum; cnt++) {
					HSSFRow newContactRow = sheet.getRow(cnt);
					if (newContactRow.getCell(nameIndex) != null && newContactRow.getCell(emailIndex) != null) {
						Contact contact = new Contact();
						String name =newContactRow.getCell(nameIndex).getRichStringCellValue().toString();
						String email = newContactRow.getCell(emailIndex).getRichStringCellValue().toString();
						contact.setName(name);
						contact.setEmail(email);
						newContactsList.add(contact);
					} else {
						logger.info("Complete New Entries");
						break;
					}
				}

			return newContactsList;
		} catch (IOException e) {
			throw e;
		}
	}

	public void process(Exchange exchange) throws Exception {
		List<Contact> contacts = read (exchange.getIn().getBody(InputStream.class));
		exchange.getOut().setBody(contacts);
		
	}

}
