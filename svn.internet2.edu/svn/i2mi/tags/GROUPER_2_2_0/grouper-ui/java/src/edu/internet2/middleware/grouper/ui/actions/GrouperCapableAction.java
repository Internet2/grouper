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

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.upload.MultipartRequestWrapper;

import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.ui.CallerPageException;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.Message;
import edu.internet2.middleware.grouper.ui.UIThreadLocal;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;
import edu.internet2.middleware.grouper.ui.util.NavExceptionHelper;

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
 * @version $Id: GrouperCapableAction.java,v 1.23 2009-08-12 04:52:14 mchyzer Exp $
 */

public abstract class GrouperCapableAction 
	extends LowLevelGrouperCapableAction {
	protected static Log LOG = LogFactory.getLog(GrouperCapableAction.class);
	
	public static final String HIER_DELIM = GrouperHelper.HIER_DELIM; 
	/**
	 * Action specific - must be implemented by all subclasses
	 */
	public abstract ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
		      HttpServletRequest request, HttpServletResponse response,
			  HttpSession session, GrouperSession grouperSession) throws Exception;
	
	
	/**
	 * Transaction support implemented centrally here. the execute method calls this rather than grouperExecute
	 * as used to be the case.
	 */
	public  ActionForward grouperTransactionExecute(final ActionMapping mapping, final ActionForm form,
		      final HttpServletRequest request, final HttpServletResponse response,
			  final HttpSession session, final GrouperSession grouperSession) throws Exception {
		
			return (ActionForward)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() { 
				public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException {
					try{
						return grouperExecute(mapping,form,request,response,session,grouperSession);
					}catch(Exception e) {
						throw new GrouperDAOException(e);
					}
				}});
		
	}
	
	/**
	 * Makes HttpSession and GrouperSession available to subclasses
	 * Also handles pageSize parameter and times how long is spent in an action
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
		      HttpServletRequest request, HttpServletResponse response) throws Exception{
			GrouperSession grouperSession = (GrouperSession) request.getSession().getAttribute("edu.intenet2.middleware.grouper.ui.GrouperSession");
			//if(grouperSession == null && !"/populateIndex.do".equals(request.getServletPath())) return mapping.findForward("Index");
			
			ModuleConfig modConfig = (ModuleConfig)request.getAttribute("org.apache.struts.action.MODULE");
			String modulePrefix = modConfig.getPrefix();
			if(modulePrefix==null) modulePrefix="";
			request.setAttribute("modulePrefix",modulePrefix);
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
			isWheelGroupMember(session);
			String wheelGroupAction = request.getParameter("wheelGroupAction");
			if(!isEmpty(wheelGroupAction)) doWheelGroupStuff(wheelGroupAction,session);
			UIThreadLocal.replace("isActiveWheelGroupMember",new Boolean(isActiveWheelGroupMember(session)));
			
	    if (grouperSession != null) {
	      if (isWheelGroupMember(session)) {
	        grouperSession.setConsiderIfWheelMember(isActiveWheelGroupMember(session));
	      } else {
	        // we'll set this back to the default
	        grouperSession.setConsiderIfWheelMember(true);
	      }
	    }
			
			if(form!=null)request.setAttribute("grouperForm",form);
			Object sessionMessage = session.getAttribute("sessionMessage");
			if(isEmpty(request.getAttribute("message")) && !isEmpty(sessionMessage)) {
				request.setAttribute("message",sessionMessage);
				session.removeAttribute("sessionMessage");
			}
			request.setAttribute("linkBrowseMode",getLinkBrowseMode(session));
			Date before = new Date();
			ActionForward forward =  null;
			try {
				if(isEmpty(wheelGroupAction)) {
					
						forward=grouperTransactionExecute(mapping,form,request,response,session,grouperSession);
					
				}else forward = new ActionForward(GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("admin.browse.path"),true);
			}catch(GrouperDAOException e) {
				Throwable cause=e.getCause();
				
				Throwable causeCause = cause == null ? null : cause.getCause();
				
        Throwable causeCauseCause = causeCause == null ? null : causeCause.getCause();
        
				HookVeto hookVeto = (cause instanceof HookVeto) ? (HookVeto)cause : null;
				
				hookVeto = ((hookVeto == null) && (causeCause instanceof HookVeto)) ? (HookVeto)causeCause : hookVeto;
				
        hookVeto = ((hookVeto == null) && (causeCauseCause instanceof HookVeto)) ? (HookVeto)causeCauseCause : hookVeto;

        if (hookVeto != null) {
          Message.addVetoMessageToScreen(request, hookVeto);
        } else if(!(cause instanceof UnrecoverableErrorException)) {
					LOG.error(NavExceptionHelper.toLog(cause));
					cause=new UnrecoverableErrorException(cause);
				}
        if (cause instanceof UnrecoverableErrorException) {
  				NavExceptionHelper neh=getExceptionHelper(session);
  				String msg = neh.getMessage((UnrecoverableErrorException)cause);
  				request.setAttribute("seriousError",msg);
        }
				forward=mapping.findForward("ErrorPage");
			}
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
			if(forward != null && forward.getRedirect()&& !isEmpty(request.getAttribute("message"))) {
				try {
				session.setAttribute("sessionMessage",request.getAttribute("message"));
				}catch(IllegalStateException e){}
			}
			
			if(Boolean.TRUE.equals(request.getAttribute("loggedOut"))) {
				return forward;
			}
			try {
				GrouperHelper.fixSessionFields((Map)session.getAttribute("fieldList"));
			}catch(SchemaException e) {
				LOG.error(e);
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
	 */
	public static void saveAsCallerPage(HttpServletRequest request,DynaActionForm form){
		 GrouperCapableAction.saveAsCallerPage(request,form,"");
	}
	
	/**
	 * Saves the current page so it can be restored. Saves specified session attributes
	 * @param request
	 * @param form
	 * @param sessionKeepers - splt on spaces to get attribute names
	 */
	public static void saveAsCallerPage(HttpServletRequest request,DynaActionForm form,String sessionKeepers){
		
		//If we have already got a thisPageId don't overwrite it
		if(request.getAttribute("thisPageId")!=null) return;
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
		Map reqMap = request.getParameterMap();
		if(reqMap==null) {
			if(request instanceof MultipartRequestWrapper) {
				reqMap = new HashMap();

				for (Enumeration<String> e = request.getParameterNames() ; e.hasMoreElements() ;) {
			         String name = e.nextElement();
			         reqMap.put(name, request.getParameter(name));
			     }
			}else{
				reqMap = new HashMap();
			}
			
		}
		data[0] = new HashMap(reqMap);
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
	
	protected void augmentCallerPageRequest(HttpServletRequest request, String name, String value) {
		String id = (String)request.getAttribute("thisPageId");
		if(id==null) return;
		Map callerPageHistory=(Map)request.getSession().getAttribute("callerPageHistory");
		Map[] data = (Map[])callerPageHistory.get(id);
		if(data==null) return;
		data[0].put(name, value);
		
		
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
	 * TODO find away of purging stuff *when* it is reasonable to do so
	 * @param request
	 * @param id
	 * @return Map containing details of previous pages visited
	 * @throws IllegalStateException
	 */
	public static Map[] getCallerPageData(HttpServletRequest request ,String id)  {
		Map callerPageHistory = (Map)request.getSession().getAttribute("callerPageHistory");
		if(callerPageHistory==null || !callerPageHistory.containsKey(id)) {
			CallerPageException e= new CallerPageException("No caller page data for ID=" + id);
			throw (CallerPageException)NavExceptionHelper.fillInStacktrace(e);
		}
		return (Map[]) callerPageHistory.get(id);
	}
	
	
	/**
	 * Abstract away the logic Actions should use to determine if they should
	 * be returning a user to a previous page
	 * @param form
	 * @return whether a user should be returned to a previous page
	 */
	boolean doRedirectToCaller(DynaActionForm form) {
		String callerPageId = (String) form.get("callerPageId");
		if(isEmpty(callerPageId)) return false;
	
	return true;
	}
	
	
	/**
	 * Provides Struts with means to send the user to a saved page
	 * @param form
	 * @return an ActionForward which will return the user to a saved page
	 */
	ActionForward redirectToCaller(DynaActionForm form) {
		String callerPageId = (String) form.get("callerPageId");
		if(isEmpty(callerPageId)) throw new IllegalStateException("No caller page id");
		return new ActionForward("/gotoCallerPage?pageId=" + callerPageId,true);
	}
	
	protected void doWheelGroupStuff(String action,HttpSession session) {
		if(!isWheelGroupMember(session)) return;
		boolean activeWheelGroupMember = "toAdmin".equals(action);
    session.setAttribute("activeWheelGroupMember",activeWheelGroupMember);

		//Ensure the menu is recalculated if switching admin mode
		session.removeAttribute("cachedMenu");
	}	
	
	
}


