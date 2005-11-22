/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Bristol
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Bristol nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Bristol, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Bristol, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Grouper;
import edu.internet2.middleware.grouper.GrouperAccess;
import edu.internet2.middleware.grouper.GrouperGroup;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperList;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperStem;
import edu.internet2.middleware.subject.Subject;

/**
 * Partial implementation of RepositoryBrowser used as a superclass
 * for actual implementations so code can be shared where appropriate. Reads 
 * properties from media.properties:
 * repository.browser.create.class=edu.internet2.middleware.grouper.ui.CreateRepositoryBrowser
 * repository.browser.create.flat-capable=true
 * repository.browser.create.root-node=
 * repository.browser.create.hide-pre-root-node=true
 * repository.browser.create.flat-privs=CREATE STEM
 * repository.browser.create.flat-type=stem
 * <table width="100%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td><font face="Arial, Helvetica, sans-serif">Property</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Value</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Description</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif"> class</font></td>
    <td><font face="Arial, Helvetica, sans-serif">edu.internet2.middleware.grouper.ui.CreateRepositoryBrowser</font></td>
    <td><font face="Arial, Helvetica, sans-serif">RepositoryBrowserFactory.getInstance(&quot;Create&quot;) 
      looks up this key and returns an instance of the class specified by the 
      value</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">flat-capable</font></td>
    <td><font face="Arial, Helvetica, sans-serif">true</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates that the hierarchy 
      can be hidden and a list of stems or groups (according to flat-type) shown</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">flat-type</font></td>
    <td><font face="Arial, Helvetica, sans-serif">stem</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Currently, all other browse 
      modes have a flat-type=group</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">flat-privs</font></td>
    <td><font face="Arial, Helvetica, sans-serif">CREATE STEM</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The current subject must have 
      one of these privs before a stem or group is listed</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">root-node</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Name of a stem where browsing 
      starts. Defaults to Grouper.NS_ROOT, but could be at any level in the hierarchy</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">hide-pre-root-node</font></td>
    <td><font face="Arial, Helvetica, sans-serif">true</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If a root node is specified 
      this property determines if the user can browse to ancestor nodes of the 
      root node. This feature can be used to present a restricted view of the 
      hierarchy within the repository</font></td>
  </tr>
</table>
 * <p>By modifying these properties or writing new implementations sites can
 * customize the behaviour of existing browse modes, and create their own. Coupled
 * with the ability to control menu items, sites can  adapt the Grouper UI to 
 * institutional requirements</p>
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: AbstractRepositoryBrowser.java,v 1.1 2005-11-22 10:30:33 isgwb Exp $
 */
public abstract class AbstractRepositoryBrowser implements RepositoryBrowser {
	protected String prefix = null;
	protected String initialStems = null;
	private static String browseMode=null;
	private GrouperSession s;
	private  ResourceBundle mediaBundle = null;
	boolean isFlatCapable  = false;
	private String rootNode = null;
	private boolean hidePreRootNode = false;
	private String[] flatPrivs = {};
	private String flatType = null;
	private Subject subject = null;
	/**
	 * 
	 */
	
	public AbstractRepositoryBrowser() {

	}
	public AbstractRepositoryBrowser(GrouperSession s,ResourceBundle bundle) {
		init(s,bundle);
	}
	
	public void init(GrouperSession s,ResourceBundle bundle) {
		mediaBundle = bundle;
		this.s = s;
		this.subject=s.subject();
		
		isFlatCapable = "true".equals(getProperty("flat-capable"));
		rootNode = getProperty("root-node");
		hidePreRootNode="true".equals(getProperty("hide-pre-root-node"));
		flatType = getProperty("flat-type");
		flatPrivs = getProperty("flat-privs").split(" ");
	}

	public  String getProperty(String key) {
		try {
			return mediaBundle.getString(getPrefix() + key);
		}catch(MissingResourceException e){}
		return "";
	}
	
	protected Set getFlatChildren(int start,int pageSize,StringBuffer totalCount) {
		if("stem".equals(flatType)) return GrouperHelper.getStemsForPrivileges(
				s, flatPrivs, start, pageSize,
				totalCount);
		if(flatPrivs.length==1 && flatPrivs[0].equals("MEMBER")) {
			
			Set tmp = GrouperHelper.getMembershipsSet(getGrouperSession(),
					start, pageSize, totalCount);
			List tmpList = new ArrayList(tmp);
			tmpList = GrouperHelper.groups2Maps(getGrouperSession(),tmpList);
			return new LinkedHashSet(tmpList);
		}
		return GrouperHelper.getGroupsForPrivileges(
				s, flatPrivs, start, pageSize,
				totalCount);
	}
	
	public Set getChildren(String node,int start,int pageSize,StringBuffer totalCount,boolean isFlat,boolean isForAssignment) {
		if(isFlat) return getFlatChildren(start,pageSize,totalCount);
		
		Set results = new LinkedHashSet();
		GrouperGroup group = null;
		GrouperStem stem = null;
		try {
			group = GrouperGroup.loadByID(s,node);
		}catch(ClassCastException e) {}

		if(group==null) stem = GrouperStem.loadByID(s,node);
		 
		List allChildren = new ArrayList();
		int resultSize=0;
		if(isForAssignment) {
			if(group !=null) {//display immediate members
				allChildren = group.listImmVals();
				resultSize = allChildren.size();
				allChildren = GrouperHelper.groupList2SubjectsMaps(
						s, allChildren, start, pageSize);
				results.addAll(allChildren);
				return results;
			}
		} else if(group!=null) return results;
			//must be stem
				String stemName = null;
				if(stem!=null) {
					stemName = stem.name();
				}else if(Grouper.NS_ROOT.equals(node)){
					stemName=node;
				}else{
					throw new RuntimeException(node + " is not recognised");
				}
				allChildren = GrouperHelper.getChildrenAsMaps(s, stemName);
				//Map validStems  = GrouperHelper.getValidStems(s,browseMode);
				boolean addChild = false;
				int end = start + pageSize;
				
				Map child;
				String name;
				
				for (int i = 0; i < allChildren.size(); i++) {
					addChild = false;

					child = (Map) allChildren.get(i);
						if(isForAssignment) {
							addChild=true;
						}else{
							addChild=isValidChild(child);
						}
						if (addChild) {
							resultSize++;
						
							if (resultSize >= start && resultSize < end)
								results.add(child);
							}

						}
					
				
			
		return results;
	}
	
	public abstract Map getValidStems();
	
	protected Map getStems(List groups) {
		GrouperGroup group;
		GrouperList grouperList;
		String groupKey;
		String stem;
		int pos = 0;
		String partStem;
		String gkey;
		String name;
		Map stems = new HashMap();
		String HIER_DELIM = GrouperHelper.HIER_DELIM;
		
		for (int i = 0; i < groups.size(); i++) {
			grouperList = (GrouperList) groups.get(i);
			name = grouperList.group().name();
	
			if (!stems.containsKey(name)) {
				stems.put(name, Boolean.TRUE);
	
				pos = 0;
				while (name.indexOf(HIER_DELIM, pos) > -1) {
					pos = name.indexOf(HIER_DELIM, pos);
					partStem = name.substring(0, pos);
					pos++;
					stems.put(partStem, Boolean.TRUE);
	
				}
			}
		}
		return stems;
	}
	
	public boolean hasAtleastOneOf(String[] privileges,GrouperSession s, GrouperGroup group) {
		if(privileges == null || privileges.length==0) return true;
		GrouperAccess accessImpl = s.access();
		boolean result = false;
		for(int i=0;i<privileges.length;i++) {
			if(accessImpl.has(s,group,privileges[i])) return true;
		}
		return result;
	}
	
	public String getInitialStems() {
		if(initialStems!=null) return initialStems;
		try {
			String tmp = getMediaBundle().getString("plugin.initialstems");
			return tmp;
		}catch (Exception e){}
		return null;
	}
	
	public List getParentStems(Group groupOrStem) {
		List path = new ArrayList();
		if(groupOrStem==null) return path;
		Map map = GrouperHelper.group2Map(s, groupOrStem);

		GrouperStem curStem = null;
		String endPoint = Grouper.NS_ROOT;
		
		boolean isEndPointReached = false;
		if(isHidePreRootNode()) {
			endPoint = getRootNode();
			if(map.get("name").equals(endPoint)) isEndPointReached=true;
		}

		while (!isEndPointReached && !Grouper.NS_ROOT.equals(map.get("stem"))) {
			curStem = GrouperStem.loadByName(s, (String) map.get("stem"));
			if (curStem != null) {
				map = GrouperHelper.stem2Map(s, curStem);
				path.add(0, map);
				if(curStem.getName().equals(endPoint))isEndPointReached=true;
			}
		}
		return path;
		
	}
	
	public abstract boolean isValidChild(Map child);
	/**
	 * @return Returns the browseMode.
	 */
	protected static String getBrowseMode() {
		return browseMode;
	}
	/**
	 * @param browseMode The browseMode to set.
	 */
	protected static void setBrowseMode(String browseMode) {
		AbstractRepositoryBrowser.browseMode = browseMode;
	}
	/**
	 * @return Returns the prefix.
	 */
	protected  String getPrefix() {
		return prefix;
	}
	
	/**
	 * @return Returns the isFlatCapable.
	 */
	public boolean isFlatCapable() {
		return isFlatCapable;
	}
	/**
	 * @param isFlatCapable The isFlatCapable to set.
	 */
	protected void setFlatCapable(boolean isFlatCapable) {
		this.isFlatCapable = isFlatCapable;
	}
	/**
	 * @return Returns the mediaBundle.
	 */
	protected ResourceBundle getMediaBundle() {
		return mediaBundle;
	}
	/**
	 * @param mediaBundle The mediaBundle to set.
	 */
	protected void setMediaBundle(ResourceBundle mediaBundle) {
		this.mediaBundle = mediaBundle;
	}
	
	/**
	 * @return Returns the rootNode.
	 */
	public String getRootNode() {
		return rootNode;
	}
	/**
	 * @param rootNode The rootNode to set.
	 */
	protected void setRootNode(String rootNode) {
		this.rootNode = rootNode;
	}
	/**
	 * @return Returns the s.
	 */
	protected GrouperSession getGrouperSession() {
		return s;
	}
	/**
	 * @param s The s to set.
	 */
	protected void setGrouperSession(GrouperSession s) {
		this.s = s;
	}
	/**
	 * @return Returns the subject.
	 */
	protected Subject getSubject() {
		return subject;
	}
	/**
	 * @param subject The subject to set.
	 */
	protected void setSubject(Subject subject) {
		this.subject = subject;
	}
	
	/**
	 * @return Returns the flatPrivs.
	 */
	protected String[] getFlatPrivs() {
		return flatPrivs;
	}
	/**
	 * @param flatPrivs The flatPrivs to set.
	 */
	protected void setFlatPrivs(String[] flatPrivs) {
		this.flatPrivs = flatPrivs;
	}
	/**
	 * @return Returns the flatType.
	 */
	protected String getFlatType() {
		return flatType;
	}
	/**
	 * @param flatType The flatType to set.
	 */
	protected void setFlatType(String flatType) {
		this.flatType = flatType;
	}
	/**
	 * @return Returns the hidePreRootNode.
	 */
	public boolean isHidePreRootNode() {
		return hidePreRootNode;
	}
}
