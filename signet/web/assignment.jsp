<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: assignment.jsp,v 1.25 2005-11-22 03:56:44 acohen Exp $
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
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>

<%@ page import="edu.internet2.middleware.subject.Subject" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.tree.TreeNode" %>
<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.Limit" %>
<%@ page import="edu.internet2.middleware.signet.LimitValue" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
  Assignment currentAssignment
    = (Assignment)
         (request.getSession().getAttribute("currentAssignment"));
         
  Subject grantee
    = signet.getSubject
        (currentAssignment.getGrantee().getSubjectTypeId(),
         currentAssignment.getGrantee().getSubjectId());
  Subject grantor
  	= signet.getSubject
  		(currentAssignment.getGrantor().getSubjectTypeId(),
  		 currentAssignment.getGrantor().getSubjectId());
  		 
  Subject proxy = null;
  if (currentAssignment.getProxy() != null)
  {
    proxy
      = signet.getSubject
          (currentAssignment.getProxy().getSubjectTypeId(),
           currentAssignment.getProxy().getSubjectId());
  }
         
  boolean canUse = currentAssignment.canUse();
  boolean canGrant = currentAssignment.canGrant();
         
  DateFormat dateFormat = DateFormat.getDateInstance();
%>
    <div class="section">
      <h2>Assignment details</h2>
			
      <table>
    
      <tr>
        <th class="label" scope="row">
          Granted to:
        </th>
        <td>
          <%=grantee.getName()%>
        </td>
      </tr>
    
    
    
      <tr>
      	<th class="label" scope="row">Type:</th>
      	<td><%=currentAssignment.getFunction().getSubsystem().getName()%></td>
     	</tr>
      <tr>
      	<th class="label" scope="row">Privilege:</th>
      	<td><span class="category"><%=currentAssignment.getFunction().getCategory().getName()%></span> : <span class="function"><%=currentAssignment.getFunction().getName()%></span><br />
      	    <%=currentAssignment.getFunction().getHelpText()%></td>
      </tr>
      <tr>
        <th class="label" scope="row">
          Scope:
        </th>
        <td>
		  <%=signet.displayAncestry
                    (currentAssignment.getScope(),
                     " : ",  // childSeparatorPrefix
                     "",     // levelPrefix
                     "",     // levelSuffix
                     "")     // childSeparatorSuffix
                 %>
        </td>
      </tr>

<%
  Limit[] limits
  	= Common.getLimitsInDisplayOrder
        (currentAssignment.getFunction().getLimits());
  LimitValue[] limitValues = Common.getLimitValuesInDisplayOrder(currentAssignment);
  for (int limitIndex = 0; limitIndex < limits.length; limitIndex++)
  {
    Limit limit = limits[limitIndex];
%>
      <tr>
        <th class="label" scope="row">
          <%=limit.getName()%>:
        </th>
        <td>
<%
  StringBuffer strBuf = new StringBuffer();
  int limitValuesPrinted = 0;
  for (int limitValueIndex = 0;
       limitValueIndex < limitValues.length;
       limitValueIndex++)
  {
    LimitValue limitValue = limitValues[limitValueIndex];
    if (limitValue.getLimit().equals(limit))
    {
      strBuf.append((limitValuesPrinted++ > 0) ? ", " : "");
      strBuf.append(limitValue.getDisplayValue());
    }
  }
%>
          <%=strBuf%>
        </td>
      </tr>
<%
  }
%>

      <tr>
        <th class="label" scope="row">Duration:</th>
        <td>
          Until
          <%=currentAssignment.getExpirationDate() == null
             ? "revoked"
             : dateFormat.format(currentAssignment.getExpirationDate())%>
        </td>
      </tr>

      <tr>
        <th class="label" scope="row">
          Extensibility:
        </th>
        <td>
          <%=canUse?"can use":""%><%=(canUse && canGrant ? ", " : "")%><%=canGrant?"can grant":""%>
        </td>
      </tr>

      <tr>
        <th class="label" scope="row">
          Status:
        </th>
        <td>
          <%=Common.displayStatusForDetailPopup(currentAssignment)%>
        </td>
      </tr>

      <tr>
      	<th class="label" scope="row">Effective:</th>
      	<td><%=dateFormat.format(currentAssignment.getEffectiveDate())%> </td>
     	</tr>
      <tr>
        <th class="label" scope="row">Granted on:</th>
        <td><!-- DATE/TIME GOES HERE --> </td>
      </tr>
      <tr>
        <th class="label" scope="row">
          Granted by:
        </th>
        <td>
          <%=(proxy==null ? "" : (proxy.getName() + ", acting as ")) + grantor.getName()%>
        </td>
      </tr>
      
    </table>
  </div>
	
  </body>
</html>