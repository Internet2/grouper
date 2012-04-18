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
/*
Copyright 2004-2007 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2007 The University Of Bristol

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
package edu.internet2.middleware.grouper.ui;

import java.util.ResourceBundle;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.subject.Subject;

/**
 * Factory for obtaining configured UIGroupPrivilegeResolver
 * 
 * see https://bugs.internet2.edu/jira/browse/GRP-72
 * 
 * @author Gary Brown.
 * @version $Id: UIGroupPrivilegeResolverFactory.java,v 1.2 2008-04-17 18:59:46 isgwb Exp $
 */

public class UIGroupPrivilegeResolverFactory {
	/**
	 * Returns the configured UIGroupPrivilegeResolver - derived from the key:
	 * edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver in the bundle - 
	 * typically from media.properties
	 * @param s
	 * @param bundle
	 * @param g
	 * @param subj
	 * @return configured instance
	 */
	public static UIGroupPrivilegeResolver getInstance(GrouperSession s,ResourceBundle bundle,Group g,Subject subj) {
		String resolverName = null;
		try {
			resolverName = bundle
					.getString("edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver");
		} catch (Exception e) {
			resolverName = "edu.internet2.middleware.grouper.ui.DefaultUIGroupPrivilegeResolver";
		}
		
		Class resolverClass = null;
		UIGroupPrivilegeResolver resolver = null;
		try {
			resolverClass = Class.forName(resolverName);
			resolver = (UIGroupPrivilegeResolver) resolverClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(
					"No class found for UIGroupPrivilegeResolver - " + resolverName);

		} catch (InstantiationException e) {
			throw new IllegalArgumentException(
					"Cannot instantiate UIGroupPrivilegeResolver - " + resolverName);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(resolverName
					+ " does not implement UIGroupPrivilegeResolver");
		} catch (Exception e) {
			throw new IllegalArgumentException("Problem loading "
					+ resolverName + ":" + e.getMessage());
		}
		resolver.setGroup(g);
		resolver.setGrouperSession(s);
		resolver.setSubject(subj);
		resolver.init();
		return resolver;
		
	}
}
