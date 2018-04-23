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

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.GrouperSession;

/**
 * Interface which allows pluggable business rules for browsing the Grouper
 * repository. Default implementaions for My, Create, Manage, Join and All
 * browse modes are provided. The appropriate RepositoryBrowser implemenation
 * instance is loaded by the RepositoryBrowserFactory. The default
 * implementations indicated above all extend AbstractRepositoryBowser.
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: RepositoryBrowser.java,v 1.9 2009-04-13 03:18:39 mchyzer Exp $
 */

public interface RepositoryBrowser {

	/**
	 * Factory method uses no argument constructor. init passes in the
	 * essentials
	 * @param s
	 * @param navBundle
	 * @param mediaBundle
	 */
	public void init(GrouperSession s, ResourceBundle navBundle,ResourceBundle mediaBundle);

	/**
	 * Given a node return children as appropriate for browse mode
	 * @param node
	 * @param start 0 based start index
	 * @param pageSize
	 * @param totalCount
	 * @param isFlat
	 * @param isForAssignment
	 * @param omitForAssignment
	 * @param context
	 * @param request
	 * @return Set of children for current node
	 * @throws Exception
	 */
	public Set getChildren(String node, String listField,int start, int pageSize,
			StringBuffer totalCount, boolean isFlat, boolean isForAssignment,
			String omitForAssignment,String context,HttpServletRequest request)
			throws Exception;

	/**
	 * Does this browse mode have a flat mode i.e. can it hide the hierarchy?
	 * @return whether user should have option to select an initial stems view
	 */
	public boolean isFlatCapable();
	
	/**
	 * stems or groups - used to create correct screen text
	 * @return flatten type
	 */
	public String getFlattenType();

	/**
	 * Each browse mode can have its own root node. 
	 * @return id of stem
	 */
	public String getRootNode();
	
	/**
	 * Should the nodes before the root node be hidden?
	 * @return whether the nodes before the root node be hidden?
	 */
	public boolean isHidePreRootNode();

	
	/**
	 * Returns the name of the implementation - if there is one
	 * @return the name of the Java class
	 */
	public String getInitialStems();

	
	/**
	 * Returns a list of parent stems as maps taking account of root node
	 * properties
	 * @param groupOrStem
	 * @return List of anvestor stems for specified group or stem
	 * @throws Exception
	 */
	public List getParentStems(GroupOrStem groupOrStem) throws Exception;

	

	/**
	 * Search repository and return results as appropriate for the browse mode
	 * @param s
	 * @param query
	 * @param from
	 * @param attr
	 * @return List of stems or groups. A human readable list of search terms is also returned if a List is provided 
	 * @throws Exception
	 */
	public List search(GrouperSession s, String query, String from, Map attr,List outTerms)
			throws Exception;
	
	/**
	 * Advanced search of repository
	 * @param s
	 * @param from
	 * @param attr
	 * @param outTerms - empty list used to return info for deriving human readable query
	 * @return List of stems or groups. A human readable list of search terms is also returned if a List is provided
	 * @throws Exception
	 */
	public List advancedSearch(GrouperSession s,String from,Map attr,List outTerms)
			throws Exception;

	
}
