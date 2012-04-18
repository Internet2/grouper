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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;



/**
 * Implementation of RepositoryBrowser responsible for 'Create' browse mode
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: CreateRepositoryBrowser.java,v 1.8 2009-04-13 03:18:39 mchyzer Exp $
 */


public class CreateRepositoryBrowser extends AbstractRepositoryBrowser{
	
	
	
	public CreateRepositoryBrowser(){
		prefix = "repository.browser.create.";
		browseMode="Create";
	}
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#isValidChild(java.util.Map)
	 */
	protected boolean isValidChild(Map child) throws Exception{
		GrouperSession s = getGrouperSession();
		Map validStems = getValidStems();
		String id = (String)child.get("id");
		GroupOrStem groupOrStem = GroupOrStem.findByID(s,id);
		String name = null;
		try {
			if(groupOrStem.isGroup()) {
				
				name = groupOrStem.getGroup().getParentStem().getName();
				
			}else {
				name = groupOrStem.getStem().getName();
			}
		}catch(Exception e) {throw new RuntimeException(e);}
		return (validStems.get(name)!=null);
		
	}
	
	/**
	 * In order to have a generic search method, the decision to keep, or
	 * not, a result has been factored out
	 * @param searchResult
	 * @return getGrouperSession().getSubject()
	 * @throws Exception
	 */
	protected boolean isValidSearchResult(Stem searchResult) throws Exception {
		Subject subj = getGrouperSession().getSubject();
		return (searchResult.hasStem(subj)|| searchResult.hasCreate(subj));
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.AbstractRepositoryBrowser#getValidStems()
	 */
	protected Map getValidStems() throws Exception{
		Map validStems = savedValidStems;
		if(validStems !=null) return validStems;
		List stems = new ArrayList();
		GrouperSession s = getGrouperSession();
		Member member = MemberFinder.findBySubject(s,s.getSubject(), true);
		stems.addAll(member.hasStem());
		stems.addAll(member.hasCreate());
		
		
		validStems= getStems(stems);
		return validStems;
	}
}
