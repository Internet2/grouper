<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: not-yet-implemented.jsp,v 1.1 2006-01-10 22:37:02 acohen Exp $
  $Date: 2006-01-10 22:37:02 $
  
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
         (request.getSession().getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME));
         
   Subsystem currentSubsystem
     = (Subsystem)
         (request.getSession().getAttribute(Constants.SUBSYSTEM_ATTRNAME));
         
   DateFormat dateFormat = DateFormat.getDateInstance();
%>
    <form name="form1" action="">
      <tiles:insert page="/tiles/header.jsp" flush="true" />
      <div id="Navbar">
        <span class="logout">
<!--
          <a href="blank.html">
            Your Name: Logout
          </a>
-->
        </span> <!-- Navbar -->
        <span class="select">
          <a href="Start.do?<%=Constants.CURRENTPSUBJECT_HTTPPARAMNAME%>=<%=Common.buildCompoundId(loggedInPrivilegedSubject.getEffectiveEditor())%>">
            <%=Common.homepageName(loggedInPrivilegedSubject)%>
          </a>
            > Logging out </span> <!-- select -->
      </div> 
      <!-- Navbar -->
      
      <div id="Layout">
        <div id="Content">
          <h1>Logout is not complete.</h1>
          <p>You've reached this page because:</p>
          <ul>
            <li>you clicked Logout, or </li>
            <li>you selected a feature that has not been implemented, or </li>
            <li>the system encountered an unexpected error. </li>
          </ul>
          <p><span class="big alert">In order to close your Signet session, you must  quit (exit) the browser.</span></p>
          <p>If you are not the only person who has access to this computer, another user could re-open your Signet session. <br />
          Quitting the browser will remove all traces of your session.</p>
          <p style="margin-top: 25px;"> 
            <a href="Start.do?<%=Constants.CURRENTPSUBJECT_HTTPPARAMNAME%>=<%=Common.buildCompoundId(loggedInPrivilegedSubject.getEffectiveEditor())%>">
          <img src="images/arrow_left.gif" alt="" />Return to your Signet session</a></p>
        </div> 
        <!-- Content -->
        <tiles:insert page="/tiles/footer.jsp" flush="true" />

      </div>	
    </form>
  </body>
</html>