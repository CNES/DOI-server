package fr.cnes.doi.ldap.util;

public class LDAPUser {
  
	private String username;
	
    private String email;
    
    private String fullname;
	
    public String getUsername() {
		return username;
	}
	
    public void setUsername(String username) {
		this.username = username;
	}
	
    public String getEmail() {
		return email;
	}
	
    public void setEmail(String email) {
		this.email = email;
	}
	
    public String getFullname() {
		return fullname;
	}
	
    public void setFullname(String fullname) {
		this.fullname = fullname;
	}
}
