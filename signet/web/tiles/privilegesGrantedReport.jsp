<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.util.Set" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="java.util.Iterator" %>

<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Status" %>
<%@ page import="edu.internet2.middleware.signet.Proxy" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>
<%@ page import="edu.internet2.middleware.signet.ui.PrivDisplayType" %>
<%@ page import="edu.internet2.middleware.signet.ui.UnusableStyle" %>

<tiles:useAttribute name="pSubject"         classname="PrivilegedSubject" />
<tiles:useAttribute name="privDisplayType"  classname="PrivDisplayType" />
<tiles:useAttribute name="currentSubsystem" classname="Subsystem" />

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
    </span> <!-- dropback -->
  </div> <!-- ViewHead -->

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
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
      href="MainPrint.do">
<%
  }
  else
  {
%>
      href="PersonViewPrint.do">
<%
  }
%>
      <img
        src="images/print.gif"
        alt="" />
      Printable version
    </a>
    <h2><%=pSubject.getName()%> : <%=privDisplayType.getDescription()%><%=(currentSubsystem == Constants.WILDCARD_SUBSYSTEM ? "" : (" : " + currentSubsystem.getName()))%></h2>
  </div> <!-- tableheader -->
  
  <div class="tablecontrols">
    <form
      name="personSearchForm"
      method="post"
      action="PersonView.do">
      Change the view to show 
      <div style="display: inline;">
        <select
          name="<%=Constants.PRIVDISPLAYTYPE_HTTPPARAMNAME%>"
          id="<%=Constants.PRIVDISPLAYTYPE_HTTPPARAMNAME%>">
          <%=Common.displayOption(PrivDisplayType.CURRENT_RECEIVED, privDisplayType)%>
          <%=Common.displayOption(PrivDisplayType.CURRENT_GRANTED, privDisplayType)%>
        </select>
        <input
          name="Button"
          value="Show"
          type="submit"
          class="button1" />
      </div>
    </form>
  </div> <!-- tablecontrols -->
  
  <div id="Paging" style="margin: 5px;">
    Privilege types: <%=Common.subsystemLinks(pSubject, privDisplayType, currentSubsystem)%>
  </div>		
             
  <form
      onsubmit
        ="return confirm
           ('Are you sure you want to revoke the'
            + (selectCount() == 1 ? '' : (' ' + selectCount()))
            + ' selected privilege'
            + (selectCount() > 1 ? 's' : '')
            + '?'
            + ' This action cannot be undone.'
            + ' Click OK to confirm.');"
      action="Revoke.do"
      method="post"
      name="checkform"
      id="checkform">

    <div class="tablecontent"> 
      <table>            
        <tr class="columnhead"> 
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
          <th>
            Subject
          </th>
<%
  }
%>
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
          <th width="10%">
            All:
            <input
               name="checkAll"
               type="checkbox"
               id="checkAll"
               onclick="selectAll(this.checked);"
               value="Check All" />
          </th>
        </tr>
    
<%
  Set assignmentSet;
  Set proxySet;
  Subsystem subsystemFilter = null;
  
  if (!currentSubsystem.equals(Constants.WILDCARD_SUBSYSTEM))
  {
    subsystemFilter = currentSubsystem;
  }
  
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
    assignmentSet
      = new TreeSet
          (Common.getAssignmentsGrantedForReport
            (pSubject, subsystemFilter));

    proxySet
      = new TreeSet
          (Common.getProxiesGrantedForReport
            (pSubject, subsystemFilter));
  }
  else
  {
    assignmentSet
      = new TreeSet
          (Common.getAssignmentsReceivedForReport
            (pSubject, subsystemFilter));

    proxySet
      = new TreeSet
          (Common.getProxiesReceivedForReport
            (pSubject, subsystemFilter));
  }
  
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
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
          <td class="sorted"> <!-- person -->
            <a
              href="PersonView.do?granteeSubjectTypeId=<%=grantee.getSubjectTypeId()%>&granteeSubjectId=<%=grantee.getSubjectId()%><%=(subsystem == null ? "" : ("&subsystemId=" + subsystem.getId()))%>">
              <%=grantee.getName()%>
            </a>
          </td> <!-- person -->
<%
  }
%>
              
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

          <%=Common.revokeBox(pSubject, assignment, UnusableStyle.DIM)%>
        </tr>
    
<% 
  }
%>
  
  
  
<%  
  Iterator proxyIterator = proxySet.iterator();
  while (proxyIterator.hasNext())
  {
    Proxy proxy = (Proxy)(proxyIterator.next());
    PrivilegedSubject grantee = proxy.getGrantee();
    Subsystem subsystem = proxy.getSubsystem();
%>
  
        <tr>
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
          <td class="sorted"> <!-- person -->
            <a
              href="PersonView.do?granteeSubjectTypeId=<%=grantee.getSubjectTypeId()%>&granteeSubjectId=<%=grantee.getSubjectId()%><%=(subsystem == null ? "" : ("&subsystemId=" + subsystem.getId()))%>">
              <%=grantee.getName()%>
            </a>
          </td> <!-- person -->
<%
  }
%>
              
          <td> <!-- privilege -->
            <%=Common.proxyPopupIcon(proxy)%>
            Proxy
          </td> <!-- privilege -->
              
          <td> <!-- scope -->
             <span class="label">acting as </span><%=proxy.getGrantor().getName()%>
          </td> <!-- scope -->
              
          <td> <!-- limits -->
            <%=Common.editLink(pSubject, proxy)%>
            <span class="label"><%=Common.displayLimitType(proxy)%> </span><%=Common.displaySubsystem(proxy)%>
          </td> <!-- limits -->
              
          <td> <!-- status -->
            <%=Common.displayStatus(proxy)%>
          </td> <!-- status -->

          <%=Common.revokeBox(pSubject, proxy, UnusableStyle.DIM)%>
        </tr>
    
<% 
  }
%>
  
       
  
        <tr >
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
          <td>&nbsp;</td>
<%
  }
%>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
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
  </form> <!-- checkform -->
</div> <!-- Content -->