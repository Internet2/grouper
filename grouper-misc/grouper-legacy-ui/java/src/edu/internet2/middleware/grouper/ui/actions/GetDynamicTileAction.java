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

import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
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
 * @version $Id: GetDynamicTileAction.java,v 1.3 2009-08-12 04:52:14 mchyzer Exp $
 */
public class GetDynamicTileAction extends LowLevelGrouperCapableAction {

  //------------------------------------------------------------ Action Methods

  public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response,
	  HttpSession session,GrouperSession grouperSession)
      throws Exception {
  		
  		//Get tiles attributes set using 'put' tags
  		Map tilesAttributes = getTilesAttributes(request); //from GrouperCapableAction
  		
  		//Get the ResourceBundle which contains key / values
  		//we will check in our template finding algorithm
  		ResourceBundle mediaResources = GrouperUiFilter.retrieveSessionMediaResourceBundle();
  		
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
