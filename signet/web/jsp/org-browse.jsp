<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: org-browse.jsp,v 1.3 2006-05-16 17:37:35 ddonn Exp $
  $Date: 2006-05-16 17:37:35 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
-->

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <meta name="robots" content="noindex, nofollow">
    <title>
      <%=ResLoaderUI.getString("signet.title") %>
    </title>
    <link href="styles/signet.css" rel="stylesheet" type="text/css">
    <script language="JavaScript" type="text/javascript" src="scripts/signet.js">
    </script>
    </head>

  <body>
  
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>

<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.Signet" %>

<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>

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
         (request.getSession().getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME));
         
   Subsystem currentSubsystem
     = (Subsystem)
         (request.getSession().getAttribute(Constants.SUBSYSTEM_ATTRNAME));
         
   Category currentCategory
     = (Category)
         (request.getSession().getAttribute("currentCategory"));
         
   Function currentFunction
     = (Function)
         (request.getSession().getAttribute("currentFunction"));
         
   DateFormat dateFormat = DateFormat.getDateInstance();
   
   String personViewHref
     = "PersonView.do?granteeSubjectTypeId="
       + currentGranteePrivilegedSubject.getSubjectTypeId()
       + "&granteeSubjectId="
       + currentGranteePrivilegedSubject.getSubjectId()
       + "&subsystemId="
       + currentSubsystem.getId();
       
   String functionsHref
     = "Functions.do?select="
       + currentSubsystem.getId();
%>

    <form name="form1" action="Conditions.do">  
      <tiles:insert page="/tiles/header.jsp" flush="true" />
			 <div id="Navbar">
        <span class="logout">
          <%=Common.displayLogoutHref(request)%>
        </span> <!-- logout -->
        <span class="select">
          <a href="Start.do?<%=Constants.CURRENTPSUBJECT_HTTPPARAMNAME%>=<%=Common.buildCompoundId(loggedInPrivilegedSubject.getEffectiveEditor())%>">
            <%=Common.homepageName(loggedInPrivilegedSubject)%>
          </a>
           &gt; <!-- displays as text right-angle bracket -->
          <a href="<%=personViewHref%>"><%=ResLoaderUI.getString("org-browse.subjview.txt") %>
            [<%=currentGranteePrivilegedSubject.getName()%>]
          </a>
          &gt; <%=ResLoaderUI.getString("org-browse.form1.grant.txt") %>
        </span> <!-- select -->
      </div> <!-- Navbar -->
      <div id="Layout">
        <div id="Content">
          <div id="ViewHead">
			<span class="dropback"><%=ResLoaderUI.getString("org-browse.form1.grantnew.txt") %></span>           	
     	    	<h1>
     	      	<%=currentGranteePrivilegedSubject.getName()%>
     	    	</h1>
     	    	<span class="ident"><%=currentGranteePrivilegedSubject.getDescription()%></span><!--,	Technology Strategy and Support Operations-->
         	</div> <!-- ViewHead -->

         	<div class="section" id="summary">
				 		<h2><%=ResLoaderUI.getString("org-browse.summary.hdr") %></h2>
							<table>
              	<tr>
              		<th class="label" scope="row"><%=ResLoaderUI.getString("org-browse.summary.type.lb") %></th>
              		<td class="data"><%=currentSubsystem.getName()%></td>
              		<td class="control">
										<a href="<%=personViewHref%>">
		               	<img src="images/arrow_left.gif" alt="" /><%=ResLoaderUI.getString("org-browse.summary.change.txt") %>
    			         	</a>
									</td>
             		</tr>								
              	<tr>
              		<th class="label" scope="row"><%=ResLoaderUI.getString("org-browse.privilege.hdr") %></th>
              		<td class="data"><span class="category"><%=currentCategory.getName()%></span> : <span class="function"><%=currentFunction.getName()%></span><br />
						<%=currentFunction.getHelpText()%>
					</td>
              		<td class="control">
										<a href="<%=functionsHref%>"><img src="images/arrow_left.gif" alt="" /><%=ResLoaderUI.getString("org-browse.change_href.txt") %></a>
									</td>
             		</tr>								
							</table>						
				 </div>
				 
           	<div class="section">
           	<h2>
             	<%=ResLoaderUI.getString("org-browse.selscope.hdr") %>
         	 </h2>
			  <p class="dropback"><label for "scope">
           	  <%=ResLoaderUI.getString("org-browse.scope.lbl") %>
             	</label>
			  </p>
			  <select
              	name="scope"
	            	id="scope"
              	size=20
              	onchange="javascript:document.form1.continueButton.disabled=false">
               	<%=signet.printTreeNodesInContext
                    ("<option disabled value=\"",  // ancestorPrefix
                     "<option value=\"",           // selfPrefix
                     "<option value=\"",           // descendantPrefix
                     "",                           // prefixIncrement
                     "\">\n",                      // infix
                     ". ",                         // infixIncrement
                     "</option>\n",                // suffix
                     loggedInPrivilegedSubject.getGrantableScopes(currentFunction))%>
           	          </select>
		  </div>
           	<!-- end section -->
	
          <div class="section">
			<h2>
           	<%=ResLoaderUI.getString("org-browse.continue.hdr") %>
         	</h2> 		  
      	    <input
        name="continueButton"
        disabled="true"
        type="submit"
        class="button-def"
        value="<%=ResLoaderUI.getString("org-browse.continue.bt") %> >>" />
         
         	<p>
           	<a href="<%=personViewHref%>">
             	<img src="images/arrow_left.gif" alt="" /><%=ResLoaderUI.getString("org-browse.cancel_href.txt") %> [<%=currentGranteePrivilegedSubject.getName()%>]
           	</a>
           	</p>
         </div> <!-- end section -->  	
       	</div><!-- end Content -->	
        <div id="Sidebar">     
          <div class="helpbox">
          	<h2><%=ResLoaderUI.getString("org-browse.help.hdr") %></h2>
          	<div class="helpbox">
          	  <p><%=ResLoaderUI.getString("org-browse.helpsteps.txt") %></p>
          	  <ol>
          	    <li class="dropback"><%=ResLoaderUI.getString("org-browse.helpsel_1.txt") %> </li>
          	    <li><b><%=ResLoaderUI.getString("org-browse.helpsel_2.txt") %></b></li>
          	    <p style="margin: 10px 0px 2px -20px;"><%=ResLoaderUI.getString("org-browse.next_1.txt") %> </p>
			<li><%=ResLoaderUI.getString("org-browse.next_2.txt") %></li>
       	      </ol>
          	</div>
          	<!-- end helpbox -->
		  </div>  
          <!-- end helpbox -->
        </div> <!-- end Sidebar -->
        <tiles:insert page="/tiles/footer.jsp" flush="true" />
      </div>	<!-- end Layout -->
    </form>
  </body>
</html>