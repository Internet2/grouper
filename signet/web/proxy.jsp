<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: proxy.jsp,v 1.6 2005-11-22 03:56:44 acohen Exp $
  $Date: 2005-11-22 03:56:44 $
  
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
  <body onload="window.focus();">

<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Date" %>
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
         
  PrivilegedSubject grantee      = currentProxy.getGrantee();
  PrivilegedSubject grantor      = currentProxy.getGrantor();
  PrivilegedSubject proxySubject = currentProxy.getProxy();
         
  boolean canUse = currentProxy.canUse();
  boolean canExtend = currentProxy.canExtend();
         
  DateFormat dateFormat = DateFormat.getDateInstance();
%>
    <div class="section">
      <h2>Proxy details</h2>
      
      <table>
    
        <tr>
          <th class="label" scope="row">
            Designated:
          </th>
          <td>
            <%=grantee.getName()%>
          </td>
        </tr>
        
        <tr>
          <th class="label" scope="row">
		  	As:
		  </th>
          <td><span class="function">
              Granting Proxy
              </span>
          </td>
        </tr>
        
        <tr>
          <th class="label" scope="row">
            Scope:
          </th>
          <td>
             <span class="label">acting as </span><%=grantor.getName()%>
          </td>
        </tr>

        <tr>
          <th class="label" scope="row">
            <%=Common.displayLimitType(currentProxy)%>:
          </th>
          <td>
            <%=Common.displaySubsystem(currentProxy)%>
          </td>
        </tr>

        <tr>
          <th class="label" scope="row">Duration:</th>
          <td>
            Until
            <%=currentProxy.getExpirationDate() == null
               ? "revoked"
               : dateFormat.format(currentProxy.getExpirationDate())%>
          </td>
        </tr>

        <tr>
          <th class="label" scope="row">
            Status:
          </th>
          <td>
            <%=Common.displayStatusForDetailPopup(currentProxy)%>
          </td>
        </tr>
      
        <tr>
          <th class="label" scope="row">
		  Effective:
		  </th>
          <td><%=dateFormat.format(currentProxy.getEffectiveDate())%> </td>
        </tr>
        
        <tr>
          <th class="label" scope="row">
		  Designated on:
		  </th>
          <td><!-- time of last proxy-edit goes here --></td>
        </tr>
        
        <tr>
          <th class="label" scope="row">
            Designated by:
          </th>
          <td>
            <%=(proxySubject==null ? "" : (proxySubject.getName() + ", acting as ")) + grantor.getName()%>
          </td>
        </tr>
      
      </table>
    </div>
  </body>
</html>