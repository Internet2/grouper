<!--
  $Id: org-browse.jsp,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
-->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <meta name="robots" content="noindex, nofollow">
    <title>
      Signet
    </title>
    <link href="styles/signet.css" rel="stylesheet" type="text/css">
    <script language="JavaScript" type="text/javascript" src="scripts/signet.js">
    </script>
  </head>

  <body>

<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>

<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.signet.Category" %>
<%@ page import="edu.internet2.middleware.signet.Assignment" %>
<%@ page import="edu.internet2.middleware.signet.Function" %>
<%@ page import="edu.internet2.middleware.signet.Signet" %>

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
         
   DateFormat dateFormat = DateFormat.getDateInstance();
   
   String personViewHref
     = "PersonView.do?granteeSubjectTypeId="
       + currentGranteePrivilegedSubject.getSubjectTypeId()
       + "&granteeSubjectId="
       + currentGranteePrivilegedSubject.getSubjectId();
       
   String functionsHref
     = "Functions.do?select="
       + currentSubsystem.getId();
%>

    <form name="form1" action="Limits.do">  
      <div id="Header">  
        <div id="Logo">
          <img src="images/KITN.gif" alt="logo" height="60" width="216">
        </div>
        <div id="Signet">
					<img src="images/signet.gif" alt="Signet" height="60" width="49">
        </div>
      </div>
      <div id="Navbar">
        <span class="logout">
          <a href="NotYetImplemented.do">
            <%=loggedInPrivilegedSubject.getName()%>: Logout
          </a>
        </span>
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
        </span>
      </div>
      <div id="Layout">
        <div id="Content">
          <div class="table1">
            Granting  privilege to
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
                class="button1"
                onclick=(parent.location='<%=functionsHref%>')
                value="&lt;&lt; Change privilege"
                type="button">
            </div> <!-- end textcontent -->
              <div class="tableheader">
                Organizational scope
              </div>
              <div class="textcontent">
                <table border="0" width="200">
                  <tbody>
<!--
                    <tr>
                      <td nowrap="nowrap">
                        <select class="long">
                          <option selected="selected">
                            (recently selected scopes)
                          </option>
                          <option>Administration/Finance</option>
                          <option> Capital Accounting </option>
                          <option> Central Mgmt-Payroll </option>
                          <option> Central Mgmt-Student Loans </option>
                          <option> Central Mgmt-Taxes </option>
                          <option> Clinical Pharmacology </option>
                          <option> Communication </option>
                          <option> Controller's Office Operations </option>
                          <option> Corporate Relations </option>
                          <option> Dean's Office </option>
                          <option> Department of Athletics, P.E. an </option>
                          <option> Department of Capital Planning </option>
                          <option> Department of Project Management </option>
                          <option> Dermatology </option>
                          <option> Development - Humanities and Sci </option>
                          <option> Dining Services Central Operatio </option>
                          <option> Division of Literature, Cultures </option>
                          <option> Domestic Water System </option>
                          <option> Electric System </option>
                          <option> Faculty/Staff Housing </option>
                          <option> Financial Planning </option>
                          <option> Gastroenterology and Hepatology </option>
                          <option> Gift Processing </option>
                          <option> Human Resources </option>
                          <option> Humanities and Sciences </option>
                          <option> Infectious Diseases </option>
                          <option>Kansas Institute of Technology</option>
                          <option>Land and Buildings Office</option>
                          <option>Marketing &amp; Communications </option>
                          <option> Mathematics </option>
                          <option> Office for Religious Life </option>
                          <option>Office of Chief Information Officer</option>
                          <option>Office of Research Administration</option>
                          <option> Parking and Transportation </option>
                          <option> President's Office Operations </option>
                          <option> Procurement </option>
                          <option> Provost's Office Operations </option>
                          <option> Public Affairs Administration </option>
                          <option> Radiology </option>
                          <option>Records &amp; Central Files </option>
                          <option> Utilities Administration </option>
                        </select>
                      </td>
                      <td>
                        or
                      </td>
                      <td nowrap="nowrap" valign="bottom">
                        <span class="dropback">
                          Search for an organization
                        </span>
                        <br>
                        <input name="textfield" class="long" type="text">
                        <input
                          name="Button"
                          class="button1"
                          onclick='(parent.location="org-search.html")'
                          value="Search"
                          type="button">
                      </td>
                    </tr>
                  <tr>
                  <td> </td>
                  <td>or</td>
                  <td nowrap="nowrap" valign="bottom">
                    <a href="org-browse.html">
                      &gt;Browse scope hierarchy
                    </a>
                  </td>
-->
                </tbody>
              </table>
              <span class="columnhead">
                Complete organizational hierarchy
              </span>
              <br>
<!--
              <div id="Scroll">
-->
              <select
              	name="scope"
              	size=17
              	onclick="javascript:document.form1.continueButton.disabled=false">
                <%=signet.printTreeNodesInContext
                    ("<option disabled value=\"",  // ancestorPrefix
                     "<option value=\"",           // selfPrefix
                     "<option value=\"",           // descendantPrefix
                     "",                           // prefixIncrement
                     "\">\n",                      // infix
                     ". ",                         // infixIncrement
                     "</option>\n",                // suffix
                     loggedInPrivilegedSubject.getGrantableScopes(currentFunction))%>
              </select>
              
              
<!--
            </div>
-->
          </div> <!-- end textcontent -->

    <p>
      <input
        name="continueButton"
        disabled="true"
        type="submit"
        class="button-def"
        value="Continue >>" />
    </p>
            <p>
              <a href="<%=personViewHref%>">
                <img src="images/icon_arrow_left.gif" width="16" height="16" class="icon" />Cancel and return to <%=currentGranteePrivilegedSubject.getName()%>'s view
              </a>
            </p>
          </div> <!-- end table1 -->
          <jsp:include page="footer.jsp" flush="true" />
        </div> <!-- end Content -->
        <div id="Sidebar">     
          <div class="box2">
            <div class="actionheader">
              Info
            </div>
            <div class="actionbox">
              <p>Contextual help goes here. </p>
            </div>
   	  </div> <!-- end box1 -->
        </div> <!-- end Sidebar -->
      </div>	<!-- end Layout -->
    </form>
  </body>
</html>