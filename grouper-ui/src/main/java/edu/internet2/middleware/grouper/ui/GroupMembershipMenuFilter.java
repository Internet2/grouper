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

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.GrouperSession;

/**
 * Allow display of menu items according to whether the current Subject
 * is, or is not, a member of a group. This class depends on an instance of 
 * UiPermissions. 
@see edu.internet2.middleware.grouper.ui.UiPermissions
 * for how to configure UiPermissions.
 * 
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: GroupMembershipMenuFilter.java,v 1.1 2007-10-30 10:53:06 isgwb Exp $
 */
public class GroupMembershipMenuFilter implements MenuFilter {


	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.MenuFilter#isValid(edu.internet2.middleware.grouper.GrouperSession, java.util.Map, javax.servlet.http.HttpServletRequest)
	 */
	public boolean isValid(GrouperSession s, Map menuItem,
			HttpServletRequest request) {
		
		UiPermissions uip = SessionInitialiser.getUiPermissions(request.getSession());
		if(uip==null) return false;
		return uip.can(s.getSubject(), (String)menuItem.get("functionalArea"), "view");
	}

}
