<!--
  $Id: personview-print.jsp,v 1.6 2005-02-25 22:07:57 jvine Exp $
  $Date: 2005-02-25 22:07:57 $
  
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

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>

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
  
     <jsp:include page="header.jsp" flush="true" />  
      <div id="Layout"> 
      <a href="PersonView.do"><img src="images/icon_arrow_left.gif" class="icon" />return</a>
        <h1>
          <%=currentSubsystem.getName()%> privileges assigned to <%=currentGranteePrivilegedSubject.getName()%>
        </h1>
          <%=currentGranteePrivilegedSubject.getDescription()%>
        
          <table>
            <tr>
              <td><b>Privilege</b></td>
              <td><b>Scope</b></td>
              <td><b>Limits</b></td>
              <td><b>Status</b></td>
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
  
            <tr>
              <td>
                <%=assignment.getFunction().getCategory().getName()%>
                :
                <%=assignment.getFunction().getName()%>
              </td>
              <td>
                <%=assignment.getScope().getName()%>
              </td>
              
              <td> <!-- limits -->
                <%=Common.displayLimitValues(assignment)%>
              </td> <!-- limits -->
              <td> <!-- status -->
              </td> <!-- status -->
            </tr>
<%
    }
  }
%>
            
          </table>
      <jsp:include page="footer.jsp" flush="true" />  
      </div> <!-- layout -->
    </form>
  </body>
</html>
