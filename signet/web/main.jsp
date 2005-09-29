<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: main.jsp,v 1.52 2005-09-29 20:03:00 acohen Exp $
  $Date: 2005-09-29 20:03:00 $
  
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
</head>

<body>

<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

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
<%@ page import="edu.internet2.middleware.signet.Proxy" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
   PrivilegedSubject loggedInPrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
         
   DateFormat dateFormat = DateFormat.getDateInstance();
   
   // For use by Tiles:
  request.setAttribute
  	("pSubjectAttr", loggedInPrivilegedSubject.getEffectiveEditor());
%>

    <tiles:insert page="/tiles/header.jsp" flush="true" />
    <div id="Navbar">
      <span class="logout">
        <%=Common.displayLogoutHref(request)%>
      </span> <!-- logout -->
      <span class="select">
        <%=Constants.HOMEPAGE_NAME%>
      </span> <!-- select -->
    </div> <!-- Navbar -->
  
  <div id="Layout">
  
    <tiles:insert page="/tiles/privilegesGrantedReport.jsp" flush="true" >
      <tiles:put name="pSubject"         beanName="pSubjectAttr" />
      <tiles:put name="privDisplayType"  beanName="privDisplayTypeAttr" />
      <tiles:put name="currentSubsystem" beanName="currentSubsystemAttr" />
    </tiles:insert>
    
    <tiles:insert page="/tiles/footer.jsp" flush="true" />
    <div id="Sidebar">

    <form
      name="personSearchForm"
      method="post"
      action="" 
      onsubmit ="return checkForCursorInPersonSearch('personQuickSearch.jsp', 'words', 'PersonSearchResults')">
        <div class="findperson">
        <h2>
          find a subject
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
          <br />
		  	<label for="words">Enter a subject's name, then click "Search".</label>
       	</p>
        <div id="PersonSearchResults" style="display:none">
        </div> <!-- PersonSearchResults -->
      </div><!-- findperson -->		
    </form> <!-- personSearchForm -->   
      
<div class="findperson"> 
        <form
          name="actAsForm"
          method="post"
          action="ActAs.do">
          <h2>Designated Drivers</h2>
          <%=Common.displayActingForOptions
               (loggedInPrivilegedSubject,
                Constants.ACTING_FOR_SELECT_ID)%>
          <br/>
          <a href='Designate.do'>
            <img src="images/arrow_right.gif" alt="" />
            Designate a granting proxy
          </a>
        </form>
      </div> <!-- findperson -->
      
    </div>
    <!-- Sidebar -->
 </div>	<!-- Layout -->
</body>
</html>
