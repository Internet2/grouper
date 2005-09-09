<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: org-browse.jsp,v 1.18 2005-09-09 20:49:46 acohen Exp $
  $Date: 2005-09-09 20:49:46 $
  
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

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
  PrivilegedSubject loggedInPrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute("loggedInPrivilegedSubject"));
   
   PrivilegedSubject currentGranteePrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute(Constants.GRANTEE_ATTRNAME));
         
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
          <a href="NotYetImplemented.do">
            <%=loggedInPrivilegedSubject.getName()%>: Logout
          </a>
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
				  	Granting privilege to
     	    	<h1>
     	      	<%=currentGranteePrivilegedSubject.getName()%>
     	    	</h1>
     	    	<span class="dropback"><%=currentGranteePrivilegedSubject.getDescription()%></span><!--,	Technology Strategy and Support Operations-->
         	</div> <!-- ViewHead -->

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
              		<td>
										<a href="<%=personViewHref%>">
		               	<img src="images/arrow_left.gif" alt="" />change
    			         	</a>
									</td>
             		</tr>								
              	<tr>
              		<th class="label" scope="row">Function:</td>
              		<td>
						<p><span class="category"><%=currentCategory.getName()%></span> : <span class="function"><%=currentFunction.getName()%></span></p>
						<p><%=currentFunction.getHelpText()%></p>
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
						<fieldset>
						<legend>Scopes in which you are authorized to grant</legend>
             	<p><label for "scope">
               	Select the organization to which this privilege applies.
             	</label></p>

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
						</fieldset>	
         	</div>	<!-- end section -->
	
          <div class="section">
      	<input
        name="continueButton"
        disabled="true"
        type="submit"
        class="button-def"
        value="Continue >>" />
         
         	<p>
           	<a href="<%=personViewHref%>">
             	<img src="images/arrow_left.gif" alt="" />CANCEL and return to <%=currentGranteePrivilegedSubject.getName()%>'s view
           	</a>
            	</p>
         </div> <!-- end section -->  	
       	</div><!-- end Content -->	
        <div id="Sidebar">     
          <div class="helpbox">
          	<h2>Help</h2>
          	<jsp:include page="grant-help.jsp" flush="true" />          
					</div>  <!-- end helpbox -->
        </div> <!-- end Sidebar -->
        <tiles:insert page="/tiles/footer.jsp" flush="true" />
      </div>	<!-- end Layout -->
    </form>
  </body>
</html>