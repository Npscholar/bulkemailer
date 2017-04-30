package com.lavishlife.tools.bulkemailer.model;

import lombok.Data;

@Data
public class Contact {

	private String name;
	private String email;
	
	@Override
	public String toString(){
		return name +":"+ email;
	}
}
