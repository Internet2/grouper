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
import javax.servlet.jsp.jstl.fmt.LocalizationContext;



import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.tiles.ComponentContext;
import java.util.*;
import edu.internet2.middleware.grouper.*;

/**
 * Superclass for all Actions which need to do Grouper stuff. Other handy methods 
 * shared by virtue of being here, however, should refactor so that handy methods
 * are in a base class which is extended by GrouperCapableAction as some things 
 * done here only need to be done for top level actions. 
 * <p />
 * 
 <table width="75%" border="1">
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
    <td><font face="Arial, Helvetica, sans-serif">flat</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif"><em>true</em> indicates that 
      hierarchy should not be shown - just a list of groups</font></td>
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
 * @version $Id: GrouperCapableAction.java,v 1.1.1.1 2005-08-23 13:04:15 isgwb Exp $
 */

public abstract class GrouperCapableAction 
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
	/**
	 * Convenience method to simplify calling code
	 */
	public boolean isEmpty(Object obj) {
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
	public void setAdvancedSearchMode(boolean mode,HttpSession session) {
		session.setAttribute("searchMode" + getBrowseMode(session),new Boolean(mode));
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
	
	public String getDefaultRootStemName(HttpSession session) {
		
			Map mediaMap = (Map)session.getAttribute("mediaMap");
			String defaultStem = (String)mediaMap.get("default.browse.stem");
			if(isEmpty(defaultStem) || defaultStem.startsWith("@")) defaultStem = Grouper.NS_ROOT;
			return defaultStem;	
	}
}


