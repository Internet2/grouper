<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: main.jsp,v 1.42 2005-09-15 21:08:18 jvine Exp $
  $Date: 2005-09-15 21:08:18 $
  
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
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Status" %>
<%@ page import="edu.internet2.middleware.signet.Proxy" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
   PrivilegedSubject loggedInPrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
         
   PrivilegedSubject actingAs
     = (PrivilegedSubject)
         (request.getSession().getAttribute(Constants.ACTINGAS_ATTRNAME));
         
   DateFormat dateFormat = DateFormat.getDateInstance();
%>

    <tiles:insert page="/tiles/header.jsp" flush="true" />
    <div id="Navbar">
      <span class="logout">
        <%=Common.displayLogoutHref(request)%>
      </span> <!-- logout -->
      <span class="select">
        Home
      </span> <!-- select -->
    </div> <!-- Navbar -->
  
  <div id="Layout">
    <div id="Content"> 
	<div id="ViewHead">
	  	<span class="dropback">
         Privileges overview for</span> 
         <h1>
           <%=loggedInPrivilegedSubject.getName()%>
         </h1>
         <span class="ident">
           <%=loggedInPrivilegedSubject.getDescription()%>
         </span> 
       </div>
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
                Subject
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
        (loggedInPrivilegedSubject.getAssignmentsGranted(null, null, null));
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
                <%=Common.assignmentPopupIcon(assignment)%>
                <%=subsystem.getName()%> : <%=category.getName()%> : <%=function.getName()%>
              </td> <!-- privilege -->
              
              <td> <!-- scope -->
                 <%=assignment.getScope().getName()%>
              </td> <!-- scope -->
              
              <td> <!-- limits -->
                <%=Common.editLink(loggedInPrivilegedSubject, assignment)%>
                <%=Common.displayLimitValues(assignment)%>
              </td> <!-- limits -->
              
              <td> <!-- status -->
                <%=Common.displayStatus(assignment)%>
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

    <form
      name="personSearchForm"
      method="post"
      action="" 
      onsubmit ="return checkForCursorInPersonSearch('personQuickSearch.jsp', 'words', 'PersonSearchResults')">
        <div class="findperson">
        <h2>
          find a subject
        </h2> 
        <p>
          <input
            name="words"
            type="text"
            class="short"
            id="words"
            style="width:100px"
            size="15"
            maxlength="500"
            onfocus="personSearchFieldHasFocus=true;"
            onblur="personSearchFieldHasFocus=false;" />
          <input
            name="searchbutton"
            type="submit"
            class="button1"
            value="Search"
            onclick="personSearchButtonHasFocus=true;"
            onfocus="personSearchButtonHasFocus=true;"
            onblur="personSearchButtonHasFocus=false;" />
          <br />
          <label for="words">
            Enter a subject's name, and click "Search."
          </label>
       	</p>
        <div id="PersonSearchResults" style="display:none">
        </div> <!-- PersonSearchResults -->
		    <p>
              <a href="PersonView.do?granteeSubjectTypeId=<%=loggedInPrivilegedSubject.getSubjectTypeId()%>&granteeSubjectId=<%=loggedInPrivilegedSubject.getSubjectId()%>">
                <img
                     src="images/arrow_right.gif"
                       alt="" />
            View <%= loggedInPrivilegedSubject.getName()%>'s privileges</a>
			</p>		
      </div><!-- findperson -->		
    </form> <!-- personSearchForm -->   
      
<div class="findperson"> 
        <form
          name="actAsForm"
          method="post"
          action="ActAs.do">
          <h2>Designated Drivers</h2>
          <div class="actionbox">
            <%=Common.displayActingForOptions
                 (loggedInPrivilegedSubject,
                  actingAs,
                  Constants.ACTING_FOR_SELECT_ID)%>
            <br/>
            <a href='Designate.do'>
			<img src="images/arrow_right.gif" alt="" />
			Designate a granting proxy</a>
          </div> <!-- actionbox -->
        </form>
      </div> <!-- findperson -->
      
    </div>
    <!-- Sidebar -->
 </div>	<!-- Layout -->
</body>
</html>
