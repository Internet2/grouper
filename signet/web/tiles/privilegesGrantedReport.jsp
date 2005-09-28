<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.util.Set" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="java.util.Iterator" %>

<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>

<tiles:useAttribute name="pSubject" classname="PrivilegedSubject" />

<div id="Content"> 
  <div id="ViewHead">
    <span class="dropback">
      Privileges overview for</span> 
      <h1>
        <%=pSubject.getName()%>
      </h1>
      <span class="ident">
        <%=pSubject.getDescription()%>
      </span> 
    </div> <!-- WRONG TAG HERE ->

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
        (pSubject.getAssignmentsGranted(null, null, null));
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
            <%=Common.editLink(pSubject, assignment)%>
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