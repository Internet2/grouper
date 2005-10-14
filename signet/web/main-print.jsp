<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: main-print.jsp,v 1.21 2005-10-14 22:34:53 acohen Exp $
  $Date: 2005-10-14 22:34:53 $
  
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
  
<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
   PrivilegedSubject loggedInPrivilegedSubject
     = (edu.internet2.middleware.signet.PrivilegedSubject)
         (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
         
   DateFormat dateFormat = DateFormat.getDateInstance();
%>

  	<!-- removing header to use full page for print view; form is not required 
    <form name="form1" method="post" action="">
      <tiles:insert page="/tiles/header.jsp" flush="true" />
      <div id="Layout"> 
	-->  
       <h1>Privileges granted by <%=loggedInPrivilegedSubject.getName()%></h1>
		  <p class="dropback">Report as of <%=dateFormat.format(new Date())%></p>
        <a href="Start.do"><img src="images/arrow_left.gif" alt="" />return</a>
  	    
        <table>            
          <tr>
            <td>
              <b>
                Person
              </b>
            </td>
            <td>
              <b>
                Privilege
              </b>
            </td>
            <td><b>Scope</b></td>
            <td>
              <b>
                Limits
              </b>
            </td>
            <td>
              <b>
                Status
              </b>
            </td>
          </tr>
	        
	  
<%
  Set assignmentSet
    = new TreeSet
        (Common.getAssignmentsGrantedForReport
          (loggedInPrivilegedSubject, null));

  Set proxySet
    = new TreeSet
        (Common.getProxiesGrantedForReport
          (loggedInPrivilegedSubject, null));
        
  Iterator assignmentIterator = assignmentSet.iterator();
  while (assignmentIterator.hasNext())
  {
    Assignment assignment = (Assignment)(assignmentIterator.next());
    PrivilegedSubject grantee = assignment.getGrantee();
    Subsystem subsystem = assignment.getFunction().getSubsystem();
    Function function = assignment.getFunction();
    Category category = function.getCategory();
%>
	        
          <tr>
            <td> <!-- person -->
              <%=grantee.getName()%>
            </td> <!-- person -->
            
            <td> <!-- privilege -->
              <%=subsystem.getName()%> : <%=category.getName()%> :
                <%=assignment.getFunction().getName()%>
            </td> <!-- privilege -->
            
            <td> <!-- scope -->
              <%=assignment.getScope().getName()%>
            </td> <!-- scope -->
            
            <td> <!-- limits -->
                <%=Common.displayLimitValues(assignment)%>
            </td> <!-- limits -->
            
            <td> <!-- status -->
<%=
  assignment.getStatus().getName()
  + (assignment.canUse()   ? ", can use"   : "")
  + (assignment.canGrant() ? ", can grant" : "")
%>
            </td> <!-- status -->
          </tr>
  	
<% 
  }
        
  Iterator proxyIterator = proxySet.iterator();
  while (proxyIterator.hasNext())
  {
    Proxy proxy = (Proxy)(proxyIterator.next());
    PrivilegedSubject grantee = proxy.getGrantee();
    Subsystem subsystem = proxy.getSubsystem();
%>
	        
          <tr>
            <td> <!-- person -->
              <%=grantee.getName()%>
            </td> <!-- person -->
            
            <td> <!-- privilege -->
              Proxy
            </td> <!-- privilege -->
            
            <td> <!-- scope -->
              <span class="label">acting as </span><%=proxy.getGrantor().getName()%>
            </td> <!-- scope -->
            
            <td> <!-- limits -->
              <span class="label"><%=Common.displayLimitType(proxy)%> </span><%=Common.displaySubsystem(proxy)%>
            </td> <!-- limits -->
            
            <td> <!-- status -->
              <%=Common.displayStatus(proxy)%>
            </td> <!-- status -->
          </tr>
  	
<% 
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
