package fr.cnes.doi.server.monitoring;

/**
 * Monitoring record containing:
 * <ul>
 * <li>Description of the record</li>
 * <li>Average</li>
 * <li>Nb total of recorded values (to compute the average)</li>
 * </ul>
 * 
 * @author Claire
 *
 */
public class DoiMonitoringRecord {
	
	/** Description (name) of the service to record **/
	private String description;
	
	/** Current average time to access this service **/
	private float average = 0.0f;
	
	/** Nb of values to compute the average **/
	private int nbAccess = 0;
	
	

	/**
	 * Constructor
	 * 
	 * @param description
	 * @param average
	 * @param nbAccess
	 */
	public DoiMonitoringRecord(String description, float average, int nbAccess) {
		super();
		this.description = description;
		this.average = average;
		this.nbAccess = nbAccess;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the average
	 */
	public float getAverage() {
		return average;
	}

	/**
	 * @param average the average to set
	 */
	public void setAverage(float average) {
		this.average = average;
	}

	/**
	 * @return the nbAccess
	 */
	public int getNbAccess() {
		return nbAccess;
	}

	/**
	 * @param nbAccess the nbAccess to set
	 */
	public void setNbAccess(int nbAccess) {
		this.nbAccess = nbAccess;
	}
	
	

}
