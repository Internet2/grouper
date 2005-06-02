<!--
  $Id: org-browse.jsp,v 1.10 2005-06-02 06:26:04 jvine Exp $
  $Date: 2005-06-02 06:26:04 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
-->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
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

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
  PrivilegedSubject loggedInPrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute("loggedInPrivilegedSubject"));
   
   PrivilegedSubject currentGranteePrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute("currentGranteePrivilegedSubject"));
         
   Subsystem currentSubsystem
     = (Subsystem)
         (request.getSession().getAttribute("currentSubsystem"));
         
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
         	<h2>
           	New <%=currentSubsystem.getName()%> privilege
						 		<div class="change">
									<a href="<%=functionsHref%>"><img src="images/arrow_left.gif" />change</a>
								</div>
         	</h2>
            <span class="category"><%=currentCategory.getName()%></span> : 
            <span class="function"><%=currentFunction.getName()%></span>
       	  </div>
         	<!-- end section -->

           	<div class="section">
           	<h2>
             	Scope
         	 </h2>
 
             	<p class="nomarg"><label for "scope">
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
             	<img src="images/icon_arrow_left.gif" width="16" height="16" class="icon" />CANCEL and return to <%=currentGranteePrivilegedSubject.getName()%>'s view
           	</a>
            	</p>
         </div> <!-- end section -->  	
       	</div><!-- end Content -->	
        <tiles:insert page="/tiles/footer.jsp" flush="true" />
        <div id="Sidebar">     
          <div class="helpbox">
          	<h2>Help</h2>
          	<jsp:include page="grant-help.jsp" flush="true" />          
					</div>  <!-- end helpbox -->
        </div> <!-- end Sidebar -->
      </div>	<!-- end Layout -->
    </form>
  </body>
</html>