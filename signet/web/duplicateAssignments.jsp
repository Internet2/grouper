<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: duplicateAssignments.jsp,v 1.12 2005-11-22 20:18:13 acohen Exp $
  $Date: 2005-11-22 20:18:13 $
  
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

	<script language="JavaScript">
	function ButtonChange() {
		if (document.dupForm.checkAssign.checked == true) {
			document.dupForm.complButton.value = "COMPLETE and replace selected assignment(s)";
			}
		else {
			document.dupForm.complButton.value = "COMPLETE";
			}
	}
	</script>
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
            <%=Constants.HOMEPAGE_NAME%>
          </a>
          &gt; <!-- displays as text right-angle bracket -->
          <a href="<%=personViewHref%>"> 
            <%=currentGranteePrivilegedSubject.getName()%>
          </a>          
        </span> <!-- select -->
      </div>  <!-- Navbar -->

  
  <div id="Layout"> 
    <div id="Content">
      <div id="ViewHead">
			<span class="dropback">Granting new privilege to</span>           	
        <h1>
          <%=currentGranteePrivilegedSubject.getName()%>
       	</h1>
       	<span class="ident"><%=currentGranteePrivilegedSubject.getDescription()%></span><!--,	Technology Strategy and Support Operations-->
      </div>  <!-- ViewHead -->
			
<div class="alert">
<p><img src="images/caution.gif" align="left" />Your new assignment is very similar to the others shown below. Review these  assignments, then: </p>

<ul>
	<li>check, under &quot;Replace&quot;, any assignments to be replaced by your new assignment (equivalent to revoking and reassigning authority), and </li>
  <li>complete this transaction using the &quot;COMPLETE&quot; button at the bottom 
  	of the page.</li>
  </ul>
<p>Or cancel your assignment by clicking the &quot;CANCEL&quot; link at the bottom of the page. </p>

</div>

<div class="section">
<h2>Review your New assignment<span class="status"> (not yet complete)</span></h2>
	<table class="full">
	<tr>
		<th width="37%">Privilege</th>
		<th width="19%">Scope</th>

		<th width="32%">Limits</th>
		<th width="12%">Status</th>
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
        <th width="7%" align="center"> Replace</th>
        <th width="30%">Privilege</th>
        <th width="19%">Scope</th>
        <th width="32%">Limits</th>
        <th width="12%">Status</th>
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
      Complete this assignment
    </h2>

    <input
      name="complButton"
      type="submit"
      class="button-def"
      id="complButton"
      value="COMPLETE" />

    <br />
    
    
    <a href="<%=personViewHref%>">
      <img src="images/arrow_left.gif" />
      CANCEL this assignment and return to  overview for <%=currentGranteePrivilegedSubject.getName()%></a>
  </div>

     	</form>

       <tiles:insert page="/tiles/footer.jsp" flush="true" />
   	</div>
    <!-- Content -->

      <div id="Sidebar">
      	<div class="helpbox">
      		<h2>help</h2>
      			<p>This assignment has been determined to be very similar to one or more existing assignments.</p>
<p>The subject's actual privilege will be the highest of any specified limits or conditions. If your intent is to decrease the limits or conditions of this privilege, you should replace any assignments with higher limits.</p>
<p>Your own limits and conditions may prevent you from replacing an assignment with higher limits and conditions. </p>

<p>You can find out who originally granted the privilege by clicking on the <img src="images/maglass.gif" alt="" style="vertical-align:top;" /> icon. </p>

   		</div>
   	</div>  <!-- Sidebar -->
		
</div> <!-- Layout -->

</body>
</html>