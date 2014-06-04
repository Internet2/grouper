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
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Implementation of RepositoryBrowser responsible for 'Join' browse mode
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: JoinRepositoryBrowser.java,v 1.7 2009-04-13 03:18:39 mchyzer Exp $
 */

public class JoinRepositoryBrowser extends AbstractRepositoryBrowser{
	
	
	

	public JoinRepositoryBrowser(){
		prefix = "repository.browser.join.";
		browseMode="Join";
	}
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#isValidChild(java.util.Map)
	 */
	protected boolean isValidChild(Map child) throws Exception{

	   //see if group, if so, then ok, this was filtered by the query
    if (ObjectUtils.equals(child.get("isGroup"), Boolean.TRUE)) {
      return true;
    }

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
		return (searchResult.hasOptin(subj));
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#getValidStems()
	 */
	protected Map getValidStems() throws Exception{
		Map validStems = savedValidStems;
		if(validStems !=null) return validStems;
		Set groups = null;
		GrouperSession s = getGrouperSession();
		Member member = MemberFinder.findBySubject(s,s.getSubject(), true);
		groups = member.hasOptinInStem();
				
		validStems= getStems(groups);
		return validStems;
	}

  @Override
  public Set<Group> getChildGroups(Stem stem, QueryOptions queryOptions) {
    return stem.getChildGroups(Scope.ONE, GrouperUtil.toSet(AccessPrivilege.OPTIN), queryOptions);
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

}
