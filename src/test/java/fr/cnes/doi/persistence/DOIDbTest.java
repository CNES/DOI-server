package fr.cnes.doi.persistence;


import java.util.List;

import fr.cnes.doi.persistence.exceptions.DOIDbException;
import fr.cnes.doi.persistence.impl.DOIDataAccessServiceImpl;
import fr.cnes.doi.persistence.model.DOIProject;
import fr.cnes.doi.persistence.model.DOIUser;
import fr.cnes.doi.persistence.service.DOIDataAccessService;

public class DOIDbTest {

	public static void main(String[] args) {
		
		DOIDataAccessService das = new DOIDataAccessServiceImpl();
		try {
			das.addDOIProject(10101, "project");
		    das.addDOIUser("user", false);
		    das.addDOIProjectToUser("user", 10101);
		    List<DOIProject> projects =  das.getAllDOIProjects();
		    List<DOIUser> users = das.getAllDOIusers();
		    das.setAdmin("user");
		    List<DOIUser> users2 = das.getAllDOIusers();
		    das.unsetAdmin("user");
		    das.renameDOIProject(10101, "project2");
		    List<DOIProject> projects2 =  das.getAllDOIProjects();
		    DOIUser doiuser = new DOIUser();
		    doiuser.setAdmin(true);
		    doiuser.setUsername("user");
		    List<DOIProject> projects4 = das.getAllDOIProjectsForUser(doiuser);
		    DOIProject doiproject = new DOIProject();
		    doiproject.setSuffix(10101);
		    doiproject.setProjectname("project2");
		    List<DOIUser> users3 =  das.getAllDOIUsersForProject(doiproject);
		    System.out.println("end !");
		
		} catch (DOIDbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
