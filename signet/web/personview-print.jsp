<!--
  $Id: personview-print.jsp,v 1.2 2004-12-24 04:15:46 acohen Exp $
  $Date: 2004-12-24 04:15:46 $
  
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
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>

<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.Status" %>

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
         
   Set grantableSubsystems = loggedInPrivilegedSubject.getGrantableSubsystems();
         
   DateFormat dateFormat = DateFormat.getDateInstance();
%>

    <form action="" method="post" name="form1" id="form1">
  
      <div id="Header">  
        <div id="Logo">
          <img src="images/KITN.gif" width="216" height="60" alt="logo" />
        </div>
      </div>
       
      <div id="Layout"> 
			<a href="PersonView.do"><img src="images/icon_arrow_left.gif" class="icon" />return</a>
        <h1>
          <%=(currentSubsystem == null ? "NO" : currentSubsystem.getName())%> privileges assigned to <%=currentGranteePrivilegedSubject.getName()%>
        </h1>
      	<span class="dropback">
      	  <%=currentGranteePrivilegedSubject.getDescription()%>
      	</span>
      	<br />
      	<br />
         
        <div class="tablecontent"> 
            
      	  <table class="full">
            <tr>
              <td class="line"><b>Privilege</b></td>
              <td class="line"><b>Scope</b></td>
              <td class="line"><b>Limits</b></td>
            </tr>
<%
  if (currentSubsystem != null)
  {
    Set assignmentsReceivedForCurrentSubsystem
      = currentGranteePrivilegedSubject
      		.getAssignmentsReceived(Status.ACTIVE, currentSubsystem);
    Iterator assignmentsIterator = assignmentsReceivedForCurrentSubsystem.iterator();
    while (assignmentsIterator.hasNext())
    {
      Assignment assignment = (Assignment)(assignmentsIterator.next());
%>
  
            <tr class="line" >
              <td class="line" >
                <%=assignment.getFunction().getCategory().getName()%>
                :
                <%=assignment.getFunction().getName()%>
              </td>
              <td class="line" >
                <%=assignment.getScope().getName()%>
              </td>
              <td class="line"  >
              </td>
            </tr>
<%
    }
  }
%>
            
          </table>
        </div> <!-- tablecontent -->
			<jsp:include page="footer.jsp" flush="true" />	
      </div> <!-- layout -->
    </form>
  </body>
</html>
