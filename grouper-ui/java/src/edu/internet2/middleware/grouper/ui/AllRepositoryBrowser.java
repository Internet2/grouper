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

import java.util.HashMap;
import java.util.Map;


import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.subject.Subject;



/**
 * Implementation of RepositoryBrowser responsible for 'All' browse mode
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: AllRepositoryBrowser.java,v 1.5 2006-07-14 11:04:11 isgwb Exp $
 */


public class AllRepositoryBrowser extends AbstractRepositoryBrowser{
	
	
	
	
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
		Subject subj = getGrouperSession().getSubject();
		return (searchResult.hasView(subj)|| 
				searchResult.hasRead(subj)||
				searchResult.hasUpdate(subj)||
				searchResult.hasAdmin(subj)||
				searchResult.hasOptin(subj)||
				searchResult.hasOptout(subj));
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#getValidStems()
	 */
	protected Map getValidStems() throws Exception{

		return new HashMap();
	}
}
