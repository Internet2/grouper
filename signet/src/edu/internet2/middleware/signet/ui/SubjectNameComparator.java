/*--
$Id: SubjectNameComparator.java,v 1.1 2005-06-07 01:40:48 mnguyen Exp $
$Date: 2005-06-07 01:40:48 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.util.Comparator;
import edu.internet2.middleware.signet.PrivilegedSubject;

public class SubjectNameComparator implements Comparator {

	/**
	 * Compares Subjects by their names.
	 */
	public SubjectNameComparator() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		if (o1 instanceof PrivilegedSubject
			&& o2 instanceof PrivilegedSubject) {
			String name1 = ((PrivilegedSubject)o1).getName().toLowerCase();
			String name2 = ((PrivilegedSubject)o2).getName().toLowerCase();
			return name1.compareTo(name2);
		}
		return 0;
	}

}
