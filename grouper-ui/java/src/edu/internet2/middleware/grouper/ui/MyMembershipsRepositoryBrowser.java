/*
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

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
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.subject.Subject;


/**
 * Implementation of RepositoryBrowser responsible for 'My' browse mode
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: MyMembershipsRepositoryBrowser.java,v 1.3 2005-12-19 14:22:56 isgwb Exp $
 */

public class MyMembershipsRepositoryBrowser extends AbstractRepositoryBrowser {
	
	public MyMembershipsRepositoryBrowser(){
		prefix = "repository.browser.my.";
		browseMode = "";
	}
	
	/**
	 * In order to have a generic search method, the decision to keep, or
	 * not, a result has been factored out
	 * @param searchResult
	 * @return
	 * @throws Exception
	 */
	protected boolean isValidSearchResult(Group searchResult) throws Exception {
		return searchResult.hasMember(getGrouperSession().getSubject());
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#isValidChild(java.util.Map)
	 */
	protected boolean isValidChild(Map child) throws Exception{
		GrouperSession s = getGrouperSession();
		String name = (String) child.get("name");
		Map validStems = getValidStems();
		return (validStems.get(name)!=null);
				
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#getValidStems()
	 */
	protected Map getValidStems() throws Exception{
		Map validStems = savedValidStems;
		if(validStems !=null) return validStems;
		Set groups = null;
		GrouperSession s = getGrouperSession();
		Member member = MemberFinder.findBySubject(s,s.getSubject());
		groups = member.getGroups();

		return getStems(groups);
	}
}