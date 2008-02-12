package edu.internet2.middleware.grouper.webservices;

import edu.internet2.middleware.subject.provider.JDBCSubject;

/**
 * subject bean for web services
 * 
 * @author mchyzer
 * 
 */
public class WsSubject {

	/**
	 * constructor
	 */
	public WsSubject() {
		// blank
	}

	/**
	 * constructor to convert jdbc subject to a ws subject
	 * 
	 * @param jdbcSubject
	 */
	public WsSubject(JDBCSubject jdbcSubject) {
		this.id = jdbcSubject.getId();
		this.description = jdbcSubject.getDescription();
		this.name = jdbcSubject.getName();
	}

	/** id of subject */
	private String id;

	/** name of subject */
	private String name;

	/** description of subject */
	private String description;

	/**
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @param id1
	 *            the id to set
	 */
	public void setId(String id1) {
		this.id = id1;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name1
	 *            the name to set
	 */
	public void setName(String name1) {
		this.name = name1;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @param description1
	 *            the description to set
	 */
	public void setDescription(String description1) {
		this.description = description1;
	}

}
