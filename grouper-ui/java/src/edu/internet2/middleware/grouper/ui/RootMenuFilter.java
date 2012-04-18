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

import java.util.Map;
/**
 * Implement the logic for GrouperSystem / Wheel group members - based on
 * forAdmin attribute of menuItem:
 * true -> only GrouperSystem or active Wheel group member
 * false -> GrouperSystem and active Wheel group members don't get it
 * Otherwise return true and let other filters perform their checks
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: RootMenuFilter.java,v 1.1 2007-10-30 10:53:06 isgwb Exp $
 */

import javax.servlet.http.HttpServletRequest;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;

public class RootMenuFilter implements MenuFilter {

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.MenuFilter#isValid(edu.internet2.middleware.grouper.GrouperSession, java.util.Map, javax.servlet.http.HttpServletRequest)
	 */
	public boolean isValid(GrouperSession s, Map menuItem,
			HttpServletRequest request) {
		boolean isRoot=s.getSubject().equals(SubjectFinder.findRootSubject())
		|| Boolean.TRUE.equals(request.getSession().getAttribute("activeWheelGroupMember"));
		
		if("true".equals(menuItem.get("forAdmin")) && !isRoot)
				return false;
		if("false".equals(menuItem.get("forAdmin")) && isRoot)
				return false;
		return true;
	}

}
