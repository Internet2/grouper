<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.util.Set" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.Iterator" %>

<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Grantable" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Status" %>
<%@ page import="edu.internet2.middleware.signet.Proxy" %>
<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>
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
      <%=ResLoaderUI.getString("privilegesGrantedReport.page.hdr") %></SPAN> 
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
    <A href="PersonViewPrint.do">
      <IMG
        src="images/print.gif"
        alt="" />
      <%=ResLoaderUI.getString("privilegesGrantedReport.print.txt") %>
    </A>
  <A href="PrivilegesXML.do" target="_blank">
                <IMG
                	src="images/xml.gif"
                    alt="" />
                <%=ResLoaderUI.getString("privilegesGrantedReport.viewXML.txt") %>
               </A>    
  <H2 class="inlinecontrol">
      <%=privDisplayType.getDescription()%>
	</H2>
	<FORM class="inlinecontrol"
      id="personViewForm"
      name="personViewForm"
      method="post"
      action="PersonView.do">
        <SELECT
          name="privDisplayType"
          id="privDisplayType"
		  onChange="setShowButtonStatus()">
		  <OPTION value="" selected><%=ResLoaderUI.getString("privilegesGrantedReport.privDisplayType.txt") %></OPTION>
          <%=Common.displayOption(PrivDisplayType.CURRENT_RECEIVED, null)%>
          <%=Common.displayOption(PrivDisplayType.FORMER_RECEIVED, null)%>
          <%=Common.displayOption(PrivDisplayType.CURRENT_GRANTED, null)%>
          <%=Common.displayOption(PrivDisplayType.FORMER_GRANTED, null)%>
        </SELECT>
        <INPUT
          name="showButton"
          id="showButton"	  
          value="<%=ResLoaderUI.getString("privilegesGrantedReport.show.bt") %>"
          type="submit"
              <%="disabled=\"disabled\""%>	  
          class="button1" />
    </FORM>
  </DIV> <!-- tableheader -->
  
  <DIV class="tablecontrols">
    <%=ResLoaderUI.getString("privilegesGrantedReport.privilegeTypes.lbl") %> <%=Common.subsystemLinks(pSubject, privDisplayType, currentSubsystem)%>
  </DIV> <!-- tablecontrols -->


   <FORM name="checkform" action="Revoke.do" method="post" id="checkform" onSubmit="return confirmRevokeMsg()">

    <DIV class="tablecontent">    
      <TABLE>
        <TR class="columnhead">
          <%
  if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED)
      || privDisplayType.equals(PrivDisplayType.FORMER_GRANTED))
  {
%>
          <TH><%=ResLoaderUI.getString("privilegesGrantedReport.subject.hdr") %></TH>
          <%
  }
%>
          <TH width="40%"><%=ResLoaderUI.getString("privilegesGrantedReport.privilege.hdr")%></TH>
          <TH><%=ResLoaderUI.getString("privilegesGrantedReport.scope.hdr")%></TH>
          <TH><%=ResLoaderUI.getString("privilegesGrantedReport.limits.hdr")%></TH>
          <TH><%=ResLoaderUI.getString("privilegesGrantedReport.status.hdr")%></TH>
          <TH width="60"><%=ResLoaderUI.getString("privilegesGrantedReport.all.hdr")%>
              <INPUT
               name="checkAll"
               type="checkbox"
               id="checkAll"
               onClick="selectAll(this.checked);"
               value="<%=ResLoaderUI.getString("privilegesGrantedReport.checkall.bt")%>" />
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
  
  SortedSet assignmentsAndProxies;

  assignmentsAndProxies
    = Common.getGrantablesForReport
        (pSubject, subsystemFilter, privDisplayType);
             
  Iterator assignmentsAndProxiesIterator = assignmentsAndProxies.iterator();
  while (assignmentsAndProxiesIterator.hasNext())
  {
    Grantable grantable = (Grantable)(assignmentsAndProxiesIterator.next());
    PrivilegedSubject grantee = grantable.getGrantee();
%>
        <TR>
          <%
    if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED)
        || privDisplayType.equals(PrivDisplayType.FORMER_GRANTED))
    {
%>
          <TD><!-- subject -->
              <A
              href="PersonView.do?granteeSubjectTypeId=<%=grantee.getSubjectTypeId()%>&granteeSubjectId=<%=grantee.getSubjectId()%><%=(subsystemFilter == null ? "" : ("&subsystemId=" + subsystemFilter.getId()))%>"> <%=grantee.getName()%> </A> </TD>
          <!-- subject -->
          <%
    }
%>
          <TD><!-- privilege -->
              <%=Common.popupIcon(grantable)%> <%=Common.privilegeStr(signet, grantable)%> </TD>
          <!-- privilege -->
          <TD><!-- scope -->
              <%=Common.scopeStr(grantable)%> </TD>
          <!-- scope -->
          <TD><!-- limits -->
              <%=Common.editLink(loggedInPrivilegedSubject, grantable)%> <%=Common.displayLimitValues(grantable)%> </TD>
          <!-- limits -->
          <TD><!-- status -->
              <%=Common.displayStatus(grantable)%> </TD>
          <!-- status -->
          <%=Common.revokeBox
               (loggedInPrivilegedSubject,
                grantable,
                UnusableStyle.DIM)%> </TR>
        <% 
  }
%>
        <TR >
          <%
    if (privDisplayType.equals(PrivDisplayType.CURRENT_GRANTED)
      || privDisplayType.equals(PrivDisplayType.FORMER_GRANTED))
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
          <TD width="60" align="center" ><INPUT
              name="revokeButton"
              type="submit"
              disabled="true"
              class="button1"
              value="<%=ResLoaderUI.getString("privilegesGrantedReport.revoke.bt") %>" />
          </TD>
        </TR>
      </TABLE>
    </DIV> 
    <!-- tablecontent -->
  </FORM> <!-- checkform -->
</DIV> <!-- Content -->