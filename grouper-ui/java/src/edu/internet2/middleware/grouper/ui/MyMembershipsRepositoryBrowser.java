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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;


/**
 * Implementation of RepositoryBrowser responsible for 'My' browse mode
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: MyMembershipsRepositoryBrowser.java,v 1.9 2009-04-13 03:18:39 mchyzer Exp $
 */

public class MyMembershipsRepositoryBrowser extends AbstractRepositoryBrowser {
	private Map<Group, Object> groups=null;
	public MyMembershipsRepositoryBrowser(){
		prefix = "repository.browser.my.";
		browseMode = "";
	}
	
	/**
	 * 
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#pagedQuery()
	 */
	@Override
  protected boolean pagedQuery() {
    return true;
  }

	/**
	 * 
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#sortedQuery()
	 */
  @Override
  protected boolean sortedQuery() {
    return true;
  }

  /**
	 * 
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#getChildGroups(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
	 */
	@Override
  public Set<Group> getChildGroups(Stem stem, QueryOptions queryOptions) {
    return stem.getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, queryOptions);
	}
	
	/**
	 * In order to have a generic search method, the decision to keep, or
	 * not, a result has been factored out
	 * @param searchResult
	 * @return
	 * @throws Exception
	 */
	protected boolean isValidSearchResult(Group searchResult) throws Exception {
		
		return getGroups().containsKey(searchResult);
		//return searchResult.hasMember(getGrouperSession().getSubject());
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
	  if (validStems != null)
	    return validStems;

	  Subject curSubj = getGrouperSession().getSubject();
	  Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), curSubj, true);
	  Set groups = member.getGroups();

	  return getStems(groups);
	}
	
	protected Map getGroups() throws Exception{
		if(groups!=null) return groups;
		groups=new HashMap<Group, Object>();
		GrouperSession s = getGrouperSession();
		Member member = MemberFinder.findBySubject(s,s.getSubject(), true);
		Set<Group> groupsSet = member.getGroups();
		for(Group g : groupsSet) {
			groups.put(g,"");
		}
		return groups;
	}
}
