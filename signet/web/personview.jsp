<!--
  $Id: personview.jsp,v 1.8 2005-02-24 19:15:24 acohen Exp $
  $Date: 2005-02-24 19:15:24 $
  
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
  <script type="text/javascript">
    function selectThis(isChecked)
    {
      var theCheckAllBox = document.checkform.checkAll;
      if (!isChecked)
      {
        theCheckAllBox.checked = false;
      }
      
      if (selectCount() > 0)
      {
        document.checkform.revokeButton.disabled = false;
      }
      else
      {
        document.checkform.revokeButton.disabled = true;
      }
    }
    
    function selectAll(isChecked)
    {
      var theForm = document.checkform;

      for (var i = 0; i < theForm.elements.length; i++)
      {
        if (theForm.elements[i].name != 'checkAll'
            && theForm.elements[i].type == 'checkbox'
            && theForm.elements[i].disabled == false)
        {
          theForm.elements[i].checked = isChecked;
        }
      }
      
      if (selectCount() > 0)
      {
        document.checkform.revokeButton.disabled = false;
      }
      else
      {
        document.checkform.revokeButton.disabled = true;
      }
    }
    
    function selectCount()
    {
      var theForm = document.checkform;
      var count = 0;
      
      for (var i = 0; i < theForm.elements.length; i++)
      {
        if ((theForm.elements[i].name != 'checkAll')
            && (theForm.elements[i].type == 'checkbox')
            && (theForm.elements[i].checked == true))
        {
          count++;
        }
      }
      
      return count;
    }
  </script>

<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="java.util.HashSet" %>

<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.Status" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
   PrivilegedSubject loggedInPrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute("loggedInPrivilegedSubject"));
   
   PrivilegedSubject currentGranteePrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute("currentGranteePrivilegedSubject"));
         
   Subsystem currentSubsystem
     = (Subsystem)
         (request.getSession().getAttribute("currentSubsystem"));
         
   Set grantableSubsystems = loggedInPrivilegedSubject.getGrantableSubsystems();
         
   DateFormat dateFormat = DateFormat.getDateInstance();
%>
  <div id="Header">  
    <div id="Logo">
      <img src="images/KITN.gif" width="216" height="60" alt="logo" />
    </div> <!-- Logo -->
    <div id="Signet">
      <img
        src="images/signet.gif"
        alt="Signet"
        height="60"
        width="49">
    </div> <!-- Signet -->
  </div> <!-- Header -->
    
  <div id="Navbar">
    <span class="logout">
      <a href="NotYetImplemented.do">
        <%= loggedInPrivilegedSubject.getName() %>: Logout
      </a>
    </span> <!-- logout -->
    <span class="select">
      <a href="Start.do">
        Home
      </a>
      > <%=currentGranteePrivilegedSubject.getName()%>
    </span> <!-- select -->
  </div> <!-- Navbar -->
  
  <div id="Layout"> 
    <div id="Content">
      <div id="ViewHead">
        Privileges assigned to
         <h1>
           <%=currentGranteePrivilegedSubject.getName()%>
         </h1>
         <span class="dropback">
           <%=currentGranteePrivilegedSubject.getDescription()%>
         </span> <!-- dropback -->  
       </div>
       <div class="tableheader">
         <form name="pickSubsystem" action="PersonView.do">
           <!-- the following two a tags must be within the form tag, but
                 before the generated text that appears in the tableheader row -->
           <a
              style="float: right;"
              href="javascript:;"
              onClick="alert('This will download the data shown in the table in an Excel-readable format.')">
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
              href="PersonViewPrint.do">
             <img
                src="images/icon_printsion.gif"
                width="21"
                height="20"
                class="icon" />
             Printable version
           </a>
           <h2><%=(currentSubsystem == null ? "NO ASSIGNED" : currentSubsystem.getName())%> Privileges</h2>
          <fieldset>
           <select
              name="subsystemId"
              id="subsystem">
  
            <option
                selected="selected"
                onClick="javascript:document.pickSubsystem.showSubsystemPrivs.disabled=true">
               (assigned privilege types)
             </option>
                
<%
  Set assignmentsReceived
    = new TreeSet
        (currentGranteePrivilegedSubject
          .getAssignmentsReceived(Status.ACTIVE, null));
  Set subsystemsOfReceivedAssignments = new HashSet();
  Iterator assignmentsReceivedIterator
    = assignmentsReceived.iterator();
  while (assignmentsReceivedIterator.hasNext())
  {
    Assignment receivedAssignment
      = (Assignment)(assignmentsReceivedIterator.next());
    subsystemsOfReceivedAssignments.add
      (receivedAssignment.getFunction().getSubsystem());
  }

  Iterator subsystemsIterator
    = subsystemsOfReceivedAssignments.iterator();
  while (subsystemsIterator.hasNext())
  {
    Subsystem subsystem = (Subsystem)(subsystemsIterator.next());
%>
             <option
                value="<%=subsystem.getId()%>"
                onClick="javascript:document.pickSubsystem.showSubsystemPrivs.disabled=false">
               <%=subsystem.getName()%>
             </option>
                
<%
  }
%>
  
          </select>
                      
          <input
              class="button1"
              disabled="true"
              type="submit"
              name="showSubsystemPrivs"
              value="Show"/>
            </fieldset>  
         </form> <!-- pickSubsystem -->
       </div>
       <!-- tableheader -->
              
      <form
          onSubmit
            ="return confirm
               ('Are you sure you want to revoke the '
                + (selectCount() == 1 ? '' : selectCount())
                + ' selected assignment'
                + (selectCount() > 1 ? 's' : '')
                + '?'
                + ' This action cannot be undone.'
                + ' Click OK to confirm.');"
          action="Revoke.do"
          method="post"
          name="checkform"
          id="checkform">
         <div class="tablecontent">
           <table class="full">
             <tr class="columnhead">
               <td width="30%">
                 <img
                    src="images/icon_down_unsel.gif"
                    width="17"
                    height="17" />
                 Privilege
               </td>
               <td width="20%">
                 Scope
               </td>
               <td>
                 Limits
               </td>
               <td width="10%" align="left">Status</td>
               <td width="10%" align="left">
                 All:
                 <input
                    name="checkAll"
                    type="checkbox"
                    id="checkAll"
                    onClick="selectAll(this.checked);"
                    value="Check All" />
               </td>
             </tr>
                  
<%
  if (currentSubsystem != null)
  {
    Set assignmentsReceivedForCurrentSubsystem
      = new TreeSet
          (currentGranteePrivilegedSubject
            .getAssignmentsReceived(Status.ACTIVE, currentSubsystem));
    Iterator assignmentsIterator
      = assignmentsReceivedForCurrentSubsystem.iterator();
    while (assignmentsIterator.hasNext())
    {
      Assignment assignment = (Assignment)(assignmentsIterator.next());
%>
  
             <tr>
               <td> <!-- privilege -->
                 <a
                   style="float: right;"
                   href="javascript:openWindow
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
                 <%=assignment.getFunction().getCategory().getName()%>
                 : 
                 <%=assignment.getFunction().getName()%>
               </td> <!-- privilege -->
               
               <td> <!-- scope -->
                 <%=assignment.getScope().getName()%>
               </td> <!-- scope -->
               
               <td> <!-- limits -->
                 <a
                   style="float: right;"
                   href="NotYetImplemented.do">
                   <img
                     src="images/icon_arrow_right.gif"
                       width="16"
                       height="16"
                       class="icon" />
                   edit
                 </a>
                <%=Common.displayLimitValues(assignment)%>
               </td> <!-- limits -->
               
                 <td align="center" >&nbsp;</td>
                 <td align="center" >
                   <input
                      name="revoke"
                      type="checkbox"
                      id="<%=assignment.getNumericId()%>"
                      value="<%=assignment.getNumericId()%>"
                      <%=(loggedInPrivilegedSubject.canEdit(assignment) ? "" : "disabled=\"true\"")%>
                      onClick="selectThis(this.checked);">
                 </td>
               </tr>
                  
<%
    }
  }
%>
  
              <tr >
                 <td>&nbsp;
                      
                </td>
                 <td>&nbsp;
                      
                </td>
                 <td >&nbsp;
                      
                </td>
                 <td align="center" >&nbsp;</td>
                 <td align="center" >
                   <input
                      name="revokeButton"
                      type="submit"
                      disabled="true"
                      class="button1"
                      value="Revoke" />
                 </td>
               </tr>
             </table>
         </div> <!-- tablecontent -->
       </form>
       <!-- checkform -->
          <!-- table1 -->
  <jsp:include page="footer.jsp" flush="true" />
     </div><!-- Content -->
      <div id="Sidebar">
        
<% 
  if (!(currentGranteePrivilegedSubject.equals(loggedInPrivilegedSubject)))
  {
%>

        <div class="grant">
          <h2>
            Grant to <%=currentGranteePrivilegedSubject.getName()%>
          </h2>
          <form action="Functions.do">
            <p>
              <span class="tableheader">
                <select name="select" class="long">
<!--
                    <option selected="selected">
                      (privileges you can grant)
                    </option>
-->

<%
    Iterator grantableSubsystemsIterator = grantableSubsystems.iterator();
    while (grantableSubsystemsIterator.hasNext())
    {
      Subsystem subsystem = (Subsystem)(grantableSubsystemsIterator.next());
%>
                  <option value="<%=subsystem.getId()%>">
                    <%=subsystem.getName()%>
                  </option>
<%
    }
%>
                </select>
              </span> <!-- tableheader --> 
              <input
                  type="submit"
                  name="Button"
                  class="button1"
                  <%=grantableSubsystems.size()==0 ? "disabled=\"disabled\"" : ""%>
                  value="Start &gt;&gt;" />
                <br />
            <span class="dropback">Select the type of privilege you want to grant, then click "Start." The list of privilege types shows only those you are authorized to grant.</span></p>
              <!-- actionbox -->
          </form>
        </div> <!-- grant -->
          
<%
  }
%>
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
                  onClick="javascript:showResult();"
                  value="Search" /> 
             <br />
             <span class="dropback">
               Enter a person's name, and click "Search."
              </span>
           </p>
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
               <span class="dropback"><%=listSubject.getDescription()%></span>
<%
  }
%>
              </div> <!-- scroll -->
             </div>           <!-- results -->
         </div>         <!-- findperson -->         
          <div class="views">
            <h2>
              View privileges...
            </h2>
            <p>
              <a href="Start.do">
                <img
                    src="images/icon_arrow_right.gif"
                    width="16"
                    height="16"
                    class="icon" />
                you have granted
              </a>
            </p>
            <p>
              <a href="PersonView.do?granteeSubjectTypeId=<%=loggedInPrivilegedSubject.getSubjectTypeId()%>&granteeSubjectId=<%=loggedInPrivilegedSubject.getSubjectId()%>">
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
        </div> <!-- views -->
            
          
        <div class="helpbox">
           <h2>
            Help
          </h2>
          <jsp:include page="personview-help.jsp" flush="true" />
        </div> <!-- helpbox-->  
      </div> 
      <!-- Sidebar -->
</div> <!-- Layout -->
  </body>
</html>
