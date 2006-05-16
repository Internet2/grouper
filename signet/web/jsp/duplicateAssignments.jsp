<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: duplicateAssignments.jsp,v 1.2 2006-05-16 17:37:35 ddonn Exp $
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
  <script language="JavaScript" type="text/javascript" src="scripts/signet.js"></script>

<!-- not called from anywhere
	<script language="JavaScript">
	function ButtonChange() {
		if (document.dupForm.checkAssign.checked == true) {
			document.dupForm.complButton.value = "<%=ResLoaderUI.getString("duplicateAssignments.complete_1.bt") %>";
			}
		else {
			document.dupForm.complButton.value = "<%=ResLoaderUI.getString("duplicateAssignments.complete_2.bt") %>";
			}
	}
	</script>
-->
</head>

<body>

<%@ page import="java.util.Set" %>
<%@ page import="java.util.Iterator" %>
  
<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.tree.TreeNode" %>

<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>
<%@ page import="edu.internet2.middleware.signet.ui.UnusableStyle" %>

<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<tiles:insert page="/tiles/header.jsp" flush="true" />

<%
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
  PrivilegedSubject loggedInPrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
      
  PrivilegedSubject currentGranteePrivilegedSubject
    = (PrivilegedSubject)
        (request
           .getSession()
             .getAttribute
               (Constants.CURRENTPSUBJECT_ATTRNAME));
               
  Assignment currentAssignment
    = (Assignment)(request.getSession().getAttribute("currentAssignment"));
    
  Set duplicateAssignments
    = (Set)(request.getSession().getAttribute("duplicateAssignments"));
   
  String personViewHref
    = "PersonView.do?granteeSubjectTypeId="
      + currentGranteePrivilegedSubject.getSubjectTypeId()
      + "&granteeSubjectId="
      + currentGranteePrivilegedSubject.getSubjectId()
      + "&subsystemId="
      + currentAssignment.getFunction().getSubsystem().getId();
%>

      <div id="Navbar">
        <span class="logout">
          <%=Common.displayLogoutHref(request)%>
        </span> <!-- logout -->
        <span class="select">
          <a href="Start.do?<%=Constants.CURRENTPSUBJECT_HTTPPARAMNAME%>=<%=Common.buildCompoundId(loggedInPrivilegedSubject.getEffectiveEditor())%>">
            <%=Common.homepageName(loggedInPrivilegedSubject)%>
          </a>
          &gt; <!-- displays as text right-angle bracket -->
          <a href="<%=personViewHref%>"><%=ResLoaderUI.getString("duplicateAssignments.subjview.txt") %> 
            [<%=currentGranteePrivilegedSubject.getName()%>]
          </a>          
        </span> <!-- select -->
      </div>  <!-- Navbar -->

  
  <div id="Layout"> 
    <div id="Content">
      <div id="ViewHead">
			<span class="dropback"><%=ResLoaderUI.getString("duplicateAssignments.grantingnew.txt") %></span>           	
        <h1>
          <%=currentGranteePrivilegedSubject.getName()%>
       	</h1>
       	<span class="ident"><%=currentGranteePrivilegedSubject.getDescription()%></span><!--,	Technology Strategy and Support Operations-->
      </div>  <!-- ViewHead -->
			
<div class="alert">
<p><img src="images/caution.gif" align="left" /><%=ResLoaderUI.getString("duplicateAssignments.alert_1.txt") %> </p>

<ul>
	<li><%=ResLoaderUI.getString("duplicateAssignments.alert_2.txt") %></li>
  <li><%=ResLoaderUI.getString("duplicateAssignments.alert_3.txt") %></li>
  </ul>
<p><%=ResLoaderUI.getString("duplicateAssignments.alert_4.txt") %></p>

</div>

<div class="section">
<h2><%=ResLoaderUI.getString("duplicateAssignments.review_1.hdr") %><span class="status"> <%=ResLoaderUI.getString("duplicateAssignments.review_2.hdr") %></span></h2>
	<table class="full" style="margin-left: 75px;">
	<tr>
		<th><%=ResLoaderUI.getString("duplicateAssignments.priv.lbl") %></th>
		<th><%=ResLoaderUI.getString("duplicateAssignments.scope.lbl") %></th>
		<th><%=ResLoaderUI.getString("duplicateAssignments.limits.lbl") %></th>
		<th><%=ResLoaderUI.getString("duplicateAssignments.status.lbl") %></th>
		</tr>
    <tr >
      <td>
        <%=currentAssignment.getFunction().getCategory().getName()%>  : <%=currentAssignment.getFunction().getName()%>
      </td>
		<td class="hierarchy"><%=currentAssignment.getScope().getName()%></td>
		<td><table class="invis">
          <%=Common.displayLimitValues(currentAssignment)%>
		</table></td>
		<td>
		  <%=Common.displayStatus(currentAssignment)%>
        </td>
		</tr>
</table>
</div>

<form
  action="RevokeAndGrant.do"
  method="post"
  name="dupForm"
  id="dupForm">
  
  <div class="section">
    <h2>check any Existing assignment(s) you want to replace</h2>
    <table class="full">
      <tr>
        <th width="50" align="center"> <%=ResLoaderUI.getString("duplicateAssignments.dupform_1.lbl") %></th>
		<th><%=ResLoaderUI.getString("duplicateAssignments.priv.lbl") %></th>
		<th><%=ResLoaderUI.getString("duplicateAssignments.scope.lbl") %></th>
		<th><%=ResLoaderUI.getString("duplicateAssignments.limits.lbl") %></th>
		<th><%=ResLoaderUI.getString("duplicateAssignments.status.lbl") %></th>
      </tr>
      
<%
  Iterator duplicateAssignmentsIterator = duplicateAssignments.iterator();
  while (duplicateAssignmentsIterator.hasNext())
  {
    Assignment dup = (Assignment)(duplicateAssignmentsIterator.next());
%>
      <tr>
        <%=Common.revokeBox(loggedInPrivilegedSubject, dup, UnusableStyle.TEXTMSG)%>
        <td>
          <%=Common.assignmentPopupIcon(dup)%>
          <%=dup.getFunction().getCategory().getName()%>  : <%=dup.getFunction().getName()%>
        </td>
		<td class="hierarchy">
		  <%=dup.getScope().getName()%></td>
		<td>
		  <table class="invis">
            <%=Common.displayLimitValues(dup)%>
		  </table>
		</td>
		<td>
		  <%=Common.displayStatus(dup)%>
        </td>
      </tr>
<%
  }
%>
    </table>
    <p>&nbsp;</p>
  </div> <!-- section -->				

  <div class="section">
    <h2>
      <a name="complete" id="complete"></a>
      <%=ResLoaderUI.getString("duplicateAssignments.completeassign.hdr") %>
    </h2>

    <input
      name="complButton"
      type="submit"
      class="button-def"
      id="complButton"
      value="<%=ResLoaderUI.getString("duplicateAssignments.complete_2.bt") %>" />
    <br />
    
    <a href="<%=personViewHref%>">
      <img src="images/arrow_left.gif" />
      <%=ResLoaderUI.getString("duplicateAssignments.cancel.txt") %> <%=ResLoaderUI.getString("duplicateAssignments.subjview.txt") %> [<%=currentGranteePrivilegedSubject.getName()%>]</a>
  </div>

     	</form>

       <tiles:insert page="/tiles/footer.jsp" flush="true" />
   	</div>
    <!-- Content -->

      <div id="Sidebar">
      	<div class="helpbox">
      		<h2><%=ResLoaderUI.getString("duplicateAssignments.help.hdr") %></h2>
      			<p><%=ResLoaderUI.getString("duplicateAssignments.help_1.txt") %></p>
                <p><%=ResLoaderUI.getString("duplicateAssignments.help_2.txt") %></p>
                <p><%=ResLoaderUI.getString("duplicateAssignments.help_3.txt") %></p>
                <p><%=ResLoaderUI.getString("duplicateAssignments.help_4.txt") %> <img src="images/maglass.gif" alt="" style="vertical-align:top;" /> </p>
   		</div>
   	</div>  <!-- Sidebar -->
		
</div> <!-- Layout -->

</body>
</html>