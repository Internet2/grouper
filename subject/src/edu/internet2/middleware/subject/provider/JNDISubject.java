/*--
$Id: JNDISubject.java,v 1.2 2006-06-07 19:00:43 esluss Exp $
$Date: 2006-06-07 19:00:43 $

Copyright 2005 Internet2.  All Rights Reserved.
See doc/license.txt in this distribution.
*/
/*
 * JNDISubject.java
 *
 * Created on March 6, 2006
 *
 * Author Ellen Sluss
 */
package edu.internet2.middleware.subject.provider;

import java.util.Set;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;



/**
 * JNDI Subject implementation.
 */
public class JNDISubject 
	implements Subject {
	

	private static Log log = LogFactory.getLog(JNDISubject.class);
	
	private JNDISourceAdapter adapter;
	
	private String id;
	private String name;
        private String description = null;
	private SubjectType type = null;
	private Map attributes = null;
	
	/*
	 * Constructor called by SourceManager.
	 */
	protected JNDISubject(String id, String name, String description,
			SubjectType type, JNDISourceAdapter adapter) {
		log.debug("Name = "  + name);
		this.id = id;
		this.name = name;
		this.type = type;
                this.description = description;
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
		return this.description;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAttributeValue(String name) {
		if (attributes == null) {
			this.adapter.loadAttributes(this);
		}
		Set values = (Set)this.attributes.get(name);
		if (values != null) {
			return ((String[])values.toArray(new String[0]))[0];
		}
		else {
			return null;
		}
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

	/**
	 * {@inheritDoc}
	 */
	public Source getSource() {
		return this.adapter;
	}
	
	protected void setAttributes(Map attributes) {
		this.attributes = attributes;
	}
}
