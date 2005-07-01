<!--
  $Id: main.jsp,v 1.29 2005-07-01 23:06:52 acohen Exp $
  $Date: 2005-07-01 23:06:52 $
  
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
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Status" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
   PrivilegedSubject loggedInPrivilegedSubject
     = (edu.internet2.middleware.signet.PrivilegedSubject)
         (request.getSession().getAttribute("loggedInPrivilegedSubject"));
         
   DateFormat dateFormat = DateFormat.getDateInstance();
%>

  <form
    name="form1"
    method="post"
    action="" 
    onsubmit ="return checkForCursorInPersonSearch()">
    <tiles:insert page="/tiles/header.jsp" flush="true" />
    <div id="Navbar">
      <span class="logout">
        <a href="NotYetImplemented.do">
          <%= loggedInPrivilegedSubject.getName() %>: Logout
        </a>
      </span> <!-- logout -->
      <span class="select">
        Home
      </span> <!-- select -->
    </div> <!-- Navbar -->
  
  <div id="Layout">
    <div id="Content"> 
        <div class="tableheader">
          <a
            href="javascript:;"
            onclick="alert('This will download the data shown in the table in an Excel-readable format.')">
            <img
              src="images/export.gif"
              alt="" />
            Export to Excel
          </a>
          <a
            href="MainPrint.do">
            <img
              src="images/print.gif"
              alt="" />
            Printable version
          </a>
          <h2>Privileges you have granted</h2>

<!--
          <select name="subsystem" class="long" id="subsystem">
            <option selected="selected">ALL</option>
            <option>in the past 7 days</option>
            <option>in the past 30 days</option>
            <option>that will expire within 7 days</option>
            <option>that have changed in the past 7 days</option>
            <option>that are not yet active (pending)</option>
            <option>that are inactive (history)</option>
          </select>
          <input name="Submit" type="submit" class="button1" value="Show" />
-->

        </div> <!-- tableheader -->
        <div class="tablecontent"> 
          <table>            
            <tr class="columnhead"> 
              <th>
                Person
							</th>
              <th width="30%">
                Privilege
              </th>
              <th width="20%">
								Scope
							</th>
              <th>
                Limits
              </th>
              <th>
                Status
              </th>
              <th>
                Granted
              </th>
            </tr>
	  
<%
  Set assignmentSet
    = new TreeSet
        (loggedInPrivilegedSubject.getAssignmentsGranted(Status.ACTIVE, null));
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
              <td class="sorted"> <!-- person -->
                <a
                  href="PersonView.do?granteeSubjectTypeId=<%=grantee.getSubjectTypeId()%>&granteeSubjectId=<%=grantee.getSubjectId()%>&subsystemId=<%=subsystem.getId()%>">
                  <%=grantee.getName()%>
                </a>
              </td> <!-- person -->
              
              <td> <!-- privilege -->
                <a
                  style="float: right;"
                  href
                    ="javascript:openWindow
                        ('Assignment.do?assignmentId=<%=assignment.getId()%>',
                         'popup',
                         'scrollbars=yes,
                         resizable=yes,
                         width=500,
                         height=250');">
                  <img
                    src="images/maglass.gif"
										alt="More info about this assignment..." />
                </a>
                <%=subsystem.getName()%> : <%=category.getName()%> : <%=function.getName()%>
              </td> <!-- privilege -->
              
              <td> <!-- scope -->
                 <%=assignment.getScope().getName()%>
              </td> <!-- scope -->
              
              <td> <!-- limits -->
                 <a
                   style="float: right;"
                   href="Conditions.do?assignmentId=<%=assignment.getId()%>">
                   <img
                     src="images/arrow_right.gif"
                       alt="" />
                   edit
                 </a>
                <%=Common.displayLimitValues(assignment)%>
              </td> <!-- limits -->
              
              <td> <!-- status -->
<%=
  assignment.getStatus().getName()
  + (assignment.isGrantOnly()==false?", can use":"")
  + (assignment.isGrantable()?", can grant":"")
%>
              </td> <!-- status -->
              <td class="date">
<%=
  // assignment.getCreateDateTime() is no longer supported. Eventually,
  // I'll need to remove this reference a little more completely.
  // dateFormat.format(assignment.getCreateDateTime())
  ""
%>
              </td>
            </tr>
  	
<% 
  }
%>
	
            
          </table>
      </div> <!-- tablecontent -->
    </div> <!-- Content -->
    <tiles:insert page="/tiles/footer.jsp" flush="true" />
    <div id="Sidebar">
			<div class="findperson"> 
        <h2>
          find a subject </h2> 
          <p>
          	<input
              name="words"
              type="text"
              class="short"
              id="words"
              style="width:100px"
              size="15"
              maxlength="500"
              onFocus="personSearchFieldHasFocus=true;"
              onBlur="personSearchFieldHasFocus=false;" />
          	<input
              name="searchbutton"
              type="button"
              class="button1"
              onclick="javascript:loadXMLDoc('personQuickSearch.jsp?searchString=' + document.getElementById('words').value);"
              value="Search"
              onFocus="personSearchButtonHasFocus=true;"
              onBlur="personSearchButtonHasFocus=false;" />
			<br />
          	<label for="words">Enter a subject's name, and click "Search."
          	</label>
       	</p>
        <div id="PersonSearchResults" style="display:none">
        </div> <!-- PersonSearchResults -->
      </div><!-- findperson -->		 
      <div class="views">
 		    <h2>
          View privileges...
        </h2> 
        <p>
          <a
              href="PersonView.do?granteeSubjectTypeId=<%=loggedInPrivilegedSubject.getSubjectTypeId()%>&granteeSubjectId=<%=loggedInPrivilegedSubject.getSubjectId()%>">
            <img
                src="images/arrow_right.gif"
                alt="" />
            assigned to you
          </a>
        </p>
        <p>
          <a href="NotYetImplemented.do">
            <img
                src="images/arrow_right.gif"
                alt="" />
            by scope
          </a>
        </p>
      </div> <!-- views-->
	    <div class="helpbox">
       <h2>
          Help
       </h2>
				<jsp:include page="main-help.jsp" flush="true" />
      </div> <!-- helpbox-->	
      
  </div> <!-- Sidebar -->
 </div>	<!-- Layout -->
</form>
</body>
</html>
