package fr.cnes.doi.plugin.impl.db.persistence.model;

public class DOIProject {

    private int suffix;

    private String projectname;

    public int getSuffix() {
        return suffix;
    }

    public void setSuffix(int suffix) {
        this.suffix = suffix;
    }

    public String getProjectname() {
        return projectname;
    }

    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }

    public Boolean isEqualTo(DOIProject doiProject) {
        return (this.suffix == doiProject.getSuffix())
                && this.projectname.equals(doiProject.getProjectname());
    }

}
