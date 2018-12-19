package fr.cnes.doi.persistence.model;

import java.util.List;

public class DOIUser {
   
	private String username;
	
	private Boolean admin;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}
	
	
}
