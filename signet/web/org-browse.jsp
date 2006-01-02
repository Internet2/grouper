<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: org-browse.jsp,v 1.28 2006-01-02 04:59:07 acohen Exp $
  $Date: 2006-01-02 04:59:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
-->

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <meta name="robots" content="noindex, nofollow">
    <title>
      Signet
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
          <a href="<%=personViewHref%>">Subject View
            [<%=currentGranteePrivilegedSubject.getName()%>]
          </a>
          &gt; Grant new privilege
        </span> <!-- select -->
      </div> <!-- Navbar -->
      <div id="Layout">
        <div id="Content">
          <div id="ViewHead">
			<span class="dropback">Granting new privilege to</span>           	
     	    	<h1>
     	      	<%=currentGranteePrivilegedSubject.getName()%>
     	    	</h1>
     	    	<span class="ident"><%=currentGranteePrivilegedSubject.getDescription()%></span><!--,	Technology Strategy and Support Operations-->
         	</div> <!-- ViewHead -->

         	<div class="section">
				 		<h2>New assignment details</h2>
							<table>
              	<tr>
              		<th class="label" scope="row">Type:</th>
              		<td><%=currentSubsystem.getName()%></td>
              		<td nowrap="nowrap">
										<a href="<%=personViewHref%>">
		               	<img src="images/arrow_left.gif" alt="" />change
    			         	</a>
									</td>
             		</tr>								
              	<tr>
              		<th class="label" scope="row">Privilege:</th>
              		<td><span class="category"><%=currentCategory.getName()%></span> : <span class="function"><%=currentFunction.getName()%></span><br />
						<%=currentFunction.getHelpText()%>
					</td>
              		<td>
										<a href="<%=functionsHref%>"><img src="images/arrow_left.gif" alt="" />change</a>
									</td>
             		</tr>								
							</table>						
				 </div>
				 
           	<div class="section">
           	<h2>
             	Select scope
         	 </h2>
			  <p class="dropback"><label for "scope">
           	  Select the scope to which this privilege applies.
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
           	Continue to next step : Limits &amp; Conditions
         	</h2> 		  
      	    <input
        name="continueButton"
        disabled="true"
        type="submit"
        class="button-def"
        value="Continue >>" />
         
         	<p>
           	<a href="<%=personViewHref%>">
             	<img src="images/arrow_left.gif" alt="" />CANCEL and return to Subject View [<%=currentGranteePrivilegedSubject.getName()%>]
           	</a>
           	</p>
         </div> <!-- end section -->  	
       	</div><!-- end Content -->	
        <div id="Sidebar">     
          <div class="helpbox">
          	<h2>Help</h2>
          	<div class="helpbox">
          	  <p>Steps to grant a privilege:</p>
          	  <ol>
          	    <li class="dropback">Select the privilege (done). </li>
          	    <li><b>Select the scope to which the privilege applies, then click Continue.</b></li>
          	    <p style="margin: 10px 0px 2px -20px;">Next:</p>
			<li>Set limits and conditions for the privilege.</li>
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