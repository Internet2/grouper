<!--
  $Id: functions.jsp,v 1.4 2005-02-24 22:19:29 jvine Exp $
  $Date: 2005-02-24 22:19:29 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
-->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
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
   
   Set grantableCategories
   	= loggedInPrivilegedSubject
   			.getGrantableCategories(currentSubsystem);
         
   DateFormat dateFormat = DateFormat.getDateInstance();
   
   String personViewHref
     = "PersonView.do?granteeSubjectTypeId="
       + currentGranteePrivilegedSubject.getSubjectTypeId()
       + "&granteeSubjectId="
       + currentGranteePrivilegedSubject.getSubjectId();
%>

    <form name="form1" method="post" action="OrgBrowse.do">
      <div id="Header">
        <div id="Logo">
        <img src="images/organisation-logo.jpg" width="80" height="60" alt="logo" />
				</div> 
        <!-- Logo -->
        
        <div id="Signet">
					<img src="images/signet.gif" alt="Signet" height="60" width="49">
        </div> <!-- Signet -->
    
        <div id="Navbar">
          <span class="logout">
            <a href="NotYetImplemented.do">
              <%= loggedInPrivilegedSubject.getName() %>: Logout
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
      </div> <!-- Header -->
    
      <div id="Layout"> 
        <div id="Content">
          <div id="ViewHead">
						Granting new privilege to
           	<h1><%=currentGranteePrivilegedSubject.getName()%></h1>
           	<span class="dropback">
						<%=currentGranteePrivilegedSubject.getDescription()%></span><!--,	Technology Strategy and Support Operations-->
					</div> <!-- ViewHead -->
         
         	<div class="section">
					<h2>
           	New <%=currentSubsystem.getName()%> privilege
         	</h2> 
           	<p class="dropback">
             	 Select the privilege you want to grant. Only privileges you are authorized to grant are listed.
							</p>
							
               	<select name="step3" size="10" class="long" id="step3" style="float: left;">
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
     
							<div class="description">Category name : <span class="keyname">Function name</span><br />
		 						Description goes here.
							</div>  <!-- description -->
					 </div> 	<!-- section -->
					
           <div class="section">
             	<input
                  name="Button"
                  type="submit"
                  class="button-def"
                  value="Continue &gt;&gt;" />
           	
           	<p>
             	<a href="<%=personViewHref%>">
               	<img src="images/icon_arrow_left.gif" width="16" height="16" class="icon" />CANCEL and return to <%=currentGranteePrivilegedSubject.getName()%>'s view
             	</a>
           	</p>
         	</div>	<!-- section -->
					
       	</div><!-- Content -->
				<jsp:include page="footer.jsp" flush="true" />
        <div id="Sidebar">
          <div class="helpbox">
          	<h2>Help</h2>
          	<jsp:include page="grant-help.jsp" flush="true" />          
					</div>  <!-- end helpbox -->
        </div> <!-- Sidebar -->
      </div> <!-- Layout -->
    </form>
  </body>
</html>
