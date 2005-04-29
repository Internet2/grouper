/*--
$Id: BaseSourceAdapter.java,v 1.1 2005-04-29 09:14:11 mnguyen Exp $
$Date: 2005-04-29 09:14:11 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
*/
package edu.internet2.middleware.subject.provider;

import java.util.Set;
import java.util.HashSet;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;


/**
 * Base Source adapter.
 */
public abstract class BaseSourceAdapter
	implements Source {

	private static Log log = LogFactory.getLog(BaseSourceAdapter.class);
	
	protected String id = null;
	protected String name = null;
	protected Set types = new HashSet();
	protected Properties params = new Properties();
	
	/**
	 * Default constructor.
	 */
	public BaseSourceAdapter() {}

	/**
	 * Allocates adapter with ID and name.
	 * @param id
	 * @param name
	 */
	public BaseSourceAdapter(String id, String name){
		this.id = id;
		this.name = name;
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
	public void setId(String id) {
		this.id = id;
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
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set getSubjectTypes() {
		return types;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSubjectType(String type) {
		this.types.add(type);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setSubjectTypes(Set types) {
		this.types = types;
	}

	/**
	 * (non-javadoc)
	 * @param name
	 * @param value
	 */
	public void addInitParam(String name, String value) {
		this.params.setProperty(name, value);
	}

	/**
	 * (non-javadoc)
	 * @param name
	 * @return param
	 */
	public String getInitParam(String name) {
		return this.params.getProperty(name);
	}
	
	/**
	 * (non-javadoc)
	 * @return params
	 */
	public Properties getInitParams() {
		return this.params;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public abstract Subject getSubject(String id)
		throws SubjectNotFoundException;

	/**
	 * {@inheritDoc}
	 */
	public abstract Set search(String searchValue);

	/**
	 * {@inheritDoc}
	 */
	public abstract Set searchByIdentifier(String id);

	/**
	 * {@inheritDoc}
	 */
	public abstract Set searchByIdentifier(String id, SubjectType type);
	
	/**
	 * {@inheritDoc}
	 */
	public abstract void init()
		throws SourceUnavailableException;

	/**
	 * {@inheritDoc}
	 */
	public abstract void destroy();

}
