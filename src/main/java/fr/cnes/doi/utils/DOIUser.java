package fr.cnes.doi.utils;

public class DOIUser {

    private String username;

    private Boolean admin;

    private String email;

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

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    // Compare two DOIUser objects
    public Boolean isEqualTo(DOIUser testuser) {
        return this.username.equals(testuser.getUsername())
                && this.admin.equals(testuser.getAdmin())
                && this.email.equals(testuser.getEmail());
    }
}
