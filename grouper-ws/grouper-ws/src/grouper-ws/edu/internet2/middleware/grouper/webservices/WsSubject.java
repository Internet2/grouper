package edu.internet2.middleware.grouper.webservices;

import edu.internet2.middleware.subject.provider.JDBCSubject;

/**
 * subject bean for web services
 * @author mchyzer
 *
 */
public class WsSubject {
	
	public WsSubject(){}
	
	public WsSubject(JDBCSubject jdbcSubject) {
		this.id = jdbcSubject.getId();
		this.description = jdbcSubject.getDescription();
		this.name = jdbcSubject.getName();
	}
	
	private String id;
	private String name;
	private String description;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	
}
