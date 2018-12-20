package fr.cnes.doi.persistence;

import java.util.List;
import fr.cnes.doi.persistence.exceptions.DOIDbException;
import fr.cnes.doi.persistence.impl.DOIDbDataAccessServiceImpl;
import fr.cnes.doi.persistence.model.DOIProject;
import fr.cnes.doi.persistence.model.DOIUser;
import fr.cnes.doi.persistence.service.DOIDbDataAccessService;

public class DOIDbTest {

	public static void main(String[] args) {
		
		DOIDbDataAccessService das = new DOIDbDataAccessServiceImpl();
		try {
			das.addDOIProject(10101, "project");
			String projectname = das.getDOIProjectName(10101);
		    das.addDOIUser("user", false);
		    das.addDOIUser("user2", false, "toto@gmai.com");
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
		    List<DOIProject> projects4 = das.getAllDOIProjectsForUser(doiuser.getUsername());
		    DOIProject doiproject = new DOIProject();
		    doiproject.setSuffix(10101);
		    doiproject.setProjectname("project2");
		    List<DOIUser> users3 =  das.getAllDOIUsersForProject(doiproject.getSuffix());
			das.addToken("token1");
			das.addToken("token2");
			List<String> tokens = das.getTokens();
			das.deleteToken("lkolko");
			das.deleteToken("lko");
			System.out.println("end !");
		
		} catch (DOIDbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
