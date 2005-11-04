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

package edu.internet2.middleware.grouper.ui.actions;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;



import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.tiles.ComponentContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;

import edu.internet2.middleware.grouper.*;

/**
 * Superclass for GrouperCapableAction. This class is intended to be used by 
 * Tile controllers whereas GrouperCapabaleAction should be used bt top level Actions
 *  
 * <p />
 * 

 * 
 * @author Gary Brown.
 * @version $Id: LowLevelGrouperCapableAction.java,v 1.1 2005-11-04 14:58:35 isgwb Exp $
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
			
			GrouperSession grouperSession = (GrouperSession) request.getSession().getAttribute("edu.intenet2.middleware.grouper.ui.GrouperSession");
			HttpSession session = request.getSession();			
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
	public void setBrowseMode(String mode,HttpSession session) {
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
	public Group getCurrentGroupOrStem(GrouperSession s,HttpSession session) {
		String node = getBrowseNode(session);
		
		if(node==null) {
			try {
				
				String defaultStem = getDefaultRootStemName(session);
				Group defaultStemObj = GrouperStem.loadByName(s,defaultStem);
				return defaultStemObj;
			}catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
			
		try {
			GrouperGroup group = GrouperGroup.loadByID(s,node);
			return group;
		}catch(Exception e) {
			//@TODO when we have typed exceptions check error
		}
		GrouperStem stem = GrouperStem.loadByID(s,node);
		return stem;
	} 
	
	
	
	/**
	 * Return configured stem - or the root stem if none configured
	 * @param session
	 * @return
	 */
	public  String getDefaultRootStemName(HttpSession session) { 
		
			Map mediaMap = (Map)session.getAttribute("mediaMap");
			String defaultStem = (String)mediaMap.get("default.browse.stem");
			if(isEmpty(defaultStem) || defaultStem.startsWith("@")) defaultStem = Grouper.NS_ROOT;
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
		String prefsFile=x.substring(0,x.lastIndexOf("\\")) + "\\WEB-INF\\debugPrefs.obj";
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
	
}


