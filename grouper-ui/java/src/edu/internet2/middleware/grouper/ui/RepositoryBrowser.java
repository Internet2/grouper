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

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
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
 * @version $Id: RepositoryBrowser.java,v 1.3 2006-01-03 13:29:09 isgwb Exp $
 */

public interface RepositoryBrowser {

	/**
	 * Factory method uses no argument constructor. init passes in the
	 * essentials
	 * @param s
	 * @param mediaBundle
	 */
	public void init(GrouperSession s, ResourceBundle mediaBundle);

	/**
	 * Given a node return children as appropriate for browse mode
	 * @param node
	 * @param start
	 * @param pageSize
	 * @param totalCount
	 * @param isFlat
	 * @param isForAssignment
	 * @return
	 * @throws Exception
	 */
	public Set getChildren(String node, int start, int pageSize,
			StringBuffer totalCount, boolean isFlat, boolean isForAssignment)
			throws Exception;

	/**
	 * Does this browse mode have a flat mode i.e. can it hide the hierarchy?
	 * @return
	 */
	public boolean isFlatCapable();
	
	/**
	 * stems or groups - used to create correct screen text
	 * @return
	 */
	public String getFlattenType();

	/**
	 * Each browse mode can have its own root node. 
	 * @return
	 */
	public String getRootNode();
	
	/**
	 * Should the nodes before the root node be hidden?
	 * @return
	 */
	public boolean isHidePreRootNode();

	
	/**
	 * Returns the name of the implementation - if there is one
	 * @return
	 */
	public String getInitialStems();

	
	/**
	 * Returns a list of parent stems as maps taking account of root node
	 * properties
	 * @param groupOrStem
	 * @return
	 * @throws Exception
	 */
	public List getParentStems(GroupOrStem groupOrStem) throws Exception;

	

	/**
	 * Search repository and return results as appropriate for the browse mode
	 * @param s
	 * @param query
	 * @param from
	 * @param attr
	 * @return
	 * @throws Exception
	 */
	public List search(GrouperSession s, String query, String from, Map attr)
			throws Exception;

	
}