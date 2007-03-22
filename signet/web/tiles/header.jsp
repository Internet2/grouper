<!--
   $Id: header.jsp,v 1.3 2007-03-22 23:00:55 ddonn Exp $
   $Date: 2007-03-22 23:00:55 $

   Copyright 2004, 2005 Internet2 and Stanford University.  All Rights Reserved.
   Licensed under the Signet License, Version 1, see doc/license.txt in this distribution.
-->
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>

<DIV id="Header">  
  <DIV id="Logo">
    <IMG src=<%=ResLoaderUI.getString("header.logo.image") %> alt="logo" />
  </DIV> <!-- Logo -->
  <DIV id="Signet">
    <IMG src=<%=ResLoaderUI.getString("header.signet.image") %> alt="Signet" >
  </DIV> <!-- Signet -->
</DIV> <!-- Header -->
