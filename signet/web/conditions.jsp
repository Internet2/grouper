<!--
  $Id: conditions.jsp,v 1.4 2005-02-16 17:19:29 acohen Exp $
  $Date: 2005-02-16 17:19:29 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
-->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
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

<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.tree.TreeNode" %>
<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.Limit" %>
<%@ page import="edu.internet2.middleware.signet.ui.LimitRenderer" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
  PrivilegedSubject loggedInPrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute("loggedInPrivilegedSubject"));
   
   PrivilegedSubject currentGranteePrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute("currentGranteePrivilegedSubject"));
         
   Subsystem currentSubsystem
     = (Subsystem)
         (request.getSession().getAttribute("currentSubsystem"));
         
   Category currentCategory
     = (Category)
         (request.getSession().getAttribute("currentCategory"));
         
   Function currentFunction
     = (Function)
         (request.getSession().getAttribute("currentFunction"));
         
   TreeNode currentScope
     = (TreeNode)
         (request.getSession().getAttribute("currentScope"));
         
   Limit[] currentLimits = currentFunction.getLimitsArray();
         
   DateFormat dateFormat = DateFormat.getDateInstance();
   
   String personViewHref
     = "PersonView.do?granteeSubjectTypeId="
       + currentGranteePrivilegedSubject.getSubjectTypeId()
       + "&granteeSubjectId="
       + currentGranteePrivilegedSubject.getSubjectId();
       
   String functionsHref
     = "Functions.do?select="
       + currentSubsystem.getId();
       
   String orgBrowseHref
   	= "OrgBrowse.do?step3="
   		+ currentFunction.getId();
%>

    <form name="form1" action="Confirm.do">
      <div id="Header">  
        <div id="Logo">
          <img src="images/KITN.gif" width="216" height="60" alt="logo" />
        </div> <!-- Logo -->
        <div id="Signet">
					<img src="images/signet.gif" alt="Signet" height="60" width="49">
        </div> <!-- Signet -->
      </div> <!-- Header -->
      
      <div id="Navbar">
        <span class="logout">
          <a href="NotYetImplemented.do">
            <%=loggedInPrivilegedSubject.getName()%>: Logout
          </a>
        </span> <!-- logout -->
        <span class="select">
          <a href="Start.do">
            Home
          </a>
          >  <!-- This single right-angle-bracket is just a text element, not an HTML token. -->
          <a href="<%=personViewHref%>"
            >  <!-- This single right-angle-bracket is just a text element, not an HTML token. -->
            <%=currentGranteePrivilegedSubject.getName()%>
          </a>
          &gt; Grant new privilege
        </span> <!-- select -->
      </div>  <!-- Navbar -->
      
      <div id="Layout">
        <div id="Content">
          <div class="table1">
            Granting new privilege to
            <h1>
              <%=currentGranteePrivilegedSubject.getName()%>
       	    </h1>
       	    <%=currentGranteePrivilegedSubject.getDescription()%><!--,	Technology Strategy and Support Operations-->
            <br />
            <br />
          
            <div class="tableheader">
              New <%=currentSubsystem.getName()%> privilege
            </div>
            <div class="textcontent">
              <ul class="none">
                <li>
                  <%=currentCategory.getName()%>
                  <ul class="arrow">
                    <li>
                      <%=currentFunction.getName()%>
                    </li>
                  </ul>
                </li>
              </ul>
              <input
                name="Button"
                type="button"
                class="button1"
                onclick=(parent.location='<%=functionsHref%>')
                value="&lt;&lt; Change privilege" />
            </div> <!-- textcontent -->
            
            <div class="tableheader">
              scope
            </div> <!-- tableheader -->
            <div class="textcontent">
              <ul class="none">
              
                <%=signet.displayAncestry
                    (currentScope,
                     "<ul class=\"arrow\">\n",  // childSeparatorPrefix
                     "\n<li>\n",                // levelPrefix
                     "\n</li>\n",               // levelSuffix
                     "\n</ul>")                 // childSeparatorSuffix
                 %>
              
              </ul>
            
              <input
                name="Button"
                type="button"
                class="button1"
                onclick=(parent.location='<%=orgBrowseHref%>')
                value="&lt;&lt; Change scope" />
            </div> <!-- textcontent -->
            
<%
  if (currentLimits.length > 0)
  {
%>
            <div class="tableheader">
              Set Limits
            </div> <!-- tableheader -->
            <div class="textcontent">	
<%
    for (int i = 0; i < currentLimits.length; i++)
    {
      request.setAttribute("limit", currentLimits[i]);
%>
              
              <fieldset>
                <legend>
                  <%=currentLimits[i].getName()%>
                </legend>
                <p>
                  <%=currentLimits[i].getHelpText()%>
                </p>
                <blockquote>
                  <tiles:insert
                     page='<%="/tiles/" + currentLimits[i].getRenderer()%>'
                     flush="true">
                    <tiles:put name="limit" beanName="limit" />
                  </tiles:insert>
                </blockquote>
              </fieldset>
<%
    }
%>
            </div> <!-- textcontent -->
<%
  }
%>
		 
            <div class="tableheader">
              Set Conditions
            </div> <!-- tableheader -->
          
            <div class="textcontent">
              <fieldset>
                <legend>
                  Extensibility
                </legend>
                <p>
                  Privilege holder can:
                </p>
                <blockquote>
                  <input
                     name="can_use"
                     type="checkbox"
                     value="checkbox"
                     checked="checked" />
                  use this privilege
                  <br />
                  <input name="can_grant" type="checkbox" value="checkbox" />
                  grant this privilege to others
                </blockquote>
              </fieldset>
              <p>
                <input
                   name="Button"
                   type="submit"
                   class="button-def"
                   value="Complete assignment" />
              </p>
              <p>
                <a href="<%=personViewHref%>">
                  <img src="images/icon_arrow_left.gif" width="16" height="16" />Cancel and return to <%=currentGranteePrivilegedSubject.getName()%>'s view
                </a>
              </p>
            </div> <!-- textcontent -->
          </div>
	      <jsp:include page="footer.jsp" flush="true" />
        </div>
      
        <div id="Sidebar">
          <div class="box1">
            <div class="actionheader">
              Info
            </div> <!-- actionheader -->
            <div class="actionbox">
              <p>
                Contextual help goes here.
              </p>
            </div> <!-- actionbox -->
          </div> <!-- box1 -->
        </div> <!-- Sidebar -->
      </div>	
    </form>
  </body>
</html>
