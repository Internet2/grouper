<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: duplicateProxies.jsp,v 1.4 2006-05-16 17:37:35 ddonn Exp $
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
			document.dupForm.complButton.value = "<%=ResLoaderUI.getString("duplicateProxies.buttonchange.bt") %>";
			}
		else {
			document.dupForm.complButton.value = "<%=ResLoaderUI.getString("duplicateProxies.complete.bt") %>";
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
<%@ page import="edu.internet2.middleware.signet.Proxy" %>

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
               
  Proxy currentProxy
    = (Proxy)(request.getSession().getAttribute(Constants.PROXY_ATTRNAME));
    
  Set duplicateProxies
    = (Set)(request.getSession().getAttribute(Constants.DUP_PROXIES_ATTRNAME));
   
  String personViewHref
    = "PersonView.do?granteeSubjectTypeId="
      + currentGranteePrivilegedSubject.getSubjectTypeId()
      + "&granteeSubjectId="
      + currentGranteePrivilegedSubject.getSubjectId()
      + "&subsystemId="
      + currentProxy.getSubsystem().getId();

  boolean isSubsystemOwner;
  
  if (currentProxy == null)
  {
    isSubsystemOwner
      = ((Boolean)
            (request.getSession().getAttribute
              (Constants.SUBSYSTEM_OWNER_ATTRNAME)))
          .booleanValue();
  }
  else
  {
    isSubsystemOwner = Common.isSubsystemOwner(signet, currentProxy);
  }
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
          <a href="<%=personViewHref%>">Subject View 
            [<%=currentGranteePrivilegedSubject.getName()%>]</a>
		  &gt; <%=ResLoaderUI.getString("duplicateProxies.dupedriver_href.lbl") %>
        </span> <!-- select -->
      </div>  <!-- Navbar -->

  
  <div id="Layout"> 
    <div id="Content">
      <div id="ViewHead">
		   <span class="dropback">
            <%=ResLoaderUI.getString("duplicateProxies.duplicate.txt") %>
			<%=isSubsystemOwner ? ResLoaderUI.getString("duplicateProxies.subsysowner.txt") : ResLoaderUI.getString("duplicateProxies.proxy.txt") %>				   
          </span>            	
        <h1>
          <%=currentGranteePrivilegedSubject.getName()%>
       	</h1>
       	<span class="ident"><%=currentGranteePrivilegedSubject.getDescription()%></span><!--,	Technology Strategy and Support Operations-->
      </div>  <!-- ViewHead -->
			
<div class="alert">
<p><img src="images/caution.gif" align="left" /><%=ResLoaderUI.getString("duplicateProxies.alert_1.txt") %> </p>

<ul>
	<li><%=ResLoaderUI.getString("duplicateProxies.alert_2.txt") %> </li>
    <li><%=ResLoaderUI.getString("duplicateProxies.alert_3.txt") %> </li>
  </ul>
<p><%=ResLoaderUI.getString("duplicateProxies.alert_4.txt") %> </p>

</div>

<div class="section">
<h2><%=ResLoaderUI.getString("duplicateProxies.review.hdr") %>
  <span class="status"><%=ResLoaderUI.getString("duplicateProxies.review.txt") %></span>
</h2>
	<table class="full" style="margin-left: 75px;">
	<tr>
		<th width="300"><%=ResLoaderUI.getString("duplicateProxies.review_row1.hdr") %></th>
		<th width="100"><%=ResLoaderUI.getString("duplicateProxies.review_row2.hdr") %></th>
		</tr>
    <tr >
      <td>
        <%=currentProxy.getSubsystem().getName()%>
      </td>
		<td>
		  <%=Common.displayStatus(currentProxy)%>
        </td>
		</tr>
</table>
</div>

<form
  action="RevokeAndGrantProxy.do"
  method="post"
  name="dupForm"
  id="dupForm">
  
  <div class="section">
    <h2><%=ResLoaderUI.getString("duplicateProxies.replace.hdr") %></h2>
    <table class="full">
      <tr>
        <th width="50" align="center"><%=ResLoaderUI.getString("duplicateProxies.replace_row1.hdr") %></th>
        <th width="300"><%=ResLoaderUI.getString("duplicateProxies.replace_row2.hdr") %></th>
        <th width="100"><%=ResLoaderUI.getString("duplicateProxies.replace_row3.hdr") %></th>
      </tr>
      
<%
  Iterator duplicateProxiesIterator = duplicateProxies.iterator();
  while (duplicateProxiesIterator.hasNext())
  {
    Proxy dup = (Proxy)(duplicateProxiesIterator.next());
%>
      <tr>
        <%=Common.revokeBox(loggedInPrivilegedSubject, dup, UnusableStyle.TEXTMSG)%>
        <td>
          <%=Common.proxyPopupIcon(dup)%>
          <%=dup.getSubsystem().getName()%>
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
      <%=ResLoaderUI.getString("duplicateProxies.complete.hdr") %> </h2>

    <input
      name="complButton"
      type="submit"
      class="button-def"
      id="complButton"
      value="<%=ResLoaderUI.getString("duplicateProxies.complete.bt") %>" />

    <br />
    
    
    <a href="<%=personViewHref%>">
      <img src="images/arrow_left.gif" />
      <%=ResLoaderUI.getString("duplicateProxies.cancel_href_1.txt") %></a> <%=ResLoaderUI.getString("duplicateProxies.cancel_href_2.txt") %> [<%=currentGranteePrivilegedSubject.getName()%>]
    
  </div>

     	</form>


     <tiles:insert page="/tiles/footer.jsp" flush="true" />
   	</div>
    <!-- Content -->

      <div id="Sidebar">
      	<div class="helpbox">
      		<h2><%=ResLoaderUI.getString("duplicateProxies.help.hdr") %></h2>
      		<div class="actionbox">
   			  <p><%=ResLoaderUI.getString("duplicateProxies.help_1.txt") %> </p>
   			  <p><%=ResLoaderUI.getString("duplicateProxies.help_2.txt") %> </p>
			  <p><%=ResLoaderUI.getString("duplicateProxies.help_3.txt") %><img src="images/maglass.gif" alt="" style="vertical-align:top;" /></p>

		  </div>
     		</div>
   	</div>  <!-- Sidebar -->
		
</div> <!-- Layout -->

</body>
</html>