<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.util.Set" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="java.util.Iterator" %>

<%@ page import="edu.internet2.middleware.signet.Signet" %>
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

<tiles:useAttribute name="signet"                    classname="Signet" />
<tiles:useAttribute name="pSubject"                  classname="PrivilegedSubject" />
<tiles:useAttribute name="loggedInPrivilegedSubject" classname="PrivilegedSubject" />
<tiles:useAttribute name="privDisplayType"           classname="PrivDisplayType" />
<tiles:useAttribute name="currentSubsystem"          classname="Subsystem" />

<DIV id="Content"> 
  <DIV id="ViewHead">
    <SPAN class="dropback">
      Privileges overview for</SPAN> 
      <H1>
        <%=pSubject.getName()%>
      </H1>
      <SPAN class="ident">
        <%=pSubject.getDescription()%>
      </SPAN>
    </span> <!-- dropback -->
  </DIV> <!-- ViewHead -->

  <DIV class="tableheader">
  <!-- stubbing out export option for release 1.0
    <A
      href="javascript:;"
      onClick="alert('This will download the data shown in the table in an Excel-readable format.')">
      <IMG
        src="images/export.gif"
        alt="" />
      Export to Excel
    </A>
	-->
    <A
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
      href="PersonViewPrint.do">
<%
  }
  else
  {
%>
      href="PersonViewPrint.do">
<%
  }
%>
      <IMG
        src="images/print.gif"
        alt="" />
      Printable version
    </A>
    <H2><%=pSubject.getName()%> : <%=privDisplayType.getDescription()%><%=(currentSubsystem == Constants.WILDCARD_SUBSYSTEM ? "" : (" : " + currentSubsystem.getName()))%></H2>
  </DIV> <!-- tableheader -->
  
  <DIV class="tablecontrols">
    <FORM
      name="personSearchForm"
      method="post"
      action="PersonView.do">
      Change the view to show 
      <DIV style="display: inline;">
        <SELECT
          name="<%=Constants.PRIVDISPLAYTYPE_HTTPPARAMNAME%>"
          id="<%=Constants.PRIVDISPLAYTYPE_HTTPPARAMNAME%>">
          <%=Common.displayOption(PrivDisplayType.CURRENT_RECEIVED, privDisplayType)%>
          <%=Common.displayOption(PrivDisplayType.CURRENT_GRANTED, privDisplayType)%>
        </SELECT>
        <INPUT
          name="Button"
          value="Show"
          type="submit"
          class="button1" />
      </DIV>
    </FORM>
  </DIV> <!-- tablecontrols -->
  
  <DIV id="Paging" style="margin: 5px;">
    Privilege types: <%=Common.subsystemLinks(pSubject, privDisplayType, currentSubsystem)%>
  </DIV>		
             
  <FORM
      onSubmit
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

    <DIV class="tablecontent"> 
      <TABLE>            
        <TR class="columnhead"> 
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
          <TH>
            Subject
          </TH>
<%
  }
%>
          <TH width="30%">
            Privilege
          </TH>
          <TH width="20%">
            Scope
          </TH>
          <TH>
            Limits
          </TH>
          <TH>
            Status
          </TH>
          <TH width="10%">
            All:
            <INPUT
               name="checkAll"
               type="checkbox"
               id="checkAll"
               onClick="selectAll(this.checked);"
               value="Check All" />
          </TH>
        </TR>
    
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
  
        <TR>
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
          <TD class="sorted"> <!-- person -->
            <A
              href="PersonView.do?granteeSubjectTypeId=<%=grantee.getSubjectTypeId()%>&granteeSubjectId=<%=grantee.getSubjectId()%><%=(subsystem == null ? "" : ("&subsystemId=" + subsystem.getId()))%>">
              <%=grantee.getName()%>
            </A>
          </TD> <!-- person -->
<%
  }
%>
              
          <TD> <!-- privilege -->
            <%=Common.assignmentPopupIcon(assignment)%>
            <%=subsystem.getName()%> : <%=category.getName()%> : <%=function.getName()%>
          </TD> <!-- privilege -->
              
          <TD> <!-- scope -->
             <%=assignment.getScope().getName()%>
          </TD> <!-- scope -->
              
          <TD> <!-- limits -->
            <%=Common.editLink(loggedInPrivilegedSubject, assignment)%>
            <%=Common.displayLimitValues(assignment)%>
          </TD> <!-- limits -->
              
          <TD> <!-- status -->
            <%=Common.displayStatus(assignment)%>
          </TD> <!-- status -->

          <%=Common.revokeBox(loggedInPrivilegedSubject, assignment, UnusableStyle.DIM)%>
        </TR>
    
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
  
        <TR>
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
          <TD class="sorted"> <!-- person -->
            <A
              href="PersonView.do?granteeSubjectTypeId=<%=grantee.getSubjectTypeId()%>&granteeSubjectId=<%=grantee.getSubjectId()%><%=(subsystem == null ? "" : ("&subsystemId=" + subsystem.getId()))%>">
              <%=grantee.getName()%>
            </A>
          </TD> <!-- person -->
<%
  }
%>
              
          <TD> <!-- privilege -->
            <%=Common.proxyPopupIcon(proxy)%>
            <%=Common.proxyPrivilegeDisplayName(signet, proxy)%>
          </TD> <!-- privilege -->
              
          <TD> <!-- scope -->
             <SPAN class="label">acting as </SPAN><%=proxy.getGrantor().getName()%>
          </TD> <!-- scope -->
              
          <TD> <!-- limits -->
            <%=Common.editLink(loggedInPrivilegedSubject, proxy)%>
            <SPAN class="label"><%=Common.displayLimitType(proxy)%> </SPAN><%=Common.displaySubsystem(proxy)%>
          </TD> <!-- limits -->
              
          <TD> <!-- status -->
            <%=Common.displayStatus(proxy)%>
          </TD> <!-- status -->

          <%=Common.revokeBox(loggedInPrivilegedSubject, proxy, UnusableStyle.DIM)%>
        </TR>
    
<% 
  }
%>
  
       
  
        <TR >
<%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED))
  {
%>
          <TD>&nbsp;</TD>
<%
  }
%>
          <TD>&nbsp;</TD>
          <TD>&nbsp;</TD>
          <TD>&nbsp;</TD>
          <TD>&nbsp;</TD>
          <TD align="center" >
            <INPUT
              name="revokeButton"
              type="submit"
              disabled="true"
              class="button1"
              value="Revoke" />
          </TD>
        </TR>     
      </TABLE>
    </DIV> <!-- tablecontent -->
  </FORM> <!-- checkform -->
</DIV> <!-- Content -->