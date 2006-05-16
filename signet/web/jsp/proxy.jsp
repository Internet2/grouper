<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: proxy.jsp,v 1.4 2006-05-16 17:37:35 ddonn Exp $
  $Date: 2006-05-16 17:37:35 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
-->

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta name="robots" content="noindex, nofollow" />
    <title>
      <%=ResLoaderUI.getString("signet.title") %>
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
<%@ page import="java.util.Arrays" %>

<%@ page import="edu.internet2.middleware.subject.Subject" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Proxy" %>
<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.ProxyHistory" %>

<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>
<%@ page import="edu.internet2.middleware.signet.ui.HistoryDateComparatorDescending" %>

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
<div id="summary">
    <div class="section">
      <h2>
        <%=ResLoaderUI.getString("proxy.proxydetails.hdr") %>
      </h2>
    </div>    
    <table>
    
      <tr>
        <th class="label" scope="row">
          <%=ResLoaderUI.getString("proxy.desig.txt") %>
        </th>
        <td>
          <%=grantee.getName()%>
        </td>
      </tr>
        
      <tr>
        <th class="label" scope="row">
          <%=ResLoaderUI.getString("proxy.priv.txt") %>
        </th>
        <td>
          <%=Common.proxyPrivilegeDisplayName(signet, currentProxy)%>
        </td>
      </tr>
        
      <tr>
        <th class="label" scope="row">
          <%=ResLoaderUI.getString("proxy.in.txt") %>
        </th>
        <td>
          <%=Common.displaySubsystem(currentProxy)%>
        </td>
      </tr>
 
      <tr>
        <th class="label" scope="row"><%=ResLoaderUI.getString("proxy.effective.txt") %></th>
        <td class="content"><%=dateFormat.format(currentProxy.getEffectiveDate())%> </td>
      </tr>
      <tr>
        <th class="label" scope="row">
          <%=ResLoaderUI.getString("proxy.duration.txt") %>
        </th>
        <td>
          <%=ResLoaderUI.getString("proxy.until.txt") %>
          <%=currentProxy.getExpirationDate() == null
             ? ResLoaderUI.getString("proxy.revoked.txt")
             : dateFormat.format(currentProxy.getExpirationDate())%>
        </td>
      </tr>

      <tr>
        <th class="label" scope="row">
          <%=ResLoaderUI.getString("proxy.status.txt") %>
        </th>
        <td>
          <%=Common.displayStatusForDetailPopup(currentProxy)%>
        </td>
      </tr>
      
    </table>
 
	    
    <div class="section">
      <h2>
        <a name="history" id="history">
        </a>
        <%=ResLoaderUI.getString("proxy.history.hdr") %>
      </h2>
	</div>

    <table>
  
<%
  Set historySet = currentProxy.getHistory();
  ProxyHistory[] historyArray = new ProxyHistory[1];
  historyArray = (ProxyHistory[])(historySet.toArray(historyArray));
  Arrays.sort(historyArray, new HistoryDateComparatorDescending());
  for (int i = 0; i < historyArray.length; i++)
  {
    ProxyHistory historyRecord = historyArray[i];
%>
      <tr>
        <th class="label" scope="row">
          <%=Common.displayDatetime(Constants.DATETIME_FORMAT_12_MINUTE, historyRecord.getDate())%>
        </td>
        <td>
          <%=Common.describeChange(historyArray, i)%>
        </td>
      </tr>

<%
  }
%>
    </table>
  </div>
  </body>
</html>