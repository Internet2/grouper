
package edu.internet2.middleware.subject;

import java.io.Serializable;

/**
 * A SubjectType may be a person, group, or organization.
 * 
 */
public abstract class SubjectType implements Serializable {

	/**
	 * Returns the name of this SubjectType.
	 * @return name
	 *
	 */
	public abstract String getName();

}
