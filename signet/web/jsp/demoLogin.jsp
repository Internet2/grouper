<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
  $Id: demoLogin.jsp,v 1.1 2006-01-13 19:01:12 acohen Exp $
  $Date: 2006-01-13 19:01:12 $
  
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

  <body onLoad="document.form1.username.focus();">
  
    <%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
    <tiles:insert page="/tiles/header.jsp" flush="true" />

    <form name="form1" action="DemoLoginCheck.do">
      
      <div id="Navbar">
        <span class="select">
		  Login
		</span> <!-- select -->
      </div> <!-- Navbar -->
      
      <div id="Layout">
        <div id="Content">
          <h1>
            Login to Signet
          </h1>

          <table class="invis" style="margin-top: 25px;">
          
            <tr>
              <th class="label" scope="row" style="border:none;">
                <label for="username">
                  User name:
                </label>
              </th>
              <td>
                <input name="username" type="text" class="short" id="username" />
              </td>
            </tr>
            
            <tr>
              <th class="label" scope="row" style="border:none;">
                <label for="password">
                  Password:
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
                  value="Login"/>
              </td>
            </tr>
          </table>
        </div> <!-- Content -->
        <tiles:insert page="/tiles/footer.jsp" flush="true" />
      </div>	
    </form>
  </body>
</html>