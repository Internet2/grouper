<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: designate.jsp,v 1.10 2006-05-16 17:37:35 ddonn Exp $
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
</head>

<body>

  <script type="text/javascript">
    
    function subsystemSelected()
    {
      var theForm = document.form1;
      var subsystemSelect = document.form1.<%=Constants.SUBSYSTEM_HTTPPARAMNAME%>;
      if (subsystemSelect.selectedIndex < 1)
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
    
    function setContinueButtonStatus()
    {
      if (subsystemSelected())
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
<%@ page import="edu.internet2.middleware.signet.Status" %>

<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>

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
   
  String personViewHref
    = "PersonView.do?granteeSubjectTypeId="
      + currentPSubject.getSubjectTypeId()
      + "&granteeSubjectId="
      + currentPSubject.getSubjectId();
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
      <a href="<%=personViewHref%>"><%=ResLoaderUI.getString("designate.subjview.txt") %> 
        [<%=currentPSubject.getName()%>]
      </a>
      &gt; <%=currentProxy==null ? "" : ResLoaderUI.getString("designate.edit.txt") %> <%=ResLoaderUI.getString("designate.designateddriver.txt") %>
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
            <%=currentProxy==null ? ResLoaderUI.getString("designate.designating.txt") : ResLoaderUI.getString("designate.editing.txt")%>
            <%=isSubsystemOwner ? ResLoaderUI.getString("designate.subsysowner.txt") : ResLoaderUI.getString("designate.proxy.txt")%>
          </span>             
          <h1><%=currentPSubject.getName()%> </h1>
          <span class="ident">
            <%=currentPSubject.getDescription()%>
          </span>
        </div> <!-- ViewHead -->
 
        <div class="section" id="summary">
		  <h2><%=(currentProxy == null) ? ResLoaderUI.getString("designate.new.hdr") : ResLoaderUI.getString("designate.current.hdr") %> <%=ResLoaderUI.getString("designate.driverdetails.hdr") %> </h2>
          <table>
            <tbody>
              <tr>
                <td class="label">
                  <%=ResLoaderUI.getString("designate.privilege.lbl") %>
                </td>
                <td class="content">
				<!-- if this is a standard granting proxy, then -->
				   <span class="category">Signet</span> : 
				   <span class="function">
					<%=isSubsystemOwner ? ResLoaderUI.getString("designate.subsysowner.txt") : ResLoaderUI.getString("designate.proxy.txt") %>				   
				   </span> :
					<%=isSubsystemOwner ? ResLoaderUI.getString("designate.actas.txt") : ResLoaderUI.getString("designate.grantas.txt") + loggedInPrivilegedSubject.getName() %>
                </td>
              </tr>
<% if (currentProxy != null)
   {
%>			  
              <tr>
                <td class="label">
                  <%=ResLoaderUI.getString("designate.forprivtype.txt") %>
                </td>
                <td class="content">
                  <%=currentProxy.getSubsystem().getName() %>
                </td>
              </tr>
<%
  }
%>			  
            </tbody>
          </table>
        </div> <!-- section -->

<% if (currentProxy == null)
   {
%>
        <div class="section">
          <h2><%=ResLoaderUI.getString("designate.selprivtype.hdr") %> </h2>
          <div style="margin-left: 25px;">
            <%=Common.subsystemSelectionSingle
                   (Constants.SUBSYSTEM_HTTPPARAMNAME,
                    Constants.SUBSYSTEM_PROMPTVALUE,
                    ResLoaderUI.getString("designate.selprivtype.txt"),
                    "setContinueButtonStatus();",
                    grantableSubsystems,
                    (currentProxy == null ? null : currentProxy.getSubsystem()))%>
          </div>
        </div> <!-- section -->
<%
  }
%>
     
        <div class="section">
          <h2><%=ResLoaderUI.getString("designate.setconditions.hdr") %> </h2>
        <table>
        
            <tr>
              <%=Common.dateSelection
                (request,
                 Constants.EFFECTIVE_DATE_PREFIX,
                 ResLoaderUI.getString("designate.effective.txt"),
                 ResLoaderUI.getString("designate.immediately.txt"),
                 ResLoaderUI.getString("designate.on.txt"),
                 currentProxy == null
                   ? null
                   : currentProxy.getEffectiveDate(),
                 currentProxy == null
                   ? true
                   : currentProxy.getStatus().equals(Status.PENDING))%>
            </tr>
            
            <tr>
              <%=Common.dateSelection
                (request,
                 Constants.EXPIRATION_DATE_PREFIX,
                 ResLoaderUI.getString("designate.duration.txt"),
                 ResLoaderUI.getString("designate.untilrevoked.txt"),
                 ResLoaderUI.getString("designate.until.txt"),
                 currentProxy == null
                   ? null
                   : currentProxy.getExpirationDate())%>
            </tr>
          </table>
<!--           <legend></legend>  -->
        </div>
        <!-- section -->
        
        <div class="section">
        
          <h2><%=ResLoaderUI.getString("designate.completedriver.hdr") %> </h2>  
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
            value="<%=(currentProxy==null ? ResLoaderUI.getString("designate.completedesignateion.txt") : ResLoaderUI.getString("designate.savechanges.txt")) %>" 
       />        
          <br />
          <a href="Start.do?<%=Constants.CURRENTPSUBJECT_HTTPPARAMNAME%>=<%=Common.buildCompoundId(loggedInPrivilegedSubject.getEffectiveEditor())%>">
            <img src="images/arrow_left.gif" />
            <%=ResLoaderUI.getString("designate.cancel.txt") %></a>
        </div>
      </div>
  
      <div id="Sidebar">      
        <div class="helpbox">
          <h2><%=ResLoaderUI.getString("designate.help.hdr") %></h2>
		  
		 <%
  if (isSubsystemOwner)
  {
%>
   <p><%=ResLoaderUI.getString("designate.owner.help_1.txt") %></p>
   <p><%=ResLoaderUI.getString("designate.owner.help_2.txt") %></p>
   <p><%=ResLoaderUI.getString("designate.owner.help_3.txt") %></p>
     <ol>
     <li><%=ResLoaderUI.getString("designate.owner.help_4.txt") %></li>
     <li><%=ResLoaderUI.getString("designate.owner.help_5.txt") %></li>
     <li><%=ResLoaderUI.getString("designate.owner.help_6.txt") %></li>
     </ol>     
<%
  }else{
%>
   <p><%=ResLoaderUI.getString("designate.proxy.help_1.txt") %></p>
   <p><%=ResLoaderUI.getString("designate.proxy.help_2.txt") %></p>
   <p><%=ResLoaderUI.getString("designate.proxy.help_3.txt") %></p>
     <ol>
     <li><%=ResLoaderUI.getString("designate.proxy.help_4.txt") %><br />
        <span class="dropback"><%=ResLoaderUI.getString("designate.proxy.help_5.txt") %></span></li>
     <li><%=ResLoaderUI.getString("designate.proxy.help_6.txt") %></li>
     <li><%=ResLoaderUI.getString("designate.proxy.help_7.txt") %></li>
     </ol>          
<%
  }
%>
		  
		  

        </div>
      </div>
  
      <tiles:insert page="/tiles/footer.jsp" flush="true" />
  
    </div>  
  </form>
</body>
</html>
