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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import edu.internet2.middleware.grouper.ComplementFilter;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupAttributeFilter;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperQuery;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.IntersectionFilter;
import edu.internet2.middleware.grouper.QueryFilter;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.UnionFilter;
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
 * repository.browser.create.search=stems
 * repository.browser.create.initial-stems=edu...InitialStemsImpl
 *<table width="100%" border="1">
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
      starts. Defaults to empty = root node, but could be at any level in the hierarchy. If not specified
      will look at media.properties:default.browse.stem</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">hide-pre-root-node</font></td>
    <td><font face="Arial, Helvetica, sans-serif">true</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If a root node is specified 
      this property determines if the user can browse to ancestor nodes of the 
      root node. This feature can be used to present a restricted view of the 
      hierarchy within the repository</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">search</font></td>
    <td><font face="Arial, Helvetica, sans-serif">groups / stems</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Indicates what to search</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">initial-stems</font></td>
    <td>edu...InitialStemsImpl</td>
    <td><font face="Arial, Helvetica, sans-serif">class name for an InitialStems 
      implementation. Defaults to media.properties:plugin.initialstems value </font></td>
  </tr>
</table>
 * <p>By modifying these properties or writing new implementations sites can
 * customize the behaviour of existing browse modes, and create their own. Coupled
 * with the ability to control menu items, sites can  adapt the Grouper UI to 
 * institutional requirements</p>
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: AbstractRepositoryBrowser.java,v 1.7 2006-02-22 12:40:42 isgwb Exp $
 */
public abstract class AbstractRepositoryBrowser implements RepositoryBrowser {
	
	protected String prefix = null;
	protected String initialStems = null;
	protected String browseMode=null;
	private GrouperSession s;
	private  ResourceBundle mediaBundle = null;
	boolean isFlatCapable  = false;
	private String rootNode = null;
	private boolean hidePreRootNode = false;
	private String[] flatPrivs = {};
	private String flatType = null;
	private Subject subject = null;
	protected String search=null;
	protected Map savedValidStems=null;
	
	/**
	 * Default no argument constructor
	 */
	public AbstractRepositoryBrowser() {

	}
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.RepositoryBrowser#init(edu.internet2.middleware.grouper.GrouperSession, java.util.ResourceBundle)
	 */
	public void init(GrouperSession s,ResourceBundle bundle) {
		mediaBundle = bundle;
		this.s = s;
		this.subject=s.getSubject();
		
		isFlatCapable = "true".equals(getProperty("flat-capable"));
		rootNode = getProperty("root-node");
		if("".equals(rootNode)){
			rootNode=mediaBundle.getString("default.browse.stem");
			if(rootNode.startsWith("@"))rootNode="";
		}
		hidePreRootNode="true".equals(getProperty("hide-pre-root-node"));
		flatType = getProperty("flat-type");
		flatPrivs = getProperty("flat-privs").split(" ");
		search = getProperty("search");
		initialStems = getProperty("initial-stems");
	}
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.RepositoryBrowser#getFlattenType()
	 */
	public String getFlattenType() {
		// TODO Auto-generated method stub
		return flatType + "s";
	}

	/**
	 * Returns configuration settings
	 * @param key
	 * @return
	 */
	protected  String getProperty(String key) {
		try {
			return mediaBundle.getString(getPrefix() + key);
		}catch(MissingResourceException e){}
		return "";
	}
	
	/**
	 * Called from getChildren if in flat mode
	 * @param start
	 * @param pageSize
	 * @param totalCount
	 * @return
	 * @throws Exception
	 */
	protected Set getFlatChildren(int start,int pageSize,StringBuffer totalCount) throws Exception{
		if("stem".equals(flatType)) {
			List l = GrouperHelper.stems2Maps(s,new ArrayList( GrouperHelper.getStemsForPrivileges(
		
				s, flatPrivs, start, pageSize,
				totalCount)));
			return new LinkedHashSet(l);
		}
		if(flatPrivs.length==1 && flatPrivs[0].equals("MEMBER")) {
			
			Set tmp = GrouperHelper.getMembershipsSet(getGrouperSession(),
					start, pageSize, totalCount);
			List tmpList = new ArrayList(tmp);
			tmpList = GrouperHelper.groups2Maps(getGrouperSession(),tmpList);
			return new LinkedHashSet(tmpList);
		}
		List l=GrouperHelper.groups2Maps(s,new ArrayList( GrouperHelper.getGroupsForPrivileges(
				s, flatPrivs, start, pageSize,
				totalCount)));
		return new LinkedHashSet(l);
	}
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.RepositoryBrowser#getChildren(java.lang.String, int, int, java.lang.StringBuffer, boolean, boolean)
	 */
	public Set getChildren(String node,String listField,int start,int pageSize,StringBuffer totalCount,boolean isFlat,boolean isForAssignment) throws Exception{
		if(isFlat) return getFlatChildren(start,pageSize,totalCount);
		
		Set results = new LinkedHashSet();
		GroupOrStem groupOrStem = GroupOrStem.findByID(s,node);
		Group group = groupOrStem.getGroup();
		Stem stem = groupOrStem.getStem();
		if(listField==null || "".equals(listField)) listField="members";
		Field field = FieldFinder.find(listField);
		Set allChildren = new LinkedHashSet();
		int resultSize=0;
		if(isForAssignment) {
			if(group !=null) {//display immediate members
				allChildren = group.getImmediateMemberships(field);
				resultSize = allChildren.size();
				results.addAll(GrouperHelper.groupList2SubjectsMaps(
						s, new ArrayList(allChildren), start, pageSize));
				
				return results;
			}
		} else if(group!=null) return results;
			//must be stem
				String stemName = null;
				if(stem!=null) {
					stemName = stem.getName();
				}else if(GrouperHelper.NS_ROOT.equals(node)){
					stemName=node;
				}else{
					throw new RuntimeException(node + " is not recognised");
				}
				allChildren.addAll(GrouperHelper.getChildrenAsMaps(s, stemName));
				//Map validStems  = GrouperHelper.getValidStems(s,browseMode);
				boolean addChild = false;
				int end = start + pageSize;
				
				Map child;
				String name;
				Iterator it = allChildren.iterator();
				int count=0;
				while(it.hasNext()) {
					addChild = false;

					child = (Map) it.next();
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
	
	/**
	 * USed to filter unwanted children
	 * @return
	 * @throws Exception
	 */
	protected abstract Map getValidStems() throws Exception;
	
	/**
	 * Given a Collection of groups, find all their stems and return as a Map.
	 * Called by getValidStems implementations
	 * @param groups
	 * @return
	 * @throws Exception
	 */
	protected Map getStems(Collection groups) throws Exception{
		Group group;
		Stem stem;
		Object item;
		String groupKey;
		
		int pos = 0;
		String partStem;
		String gkey;
		String name;
		Map stems = new HashMap();
		String HIER_DELIM = GrouperHelper.HIER_DELIM;
		Iterator it = groups.iterator();
		while(it.hasNext()) {
			item =  it.next();
			if(item instanceof Group) {
				stems.put(((Group)item).getName(), Boolean.TRUE);
				name = ((Group)item).getParentStem().getName();
			}else{
				name = ((Stem)item).getName();
			}
	
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
		savedValidStems = stems;
		return stems;
	}
	
	/**
	 * Convenience method
	 * @param privileges
	 * @param s
	 * @param group
	 * @return
	 */
	protected boolean hasAtleastOneOf(String[] privileges,GrouperSession s, Group group) {
		if(privileges == null || privileges.length==0) return true;
		
		boolean result = false;
		for(int i=0;i<privileges.length;i++) {
			if(privileges[i].equals("admin") && group.hasAdmin(s.getSubject())) return true;
			if(privileges[i].equals("update") && group.hasUpdate(s.getSubject())) return true;
			if(privileges[i].equals("read") && group.hasRead(s.getSubject())) return true;
			if(privileges[i].equals("view") && group.hasView(s.getSubject())) return true;
			if(privileges[i].equals("optin") && group.hasOptin(s.getSubject())) return true;
			if(privileges[i].equals("optout") && group.hasOptout(s.getSubject())) return true;
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.RepositoryBrowser#getInitialStems()
	 */
	public String getInitialStems() {
		if(initialStems!=null && !"".equals(initialStems)) return initialStems;
		try {
			String tmp = getMediaBundle().getString("plugin.initialstems");
			return tmp;
		}catch (Exception e){}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.RepositoryBrowser#getParentStems(edu.internet2.middleware.grouper.ui.GroupOrStem)
	 */
	public List getParentStems(GroupOrStem groupOrStem) throws Exception{
		List path = new ArrayList();
		if(groupOrStem==null) return path;
		Map map = GrouperHelper.group2Map(s, groupOrStem);

		Stem curStem = null;
		String endPoint = GrouperHelper.NS_ROOT;
		
		boolean isEndPointReached = false;
		if(isHidePreRootNode()) {
			endPoint = getRootNode();
			if(map.get("name").equals(endPoint)) isEndPointReached=true;
		}

		while (!isEndPointReached && !"".equals(map.get("stem")) && !GrouperHelper.NS_ROOT.equals(map.get("stem"))) {
			curStem = StemFinder.findByName(s, (String) map.get("stem"));
			if (curStem != null) {
				map = GrouperHelper.stem2Map(s, curStem);
				path.add(0, map);
				if(curStem.getName().equals(endPoint))isEndPointReached=true;
			}
		}
		if(!isEndPointReached) {
			path.add(0, GrouperHelper.stem2Map(s, StemFinder.findRootStem(s)));
		}
		return path;
		
	}
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.RepositoryBrowser#search(edu.internet2.middleware.grouper.GrouperSession, java.lang.String, java.lang.String, java.util.Map)
	 */
	public List search(GrouperSession s, String query,String from, Map attr,List outTerms) throws Exception {
		String searchInDisplayNameOrExtension = getSingle("searchInDisplayNameOrExtension",attr);
		String searchInNameOrExtension = getSingle("searchInNameOrExtension",attr);
		boolean isAdvancedSearch = "Y".equals(getSingle("advSearch",attr));
		List results = null;
		if("stems".equals(search)) {
			results = GrouperHelper.searchStems(s,query,from,searchInDisplayNameOrExtension,searchInNameOrExtension);
		}else{
			if(isAdvancedSearch) {
				results = advancedSearch(s,from,attr,outTerms);
			}else {
				results = GrouperHelper.searchGroups(s,query,from,searchInDisplayNameOrExtension,searchInNameOrExtension,browseMode);
				if(outTerms!=null) outTerms.add(query);
			}
		}
		List filtered = new ArrayList();
		Object obj = null;
		for(int i=0;i<results.size();i++) {
			obj=results.get(i);
			if(isValidSearchResult(obj)) filtered.add(obj);
		}
		return filtered;
	}
	
	public List advancedSearch(GrouperSession s,String from,Map attr,List outTerms) throws Exception{
		List res = new ArrayList();
		String maxCountStr = getSingle("maxFields",attr);
		int maxCount = Integer.parseInt(maxCountStr);
		String lastQuery = null;
		String lastField = null;
		String lastAndOrNot = null;
		String field;
		String query;
		String andOrNot;
		QueryFilter queryFilter = null;
		if(outTerms==null) outTerms=new ArrayList();
		
		Stem fromStem = StemFinder.findByName(s,from);
		for (int i=1;i<=maxCount;i++) {
			field = getSingle("searchField." + i,attr);
			query = getSingle("searchField." + i + ".query",attr);
			if(i==1 && (field==null || query==null)) throw new IllegalArgumentException("The first search field and query value must be enetered");
			andOrNot = getSingle("searchField." + i + ".searchAndOrNot",attr);
			if(query==null || "".equals(query)) query = lastQuery;
			if(i>1) {
				if(queryFilter==null) {
					queryFilter=new GroupAttributeFilter(lastField,lastQuery,fromStem);
					outTerms.add(lastQuery);
					outTerms.add(lastField);
				}
				if(field==null && i==2) {
					break;
				}
				if(field==null && i>2) break;
				
				if("and".equals(lastAndOrNot)) {
					queryFilter = new IntersectionFilter(queryFilter,new GroupAttributeFilter(field,query,fromStem));
				}else if("or".equals(lastAndOrNot)){
					queryFilter = new UnionFilter(queryFilter,new GroupAttributeFilter(field,query,fromStem));
				}else{
					queryFilter = new ComplementFilter(queryFilter,new GroupAttributeFilter(field,query,fromStem));
				}
				outTerms.add(andOrNot);
				outTerms.add(query);
				outTerms.add(field);
				
			}
			lastQuery = query;
			lastField = field;
			lastAndOrNot = andOrNot;
		}
		
		GrouperQuery q = GrouperQuery.createQuery(s,queryFilter);
		res.addAll(q.getGroups());
		return res;
	}

	
	/**
	 * In order to have a generic getChildren method, the decision to keep, or
	 * not, a child has been factored out
	 * @param child
	 * @return
	 * @throws Exception
	 */
	protected abstract boolean isValidChild(Map child) throws Exception;
	
	/**
	 * In order to have a generic search method, the decision to keep, or
	 * not, a result has been factored out
	 * @param searchResult
	 * @return
	 * @throws Exception
	 */
	protected boolean isValidSearchResult(Object searchResult) throws Exception {
		if(searchResult instanceof Group) return isValidSearchResult((Group)searchResult);
		if(searchResult instanceof Stem) return isValidSearchResult((Stem)searchResult);
		throw new IllegalArgumentException("Only understand Groups or Stems");
	}
	
	/**
	 * In order to have a generic search method, the decision to keep, or
	 * not, a result has been factored out
	 * @param searchResult
	 * @return
	 * @throws Exception
	 */
	protected boolean isValidSearchResult(Group searchResult) throws Exception {
		return false;
	}
	
	/**
	 * In order to have a generic search method, the decision to keep, or
	 * not, a result has been factored out
	 * @param searchResult
	 * @return
	 * @throws Exception
	 */
	protected boolean isValidSearchResult(Stem searchResult) throws Exception {
		return false;
	}
	/**
	 * @return Returns the browseMode.
	 */
	protected String getBrowseMode() {
		return browseMode;
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
	 * @return Returns the mediaBundle.
	 */
	protected ResourceBundle getMediaBundle() {
		return mediaBundle;
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
	 * @return Returns the subject.
	 */
	protected Subject getSubject() {
		return subject;
	}
	
	
	/**
	 * @return Returns the flatPrivs.
	 */
	protected String[] getFlatPrivs() {
		return flatPrivs;
	}
	
	/**
	 * @return Returns the flatType.
	 */
	protected String getFlatType() {
		return flatType;
	}
	/**
	 * @return Returns the hidePreRootNode.
	 */
	public boolean isHidePreRootNode() {
		return hidePreRootNode;
	}
	
	private String getSingle(String key,Map map) {
		Object[] vals = (Object[])map.get(key);
		if(vals==null) return null;
		if("".equals(vals[0])) return null;
		return vals[0].toString();
	}
}
