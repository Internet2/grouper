/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/* ========================================================================
 * Copyright (c) 2009-2011 The University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */

/*
 * Simple subject comparator
 * @author fox
 */

package edu.internet2.middleware.subject.provider;

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Subject;

public class LdapComparator implements Comparator<Subject> {

	private static Log log = LogFactory.getLog(LdapComparator.class);

	public LdapComparator() {
		super();
	}

	/**
	 * Compares two subjects.
	 * 
	 * @param so0
	 *            first subject
	 * @param so1
	 *            second subject
	 */
	public int compare(Subject so0, Subject so1) {

		try {

			Subject s0 = (Subject) so0;
			Subject s1 = (Subject) so1;
			String s0d = s0.getDescription();
			String s1d = s1.getDescription();

			// log.debug("comparing " + s0d + " to " + s1d);
			return s0d.compareTo(s1d);
		} catch (Exception e) {
			log.debug("exception " + e);
		}
		return (1);
	}
}
