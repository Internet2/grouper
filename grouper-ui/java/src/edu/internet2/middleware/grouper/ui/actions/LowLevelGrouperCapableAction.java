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
Copyright 2004-2008 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2008 The University Of Bristol

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
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.tiles.ComponentContext;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.GrouperComparator;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.RepositoryBrowser;
import edu.internet2.middleware.grouper.ui.RepositoryBrowserFactory;
import edu.internet2.middleware.grouper.ui.SessionInitialiser;
import edu.internet2.middleware.grouper.ui.UIThreadLocal;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Superclass for GrouperCapableAction. This class is intended to be used by 
 * Tile controllers whereas GrouperCapabaleAction should be used bt top level Actions
 *  
 * <p />
 * 

 * 
 * @author Gary Brown.
 * @version $Id: LowLevelGrouperCapableAction.java,v 1.25 2009-08-12 04:52:14 mchyzer Exp $
 */

/**
 * @author isgwb
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class LowLevelGrouperCapableAction 
	extends org.apache.struts.action.Action {
	protected static Log LOG = LogFactory.getLog(LowLevelGrouperCapableAction.class);
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
	  
	   boolean needsThreadLocalInit = GrouperUiFilter.retrieveHttpServletRequest() == null;
	    if (needsThreadLocalInit) {
	      request = new GrouperRequestWrapper(request);
        GrouperUiFilter.initRequest((GrouperRequestWrapper)request, response);
	    }
	    try {
	  
  			HttpSession session = request.getSession();
  			GrouperSession grouperSession = (GrouperSession) session.getAttribute("edu.intenet2.middleware.grouper.ui.GrouperSession");
  						
  			DynaActionForm dummyForm = (DynaActionForm)form;
  			if(form!=null)request.setAttribute("grouperForm",form);
  			//tell grouper session if we are in admin mode or user mode
  	    Boolean originalConsiderIfWheelMember = grouperSession == null ? null : grouperSession.isConsiderIfWheelMember();
  			
  	    //are we acting as self or wheel?
  	    if (grouperSession != null) {
  	      
	        if (isWheelGroupMember(session)) {
	          grouperSession.setConsiderIfWheelMember(Boolean.TRUE.equals(UIThreadLocal.get("isActiveWheelGroupMember")));
	        } else {
	          // we'll set this back to the default
	          grouperSession.setConsiderIfWheelMember(true);
	        }
  	      
  	    }	    
  	    try {
  			ActionForward forward =  grouperExecute(mapping,form,request,response,session,grouperSession);
  			
  			return forward;
  	    } finally {
  	      if (grouperSession != null && originalConsiderIfWheelMember != null) {
  	        grouperSession.setConsiderIfWheelMember(originalConsiderIfWheelMember);
  	}
  	    }
	    } finally {
	      if (needsThreadLocalInit) {
	        GrouperUiFilter.finallyRequest();
	      }
	    }
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
			ResourceBundle mediaBundle = GrouperUiFilter.retrieveSessionMediaResourceBundle();
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
	public String getLinkBrowseMode(HttpSession session) {
		String browseMode = (String) session.getAttribute(
		"browseMode");
		if (isEmpty(browseMode ))
			browseMode = "My";
		return browseMode;
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
		try {
			return GroupOrStem.findByID(s,node);
		}catch (Exception e) {
			LOG.error(e);
			throw new UnrecoverableErrorException("error.browse.bad-current-id",e,node);
		}
	} 
	
	
	
	/**
	 * Return configured stem - or the root stem if none configured
	 * @param session
	 * @return name of the default root stem
	 */
	public  String getDefaultRootStemName(HttpSession session) { 
		
			Map mediaMap = (Map)session.getAttribute("mediaNullMap");
			String defaultStem = null;
      try {
        defaultStem = (String)mediaMap.get("default.browse.stem");
      } catch (MissingResourceException mre) {
        //thats ok, just ignore
      }

			if(isEmpty(defaultStem) || defaultStem.startsWith("@")) defaultStem = GrouperHelper.NS_ROOT;
			return defaultStem;	
	}
	
	/**
	 * Return a named cookie - so caling code doesn`t have to iterate through Cookie array
	 * @param name
	 * @param request
	 * @return Cookie with specified name
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
	 * @return Map representing saved preferences
	 * @throws Exception
	 */
	public static Map readDebugPrefs(HttpServletRequest request) throws Exception{
		
		String prefsFile=getDebugPrefsFileName(request);
		
		File pFile = null;
		if(prefsFile!=null) pFile=new File(prefsFile);
		Map prefs = null;
		HttpSession session = request.getSession();
		if(pFile!=null && pFile.exists()) {
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
	
	    public boolean saveDebugPrefs(Map map,HttpServletRequest request) throws Exception{
	    	
			String prefsFile=getDebugPrefsFileName(request);
			if(prefsFile==null) return false;
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(prefsFile));
			oos.writeObject(map);
			oos.close();
			return true;
	    }
	    
	    private static String getDebugPrefsFileName(HttpServletRequest request) {
	    	String val = GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("debug.prefs.dir");
	    	File file = new File(val);
	    	if(file.exists() && file.isDirectory()) {
	    		return val + File.separator + "debugprefs.obj";
	    		
	    	}else{
	    		return null;
	    	}
	    	
	    }
	
	/**
	 * Put here so not duplicated in Actions
	 * @return whether flat mode is on or not
	 */
	protected boolean processFlatForMode(String mode,HttpServletRequest request,HttpSession session) {
		GrouperSession s = SessionInitialiser.getGrouperSession(session);
		RepositoryBrowser rb = getRepositoryBrowser(s, session);
		if(!rb.isFlatCapable()) {
			request.setAttribute("isFlat",Boolean.FALSE);
			return false;
		}
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
		RepositoryBrowser rb = RepositoryBrowserFactory.getInstance(browseMode,s,GrouperUiFilter.retrieveSessionNavResourceBundle(),GrouperUiFilter.retrieveSessionMediaResourceBundle());
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
			if("true".equals(GrouperConfig.getProperty("groups.wheel.use"))) {
				try {
					Subject subj = SubjectFinder.findById("GrouperSystem", true);
					GrouperSession s = GrouperSession.start(subj);
					obj=GroupFinder.findByName(s,GrouperConfig.getProperty("groups.wheel.group"), true);
					session.getServletContext().setAttribute("wheelGroup",obj);
				}catch(Exception e) {
					session.setAttribute("isWheelGroupMember",Boolean.FALSE);
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
					group=GroupFinder.findByUuid(grouperSession,subj.getId(), true);
					savedAsMaps.add(GrouperHelper.group2Map(grouperSession,group));
				}catch(GroupNotFoundException e) {
					it.remove();
				}
			}
		}
		String sortContext="search";
		String field = (String)session.getAttribute("groupSearchResultField");
		if(field !=null) {
			sortContext="search:" + field;
		}
		savedAsMaps=sort(savedAsMaps,request,sortContext, -1, null);
		request.setAttribute("savedSubjects",new ArrayList(savedAsMaps));
		request.setAttribute("savedSubjectsSize",new Integer(savedAsMaps.size()));	
	}
	
	protected void makeSavedSubjectsAvailable(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String field = (String)session.getAttribute("groupSearchResultField");
		String sortContext="search";
		if(field !=null) {
			sortContext="search:" + field;
		}
		List savedAsMaps =  GrouperHelper.subjects2Maps(getSavedSubjects(session).toArray());
		savedAsMaps=sort(savedAsMaps,request,sortContext, -1, null);
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
	
	
	protected void makeSavedStemsAvailable(HttpServletRequest request) throws Exception{
		HttpSession session = request.getSession();
		List savedAsMaps =  new ArrayList();
		Set savedStems=getSavedStems(session);
		Stem stem;
		Iterator it = savedStems.iterator();
		GrouperSession grouperSession = (GrouperSession) session.getAttribute("edu.intenet2.middleware.grouper.ui.GrouperSession");
		
		while(it.hasNext()) {
			stem=(Stem)it.next();
				try {
					stem=StemFinder.findByUuid(grouperSession,stem.getUuid(), true);
					savedAsMaps.add(GrouperHelper.stem2Map(grouperSession,stem));
				}catch(StemNotFoundException e) {
					it.remove();
				}
		}
		String sortContext="search";
		String field = (String)session.getAttribute("stemSearchResultField");
		if(field !=null) {
			sortContext="search:" + field;
		}
		savedAsMaps=sort(savedAsMaps,request,sortContext, -1, null);
		request.setAttribute("savedStems",new ArrayList(savedAsMaps));
		request.setAttribute("savedStemsSize",new Integer(savedAsMaps.size()));	
	}
	

	
	protected void addSavedStem(HttpSession session,Stem stem) {
		getSavedStems(session).add(stem);
	}
	
	protected void removeSavedStem(HttpSession session,Stem stem) {
		Set saved = getSavedStems(session);
		Iterator it = saved.iterator();
		Stem savedStem;
		while(it.hasNext()){
			savedStem = (Stem)it.next();
			if(stem.getUuid().equals(savedStem.getUuid())) it.remove();
		}
	}
	
	protected Set getSavedStems(HttpSession session) {
		Set savedStems = (Set)session.getAttribute("savedStemsSet");
		if(savedStems==null) {
			savedStems = new LinkedHashSet();
			session.setAttribute("savedStemsSet",savedStems);
		}
		return savedStems;
	}
	
	
	/**
	 * This method assumes that there is a HttpSession attribute, 'GrouperComparator',
	 * which is an implementation of the {@link GrouperComparator} interface. If not present
	 * this method will instantiate an implementation instance using the value for the
	 * media.properties key 'comparator.impl'. The media.properties key 'comparator.sort.limit'
	 * defines the maximum input size that will be sorted. If the input Collection is larger
	 * than this limit, it is effectively returned as is, though if the Collection was not a List
	 * it will be returned as an ArrayList.
	 * @see edu.internet2.middleware.grouper.ui.DefaultComparatorImpl
	 * @param input the Collection to sort
	 * @param request the current request object
	 * @param context the context in which sorting is taking place
	 * @param collectionSize is the total size of the collection, or -1 if the collection is the entire thing
	 * @param searchTerm is the term to search for or null if not applicable
	 * @return the input Collection as a sorted List
	 */
	public static List sort(Collection input,HttpServletRequest request,
	    String context, int collectionSize, String searchTerm) {
		HttpSession session = request.getSession();
		GrouperComparator gc = (GrouperComparator)session.getAttribute("GrouperComparator");
		ResourceBundle config = GrouperUiFilter.retrieveSessionMediaResourceBundle();
		String maxStr=config.getString("comparator.sort.limit");
		int max=Integer.parseInt(maxStr);
		if(gc==null) {
			String comparatorClass = null;
			try {
				comparatorClass=config.getString("comparator.impl");
				
			}catch(Exception e){}
			if(comparatorClass==null) comparatorClass="edu.internet2.middleware.grouper.ui.DefaultComparatorImpl";
			try {
				gc = (GrouperComparator)Class.forName(comparatorClass).newInstance();
			}catch(Exception e) {
				throw new IllegalStateException("Cannot create " + comparatorClass + " instance");
			}
			session.setAttribute("GrouperComparator",gc);
		}
		gc.setContext(context);
		
		List toSort=null;
		if(input instanceof List) {
			toSort=(List)input;
		}else{
			toSort=new ArrayList(input);
		}
		int toSortSize = collectionSize == -1 ? toSort.size() : collectionSize;
    if(toSortSize<=max) Collections.sort(toSort,gc);
    
    //we need to bring important matches to the top
    boolean isSubjects = true;
    for (Object item : toSort) {
      if (!(item instanceof Subject)) {
        isSubjects = false;
      }
    }
    if (isSubjects) {
      Set<Subject> subjectsOut = SubjectHelper.sortSetForSearch(toSort, searchTerm);
      //avoid a null pointer...
      toSort = GrouperUtil.length(subjectsOut) > 0 ? new ArrayList<Subject>(subjectsOut) : toSort;
    }
		return toSort;
	}
	
	public static NavExceptionHelper getExceptionHelper(HttpSession session) {
		NavExceptionHelper neh = (NavExceptionHelper)session.getAttribute("navExceptionHelper");
		if(neh==null) {
			neh=new NavExceptionHelper();
			session.setAttribute("navExceptionHelper", neh);
		}
		return neh;
	}
	
	protected void addMessage(Message msg,HttpServletRequest request) {
		List messages = (List)request.getAttribute("messages");
		Message m = (Message)request.getAttribute("message");
		if(messages==null) {
			messages=new ArrayList();
			if(m!=null) messages.add(m);
			request.setAttribute("messages",messages);
		}
		messages.add(msg);
	}
}


