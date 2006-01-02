<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: designate.jsp,v 1.17 2006-01-02 04:59:07 acohen Exp $
  $Date: 2006-01-02 04:59:07 $
  
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

  <script type="text/javascript">
    
    function subsystemSelected(subsystemSelectId, subsystemPromptValue)
    {
      var theForm = document.form1;
      var subsystemSelect = document.getElementById(subsystemSelectId);
      if (subsystemSelect.value == subsystemPromptValue)
      {
        // Choosing "please choose a subsystem" doesn't really count as a
        // selection.
        return false;
      }
      else
      {
        return true;
      }
    }
    
    function setContinueButtonStatus(subsystemSelectId, subsystemPromptValue)
    {
      if (subsystemSelected(subsystemSelectId, subsystemPromptValue))
      {
        document.form1.continueButton.disabled=false;
      }
      else
      {
        document.form1.continueButton.disabled=true;
      }
    }
    
  </script>

<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="java.util.Set" %>

<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Proxy" %>

<%@ page import="edu.internet2.middleware.signet.ui.Common" %>
<%@ page import="edu.internet2.middleware.signet.ui.Constants" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));

  PrivilegedSubject loggedInPrivilegedSubject
    = (edu.internet2.middleware.signet.PrivilegedSubject)
        (request.getSession().getAttribute(Constants.LOGGEDINUSER_ATTRNAME));
         
  PrivilegedSubject currentPSubject
    = (PrivilegedSubject)
        (request.getSession().getAttribute(Constants.CURRENTPSUBJECT_ATTRNAME));

  Set grantableSubsystems
    = loggedInPrivilegedSubject.getGrantableSubsystemsForProxy();
         
  // If the session contains a "currentProxy" attribute, then we're
  // editing an existing Proxy. Otherwise, we're attempting to create a
  // new one.
  Proxy currentProxy
    = (Proxy)(request.getSession().getAttribute(Constants.PROXY_ATTRNAME));
    
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
      &gt; <%=currentProxy==null?"":"Edit"%> Designated Driver
    </span> <!-- select -->
  </div> <!-- Navbar -->

  <form
    name="form1"
    method="post"
    action="ConfirmProxy.do">

    <div id="Layout">
      <div id="Content">
        <div id="ViewHead">
          <span class="dropback">
            <%=currentProxy==null?"Designating as":"Editing"%>
            <%=isSubsystemOwner ? "subsystem owner" : "proxy"%>
          </span>             
          <h1>
            <%=currentPSubject.getName()%>
          </h1>
          <span class="ident">
            <%=currentPSubject.getDescription()%>
          </span>
        </div> <!-- ViewHead -->
 
<%
  if (currentProxy != null)
  {
%>
        <div class="section">
          <h2>
            Editing proxy
          </h2>
          <span style="font-weight: bold;" id="categoryName">
          </span>
          <table>
            <tbody>
              <tr>
                <td class="label" width="15%">
                  Granted to:
                </td>
                <td width="85%">
                  <%=currentProxy.getGrantee().getName()%>
                  <span class="ident">
                    <%=currentProxy.getGrantee().getDescription()%>
                  </span>
                </td>
              </tr>
              <tr>
                <td class="label">
                  Privilege Type:
                </td>
                <td>
                  <%=currentProxy.getSubsystem().getName()%>
                </td>
              </tr>
            </tbody>
          </table>
        </div> <!-- section -->
<%
  }
  else
  {
%>
        <div class="section">
          <h2>
            select a privilege type...
          </h2>
          <div style="margin-left: 25px;">
            <%=Common.subsystemSelectionSingle
                   (Constants.SUBSYSTEM_HTTPPARAMNAME,
                    Constants.SUBSYSTEM_PROMPTVALUE,
                    "select a privilege type...",
                    "setContinueButtonStatus('" + Constants.SUBSYSTEM_HTTPPARAMNAME + "', '" + Constants.SUBSYSTEM_PROMPTVALUE+ "');",
                    grantableSubsystems,
                    (currentProxy == null ? null : currentProxy.getSubsystem()))%>
          </div>
        </div> <!-- section -->
<%
  }
%>
     
        <div class="section">
          <h2>Set conditions</h2>
        <table>
        
            <tr>
              <%=Common.dateSelection
                (request,
                 Constants.EFFECTIVE_DATE_PREFIX,
                 "Effective:",
                 "immediately",
                 "on",
                 currentProxy == null
                   ? null
                   : currentProxy.getEffectiveDate(),
                 (currentProxy == null))%>
            </tr>
            
            <tr>
              <%=Common.dateSelection
                (request,
                 Constants.EXPIRATION_DATE_PREFIX,
                 "Duration:",
                 "until revoked",
                 "until",
                 currentProxy == null
                   ? null
                   : currentProxy.getExpirationDate())%>
            </tr>
          </table>
          <legend></legend>
        </div>
        <!-- section -->
        
        <div class="section">
        
          <h2>Complete this designation </h2>  
          <input
            name="continueButton"
<%
  if (currentProxy == null)
  {
%>
            disabled="true"
<%
  }
%>
            type="submit"
            class="button-def"
            onclick="personSearchButtonHasFocus=false;"
            onfocus="personSearchButtonHasFocus=false;"
            value="<%=(currentProxy==null?"Complete designation":"Save changes")%>" 
       />        
          <br />
          <a href="Start.do?<%=Constants.CURRENTPSUBJECT_HTTPPARAMNAME%>=<%=Common.buildCompoundId(loggedInPrivilegedSubject.getEffectiveEditor())%>">
            <img src="images/arrow_left.gif" />
            CANCEL and return to My View </a>
        </div>
      </div>
  
      <div id="Sidebar">      
        <div class="helpbox">
          <h2>
            help
          </h2>
            <p>The proxy you designate will be able to grant all of your <b>grantable</b> privileges, within the privilege type you select.</p>
            <p>The proxy <i>will not have</i> your <b>usable</b> privileges.</p>
            <p>Steps to designate a proxy:</p>
            <ol>
              <li>Select the privilege type. <i><br />
              (Only the types in which you have grantable authority are listed.)</i></li>
              <li>Search for and select the person you want to be your proxy.</li>
              <li>Set a start and/or end date for the proxy, or leave it open-ended.</li>
              <li>Click Complete designation. </li>
            </ol>
        </div>
      </div>
  
      <tiles:insert page="/tiles/footer.jsp" flush="true" />
  
    </div>  
  </form>
</body>
</html>