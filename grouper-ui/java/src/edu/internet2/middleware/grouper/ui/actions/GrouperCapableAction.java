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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.config.ActionConfig;
import java.util.*;

import edu.internet2.middleware.grouper.*;

/**
 * Superclass for all Actions which need to do Grouper stuff. Other handy methods 
 * shared by virtue of being here, however, should refactor so that handy methods
 * are in a base class which is extended by GrouperCapableAction as some things 
 * done here only need to be done for top level actions. 
 * <p />
 * <table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">pageSize</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used to reset the Session default. 
      If a Strut's DynaActionForm is present attempts to set the pageSize field 
      for it</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">advancedSearch</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used to reset the Session default 
      for current browseMode</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">callerPageId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">the pageId of the previous page 
      </font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">flat</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif"><em>true</em> indicates that 
      hierarchy should not be shown - just a list of groups</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">_reinstatePageId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif"> indicates that this page is 
      being restored - and so shouldn`t be saved again</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">loggedOut</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif"><em>true </em>indicates Session 
      has been invalidated and should not be used</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">isAdvancedSearch</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif"><em>Takes </em>the Session default 
      and makes it available to Request</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">isFlat</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif"><em>true </em>indicates that 
      hierarchy should not be shown - just a list of groups</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">thisPageId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">the page id used to look up 
      the saved data for this page (assuming it was saved)</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">default.pagesize</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Set if pageSize request parameter 
      set, or read for default otherwise</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">nav, media</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used to obtain LocalizationContext 
      of that name from which ResourceBundle can be retrieved</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">isFlat&lt;Mode&gt;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Set if flat request parameter 
      set, or read for default otherwise</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseMode</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Methods present to maintain 
      state</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">browseNodeId&lt;browseMode&gt;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Methods present to maintain 
      state</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">searchMode&lt;browseMode&gt;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Methods present to maintain 
      state</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">mediaMap</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Used to obtain default.browse.stem 
      value from media RresourceBundle</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Strut's Action Parameter</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
</table>
 
 * 
 * @author Gary Brown.
 * @version $Id: GrouperCapableAction.java,v 1.2 2005-11-04 12:23:39 isgwb Exp $
 */

public abstract class GrouperCapableAction 
	extends LowLevelGrouperCapableAction {
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
			//if(grouperSession == null && !"/populateIndex.do".equals(request.getServletPath())) return mapping.findForward("Index");
			HttpSession session = request.getSession();
			String pageSize = request.getParameter("pageSize");
			if(pageSize !=null && pageSize.length() > 0) {
				session.setAttribute("default.pagesize",pageSize);
			}
			
			DynaActionForm dummyForm = (DynaActionForm)form;
			if(dummyForm != null) {
				try {
					dummyForm.set("pageSize","" + getPageSize(session));
				}catch(Exception e) {
					//Ok so form doesn't care about pageSize
					//let's just ignore it
				}
			}
			if(form!=null)request.setAttribute("grouperForm",form);
			Date before = new Date();
			ActionForward forward =  grouperExecute(mapping,form,request,response,session,grouperSession);
			Date after = new Date();
			long diff = after.getTime()-before.getTime();
			String url = request.getServletPath();
			Long ms = (Long)request.getAttribute("timingsMS");
			long mss=0;
			if(ms !=null) mss=ms.longValue();
			if(diff > 25) {
				request.setAttribute("timingsClass",this.getClass().getName());
				request.setAttribute("timingsMS",new Long(diff+mss));
			}
			if(Boolean.TRUE.equals(request.getAttribute("loggedOut"))) {
				return forward;
			}
			String advSearch = request.getParameter("advancedSearch");
			try {
				session.getAttribute("");
			}catch(Exception e) {
				return forward;
			}
			if(!isEmpty(advSearch)) {
				setAdvancedSearchMode("true".equals(advSearch),session);
			}
			request.setAttribute("isAdvancedSearch",getAdvancedSearchMode(session));
			return forward;
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
	public void setAdvancedSearchMode(boolean mode,HttpSession session) {
		session.setAttribute("searchMode" + getBrowseMode(session),new Boolean(mode));
	}
	
	
	
	
	
	/**
	 * Saves a DynaActionForm in the session so it can be restored later. Used
	 * so that all parameters don`t have to be passed through complex chains
	 * of pages
	 * @param session
	 * @param form
	 * @param name
	 */
	public static void saveDynaFormBean(HttpSession session,DynaActionForm form,String name) {
		Map map = new HashMap(form.getMap());
		session.setAttribute(name,map);
	}
	
	/**
	 * Restores previously saved DynaFormBean
	 * @param session
	 * @param form
	 * @param name
	 */
	public static void restoreDynaFormBean(HttpSession session,DynaActionForm form,String name) {
		Map map = (Map)	session.getAttribute(name);
		if(map==null) return;
		Map.Entry entry;
		Iterator it = map.entrySet().iterator();
		while(it.hasNext()) {
			entry = (Map.Entry)it.next();
			form.set((String)entry.getKey(),entry.getValue());	
		}
	}
	
	/**
	 * Saves the current page so it can be restored. This signature doesn`t save
	 * any session attributes
	 * @param request
	 * @param form
	 * @return
	 */
	public static String saveAsCallerPage(HttpServletRequest request,DynaActionForm form){
		return saveAsCallerPage(request,form);
	}
	
	/**
	 * Saves the current page so it can be restored. Saves specified session attributes
	 * @param request
	 * @param form
	 * @param sessionKeepers - splt on spaces to get attribute names
	 */
	public static void saveAsCallerPage(HttpServletRequest request,DynaActionForm form,String sessionKeepers){
		
		//Dont`s save again if we are restoring this page + retrieve previously
		//saved pageId
		if(!isEmpty(request.getParameter("_reinstatePageId"))) {
			Map[] savedData = getCallerPageData(request ,request.getParameter("_reinstatePageId"));
			request.setAttribute("thisPageId",savedData[0].get("_thisPageId"));
			
			return;
		}
		//Generate a unique page id for this session
		int rnd = (int)(Math.random() * 1000);
		String id = (new Date()).getTime() + "-" + rnd;
		
		
		HttpSession session = request.getSession();
		
		//Retrieve (or create if necessary) Map to store pages
		Map callerPageHistory=(Map)session.getAttribute("callerPageHistory");
		if(callerPageHistory==null) {
			callerPageHistory=new HashMap();
			session.setAttribute("callerPageHistory",callerPageHistory);
		}
		
		//Get ActionConfig to determine the current path - so we can return to it
		ActionConfig ac= (ActionConfig)request.getAttribute("org.apache.struts.action.mapping.instance");
		Map[] data = new Map[2];
		//Save request parameters
		data[0] = new HashMap(request.getParameterMap());
		//save path
		data[0].put("_callerPagePath",ac.getPath());
		
		//Save session variables
		data[1]=new HashMap();
		StringTokenizer st = new StringTokenizer(sessionKeepers);
		String key;
		while(st.hasMoreTokens()) {
			key = st.nextToken();
			data[1].put(key,session.getAttribute(key));
		}
		
		//put the data in the session
		callerPageHistory.put(id,data);
		
		//Make the page id available for templates
		request.setAttribute("thisPageId",id);
		
		//Store page id in session
		data[0].put("_thisPageId",id);
		
		
		
	}
	
	/**
	 * Wipe out saved pages - should probably be called when changing browse mode
	 * @param request
	 */
	public static void clearCallerPageHistory(HttpServletRequest request) {
		Map callerPageHistory = (Map)request.getSession().getAttribute("callerPageHistory");
		if(callerPageHistory!=null)callerPageHistory.clear();
	}
	
	
	/**
	 * Retrieve actual data which was saved - called by Filter. Chose to keep
	 * all logic here for saving and retrieving data
	 * @param request
	 * @param id
	 * @return
	 * @throws IllegalStateException
	 */
	public static Map[] getCallerPageData(HttpServletRequest request ,String id) throws IllegalStateException {
		Map callerPageHistory = (Map)request.getSession().getAttribute("callerPageHistory");
		if(callerPageHistory==null || !callerPageHistory.containsKey(id)) throw new IllegalStateException("No caller page data for ID=" + id);
		return (Map[]) callerPageHistory.get(id);
	}
	
	
	/**
	 * Abstract away the logic Actions should use to determine if they should
	 * be returning a user to a previous page
	 * @param form
	 * @return
	 */
	boolean doRedirectToCaller(DynaActionForm form) {
		String callerPageId = (String) form.get("callerPageId");
		if(isEmpty(callerPageId)) return false;
	
	return true;
	}
	
	
	/**
	 * Provides Struts with means to send the user to a saved page
	 * @param form
	 * @return
	 */
	ActionForward redirectToCaller(DynaActionForm form) {
		String callerPageId = (String) form.get("callerPageId");
		if(isEmpty(callerPageId)) throw new IllegalStateException("No caller page id");
		return new ActionForward("/gotoCallerPage?pageId=" + callerPageId,true);
	
	}
	
	
}


