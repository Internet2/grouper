<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: confirmProxy.jsp,v 1.3 2006-01-26 02:48:05 jvine Exp $
  $Date: 2006-01-26 02:48:05 $
  
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
        <a href="<%=personViewHref%>">Subject View 
          [<%=currentGranteePrivilegedSubject.getName()%>]
        </a>
        &gt; Designated Driver
      </span> <!-- select -->
    </div>  <!-- Navbar -->
      
    <div id="Layout">
      <div id="Content">
        <div id="ViewHead">
          <span class="dropback">Designated a <%=isSubsystemOwner ? "subsystem owner" : "granting proxy"%>:</span>
          <h1>
            <%=currentGranteePrivilegedSubject.getName()%>
          </h1>
          <span class="ident">
            <%=currentGranteePrivilegedSubject.getDescription()%>
          </span>
        </div> <!-- ViewHead -->
           
        <div class="section" id="summary">
          <h2>Completed Designated Driver</h2>
            <table>       
              <tr>
                <th class="label" scope="row">Designated as:</th>
                <td class="content"><span class="function">
                      Granting Proxy
                    </span>
                </td>
              </tr>
              <tr>
                <th class="label" scope="row"><%=Common.displayLimitType(currentProxy)%>:</th>
                <td class="content"><%=Common.displaySubsystem(currentProxy)%></td>
              </tr>
              <tr>
                <th class="label" scope="row">Status:</th>
                <td class="content"><%=Common.displayStatus(currentProxy)%></td>
              </tr>
              <tr>
                <th class="label" scope="row">Effective:</th>
                <td class="content"><!-- DATE (or condition) GOES HERE --></td>
              </tr>
              <tr>
        		<th class="label" scope="row">Duration:</th>
        		<td class="content"><!-- DATE (or condition) GOES HERE --></td>
      		  </tr>
            </table>            
        </div><!-- section -->
         
          <div class="section">
            <h2>
              Continue
            </h2>
			<p class="default">
				<img src="images/arrow_right.gif" alt="" />
				Subject view [name]
			</p>
            <p>
              <a href="Designate.do?<%=Constants.NEW_PROXY_HTTPPARAMNAME%>=true">
                <img src="images/arrow_right.gif" alt="" />
                Designate another granting proxy</a>
            </p>
            <p>
              <a href="Designate.do">
                <img src="images/arrow_right.gif" alt="" />
                Edit this designation</a>
            </p>			
			<p>
              <a href="Start.do?<%=Constants.CURRENTPSUBJECT_HTTPPARAMNAME%>=<%=Common.buildCompoundId(loggedInPrivilegedSubject.getEffectiveEditor())%>">
                <img src="images/arrow_right.gif" alt="" />
            <span class="default">My View </span> : Current assignments to others</a></p>
          
          </div>
        </div> <!-- Content -->    
        <div id="Sidebar">

          <form
            name="personSearchForm"
            method="post"
            action="" 
            onsubmit ="return checkForCursorInPersonSearch('personQuickSearch.jsp', 'words', 'PersonSearchResults')">
            <div class="findperson">
              <h2>
                Find a subject
              </h2>
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
                  value="Search"
                  onclick="personSearchButtonHasFocus=true;"
                  onfocus="personSearchButtonHasFocus=true;"
                  onblur="personSearchButtonHasFocus=false;" />
              </p>
                <p class="dropback">
                  <label for="words">Enter a person's or group's name, then click Search.</label>
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
