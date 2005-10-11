<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: main.jsp,v 1.59 2005-10-11 17:13:20 acohen Exp $
  $Date: 2005-10-11 17:13:20 $
  
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

<body onload="javascript:initControls();">
  <script type="text/javascript">
    function initControls()
    {
      initRevokeAllCheckbox();
    }
    
    function setStartButtonStatus()
    {
      // The first entry in the list of grantable Subsystems is always
      // a place-holder that reads "select a privilege type...", to prompt the
      // user to go ahead and pick a Subsystem.
      //
      // As long as that first entry is selected, the "start" button, which
      // initiates the privilege-granting process, should be dimmed.
      
      var theGrantableSubsystemList = document.grantForm.grantableSubsystems;
      var theGrantButton = document.grantForm.grantButton;
      
      if (theGrantableSubsystemList.selectedIndex < 1)
      {
        theGrantButton.disabled = true;
      }
      else
      {
        theGrantButton.disabled = false;
      }
    }
    
    function initRevokeAllCheckbox()
    {
      // If there are no revocable Assignments on this page, then it makes
      // no sense to have the "revoke all" checkbox enabled.
      var theCheckAllBox = document.checkform.checkAll;
      
      if (selectableCount() > 0)
      {
        theCheckAllBox.disabled = false;
      }
      else
      {
        theCheckAllBox.disabled = true;
      }
    }
    
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
    
    function selectableCount()
    {
      var theForm = document.checkform;
      var count = 0;
      
      for (var i = 0; i < theForm.elements.length; i++)
      {
        if ((theForm.elements[i].name != 'checkAll')
            && (theForm.elements[i].type == 'checkbox')
            && (theForm.elements[i].disabled == false))
        {
          count++;
        }
      }
      
      return count;
    }
  </script>

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
         
   PrivilegedSubject currentPSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME));
         
   DateFormat dateFormat = DateFormat.getDateInstance();
%>

    <tiles:insert page="/tiles/header.jsp" flush="true" />
    <div id="Navbar">
      <span class="logout">
        <%=Common.displayLogoutHref(request)%>
      </span> <!-- logout -->

      <span class="select">
<%
  if (currentPSubject.equals(loggedInPrivilegedSubject.getEffectiveEditor()))
  { 
%>
        <%=Constants.HOMEPAGE_NAME%>
<%
  }
  else
  {
%>
      <a href="Start.do">
        <%=Constants.HOMEPAGE_NAME%>
      </a>
      &gt; <%=currentPSubject.getName()%>
<%
  }
%>
      </span> <!-- select -->

    </div> <!-- Navbar -->
  
  <div id="Layout">
  
    <tiles:insert page="/tiles/privilegesGrantedReport.jsp" flush="true" >
      <tiles:put name="pSubject"         beanName="currentPrivilegedSubject" />
      <tiles:put name="privDisplayType"  beanName="privDisplayTypeAttr" />
      <tiles:put name="currentSubsystem" beanName="currentSubsystemAttr" />
    </tiles:insert>
    
    <tiles:insert page="/tiles/footer.jsp" flush="true" />
    <div id="Sidebar">
    
        
<% 
  if (!(currentPSubject.equals(loggedInPrivilegedSubject.getEffectiveEditor())))
  {
    Set grantableSubsystems
      = loggedInPrivilegedSubject.getGrantableSubsystemsForAssignment();
%>

      <div class="grant">
        <h2>
          Grant a privilege
        </h2>
        <p>to <%=currentPSubject.getName()%></p>
        <form name="grantForm" id="grantForm" action="Functions.do">
          <p>
            <select
              id="grantableSubsystems"
              name="grantableSubsystems"
              class="long" 
              onchange="setStartButtonStatus()">
                
              <option value="prompt">
                select a privilege type...
              </option>

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
       
            <input
              type="submit"
              name="grantButton"
              id="grantButton"
              class="button1"
              <%="disabled=\"disabled\""%>
              value="Start &gt;&gt;" />
            <br />
            <label for="grantableSubsystems">
              Select the type of privilege you want to grant, then click "Start."
            </label>
          </p>
        </form> <!-- grantForm -->
      </div> <!-- grant -->
          
<%
  }
%>

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

      
<% 
  if (currentPSubject.equals(loggedInPrivilegedSubject.getEffectiveEditor()))
  {
    Set grantableSubsystems
      = loggedInPrivilegedSubject.getGrantableSubsystemsForAssignment();
%>
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
        <a href='Designate.do?<%=Constants.NEW_PROXY_HTTPPARAMNAME%>=true'>
          <img src="images/arrow_right.gif" alt="" />
          Designate a granting proxy
        </a>
      </form>
    </div> <!-- findperson -->        
<%
  }
%>


  </div> <!-- Sidebar -->
</div>  <!-- Layout -->
</body>
</html>
