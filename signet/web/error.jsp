<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: error.jsp,v 1.6 2005-07-27 18:31:49 jvine Exp $
  $Date: 2005-07-27 18:31:49 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
-->

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta name="robots" content="noindex, nofollow" />
    <title>
      Unexpected Error
    </title>
    <link href="styles/signet.css" rel="stylesheet" type="text/css" />
    <script language="JavaScript" type="text/javascript" src="scripts/signet.js">
    </script>
  </head>
  <body>
  
    <%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>

  <%@ page import="org.apache.struts.Globals" %>
  <%@ taglib uri="/tags/struts-bean" prefix="bean" %>
  <%@ taglib uri="/tags/struts-logic" prefix="logic" %>
  <tiles:insert page="/tiles/header.jsp" flush="true" /> 
      
      <div id="Navbar">
        <span class="logout">
<!--
          <a href="blank.html">
            Your Name: Logout
          </a>
-->
        </span> <!-- Navbar -->
        <span class="select">
          <a href="Start.do">
            Home
          </a>
            > Unexpected Error
        </span> <!-- select -->
      </div> <!-- Navbar -->
      
      <div id="Layout">
        <div id="Content">
           <h1>An unexpected error has occured</h1>
	    <logic:present name="<%=Globals.EXCEPTION_KEY%>">
	      <p><bean:write name="<%=Globals.EXCEPTION_KEY%>" property="message" /></p>
	    </logic:present>

            <br />
            <a href="Start.do">
            <img src="images/arrow_left.gif" alt="" />Return to home page
            </a>
          <tiles:insert page="/tiles/footer.jsp" flush="true" />
        </div> <!-- Content -->
        
        <div id="Sidebar">
        </div>
      </div>

  </body>
</html>
