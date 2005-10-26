<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: personview-print.jsp,v 1.20 2005-10-26 16:53:24 acohen Exp $
  $Date: 2005-10-26 16:53:24 $
  
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
  
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="java.util.Date" %>

<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.Status" %>
<%@ page import="edu.internet2.middleware.signet.Proxy" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>
<%@ page import="edu.internet2.middleware.signet.ui.PrivDisplayType" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
  PrivilegedSubject loggedInPrivilegedSubject
    = (PrivilegedSubject)
        (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
   
  PrivilegedSubject pSubject
    = (PrivilegedSubject)
        (request.getSession().getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME));
         
  Subsystem currentSubsystem
    = (Subsystem)
        (request.getSession().getAttribute(Constants.SUBSYSTEM_ATTRNAME));
         
  PrivDisplayType privDisplayType
    = (PrivDisplayType)
        (request.getSession().getAttribute(Constants.PRIVDISPLAYTYPE_ATTRNAME));
         
  DateFormat dateFormat = DateFormat.getDateInstance();
%>

  	<!-- removing header to use full page for print view; form is not required 
    <form action="" method="post" name="form1" id="form1">
      <tiles:insert page="/tiles/header.jsp" flush="true" />
      <div id="Layout"> 
	 --> 
        <h1>
          <%=(currentSubsystem == Constants.WILDCARD_SUBSYSTEM ? "All" : currentSubsystem.getName())%> privileges assigned <%=(privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED) ? "by" : "to")%> <%=pSubject.getName()%>
        </h1>
          <p class="ident"><%=pSubject.getDescription()%></p> 
		  <p class="dropback">Report as of <%=Common.displayDatetime(new Date())%></p> 
      <a href="PersonView.do"><img src="images/arrow_left.gif" alt="" />return</a>

        
          <table>
            <tr>
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
              <td><b>Subject</b></td>
<%
  }
%>
              <td><b>Privilege</b></td>
              <td><b>Scope</b></td>
              <td><b>Limits</b></td>
              <td><b>Status</b></td>
            </tr>
<%
  if (currentSubsystem != null)
  {
    Subsystem subsystemFilter = null;
  
    if (!currentSubsystem.equals(Constants.WILDCARD_SUBSYSTEM))
    {
      subsystemFilter = currentSubsystem;
    }
  
    Set assignments;
    Set proxies;
    
    if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
    {
      assignments
        = new TreeSet
            (Common.getAssignmentsGrantedForReport
              (pSubject, subsystemFilter));

      proxies
        = new TreeSet
            (Common.getProxiesGrantedForReport
              (pSubject, subsystemFilter));
    }
    else
    {
      assignments
        = new TreeSet
            (Common.getAssignmentsReceivedForReport
              (pSubject, subsystemFilter));

      proxies
        = new TreeSet
            (Common.getProxiesReceivedForReport
              (pSubject, subsystemFilter));
    }
             
    Iterator assignmentsIterator = assignments.iterator();
    while (assignmentsIterator.hasNext())
    {
      Assignment assignment = (Assignment)(assignmentsIterator.next());
%>
  
            <tr>
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
              <td>
                <%=assignment.getGrantee().getName()%>
              </td>
<%
  }
%>
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
                <%=Common.displayStatus(assignment)%>
              </td> <!-- status -->
            </tr>
<%
    }
    
             
    Iterator proxiesIterator = proxies.iterator();
    while (proxiesIterator.hasNext())
    {
      Proxy proxy = (Proxy)(proxiesIterator.next());
%>
  
            <tr>
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
              <td>
                <%=proxy.getGrantee().getName()%>
              </td>
<%
  }
%>
              <td>
                <%=Common.proxyPrivilegeDisplayName(signet, proxy)%>
              </td>
              <td>
                <span class="label">acting as </span><%=proxy.getGrantor().getName()%>
              </td>
              
              <td> <!-- limits -->
                <span class="label"><%=Common.displayLimitType(proxy)%> </span><%=Common.displaySubsystem(proxy)%>
              </td> <!-- limits -->
              <td> <!-- status -->
                <%=Common.displayStatus(proxy)%>
              </td> <!-- status -->
            </tr>
<%
    }
  }
%>
            
        </table>
	<!-- removing footer and end of layout div	
        <tiles:insert page="/tiles/footer.jsp" flush="true" />
      </div> 
    </form>
	-->
  </body>
</html>
