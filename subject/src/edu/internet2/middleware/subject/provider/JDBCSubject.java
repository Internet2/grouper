/*--
$Id: JDBCSubject.java,v 1.1 2005-04-29 09:14:11 mnguyen Exp $
$Date: 2005-04-29 09:14:11 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
*/
package edu.internet2.middleware.subject.provider;

import java.util.Set;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;


/**
 * JDBC Subject implementation.
 */
public class JDBCSubject 
	implements Subject {
	
	private static final String NAME_ATTRIBUTE = "name";
	private static final String TYPE_ATTRIBUTE = "type";
	private static final String DESC_ATTRIBUTE = "description";

	private static Log log = LogFactory.getLog(JDBCSubject.class);
	
	private JDBCSourceAdapter adapter;
	
	private String id;
	private String name;
	private SubjectType type = null;
	private Map attributes = null;
	
	/*
	 * Constructor called by SourceManager.
	 */
	protected JDBCSubject(String id, String name, 
			SubjectType type, JDBCSourceAdapter adapter) {
		log.debug("Name = "  + name);
		this.id = id;
		this.name = name;
		this.type = type;
		this.adapter = adapter;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SubjectType getType() {
		return this.type;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return (String)this.attributes.get(DESC_ATTRIBUTE);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAttributeValue(String name) {
		if (attributes == null) {
			this.adapter.loadAttributes(this);
		}
		return (String)this.attributes.get(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set getAttributeValues(String name) {
		if (attributes == null) {
			this.adapter.loadAttributes(this);
		}
		return (Set)this.attributes.get(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map getAttributes() {
		if (attributes == null) {
			this.adapter.loadAttributes(this);
		}
		return attributes;
	}

	protected void setAttributes(Map attributes) {
		this.attributes = attributes;
	}
}
