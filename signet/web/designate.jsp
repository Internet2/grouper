<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: designate.jsp,v 1.13 2005-10-25 22:57:07 acohen Exp $
  $Date: 2005-10-25 22:57:07 $
  
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

    var subjectSelected            = false;

    function showSubjectSearchResult(divId)
    {
      // Fill the specified DIV with the result of the person search.
      loadXMLDoc
        ('personForProxySearch.jsp?searchString='
         + document.getElementById('subjectSearchString').value,
         divId);
       
      // Make that DIV visible.
      var divToShow = document.getElementById(divId);
      divToShow.style.display = 'block';
    }
    
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
      if (subjectSelected && subsystemSelected(subsystemSelectId, subsystemPromptValue))
      {
        document.form1.continueButton.disabled=false;
      }
      else
      {
        document.form1.continueButton.disabled=true;
      }
    }
    
    function selectSubject(subsystemSelectId, subsystemPromptValue)
    {
      var selectSubjectCompositeId
        = document.getElementById("<%=Constants.SUBJECT_SELECTLIST_ID%>").value;
          
      var subjectNameDivId = "SUBJECT_NAME:" + selectSubjectCompositeId;
      var subjectName = document.getElementById(subjectNameDivId).innerHTML;
          
      var subjectDescriptionDivId = "SUBJECT_DESCRIPTION:" + selectSubjectCompositeId;
      var subjectDescription = document.getElementById(subjectDescriptionDivId).innerHTML;
          
      var subjectWarningDivId = "SUBJECT_WARNING:" + selectSubjectCompositeId;
      var subjectWarning
        = document.getElementById(subjectWarningDivId).innerHTML;
        
      var subjectNameElement = document.getElementById("subjectName");
      var subjectDescriptionElement = document.getElementById("subjectDescription");
      var subjectWarningElement = document.getElementById("subjectWarning");
        
      subjectNameElement.firstChild.nodeValue=subjectName;
      subjectDescriptionElement.firstChild.nodeValue=subjectDescription;
      subjectWarningElement.firstChild.nodeValue=subjectWarning;
      
      subjectSelected = true;
      setContinueButtonStatus(subsystemSelectId, subsystemPromptValue);
    }
    
    // return TRUE if the form should be submitted, FALSE otherwise.
    function submitOrSearch()
    {
      if (cursorInPersonSearch())
      {
        if (!personSearchStrIsEmpty('subjectSearchString'))
        {
          subjectSelected=false;
          document.getElementById('SubjectResultDiv').style.display
            ='display:none;';
          document.getElementById('subjectName').firstChild.nodeValue
            ='';
          document.getElementById('subjectDescription').firstChild.nodeValue
            ='';
        
          // Someday, we'll start displaying these warnings, and then we'll have
          // to start erasing them, as well.
          // document.getElementById('subjectWarning').style.firstChild.nodeValue
          //   ='';

          setContinueButtonStatus
            ('<%=Constants.SUBSYSTEM_HTTPPARAMNAME%>',
             '<%=Constants.SUBSYSTEM_PROMPTVALUE%>');
         
          performPersonSearch
            ('personForProxySearch.jsp',
             'subjectSearchString',
             'SubjectResultDiv');
        }
         
         return false;
      }
      else
      {
        return true;
      }
         
//      return checkForCursorInPersonSearch
//        ('personForProxySearch.jsp',
//         'subjectSearchString',
//         'SubjectResultDiv');
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

  Set grantableSubsystems
    = loggedInPrivilegedSubject.getGrantableSubsystemsForProxy();
         
  // If the session contains a "currentProxy" attribute, then we're
  // editing an existing Proxy. Otherwise, we're attempting to create a
  // new one.
  Proxy currentProxy
    = (Proxy)(request.getSession().getAttribute(Constants.PROXY_ATTRNAME));
%>

  <tiles:insert page="/tiles/header.jsp" flush="true" />

  <div id="Navbar">
    <span class="logout">
      <%=Common.displayLogoutHref(request)%>
    </span> <!-- logout -->
    <span class="select">
      <a href="Start.do?<%=Constants.CURRENTPSUBJECT_HTTPPARAMNAME%>=<%=Common.buildCompoundId(loggedInPrivilegedSubject.getEffectiveEditor())%>">
        <%=Constants.HOMEPAGE_NAME%>
      </a>
      &gt; <%=currentProxy==null?"":"Edit"%> Designated Driver
    </span> <!-- select -->
  </div> <!-- Navbar -->

  <form
    name="form1"
    method="post"
    action="ConfirmProxy.do" 
    onsubmit="return submitOrSearch();"> <!-- TRUE if submit -->

    <div id="Layout">
      <div id="Content">
        <div id="ViewHead">
          <span class="dropback">
            <%=currentProxy==null?"Designating a":"Editing"%> granting proxy for
          </span>             
          <h1>
            <%=loggedInPrivilegedSubject.getEffectiveEditor().getName()%>
          </h1>
          <span class="ident">
            <%=loggedInPrivilegedSubject.getEffectiveEditor().getDescription()%>
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
                  <span class="dropback">
                    [<%=currentProxy.getGrantee().getDescription()%>]
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
      
        <div class="section">    
          <h2>
            Find subject
          </h2>
          <div style="margin-left: 25px;">
            <input
                name="subjectSearchString"
                type="text"
                id="subjectSearchString"
                class="long"
                maxlength="500"
                onfocus="personSearchFieldHasFocus=true;"
                onblur="personSearchFieldHasFocus=false;" />

            <input
                name="subjectSearchbutton"
                type="submit"
                class="button1"
                value="Search"
                onclick="personSearchButtonHasFocus=true;"
                onfocus="personSearchButtonHasFocus=true;"
                onblur="personSearchButtonHasFocus=false;" />
              
            <div id="SubjectResultDiv" style="display:none;">
              <!-- The contents of this DIV will be inserted by JavaScript. -->
            </div>

  
     
            <div id="subjectDetails" style="float: left; padding-left: 10px; width: 400px;">
              <span class="category" id="subjectName">
                <!-- subject name gets inserted by Javascript at subject-selection time -->
              </span> <!-- subjectName -->
              <br />        
              <span class="dropback" id="subjectDescription">
                <!-- subject description gets inserted by Javascript at subject-selection time -->
              </span> <!-- subjectDescription -->
              <br />
              <span class="status" id="subjectWarning">
                <!-- subject warning gets inserted by Javascript at subject-selection time -->
              </span>
            </div>  <!-- subjectDetails -->
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
                 "Designation will take effect:",
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
                 "...and will remain in effect:",
                 "until I revoke this designation",
                 "on",
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
            CANCEL and return to your overview
          </a>
        </div>
      </div>
  
      <div id="Sidebar">      
        <div class="helpbox">
          <h2>
            help
          </h2>
            <p>The proxy you designate will be able to grant all of your <b>grantable</b> privileges, within the privilege type you select.</p>
            <p>The proxy <i>will not have</i> your <b>usable</b> privileges.</p>
            <p>To designate a proxy:</p>
            <p>1. Select the privilege type <i>(only the types in which you have grantable authority are listed)</i>.</p>
            <p>2. Search for and select the person you want to be your proxy.</p>
            <p>3. Set a start and/or end date for the proxy, or leave it open-ended.</p>
        </div>
      </div>
  
      <tiles:insert page="/tiles/footer.jsp" flush="true" />
  
    </div>  
  </form>
</body>
</html>