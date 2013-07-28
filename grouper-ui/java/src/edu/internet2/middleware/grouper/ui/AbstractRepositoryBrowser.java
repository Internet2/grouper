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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.filter.ComplementFilter;
import edu.internet2.middleware.grouper.filter.GroupAnyAttributeFilter;
import edu.internet2.middleware.grouper.filter.GroupAttributeFilter;
import edu.internet2.middleware.grouper.filter.GroupTypeFilter;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.filter.IntersectionFilter;
import edu.internet2.middleware.grouper.filter.QueryFilter;
import edu.internet2.middleware.grouper.filter.StemDisplayExtensionFilter;
import edu.internet2.middleware.grouper.filter.StemDisplayNameFilter;
import edu.internet2.middleware.grouper.filter.StemExtensionFilter;
import edu.internet2.middleware.grouper.filter.StemNameAnyFilter;
import edu.internet2.middleware.grouper.filter.StemNameFilter;
import edu.internet2.middleware.grouper.filter.UnionFilter;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.ui.actions.LowLevelGrouperCapableAction;
import edu.internet2.middleware.grouper.ui.util.GroupAsMap;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
 * @version $Id: AbstractRepositoryBrowser.java,v 1.25 2009-11-07 14:46:34 isgwb Exp $
 */
public abstract class AbstractRepositoryBrowser implements RepositoryBrowser {
	
	protected String prefix = null;
	protected String initialStems = null;
	protected String browseMode=null;
	private GrouperSession s;
	private  ResourceBundle mediaBundle = null;
	private  ResourceBundle navBundle = null;
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
	public void init(GrouperSession s,ResourceBundle bundle,ResourceBundle mediaBundle) {
		this.mediaBundle = mediaBundle;
		navBundle=bundle;
		this.s = s;
		this.subject=s.getSubject();
		
		isFlatCapable = "true".equals(getProperty("flat-capable"));
		rootNode = getProperty("root-node");
		if("".equals(rootNode)){
		  try {
		    rootNode=mediaBundle.getString("default.browse.stem");
		  } catch (MissingResourceException mre) {
		    //thats ok, just ignore
		  }
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
	 * Returns configuration settings
	 * @param key
	 * @return
	 */
	protected  String getMediaProperty(String key) {
		try {
			return mediaBundle.getString(key);
		}catch(MissingResourceException e){}
		return "";
	}
	
	/**
	 * Called from getChildren if in flat mode
	 * @param start
	 * @param pageSize
	 * @param totalCount
	 * @param context
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected Set getFlatChildren(int start,int pageSize,StringBuffer totalCount,String context,HttpServletRequest request) throws Exception{
		if("stem".equals(flatType)) {
			List l = GrouperHelper.stems2Maps(s,LowLevelGrouperCapableAction.sort(GrouperHelper.getStemsForPrivileges(
		
				s, flatPrivs, start, pageSize,
        totalCount),request,context, -1, null));
			return new LinkedHashSet(l);
		}
		if(flatPrivs.length==1 && flatPrivs[0].equals("member")) {
			/*Set tmp = GrouperHelper.getMembershipsSet(getGrouperSession(),start, pageSize, totalCount);*/
			Set tmp = GrouperHelper.getMembershipsSet(getGrouperSession());
			totalCount.append("" + tmp.size());
      List tmpList = LowLevelGrouperCapableAction.sort(tmp,request,context, -1, null);
			int end = start + pageSize;
			if (end > tmpList.size())
				end = tmpList.size();
			tmpList = GrouperHelper.groups2Maps(getGrouperSession(),tmpList.subList(start,end));
			return new LinkedHashSet(tmpList);
		}
		Set groupsForPrivileges = GrouperHelper.getGroupsForPrivileges(
				s, flatPrivs, start, pageSize,
				totalCount);
    List l=GrouperHelper.groups2Maps(s,LowLevelGrouperCapableAction.sort( groupsForPrivileges,request,context, -1, null));
		return new LinkedHashSet(l);
	}
	
  /**
	 * @see edu.internet2.middleware.grouper.ui.RepositoryBrowser#getChildren(java.lang.String, int, int, java.lang.StringBuffer, boolean, boolean)
	 */
  public Set getChildren(String node,String listField,int start,int pageSize,
      StringBuffer totalCount,boolean isFlat,boolean isForAssignment,
      String omitForAssignment,String context,HttpServletRequest request) throws Exception{

		if(isFlat) return getFlatChildren(start,pageSize,totalCount,"flat",request);
		
		Set results = new LinkedHashSet();
		GroupOrStem groupOrStem = GroupOrStem.findByID(s,node);
		Group group = groupOrStem.getGroup();
		Stem stem = groupOrStem.getStem();
		if(listField==null || "".equals(listField)) listField="members";
		Field field = FieldFinder.find(listField, true);
		List sortedChildren=null;
    int[] resultSizeArray= new int[1];
		int resultSize=0;
		if(isForAssignment) {
			if(group !=null) {//display immediate members
        
        Set<Membership> allChildren = new LinkedHashSet<Membership>();
        ResourceBundle resourceBundle = GrouperUiFilter.retrieveSessionMediaResourceBundle();
        String sortLimitString=resourceBundle.getString("comparator.sort.limit");
        int sortLimit=Integer.parseInt(sortLimitString);
        
        allChildren = MembershipFinder.internal_findAllImmediateByGroupAndFieldAndPage(
            group, field, start, pageSize, sortLimit, resultSizeArray);
        resultSize = resultSizeArray[0];
        sortedChildren=LowLevelGrouperCapableAction.sort(allChildren,request,context, resultSize, null);

        int groupList2SubjectStart = (start >= sortedChildren.size()) ? 0 : start;
        
				results.addAll(GrouperHelper.groupList2SubjectsMaps(
            s, sortedChildren, groupList2SubjectStart, pageSize));
				if(totalCount!=null) {
					totalCount.setLength(0);
					totalCount.append(resultSize);
				}
				return results;
			}
		} else if(group!=null) return results;
        Set<GroupAsMap> allChildren = new LinkedHashSet<GroupAsMap>();

			//must be stem
				String stemName = null;
				if(stem!=null) {
					stemName = stem.getName();
				}else if(GrouperHelper.NS_ROOT.equals(node)){
					stemName=node;
				}else{
					throw new RuntimeException(node + " is not recognised");
        }
        List<GroupAsMap> listOfMaps = getChildrenAsMaps(s, stemName,
          start, pageSize, resultSizeArray);
          
        if (this.pagedQuery()) {
          resultSize = resultSizeArray[0];  
        }
        
        if (sortedQuery()){  
          listOfMaps = LowLevelGrouperCapableAction.sort(listOfMaps,request,context, -1, null);
				}
        
        allChildren.addAll(listOfMaps);
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
							//Do not try to exclude current group - so what if someone tries to add an existing member?
							//Also becomes complicated if there are custom fields
							//if(omitForAssignment!=null && omitForAssignment.equals(child.get("id"))) {
								//addChild=false;
							//}else{
								addChild=true;
							//}
						}else{
							addChild=isValidChild(child);
						}
						if (addChild) {
							if (!this.pagedQuery()) {
								resultSize++;
							}
						
							if (this.pagedQuery() || (resultSize >= start && resultSize < end)) {
								results.add(child);
							}
						}else if (this.pagedQuery()) {
							resultSize--;
						}
        }
				if(totalCount!=null) {
					totalCount.setLength(0);
					totalCount.append(resultSize);
				}
		return results;
	}
  
    
	
	/**
	 * Used to filter unwanted children
	 * @return
	 * @throws Exception
	 */
	protected abstract Map getValidStems() throws Exception;
	
	/**
   * is this a sorted query or not
   * @return if this is a sorted query
   */
  protected boolean sortedQuery() {
    return false;
  }
  
  /**
   * is this a paged query or not
   * @return if this is a sorted query
   */
  protected boolean pagedQuery() {
    return false;
  }
  
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
        name = ((Group)item).getParentStemName();
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
			curStem = StemFinder.findByName(s, (String) map.get("stem"), true);
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
			if(isAdvancedSearch) {
				results = advancedStemSearch(s,from,attr,outTerms);
			}else {
				results = GrouperHelper.searchStems(s,query,from,searchInDisplayNameOrExtension,searchInNameOrExtension);
				if(outTerms!=null) outTerms.add(query);
			}
			
		}else{
			if(isAdvancedSearch) {
				results = advancedSearch(s,from,attr,outTerms);
			}else {
				String searchInAny=getMediaProperty("search.default.any");
				if("only".equals(searchInAny) || ("true".equals(searchInAny) && "any".equals(getSingle("searchIn",attr)))) {
					searchInDisplayNameOrExtension=null;
					searchInNameOrExtension=null;
					
				}
				
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
		
		Group.initGroupObjectAttributes(filtered);
		
		return filtered;
	}
	
	/**
	 * Only accessible from Create groups, implements stem search logic
	 * @param s
	 * @param from
	 * @param attr
	 * @param outTerms
	 * @return list of Stems matching search criteria
	 * @throws Exception
	 */
	public List advancedStemSearch(GrouperSession s,String from,Map attr,List outTerms) throws Exception{
		List res = new ArrayList();
		String maxCountStr = getSingle("maxFields",attr);
		int maxCount = Integer.parseInt(maxCountStr);
		String lastQuery = null;
		String lastField = null;
		String lastAndOrNot = null;
		String field;
		String query;
		String andOrNot;
		Map fieldMaps = (Map)GrouperHelper.getFieldsAsMap().get("stems");
		String lastFieldDisplayName=null;
		QueryFilter queryFilter = null;
		if(outTerms==null) outTerms=new ArrayList();
		
		Stem fromStem = StemFinder.findByName(s,from, true);
		for (int i=1;i<=maxCount;i++) {
			field = getSingle("searchField." + i,attr);
			query = getSingle("searchField." + i + ".query",attr);
			if(i==1 && (field==null || query==null)) {
				if(getSingle("searchType.1" ,attr)!=null) break;
				throw new IllegalArgumentException("The first search field and query value must be enetered");
			}
			andOrNot = getSingle("searchField." + i + ".searchAndOrNot",attr);
			if(query==null || "".equals(query)) query = lastQuery;
			if(i>1) {
				if(queryFilter==null) {
					queryFilter=getStemAttributeFilter(lastField,lastQuery,fromStem);
					outTerms.add(lastQuery);
					lastFieldDisplayName=(String)fieldMaps.get(lastField);
					outTerms.add(lastFieldDisplayName);
				}
				if(field==null && i==2) {
					break;
				}
				if(field==null && i>2) break;
				
				if("and".equals(lastAndOrNot)) {
					queryFilter = new IntersectionFilter(queryFilter,getStemAttributeFilter(field,query,fromStem));
				}else if("or".equals(lastAndOrNot)){
					queryFilter = new UnionFilter(queryFilter,getStemAttributeFilter(field,query,fromStem));
				}else{
					queryFilter = new ComplementFilter(queryFilter,getStemAttributeFilter(field,query,fromStem));
				}
				outTerms.add(lastAndOrNot);
				outTerms.add(query);
				outTerms.add(field);
				
			}
			lastQuery = query;
			lastField = field;
			lastAndOrNot = andOrNot;
		}
		
		
		GrouperQuery q = GrouperQuery.createQuery(s,queryFilter);
		res.addAll(q.getStems());
		return res;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.RepositoryBrowser#advancedSearch(edu.internet2.middleware.grouper.GrouperSession, java.lang.String, java.util.Map, java.util.List)
	 */
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
		Map fieldMaps = GrouperHelper.getFieldsAsMap();
		String lastFieldDisplayName=null;
		QueryFilter queryFilter = null;
		if(outTerms==null) outTerms=new ArrayList();
		
		Stem fromStem = StemFinder.findByName(s,from, true);
		for (int i=1;i<=maxCount;i++) {
			field = getSingle("searchField." + i,attr);
			query = getSingle("searchField." + i + ".query",attr);
			if(i==1 && (field==null || query==null)) {
				if(getSingle("searchType.1" ,attr)!=null) break;
				throw new IllegalArgumentException("The first search field and query value must be enetered");
			}
			andOrNot = getSingle("searchField." + i + ".searchAndOrNot",attr);
			if(query==null || "".equals(query)) query = lastQuery;
			if(i>1) {
				if(queryFilter==null) {
					queryFilter=getGroupAttributeFilter(lastField,lastQuery,fromStem);
					outTerms.add(lastQuery);
					lastFieldDisplayName=(String)((Map)fieldMaps.get(lastField)).get("displayName");
					outTerms.add(lastFieldDisplayName);
				}
				if(field==null && i==2) {
					break;
				}
				if(field==null && i>2) break;
				
				if("and".equals(lastAndOrNot)) {
					queryFilter = new IntersectionFilter(queryFilter,getGroupAttributeFilter(field,query,fromStem));
				}else if("or".equals(lastAndOrNot)){
					queryFilter = new UnionFilter(queryFilter,getGroupAttributeFilter(field,query,fromStem));
				}else{
					queryFilter = new ComplementFilter(queryFilter,getGroupAttributeFilter(field,query,fromStem));
				}
				outTerms.add(lastAndOrNot);
				outTerms.add(query);
				outTerms.add(field);
				
			}
			lastQuery = query;
			lastField = field;
			lastAndOrNot = andOrNot;
		}
		//Now add GroupTYpe filter
		String groupTypeText = navBundle.getString("find.results.group-type");
		String groupType=null;
		maxCountStr = getSingle("maxTypes",attr);
		try {
			maxCount = Integer.parseInt(maxCountStr);
		}catch(NumberFormatException e) {
			maxCount=0;
		}
		GroupType gt=null;
		for (int i=1;i<=maxCount;i++) {
			groupType = getSingle("searchType." + i,attr);
			if(groupType==null) break;
			gt=GroupTypeFinder.find(groupType, true);
			
			andOrNot = getSingle("searchType." + i + ".searchAndOrNot",attr);
			
			
				if(queryFilter==null) {
					queryFilter=new GroupTypeFilter(gt,fromStem);
					outTerms.add(groupTypeText);
					outTerms.add(gt.getName());
				}else{
					if("and".equals(andOrNot)) {
						queryFilter = new IntersectionFilter(queryFilter,new GroupTypeFilter(gt,fromStem));
					}else if("or".equals(andOrNot)){
						queryFilter = new UnionFilter(queryFilter,new GroupTypeFilter(gt,fromStem));
					}else{
						queryFilter = new ComplementFilter(queryFilter,new GroupTypeFilter(gt,fromStem));
					}
					outTerms.add(andOrNot);
					outTerms.add(groupTypeText);
					outTerms.add(groupType);
				}
		}
		
		
		GrouperQuery q = GrouperQuery.createQuery(s,queryFilter);
		res.addAll(q.getGroups());
		return res;
	}
	
	private QueryFilter getGroupAttributeFilter(String field,String query,Stem from) {
		if("_any".equals(field)) {
			return new GroupAnyAttributeFilter(query,from);
		}else{
			return new GroupAttributeFilter(field,query,from);
		}
	}
	
	private QueryFilter getStemAttributeFilter(String field,String query,Stem from) {
		if("_any".equals(field)) {
			return new StemNameAnyFilter(query,from);
		}else if("extension".equals(field)){
			return new StemExtensionFilter(query,from);
		}else if("displayExtension".equals(field)){
			return new StemDisplayExtensionFilter(query,from);
		}else if("name".equals(field)){
			return new StemNameFilter(query,from);
		}else if("displayName".equals(field)){
			return new StemDisplayNameFilter(query,from);
		}
		throw new IllegalArgumentException("["  +field + "] is not a valid Stem attribute");
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

  /**
   * Given a GrouperStem id return a list of stems and groups for which the
   * GrouperStem is an immediate parent
   * @param s GrouperSession for authenticated user
   * @param stemId GrouperStem id
   * @param inPrivSet set of privileges the subject must have in each row
   * @param start 
   * @param pageSize 
   * @param resultSize result size of whole resultset
   * @return List of all stems and groups for stemId
   * @throws StemNotFoundException 
   */
  public List<GroupOrStem> getChildren(GrouperSession s, String stemId, 
      int start, int pageSize, int[] resultSize) throws StemNotFoundException{
    Stem stem =null;
    if("".equals(stemId)) {
      stem=StemFinder.findRootStem(s);
    }else{
      stem=StemFinder.findByName(s, stemId);
    }
    ArrayList res = new ArrayList();
    Set children = getChildStems(stem);
    int stemCount=children.size();
    int counter=0;
    Iterator it = children.iterator();
    Stem childStem = null;
    while(it.hasNext()) {
      childStem=(Stem)it.next();
      if(counter >=start && counter <start+pageSize) {
    	  //Page the stems
    	  res.add(GroupOrStem.findByStem(s,childStem));
      }
      counter++;
    }
    
    //Strategy is to find a page size where all required resultscan be retrieved in a single page
    //Therefore likely to have a different page size and pagenumber each time and are likely
    //to return results ant start and/or end which are not required for UI
    
    //Values below will often be reset before use
    boolean abortGroups=false;
    int groupPage = 1;
    int groupPageSize = pageSize;
    int groupStart=1;
    int end=0;
    int groupLowOffset=0; //how many initial results to discard - assuming page size larger than results required
    int groupHighOffset=pageSize; //where to start discarding end results -  - assuming page size larger than results required
    if(start + pageSize <= stemCount) {
    	//Enough stems to satisfy paging without getting to groups
    	abortGroups=true;
    	//0 indexed
    	end = start + pageSize -1;
    }else if(start + pageSize > stemCount && start <= stemCount){
    	//show last of stems and first of groups
    	end = start+pageSize-stemCount;
    	groupHighOffset = end;
	}else if(start > stemCount) {
		//Not showing any stems
		groupStart = start - stemCount;
		end = groupStart + pageSize -1;
		
		//Figures out the page size, number and the start/end index of groups to be returnedeturned
		int[] pageDetails = determineGroupPage(groupStart, end +1, pageSize);
		groupPage = pageDetails[0];
		groupPageSize=pageDetails[1];
		int recStart=pageDetails[2];
		int recEnd = pageDetails[3];
		groupLowOffset = groupStart - recStart+1;
		groupHighOffset = recEnd;
	}

    
    if(groupPage<=0) {
    	groupPage=1;
    }
    

    QueryOptions queryOptions = null;
    if (this.pagedQuery()) {
      QueryPaging queryPaging = new QueryPaging();
      queryPaging.setPageSize(groupPageSize);
      queryPaging.setPageNumber(groupPage);
      queryOptions = new QueryOptions().paging(queryPaging);
      queryOptions.retrieveCount(true);
    }
    children=getChildGroups(stem, queryOptions);
    if (GrouperUtil.length(resultSize) >= 1) {
      if (this.pagedQuery()) {
        //note: add in the size of the stems
        resultSize[0] = queryOptions.getCount().intValue() + stemCount;
        if(abortGroups) {
        	return res;
        }
      }
    }
    it = children.iterator();
    Group childGroup = null;
    int groupCounter = 0;
    while(it.hasNext()) {
      groupCounter++;
      childGroup=(Group)it.next();
      if(groupCounter >= groupLowOffset && groupCounter <= groupHighOffset) {
    	  res.add(GroupOrStem.findByGroup(s,childGroup));
      }
    }
    return res;
  }
  
  private int[] determineGroupPage(int groupStart, int end, int pagesize) {
  	
  	int counter = pagesize;
  	int groupPage=0;
  	int groupPagesize=0;
  	while(true) {
  		//increment counter until it can be used as a page size where required results 
  		//can be returned in a single page
  		int num = (end + counter -1) / counter;
  		if(counter == end || ((num != 1 && (num - 1) * counter < groupStart))) {
  			groupPagesize=counter;
  			groupPage=num;
  			break;
  		}
  		counter++;

  	}
  	return new int[] {groupPage,groupPagesize,(groupPage-1) * groupPagesize,groupPage * groupPagesize};
  
  	
  }
  
  /**
   * get child groups from a stem
   * @param stem
   * @param scope
   * @return the set of groups 
   */
  public Set<Group> getChildGroups(Stem stem, QueryOptions queryOptions) {
    return stem.getChildGroups();
	}

  /**
   * get child stems to show
   * @param stem
   * @return the stems
   */
  public Set<Stem> getChildStems(Stem stem) {
    return stem.getChildStems();
  }

  /**
   * Given a GrouperStem id return a list of Maps representing the children
   * of that stem. 
   * 
   * @param s GrouperSession for authenticated user
   * @param stemId
   * @param inPrivSet rows must have privs here
   * @param start 
   * @param pageSize 
   * @param resultSize 
   * @return List of GrouperGroups and GrouperStems wrapped as Maps
   * @throws StemNotFoundException 
   */
  public List<GroupAsMap> getChildrenAsMaps(GrouperSession s, String stemId,
      int start, int pageSize, int[] resultSize) throws StemNotFoundException{
    List<GroupOrStem> stems = getChildren(s, stemId, start, pageSize, resultSize );
    List maps = new ArrayList();
    GroupOrStem groupOrStem = null;
    for (int i = 0; i < stems.size(); i++) {
      groupOrStem = (GroupOrStem)stems.get(i);
      maps.add(groupOrStem.getAsMap());
    }
    return maps;
  }
  

}
