/*
Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2006 The University Of Bristol

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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.subject.Subject;


/**
 * Implementation of RepositoryBrowser responsible for 'Manage' browse mode
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: ManageRepositoryBrowser.java,v 1.4 2006-07-14 11:04:11 isgwb Exp $
 */



public class ManageRepositoryBrowser extends AbstractRepositoryBrowser{
	
	

	public ManageRepositoryBrowser(){
		prefix = "repository.browser.manage.";
		browseMode="Manage";
	}
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#isValidChild(java.util.Map)
	 */
	protected boolean isValidChild(Map child) throws Exception{ 
		GrouperSession s = getGrouperSession();
		Map validStems = getValidStems();
		String name = (String) child.get("name");
		if(validStems.containsKey(name)) return true;
		return false;
	}
	
	/**
	 * In order to have a generic search method, the decision to keep, or
	 * not, a result has been factored out
	 * @param searchResult
	 * @return getGrouperSession().getSubject()
	 * @throws Exception
	 */
	protected boolean isValidSearchResult(Group searchResult) throws Exception {
		Subject subj = getGrouperSession().getSubject();
		return (searchResult.hasAdmin(subj)|| searchResult.hasUpdate(subj));
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#getValidStems()
	 */
	protected Map getValidStems() throws Exception{
		Map validStems = savedValidStems;
		if(validStems !=null) return validStems;
		List groups = new ArrayList();
		GrouperSession s = getGrouperSession();
		Member member = MemberFinder.findBySubject(s,s.getSubject());
		groups.addAll(member.hasAdmin());
		groups.addAll(member.hasUpdate());
		groups.addAll(member.hasStem());
		groups.addAll(member.hasCreate());
		
		validStems= getStems(groups);
		return validStems;
	}
}
