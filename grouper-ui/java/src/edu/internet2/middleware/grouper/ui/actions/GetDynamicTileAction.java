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
 */package edu.internet2.middleware.grouper.ui.actions;

import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ui.TemplateResolver;
import edu.internet2.middleware.grouper.ui.TemplateResolverFactory;
import edu.internet2.middleware.grouper.ui.UIThreadLocal;

/**
 * Low level Strut's action which is used as a Controller for a Tile. 
 * Determines correct JSP template based on current tiles attributes
 * which specify a 'view' of an 'object' to render.
 * 
 * Institutions could extend / replace this controller to use a different 
 * algorithm to determine a JSP file name, or to actually generate HTML code
 * which would be added to the page output
 * 
 * The algorithm used here is based on a pre-defined set of key lookups
 * which try to match potential site specific configuration keys, or failing that, 
 * the application default.
 * 
 * The approach actually coded here is a first attempt and would benefit from refactoring,
 * particularly with a view to sharing a controller with Signet 
 * i.e. extending GrouperCapableAction is not very generic!
 * 
 * Suggestions on actual keys chosen welcome.
 * <p />
 <table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Tile Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">viewObject</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The object instance to render</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">view</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The name of the view to render 
      for viewObject</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">dynamicTemplate</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Path of JSP to use to render 
      viewObject </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">dynamicObjectType</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The notional object type of 
      viewObject </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">dynamicTemplateKey</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">The actual key selected by the 
      TemplateResolver</font></td>
  </tr>
</table> 
 * @author Gary Brown.
 * @version $Id: GetDynamicTileAction.java,v 1.1.1.1 2005-08-23 13:04:14 isgwb Exp $
 */
public class GetDynamicTileAction extends GrouperCapableAction {

  //------------------------------------------------------------ Action Methods

  public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response,
	  HttpSession session,GrouperSession grouperSession)
      throws Exception {
  		
  		//Get tiles attributes set using 'put' tags
  		Map tilesAttributes = getTilesAttributes(request); //from GrouperCapableAction
  		
  		//Get the ResourceBundle which contains key / values
  		//we will check in our template finding algorithm
  		ResourceBundle mediaResources = getMediaResources(request); //from GrouperCapableAction
  		
  		//Get the name of th eview and then the actual object we
  		//are showing a view of
  		String view = (String)tilesAttributes.get("view");
  		Object object = tilesAttributes.get("viewObject");
  		
  		//if mediaResources has key - edu.internet2.middleware.grouper.ui.TemplateResolver
  		//Attempts to instantiate Class from value, otherwise uses
  		//edu.internet2.middleware.grouper.ui.DefaultTemplateResolverImpl
  		TemplateResolver resolver = TemplateResolverFactory.getTemplateResolver(mediaResources);
  		String objType = resolver.getObjectType(object);
  		String templateName = resolver.getTemplateName(object,view,mediaResources,request);
  		Object dynamicTemplateKey = UIThreadLocal.get("lastDynamicTemplateKey");
  		if(isEmpty(templateName)) dynamicTemplateKey = "";
  		
  		
  		
  		//This is not ideal. Nested tiles are writing to a global
  		//request / session space. 
  		request.setAttribute("dynamicTemplate",templateName);
  		request.setAttribute("dynamicObjectType",objType);
  		request.setAttribute("dynamicTemplateKey",dynamicTemplateKey);
  		//request.setAttribute("viewObject",object);
	
	return null;
  }
  
//Here so we don't catch MissingRrsource exceptions in code and can be sure of null or a value
  protected String getResource(ResourceBundle bundle,String key) {
  	String val = null;
  	try {
  		val = bundle.getString(key);
  		if("".equals(val)) val=null;
  		
  	}catch(Exception e) {
  		
  	}
  	if(val!=null) UIThreadLocal.put("lastDynamicTemplateKey",key);
  	return val;
  }
  
  
  
}