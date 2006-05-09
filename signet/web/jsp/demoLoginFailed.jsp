<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: demoLoginFailed.jsp,v 1.2 2006-05-09 01:33:33 ddonn Exp $
  $Date: 2006-05-09 01:33:33 $
  
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

  <body onLoad="document.form1.username.focus();">

	<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>
  
    <%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
    <tiles:insert page="/tiles/header.jsp" flush="true" />

    <form name="form1" action="DemoLoginCheck.do">
      
      <div id="Navbar">
        <span class="select">
		  <%=ResLoaderUI.getString("demoLogin.navbar.lb") %>
		</span> <!-- select -->
      </div> <!-- Navbar -->
      
      <div id="Layout">
        <div id="Content">
          <h1>
            <%=ResLoaderUI.getString("demoLogin.content.h1.lb") %>
          </h1>
			
          <div class="alert">
            <p>
              <img src="images/caution.gif" align="left" />
              <%=ResLoaderUI.getString("demoLoginFailed.alert.txt") %>
            </p>

</div>

          <table class="invis" style="margin-top: 25px;">
          
            <tr>
              <th class="label" scope="row" style="border:none;">
                <label for="username">
                  <%=ResLoaderUI.getString("demoLogin.username.lb") %>
                </label>
              </th>
              <td>
                <input name="username" type="text" class="short" id="username" />
              </td>
            </tr>
            
            <tr>
              <th class="label" scope="row" style="border:none;">
                <label for="password">
                  <%=ResLoaderUI.getString("demoLogin.password.lb") %>
                </label>
              </th>
              <td>
                <input name="password" type="password" class="short" id="password" />
              </td>
            </tr>
            
            <tr>
              <td align="right">
                &nbsp;
              </td>
              <td>
                <input
                  name="Button"
                  type="submit"
                  class="button-def"
                  value=<%=ResLoaderUI.getString("demoLogin.login.bt") %> />
              </td>
            </tr>
          </table>
        </div> <!-- Content -->
        <tiles:insert page="/tiles/footer.jsp" flush="true" />
      </div>	
    </form>
  </body>
</html>