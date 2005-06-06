<!--
  $Id: assignment.jsp,v 1.11 2005-06-06 23:30:11 jvine Exp $
  $Date: 2005-06-06 23:30:11 $
  
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
         
  boolean canUse = !(currentAssignment.isGrantOnly());
  boolean canGrant = currentAssignment.isGrantable();
         
  DateFormat dateFormat = DateFormat.getDateInstance();
%>
    <div class="section">
      <h2>
      <%=currentAssignment.getFunction().getSubsystem().getName()%>
      </h2>
			<p>
  	    <span class="category"><%=currentAssignment.getFunction().getCategory().getName()%> : 
				</span>
				<span class="function">
					<%=currentAssignment.getFunction().getName()%>
				</span>
			</p>
    
    <p><%=currentAssignment.getFunction().getHelpText()%>
 	</p>
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
          Granted by:
        </td>
        <td>
          <%=grantor.getName()%>
        </td>
      </tr>
    
      <tr>
        <td class="label">
          On:
        </td>
        <td>
          <%=dateFormat.format(currentAssignment.getEffectiveDate())%>
        </td>
      </tr>
    
      <tr>
        <td class="label">
          Scope:
        </td>
        <td>
          <%=currentAssignment.getScope().getName()%>
        </td>
      </tr>

<%
  Limit[] limits = currentAssignment.getFunction().getLimitsArray();
  LimitValue[] limitValues = currentAssignment.getLimitValuesInDisplayOrder();
  for (int limitIndex = 0; limitIndex < limits.length; limitIndex++)
  {
    Limit limit = limits[limitIndex];
%>
      <tr>
        <td class="label">
          <%=limit.getName()%>:
        </td>
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
        <td class="label">
          Extensibility:
        </td>
        <td>
          <%=canUse?"can use":""%><%=(canUse && canGrant ? ", " : "")%><%=canGrant?"can grant":""%>
        </td>
      </tr>
      
    </table>
	    </div>
	
  </body>
</html>