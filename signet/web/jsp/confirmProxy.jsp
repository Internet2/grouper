<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: confirmProxy.jsp,v 1.7 2006-05-16 17:37:35 ddonn Exp $
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
    <script language="JavaScript" type="text/javascript" src="scripts/signet.js">
    </script>
  </head>

  <body>
  
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="java.util.Date" %>

<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Proxy" %>
<%@ page import="edu.internet2.middleware.signet.Signet" %>

<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
   
  PrivilegedSubject loggedInPrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
   
  PrivilegedSubject currentGranteePrivilegedSubject
    = (PrivilegedSubject)
        (request.getSession().getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME));
         
  Subsystem currentSubsystem
    = (Subsystem)
        (request.getSession().getAttribute(Constants.SUBSYSTEM_ATTRNAME));
         
  Proxy currentProxy
    = (Proxy)
        (request.getSession().getAttribute(Constants.PROXY_ATTRNAME));
         
  PrivilegedSubject proxySubject = currentProxy.getProxy();
   
  PrivilegedSubject grantor = currentProxy.getGrantor();
         
  DateFormat dateFormat = DateFormat.getDateInstance();
   
  String personViewHref
    = "PersonView.do?granteeSubjectTypeId="
      + currentGranteePrivilegedSubject.getSubjectTypeId()
      + "&granteeSubjectId="
      + currentGranteePrivilegedSubject.getSubjectId()
      + "&subsystemId="
      + currentSubsystem.getId();
       
  boolean isSubsystemOwner
    = ((Boolean)
          (request.getSession().getAttribute
            (Constants.SUBSYSTEM_OWNER_ATTRNAME)))
        .booleanValue();
%>
    
    <tiles:insert page="/tiles/header.jsp" flush="true" />
    <div id="Navbar">
      <span class="logout">
        <%=Common.displayLogoutHref(request)%>
      </span> <!-- logout -->
      <span class="select">
        <a href="Start.do?<%=Constants.CURRENTPSUBJECT_HTTPPARAMNAME%>=<%=Common.buildCompoundId(loggedInPrivilegedSubject.getEffectiveEditor())%>">
          <%=Common.homepageName(loggedInPrivilegedSubject)%>
        </a>
        &gt; <!-- displays as text right-angle bracket -->
        <a href="<%=personViewHref%>"><%=ResLoaderUI.getString("confirmProxy.subjview.txt") %> 
          [<%=currentGranteePrivilegedSubject.getName()%>]
        </a>
        &gt; <%=ResLoaderUI.getString("confirmProxy.designateddriver.txt") %>
      </span> <!-- select -->
    </div>  <!-- Navbar -->
      
    <div id="Layout">
      <div id="Content">
        <div id="ViewHead">
          <span class="dropback"><%=ResLoaderUI.getString("confirmProxy.designated.txt") %> <%=isSubsystemOwner ? ResLoaderUI.getString("confirmProxy.subsysowner.txt") : ResLoaderUI.getString("confirmProxy.proxy.txt") %></span>
          <h1>
            <%=currentGranteePrivilegedSubject.getName()%>
          </h1>
          <span class="ident">
            <%=currentGranteePrivilegedSubject.getDescription()%>
          </span>
        </div> <!-- ViewHead -->
           
        <div class="section" id="summary">
          <h2><%=ResLoaderUI.getString("confirmProxy.designateddriversummary.hdr") %> </h2>
            <table>       
              <tr>
                <th class="label" scope="row"><%=ResLoaderUI.getString("confirmProxy.privrow.lbl") %></th>
                <td class="content"><!-- if this is a standard granting proxy, then -->
                    <span class="category"><%=ResLoaderUI.getString("confirmProxy.driversignet.txt") %></span> : <span class="function"> <%=isSubsystemOwner ? ResLoaderUI.getString("confirmProxy.subsysowner.txt") : ResLoaderUI.getString("confirmProxy.proxy.txt") %> </span> : <%=isSubsystemOwner ? ResLoaderUI.getString("confirmProxy.actas.txt") : ResLoaderUI.getString("confirmProxy.grantas.txt") + loggedInPrivilegedSubject.getName()%> </td>
              </tr>
              <tr>
                <th class="label" scope="row"><%=ResLoaderUI.getString("confirmProxy.in.lbl") %></th>
                <td class="content"><%=Common.displaySubsystem(currentProxy)%></td>
              </tr>
              <tr>
                <th class="label" scope="row"><%=ResLoaderUI.getString("confirmProxy.status.lbl") %></th>
                <td class="content"><%=Common.displayStatus(currentProxy)%></td>
              </tr>
              <tr>
                <th class="label" scope="row"><%=ResLoaderUI.getString("confirmProxy.firsteffective.lbl") %></th>
                <td class="content">
					<%=dateFormat.format(currentProxy.getEffectiveDate())%>
				</td>
              </tr>
              <tr>
        		<th class="label" scope="row"><%=ResLoaderUI.getString("confirmProxy.duration.lbl") %></th>
        		<td class="content">
				  <%=ResLoaderUI.getString("confirmProxy.until.txt") %> 
	        	  <%=currentProxy.getExpirationDate() == null
    	         ? ResLoaderUI.getString("confirmProxy.revoked.txt")
        	     : dateFormat.format(currentProxy.getExpirationDate())%>
			 </td>
      		  </tr>
            </table>            
        </div><!-- section -->
         
          <div class="section">
            <h2>
              <%=ResLoaderUI.getString("confirmProxy.continue.hdr") %>
            </h2>
			<p>
				<a href="<%=personViewHref%>"><img src="images/arrow_right.gif" alt="" />
				<span class="default"><%=ResLoaderUI.getString("confirmProxy.subjview.txt") %></span> 
          [<%=currentGranteePrivilegedSubject.getName()%>]</a>
			</p>
            <p>
              <a href="Designate.do?<%=Constants.NEW_PROXY_HTTPPARAMNAME%>=true<%=Common.isSystemAdministrator(signet, loggedInPrivilegedSubject) ? "&" + Constants.SUBSYSTEM_OWNER_HTTPPARAMNAME + "=true" : ""%>">
                <img src="images/arrow_right.gif" alt="" />
                <%=ResLoaderUI.getString("confirmProxy.designate.txt") %> <%=currentGranteePrivilegedSubject.getName()%> <%=ResLoaderUI.getString("confirmProxy.again.txt") %></a>
				<%=ResLoaderUI.getString("confirmProxy.different.txt") %>
            </p>
            <p>
              <a href="Designate.do">
                <img src="images/arrow_right.gif" alt="" />
                <%=ResLoaderUI.getString("confirmProxy.editdesignation_href.txt") %></a></p>			
			<p>
              <a href="Start.do?<%=Constants.CURRENTPSUBJECT_HTTPPARAMNAME%>=<%=Common.buildCompoundId(loggedInPrivilegedSubject.getEffectiveEditor())%>">
                <img src="images/arrow_right.gif" alt="" />
            <span class="default"><%=ResLoaderUI.getString("confirmProxy.myview.txt") %> </span> : <%=ResLoaderUI.getString("confirmProxy.assigntoothers.txt") %></a></p>
          
          </div>
        </div> <!-- Content -->    
        <div id="Sidebar">

          <form
            name="personSearchForm"
            method="post"
            action="" 
            onsubmit ="return checkForCursorInPersonSearch('personQuickSearch.jsp', 'words', 'PersonSearchResults')">
            <div class="findperson">
              <h2><%=ResLoaderUI.getString("confirmProxy.findsubj.hdr") %> </h2>
              <p>
                <input
                  name="words"
                  type="text"
                  class="long"
                  id="words"
                  size="15"
                  maxlength="500"
                  onfocus="personSearchFieldHasFocus=true;"
                  onblur="personSearchFieldHasFocus=false;" />
                <input
                  name="searchbutton"
                  type="submit"
                  class="button1"
                  value=<%=ResLoaderUI.getString("confirmProxy.search.bt") %>
                  onclick="personSearchButtonHasFocus=true;"
                  onfocus="personSearchButtonHasFocus=true;"
                  onblur="personSearchButtonHasFocus=false;" />
              </p>
                <p class="dropback">
                  <label for="words"><%=ResLoaderUI.getString("confirmProxy.findhelp.lbl") %></label>
                </p>
              <div id="PersonSearchResults" style="display:none">
              </div> <!-- PersonSearchResults -->

            </div> <!-- findperson -->
          </form> <!-- personSearchForm -->  


          
        </div> <!-- Sidebar -->
        <tiles:insert page="/tiles/footer.jsp" flush="true" />
      </div> <!-- Layout -->
    </body>
</html>
