<!--
  $Id: assignment.jsp,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
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

<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.tree.TreeNode" %>
<%@ page import="edu.internet2.middleware.signet.Signet" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
   Assignment currentAssignment
     = (Assignment)
         (request.getSession().getAttribute("currentAssignment"));
         
   boolean canUse = !(currentAssignment.isGrantOnly());
   boolean canGrant = currentAssignment.isGrantable();
         
   DateFormat dateFormat = DateFormat.getDateInstance();
%>
    <div class="tableheader">
      <%=currentAssignment.getSubsystem().getName()%>
      :
      <%=currentAssignment.getFunction().getCategory().getName()%>
      :
      <%=currentAssignment.getFunction().getName()%>
    </div>
    <table border="0">
	    <tr>
        <td class="dropback">
          Scope:
        </td>
        <td>
          <%=currentAssignment.getScope().getName()%>
        </td>
      </tr>
      <tr>
        <td class="dropback">
          Extensibility:
        </td>
        <td>
          <%=canUse?"can use":""%><%=(canUse && canGrant ? ", " : "")%><%=canGrant?"can grant":""%>
        </td>
      </tr>
    </table>
  </body>
</html>