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

package edu.internet2.middleware.grouper.ui.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.tiles.ComponentContext;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.RepositoryBrowser;
import edu.internet2.middleware.grouper.ui.RepositoryBrowserFactory;
import edu.internet2.middleware.grouper.ui.SessionInitialiser;
import edu.internet2.middleware.subject.Subject;

/**
 * Superclass for GrouperCapableAction. This class is intended to be used by 
 * Tile controllers whereas GrouperCapabaleAction should be used bt top level Actions
 *  
 * <p />
 * 

 * 
 * @author Gary Brown.
 * @version $Id: LowLevelGrouperCapableAction.java,v 1.6 2006-07-13 11:41:51 isgwb Exp $
 */

/**
 * @author isgwb
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class LowLevelGrouperCapableAction 
	extends org.apache.struts.action.Action {
	public static final String HIER_DELIM = GrouperHelper.HIER_DELIM; 
	/**
	 * Action specific - must be implemented by all subclasses
	 */
	public abstract ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
		      HttpServletRequest request, HttpServletResponse response,
			  HttpSession session, GrouperSession grouperSession) throws Exception;
	
	/**
	 * Makes HttpSession and GrouperSession available to subclasses
	 * Also handles pageSize parameter and times how long is spent in an action
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
		      HttpServletRequest request, HttpServletResponse response) throws Exception{
			HttpSession session = request.getSession();
			GrouperSession grouperSession = (GrouperSession) session.getAttribute("edu.intenet2.middleware.grouper.ui.GrouperSession");
						
			DynaActionForm dummyForm = (DynaActionForm)form;
			if(form!=null)request.setAttribute("grouperForm",form);
			
			ActionForward forward =  grouperExecute(mapping,form,request,response,session,grouperSession);
			
			return forward;
	}
	/**
	 * Convenience method to extract tiles attributes as a Map
	 */
	public Map getTilesAttributes(HttpServletRequest request) {
		ComponentContext context = ComponentContext.getContext(request);
		Map attr = new HashMap();
		Iterator it = context.getAttributeNames();
		String name;
		while(it.hasNext()) {
			name = (String)it.next();
			attr.put(name,context.getAttribute(name));
		}

		return attr;
	}
	
	/**
	 * Convenience method checks request for attribute. If not present checks session
	 * TODO: check application 
	 */
	public Object findAttribute(String name,HttpServletRequest request) {
		Object obj = request.getAttribute(name);
		if(obj==null) obj = request.getSession().getAttribute(name);
		return obj;
	}
	
	/**
	 * Convenience method to retrieve nav ResourceBundle
	 */
	public ResourceBundle getNavResources(HttpServletRequest request) {
		HttpSession session = request.getSession();
		LocalizationContext localizationContext = (LocalizationContext)session.getAttribute("nav");
		ResourceBundle nav = localizationContext.getResourceBundle();
		return nav;
	}
	
	/**
	 * Convenience method to retrieve nav ResourceBundle
	 */
	public ResourceBundle getMediaResources(HttpServletRequest request) {
		HttpSession session = request.getSession();
		return getMediaResources(session);
	}
	
	/**
	 * Convenience method to retrieve media ResourceBundle given an HttpSession
	 */
	public ResourceBundle getMediaResources(HttpSession session) {
		
		LocalizationContext localizationContext = (LocalizationContext)session.getAttribute("media");
		ResourceBundle media = localizationContext.getResourceBundle();
		return media;
	}
	
	
	/**
	 * Convenience method to simplify calling code
	 */
	public static boolean isEmpty(Object obj) {
		return (obj==null || "".equals(obj));
	}
	
	/**
	 * Convenience method to stop duplication of logic in Actions
	 */
	public int getPageSize(HttpSession session) {
		int pageSize=25;
		String pageSizeStr = (String)session.getAttribute("default.pagesize");
		if(pageSizeStr==null) {
			ResourceBundle mediaBundle = getMediaResources(session);
			if(mediaBundle!=null) pageSizeStr = mediaBundle.getString("pager.pagesize.default");
		}
		if(pageSizeStr!=null) {
			try {
				pageSize = Integer.parseInt(pageSizeStr);
			}catch(Exception e) {}
		}
		return pageSize;
	}
	
	/**
	 * Place centrally for consistency and to hide session attribute name
	 */
	public String getBrowseMode(HttpSession session) {
		String browseMode = (String) session.getAttribute(
		"browseMode");
		if (browseMode == null)
			browseMode = "";
		return browseMode;
	}
	
	/**
	 * Place centrally for consistency and to hide session attribute name
	 */
	public static void setBrowseMode(String mode,HttpSession session) {
		session.setAttribute("browseMode",mode);
	}
	
	/**
	 * Place centrally for consistency and to hide session attribute name
	 */
	public String getBrowseNode(HttpSession session) {
		return (String) session.getAttribute("browseNodeId" + getBrowseMode(session));
	}
	
	/**
	 * Place centrally for consistency and to hide session attribute name
	 */
	public void setBrowseNode(String node,HttpSession session) {
		session.setAttribute("browseNodeId" + getBrowseMode(session),node);
	}
	
	
	/**
	 * Place centrally for consistency and to hide session attribute name
	 */
	public Boolean getAdvancedSearchMode(HttpSession session) {
		Boolean res = (Boolean)session.getAttribute("searchMode" + getBrowseMode(session));
		if(res==null) res = Boolean.FALSE;
		return res;
	}
	
	/**
	 * Place centrally for consistency and convenience - gets default specified
	 * in media ResourceBundle if none set
	 */
	public GroupOrStem getCurrentGroupOrStem(GrouperSession s,HttpSession session) throws Exception{
		String node = getBrowseNode(session);
		
		if(node==null) {
			return GroupOrStem.findByStem(s,StemFinder.findRootStem(s));
		}	
		return GroupOrStem.findByID(s,node);
	} 
	
	
	
	/**
	 * Return configured stem - or the root stem if none configured
	 * @param session
	 * @return
	 */
	public  String getDefaultRootStemName(HttpSession session) { 
		
			Map mediaMap = (Map)session.getAttribute("mediaMap");
			String defaultStem = (String)mediaMap.get("default.browse.stem");
			if(isEmpty(defaultStem) || defaultStem.startsWith("@")) defaultStem = GrouperHelper.NS_ROOT;
			return defaultStem;	
	}
	
	/**
	 * Return a named cookie - so caling code doesn`t have to iterate through Cookie array
	 * @param name
	 * @param request
	 * @return
	 */
	public static Cookie getCookie(String name,HttpServletRequest request) {
		Cookie cookie = null;
		Cookie[] cookies=request.getCookies();
		if(cookies==null) return cookie;
		for(int i=0;i<cookies.length;i++) {
			if(cookies[i].getName().equals(name)) return cookies[i];
		}
		return cookie;
	}
	
	
	/**
	 * Functionality moved here from PopulateDebugPrefsAction so can be called
	 * from elsewhere
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static Map readDebugPrefs(HttpServletRequest request) throws Exception{
		String x = request.getRealPath(request.getServletPath());
		//Fix submitted by jaakko.linnosaari@tkk.fi to take care of my sloppy OS handling
		String s = File.separator;
		String prefsFile=x.substring(0,x.lastIndexOf(s)) + s + "WEB-INF" + s + "debugPrefs.obj";
		//String prefsFile=x.substring(0,x.lastIndexOf("\\")) + "\\WEB-INF\\debugPrefs.obj";
		File pFile = new File(prefsFile);
		Map prefs = null;
		HttpSession session = request.getSession();
		if(pFile.exists()) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(prefsFile));
			prefs = (Map)ois.readObject();
			ois.close();
		}else{
			prefs=new HashMap();
			prefs.put("isActive",Boolean.FALSE);
			prefs.put("i2miDir","");
			prefs.put("siteDir","");
			prefs.put("doShowResources",Boolean.TRUE);
			prefs.put("doShowResourcesInSitu",Boolean.FALSE);
			prefs.put("doHideStyles",Boolean.FALSE);
			prefs.put("doSaveOutput",Boolean.FALSE);
			prefs.put("outputDir","");
			prefs.put("doShowTilesHistory",Boolean.TRUE);
			prefs.put("JSPEditor","");
		}
		session.setAttribute("debugPrefs",prefs);
		
		return prefs;
		}
	
	/**
	 * Put here so not duplicated in Actions
	 */
	protected boolean processFlatForMode(String mode,HttpServletRequest request,HttpSession session) {
		
		String flat = request.getParameter("flat");
		Boolean isFlat = (Boolean)session.getAttribute("isFlat" + mode);
		if((flat==null || flat.length()==0)) {
			if(isFlat==null) isFlat=Boolean.FALSE; 
		}else isFlat=new Boolean("true".equals(flat));
		session.setAttribute("isFlat" + mode,isFlat);
		request.setAttribute("isFlat",isFlat);
		return isFlat.booleanValue();
	}
	
	protected RepositoryBrowser getRepositoryBrowser(GrouperSession s,HttpSession session) {
		String browseMode = getBrowseMode(session);
		RepositoryBrowser rb = RepositoryBrowserFactory.getInstance(browseMode,s,getMediaResources(session));
		return rb;
	}
	
	protected String getModulePrefix(HttpServletRequest request) {
		String prefix = (String)request.getAttribute("modulePrefix");
		if(prefix ==null) {
			ModuleConfig modConfig = (ModuleConfig)request.getAttribute("org.apache.struts.action.MODULE");
			if(modConfig!=null) prefix = modConfig.getPrefix();
		}
		if(prefix==null) prefix="";
		return prefix;
	}
	
	protected Map filterParameters(HttpServletRequest request,String prefix) {
		Map map = new HashMap();
		Enumeration names = request.getParameterNames();
		String name;
		while (names.hasMoreElements()) {
			name = (String)names.nextElement();
			if(name.startsWith(prefix)) map.put(name,request.getParameter(name));
		}
		return map;
	}
	
	protected boolean isWheelGroupMember(HttpSession session) {
		if(isEmpty(SessionInitialiser.getAuthUser(session))) return false;
		if(Boolean.TRUE.equals(session.getAttribute("isWheelGroupMember"))) return true;
		if(Boolean.FALSE.equals(session.getAttribute("isWheelGroupMember"))) return false;
		Object obj= session.getServletContext().getAttribute("wheelGroup");
		if(Boolean.FALSE.equals(obj)) {
			session.setAttribute("isWheelGroupMember",Boolean.FALSE);
			return false;
		}
		if(obj==null) {
			if("true".equals(GrouperConfig.getInstance().getProperty("groups.wheel.use"))) {
				try {
					Subject subj = SubjectFinder.findById("GrouperSystem");
					GrouperSession s = GrouperSession.start(subj);
					obj=GroupFinder.findByName(s,GrouperConfig.getInstance().getProperty("groups.wheel.group"));
					session.getServletContext().setAttribute("wheelGroup",obj);
				}catch(Exception e) {
					session.setAttribute("isWheelGroupMember",Boolean.TRUE);
					return false;
				}
			}
		}
		
		Group wheelGroup = (Group)obj;
		GrouperSession s  =SessionInitialiser.getGrouperSession(session);
		if(s !=null && obj!=null && wheelGroup.hasMember(s.getSubject())) {
			session.setAttribute("isWheelGroupMember",Boolean.TRUE);
			return true;
		}
		session.setAttribute("isWheelGroupMember",Boolean.FALSE);
		return false;
		
	}
	
	protected boolean isActiveWheelGroupMember(HttpSession session) {
		return Boolean.TRUE.equals(session.getAttribute("activeWheelGroupMember"));
	}
	
	protected void makeSavedGroupsAvailable(HttpServletRequest request) throws Exception{
		HttpSession session = request.getSession();
		List savedAsMaps =  new ArrayList();
		Set savedSubjects=getSavedSubjects(session);
		Subject subj;
		Group group;
		Iterator it = savedSubjects.iterator();
		GrouperSession grouperSession = (GrouperSession) session.getAttribute("edu.intenet2.middleware.grouper.ui.GrouperSession");
		
		while(it.hasNext()) {
			subj=(Subject)it.next();
			if("group".equals(subj.getType().getName())) {
				try {
					group=GroupFinder.findByUuid(grouperSession,subj.getId());
					savedAsMaps.add(GrouperHelper.group2Map(grouperSession,group));
				}catch(GroupNotFoundException e) {
					it.remove();
				}
			}
		}
		request.setAttribute("savedSubjects",new ArrayList(savedAsMaps));
		request.setAttribute("savedSubjectsSize",new Integer(savedAsMaps.size()));	
	}
	
	protected void makeSavedSubjectsAvailable(HttpServletRequest request) {
		HttpSession session = request.getSession();
		List savedAsMaps =  GrouperHelper.subjects2Maps(getSavedSubjects(session).toArray());
		request.setAttribute("savedSubjects",new ArrayList(savedAsMaps));
		request.setAttribute("savedSubjectsSize",new Integer(savedAsMaps.size()));	
	}
	
	protected void addSavedSubject(HttpSession session,Subject subj) {
		getSavedSubjects(session).add(subj);
	}
	
	protected void removeSavedSubject(HttpSession session,Subject subj) {
		Set saved = getSavedSubjects(session);
		Iterator it = saved.iterator();
		Subject savedSubj;
		while(it.hasNext()){
			savedSubj = (Subject)it.next();
			if(subj.getId().equals(savedSubj.getId())&& subj.getSource().equals(savedSubj.getSource())) it.remove();
		}
	}
	
	private Set getSavedSubjects(HttpSession session) {
		Set savedSubjects = (Set)session.getAttribute("savedSubjectsSet");
		if(savedSubjects==null) {
			savedSubjects = new LinkedHashSet();
			session.setAttribute("savedSubjectsSet",savedSubjects);
		}
		return savedSubjects;
	}
}


