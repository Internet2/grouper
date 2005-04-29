/*--
$Id: SubjectTypeEnum.java,v 1.1 2005-04-29 09:14:11 mnguyen Exp $
$Date: 2005-04-29 09:14:11 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
*/
package edu.internet2.middleware.subject.provider;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import edu.internet2.middleware.subject.SubjectType;

/**
 * SubjectType enum for person, group, and organization.
 * 
 */
public class SubjectTypeEnum extends SubjectType {

	protected static final List PRIVATE_VALUES = new ArrayList();
	public static final List VALUES = Collections.unmodifiableList(PRIVATE_VALUES);
	
	public static final SubjectTypeEnum PERSON = new SubjectTypeEnum("person");
	public static final SubjectTypeEnum GROUP = new SubjectTypeEnum("group");
	public static final SubjectTypeEnum ORG = new SubjectTypeEnum("organization");
	
	static {
		PRIVATE_VALUES.add(PERSON);
		PRIVATE_VALUES.add(GROUP);
		PRIVATE_VALUES.add(ORG);
	}
	
	/**
     * The name of this enum constant, as declared in the enum declaration.
     */
    private final String name;
    
    /**
     * (non-Javadoc)
     * Sole constructor.
     * @param name - 
     *    The name of this enum constant, which is the identifier used
     *    to declare it.
     */
    protected SubjectTypeEnum(String name) {
        this.name = name;
    }
    
	/**
	 * (non-Javadoc)
	 * @see edu.internet2.middleware.subject.SubjectType#getName()
	 */
	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.getName();
	}
	
	/**
	 * Factory method for returning instance of datatype from the
	 * pool of valid objects.
	 */
	public static SubjectTypeEnum valueOf(String value) {
		if (value == null) {
			return null;
		}
		for (Iterator i = PRIVATE_VALUES.iterator(); i.hasNext();) {
			SubjectTypeEnum validValue = (SubjectTypeEnum)i.next();
			if (value.equalsIgnoreCase(validValue.getName())) {
				return validValue;
			}
		}

		throw new IllegalArgumentException("Unrecognized SubjectType '"
                + value + "', expecting one of " + PRIVATE_VALUES);
    }

}
