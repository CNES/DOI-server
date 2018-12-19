package fr.cnes.doi.persistence.exceptions;

import java.sql.SQLException;

public class DOIDbException extends Exception {

	public DOIDbException(String string, SQLException e) {
		super(string, e);
	}
	
}
