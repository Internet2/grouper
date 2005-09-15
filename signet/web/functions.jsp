<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: functions.jsp,v 1.19 2005-09-15 21:08:18 jvine Exp $
  $Date: 2005-09-15 21:08:18 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
-->

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta name="robots" content="noindex, nofollow" />
    <title>
      Signet
    </title>
    <link href="styles/signet.css" rel="stylesheet" type="text/css" />
    <script language="JavaScript" type="text/javascript" src="scripts/signet.js"></script>
  </head>

  <body>
    <script type="text/javascript">
      function selectFunction()
      {
        document.form1.continueButton.disabled=false;
        
        var selectedFunctionId
          = document.getElementById("functionSelectList").value;
          
        var categoryNameDivId = "CATEGORY_NAME:" + selectedFunctionId;
        var categoryName = document.getElementById(categoryNameDivId).innerHTML;
          
        var functionNameDivId = "FUNCTION_NAME:" + selectedFunctionId;
        var functionName = document.getElementById(functionNameDivId).innerHTML;
          
        var functionHelpTextDivId = "FUNCTION_HELPTEXT:" + selectedFunctionId;
        var functionHelpText
          = document.getElementById(functionHelpTextDivId).innerHTML;
        
        var categoryNameElement = document.getElementById("categoryName");
        var functionNameElement = document.getElementById("functionName");
        var functionDescriptionElement = document.getElementById("functionDescription");
        
        categoryNameElement.firstChild.nodeValue=categoryName + " : ";
        functionNameElement.firstChild.nodeValue=functionName;
        functionDescriptionElement.firstChild.nodeValue=functionHelpText;
      }
  </script>
  
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>

<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>

<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>
<%@ page import="edu.internet2.middleware.signet.ui.Common" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
  PrivilegedSubject loggedInPrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
   
   PrivilegedSubject currentGranteePrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute(Constants.GRANTEE_ATTRNAME));
         
   Subsystem currentSubsystem
     = (Subsystem)
         (request.getSession().getAttribute(Constants.SUBSYSTEM_ATTRNAME));
   
   Set grantableCategories
   	= loggedInPrivilegedSubject
   			.getGrantableCategories(currentSubsystem);
         
   DateFormat dateFormat = DateFormat.getDateInstance();
   
   String personViewHref
     = "PersonView.do?granteeSubjectTypeId="
       + currentGranteePrivilegedSubject.getSubjectTypeId()
       + "&granteeSubjectId="
       + currentGranteePrivilegedSubject.getSubjectId()
       + "&subsystemId="
       + currentSubsystem.getId();
%>

    <form name="form1" method="post" action="OrgBrowse.do">
      <tiles:insert page="/tiles/header.jsp" flush="true" />
        <div id="Navbar">
          <span class="logout">
            <%=Common.displayLogoutHref(request)%>
          </span> <!-- logout -->
          <span class="select">
            <a href="Start.do">
              Home
            </a>
            &gt; <!-- displays as text right-angle bracket -->
            <a href="<%=personViewHref%>">
						<%=currentGranteePrivilegedSubject.getName()%>
            </a>
            &gt; Grant new privilege
          </span> <!-- select -->
        </div> <!-- Navbar -->
    
      <div id="Layout"> 
        <div id="Content">
          <div id="ViewHead">
			<span class="dropback">Granting new privilege to</span>           	
			<h1><%=currentGranteePrivilegedSubject.getName()%></h1>
           	<span class="ident">
						<%=currentGranteePrivilegedSubject.getDescription()%></span><!--,	Technology Strategy and Support Operations-->
		  </div> 
          <!-- ViewHead -->
         
         <div class="section">
				 		<h2>New assignment details</h2>
							<table>
              	<tr>
              		<th width="15%" class="label" scope="row">Granted to:</td>
              		<td width="75%"><%=currentGranteePrivilegedSubject.getName()%></td>
              		<td width="10%">&nbsp;</td>
             		</tr>
              	<tr>
              		<th class="label" scope="row">Type:</td>
              		<td><%=currentSubsystem.getName()%></td>
              		<td nowrap="nowrap">
										<a href="<%=personViewHref%>">
		               	<img src="images/arrow_left.gif" alt="" />change
    			         	</a>
				  </td>
             		</tr>								
							</table>						
		  </div>
				 
				 	<div class="section">
					<h2>
           	Select privilege
         	</h2> 
						  <p><label for "scope">
							  Select a privilege, then click Continue.
							</label>
						  </p>
					  <select
               		name="functionSelectList"
               		size="10"
               		id="functionSelectList"
              		onchange="javascript:selectFunction();">
 	  <%
  Iterator grantableCategoriesIterator = grantableCategories.iterator();
  while (grantableCategoriesIterator.hasNext())
  {
    Category grantableCategory = (Category)(grantableCategoriesIterator.next());
 %>
                 	  <optgroup label="<%=grantableCategory.getName()%>">
 	  <%
    Set functions = loggedInPrivilegedSubject.getGrantableFunctions(grantableCategory);
    Iterator functionsIterator = functions.iterator();
    while (functionsIterator.hasNext())
    {
      Function function = (Function)(functionsIterator.next());
%>
                   	    <option value="<%=function.getId()%>">
                     	  <%=function.getName()%>
                   	    </option>
	  <%
    }
%>
                 	  </optgroup>
	  <%
  }
%>
             	              </select>
               	
       	              <!-- Now, let's loop again, to lay down a series of SPAN elements
               	     that will hold useful stuff like category name, function name,
               	     and function help-text.
               	-->
                      <%
  grantableCategoriesIterator = grantableCategories.iterator();
  while (grantableCategoriesIterator.hasNext())
  {
    Category grantableCategory = (Category)(grantableCategoriesIterator.next());
    Set functions = loggedInPrivilegedSubject.getGrantableFunctions(grantableCategory);
    Iterator functionsIterator = functions.iterator();
    while (functionsIterator.hasNext())
    {
      Function function = (Function)(functionsIterator.next());
%>
                      <div style="display:none" id="CATEGORY_NAME:<%=function.getId()%>">
                  <%=grantableCategory.getName()%>
                            </div>
                      <div style="display:none" id="FUNCTION_NAME:<%=function.getId()%>">
                  <%=function.getName()%>
                            </div>
                      <div style="display:none" id="FUNCTION_HELPTEXT:<%=function.getId()%>">
                  <%=function.getHelpText()%>
                            </div>
                      <%
    }
  }
%>
     
                      <div class="showdesc">
                  <span class="category" id="categoryName">
                    <!-- category name gets inserted by Javascript -->
                  </span> <!-- categoryName -->
                  <span class="function" id="functionName">
                    <!-- function name gets inserted by Javascript -->
                  </span> <!-- functionName -->
                  <p class="description" id="functionDescription">
                    <!-- function description gets inserted by Javascript -->
                  </p>
                            </div>
                      <!-- description -->
		              <div class="clear">&nbsp;</div>
		              <!--fix/hack for Safari display bug -->
		  </div>
				 	<!-- section -->
					
           <div class="section">
			<h2>
           	Continue to next step : Scope
         	</h2> 

             	<input
                  name="continueButton"
        		  		disabled="true"
                  type="submit"
                  class="button-def"
                  value="Continue &gt;&gt;" />
           	
           	<p>
             	<a href="<%=personViewHref%>">
               	<img src="images/arrow_left.gif" alt="" />CANCEL and return to <%=currentGranteePrivilegedSubject.getName()%>'s overview
             	</a>
           	</p>
       	  </div>	<!-- section -->
					
       	</div><!-- Content -->
        <div id="Sidebar">
          <div class="helpbox">
          	<h2>Help</h2>
          	...          
					</div>  
          <!-- end helpbox -->
        </div> <!-- Sidebar -->
        <tiles:insert page="/tiles/footer.jsp" flush="true" />
      </div> <!-- Layout -->
    </form>
  </body>
</html>
