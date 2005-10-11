<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: proxy.jsp,v 1.1 2005-10-11 19:27:35 acohen Exp $
  $Date: 2005-10-11 19:27:35 $
  
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
    <script language="JavaScript" type="text/javascript" src="scripts/signet.js">
    </script>
  </head>
  <body>

<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>

<%@ page import="edu.internet2.middleware.subject.Subject" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Proxy" %>
<%@ page import="edu.internet2.middleware.signet.Signet" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
  Proxy currentProxy
    = (Proxy)
         (request.getSession().getAttribute(Constants.PROXY_ATTRNAME));
         
  PrivilegedSubject grantee = currentProxy.getGrantee();
  PrivilegedSubject grantor = currentProxy.getGrantor();
         
  boolean canUse = currentProxy.canUse();
  boolean canExtend = currentProxy.canExtend();
         
  DateFormat dateFormat = DateFormat.getDateInstance();
%>
    <div class="section">
      <h2>Proxy details</h2>
      
      <table class="invis">
    
        <tr>
          <td class="label">
            Granted to:
          </td>
          <td>
            <%=grantee.getName()%>
          </td>
        </tr>
      
        <tr>
          <td class="label">
            Scope:
          </td>
          <td>
             <span class="label">acting as </span><%=grantor.getName()%>
          </td>
        </tr>

        <tr>
          <td class="label">
            Limit:
          </td>
          <td>
            <span class="label"><%=Common.displayLimitType(currentProxy)%> </span><%=Common.displaySubsystem(currentProxy)%>
          </td>
        </tr>

        <tr>
          <td class="label">
            Status:
          </td>
          <td>
            <%=Common.displayStatus(currentProxy)%>
          </td>
        </tr>
      
        <tr>
          <td class="label"> Effective on: </td>
          <td><%=dateFormat.format(currentProxy.getEffectiveDate())%> </td>
        </tr>

        <tr>
          <td class="label">
            Granted by:
          </td>
          <td>
            <%=grantor.getName()%>
          </td>
        </tr>
      
      </table>
    </div>
  </body>
</html>