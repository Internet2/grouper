<!--
  $Id: main.jsp,v 1.11 2005-02-24 22:17:55 acohen Exp $
  $Date: 2005-02-24 22:17:55 $
  
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

  <form name="form1" method="post" action="">
    <div id="Header">  
      <div id="Logo">
      <img src="images/organisation-logo.jpg" width="83" height="60" alt="logo" />      </div> 
      <!-- Logo -->
      <div id="Signet">
        <img src="images/signet.gif" alt="Signet" height="60" width="49">
      </div> <!-- Signet -->
    </div> <!-- Header -->
  
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
  
<!--
 <div id="Navbar"><span class="logout"><a href="blank.html">User Name: Logout</a></span><span class="select">Home</span></div>
-->
  
  <div id="Layout">
    <div id="Content"> 
      <div class="table1"> 
        <div class="tableheader">
          <a
            style="float: right;"
            href="javascript:;"
            onclick="alert('This will download the data shown in the table in an Excel-readable format.')">
            <img
              src="images/icon_spread.gif"
              width="20"
              height="20"
              class="icon"
              style="margin-left: 10px;" />
            Export to Excel
          </a>
          <a
            style="float: right;"
            href="MainPrint.do">
            <img
              src="images/icon_printsion.gif"
              width="21"
              height="20"
              class="icon" />
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

        </div>
        <div class="tablecontent"> 
          <table class="full">            
            <tr> 
              <td nowrap="nowrap" class="columnhead">
                <img
                  src="images/icon_down_unsel.gif"
                  alt="[sort by]"
                  width="17"
                  height="17"
                  border="0" />
                Person
              </td>
              <td nowrap="nowrap" class="columnhead">
                Privilege
              </td>
              <td nowrap="nowrap" class="columnhead">Scope</td>
              <td nowrap="nowrap" class="columnhead">
                Limits
              </td>
              <td nowrap="nowrap" class="columnhead">
                Status
              </td>
              <td nowrap="nowrap" class="columnhead">
                Granted
              </td>
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
              <td> <!-- person -->
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
                        ('Assignment.do?assignmentId=<%=assignment.getNumericId()%>',
                         'popup',
                         'scrollbars=yes,
                         resizable=yes,
                         width=500,
                         height=100');">
                  <img
                    src="images/info.gif"
                    width="20"
                    height="20" />
                </a>
                <%=subsystem.getName()%> : <%=category.getName()%> : <%=function.getName()%>
              </td> <!-- privilege -->
              
              <td> <!-- scope -->
                &nbsp;
              </td> <!-- scope -->
              
              <td> <!-- limits -->
                <%=Common.displayLimitValues(assignment)%>
              </td> <!-- limits -->
              
              <td> <!-- status -->
<%=
  assignment.getStatus().getName()
  + (assignment.isGrantOnly()==false?", can use":"")
  + (assignment.isGrantable()?", can grant":"")
%>
              </td> <!-- status -->
              <td nowrap="nowrap">
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
        </div>
      </div>
      <jsp:include page="footer.jsp" flush="true" />
    </div>
    <div id="Sidebar">
			<div class="findperson"> 
        <h2>
          find a person
        </h2> 
        <p>
          <input
              name="words"
              type="text"
              class="short"
              id="words"
              style="width:100px"
              size="15"
              maxlength="500" />
          <input
              name="searchbutton"
              type="button"
              class="button1"
              onclick="javascript:showResult();"
              value="Search" />
          <br />
          <span class="dropback">Enter a person's name, and click "Search."
          </span> </p>
        <div id="Results" style="display:none">
          Your search found:
         	<div class="scroll">
<%
  Set privilegedSubjects
    = signet.getPrivilegedSubjects();
      
  SortedSet sortSet = new TreeSet(privilegedSubjects);
  Iterator sortSetIterator = sortSet.iterator();
  while (sortSetIterator.hasNext())
  {
    PrivilegedSubject listSubject
      = (PrivilegedSubject)(sortSetIterator.next());
%>
            <br />
            <a href="PersonView.do?granteeSubjectTypeId=<%=listSubject.getSubjectTypeId()%>&granteeSubjectId=<%=listSubject.getSubjectId()%>">
              <%=listSubject.getName()%>
            </a>
            <br />
            <!--Stanford Linear Accelerator Center, --><span class="dropback"><%=listSubject.getDescription()%></span>
<%
  }
%>
						</div> <!-- scroll -->
          </div>
        <!-- results -->
          <!-- actionbox -->
      </div>
			<!-- findperson -->		 
      <div class="views">
        <h2>
          View privileges...
        </h2> 
        <p>
          <a
              href="PersonView.do?granteeSubjectTypeId=<%=loggedInPrivilegedSubject.getSubjectTypeId()%>&granteeSubjectId=<%=loggedInPrivilegedSubject.getSubjectId()%>">
            <img
                src="images/icon_arrow_right.gif"
                width="16"
                height="16"
                class="icon" />
            assigned to you
          </a>
        </p>
        <p>
          <a href="NotYetImplemented.do">
            <img
                src="images/icon_arrow_right.gif"
                width="16"
                height="16"
                class="icon" />
            by scope
          </a>
        </p>
          <!-- actionbox -->
      </div> <!-- views-->
	    <div class="helpbox">
       <h2>
          Help
       </h2>
				<jsp:include page="main-help.jsp" flush="true" />
      </div> <!-- helpbox-->	
      
  </div> <!-- Sidebar -->
 </div>	
</form>
</body>
</html>
