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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;



/**
 * Implementation of RepositoryBrowser responsible for 'All' browse mode
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: AllRepositoryBrowser.java,v 1.8 2009-04-13 03:18:39 mchyzer Exp $
 */


public class AllRepositoryBrowser extends AbstractRepositoryBrowser{
	
	
	
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


  public AllRepositoryBrowser(){
		prefix = "repository.browser.all.";
		browseMode="All";
	}
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#isValidChild(java.util.Map)
	 */
	protected boolean isValidChild(Map child) {
		
		GrouperSession s = getGrouperSession();
		if("GrouperSystem".equals(s.getSubject().getId())) return true;
		if(Boolean.TRUE.equals(UIThreadLocal.get("isActiveWheelGroupMember"))) return true;
		String personalStem = null;
		String name = (String) child.get("name");
		try {
			personalStem = getMediaBundle().getString("personal.browse.stem");
		}catch(Exception e){}

		if ((personalStem==null || !name.startsWith(personalStem)))
			return true;
	
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
		//The API now does a canView check...
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#getValidStems()
	 */
	protected Map getValidStems() throws Exception{

		return new HashMap();
	}

  /**
   * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#getChildGroups(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  @Override
  public Set<Group> getChildGroups(Stem stem, QueryOptions queryOptions) {
    return stem.getChildGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, queryOptions);
  }

}
