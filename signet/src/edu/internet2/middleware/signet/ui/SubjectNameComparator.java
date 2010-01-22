/*--
$Id: SubjectNameComparator.java,v 1.4 2007-05-08 08:40:48 ddonn Exp $
$Date: 2007-05-08 08:40:48 $

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.internet2.middleware.signet.ui;

import java.util.Comparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;

public class SubjectNameComparator implements Comparator {

	protected Log		log;


	/**
	 * Compares Subjects by their names.
	 */
	public SubjectNameComparator() {
		super();
		log = LogFactory.getLog(this.getClass().getName());
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		if ((o1 instanceof SignetSubject) && (o2 instanceof SignetSubject))
		{
			String name1 = ((SignetSubject)o1).getName();
			if (null != name1)
			{
				String name2 = ((SignetSubject)o2).getName();
				if (null != name2)
					return (name1.toLowerCase().compareTo(name2.toLowerCase()));
				else
					log.warn("Invalid/null name in SignetSubject (o2) - " + o2.toString());
			}
			else
				log.warn("Invalid/null name in SignetSubject (o1) - " + o1.toString());
		}
		return 0;
	}

}
