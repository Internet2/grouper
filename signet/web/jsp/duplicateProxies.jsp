<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: duplicateProxies.jsp,v 1.3 2006-02-07 19:52:03 jvine Exp $
  $Date: 2006-02-07 19:52:03 $
  
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
			document.dupForm.complButton.value = "COMPLETE and replace selected proxy designation(s)";
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
<%@ page import="edu.internet2.middleware.signet.Proxy" %>

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
		  &gt; Duplicate Designated Driver          
        </span> <!-- select -->
      </div>  <!-- Navbar -->

  
  <div id="Layout"> 
    <div id="Content">
      <div id="ViewHead">
		   <span class="dropback">
            Duplicate
			<%=isSubsystemOwner ? "subsystem owner" : "proxy"%>				   
          </span>            	
        <h1>
          <%=currentGranteePrivilegedSubject.getName()%>
       	</h1>
       	<span class="ident"><%=currentGranteePrivilegedSubject.getDescription()%></span><!--,	Technology Strategy and Support Operations-->
      </div>  <!-- ViewHead -->
			
<div class="alert">
<p><img src="images/caution.gif" align="left" />This  designated driver is very similar to one or more that already exists. Compare them below, then: </p>

<ul>
	<li>in the &quot;Replace&quot; column, check any  existing designated driver(s) you want to replace with this one, then </li>
    <li>click the &quot;COMPLETE&quot; button at the bottom 
  	of the page.</li>
  </ul>
<p>Or cancel this designated driver by clicking the &quot;CANCEL&quot; link at the bottom of the page.</p>

</div>

<div class="section">
<h2>Review your current  designated driver <span class="status"> (not yet complete)</span></h2>
	<table class="full" style="margin-left: 75px;">
	<tr>
		<th width="300">Subsystem</th>
		<th width="100">Status</th>
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
    <h2>Check any existing  designated driver(s) you want to replace</h2>
    <table class="full">
      <tr>
        <th width="50" align="center"> Replace</th>
        <th width="300">Subsystem</th>
        <th width="100">Status</th>
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
      Complete this  designated driver </h2>

    <input
      name="complButton"
      type="submit"
      class="button-def"
      id="complButton"
      value="COMPLETE" />

    <br />
    
    
    <a href="<%=personViewHref%>">
      <img src="images/arrow_left.gif" />
      CANCEL this designated driver</a> and return to Subject View [<%=currentGranteePrivilegedSubject.getName()%>]
    
  </div>

     	</form>


     <tiles:insert page="/tiles/footer.jsp" flush="true" />
   	</div>
    <!-- Content -->

      <div id="Sidebar">
      	<div class="helpbox">
      		<h2>Help</h2>
      		<div class="actionbox">
   			  <p>In cases where there is more than one designation for a specific privilege type, the subject's actual  conditions (effective date and duration) will be the most lenient of any specified conditions. </p>
   			  <p>If your intent is to restrict conditions, you should replace any designations with more lenient conditions.</p>
			  <p>Find out more details about the existing designations  by clicking on the <img src="images/maglass.gif" alt="" style="vertical-align:top;" /> icon. </p>

		  </div>
     		</div>
   	</div>  <!-- Sidebar -->
		
</div> <!-- Layout -->

</body>
</html>