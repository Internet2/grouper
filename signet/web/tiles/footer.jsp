<!--
  $Id: footer.jsp,v 1.5 2006-06-13 00:51:11 ddonn Exp $
  $Date: 2006-06-13 00:51:11 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
-->
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ page import="edu.internet2.middleware.signet.resource.ResLoaderUI" %>

<DIV id="Footer">
  <P>
    <%=ResLoaderUI.getString("footer.copyright.txt") %>
    <BR /> 
    <A href=<%=ResLoaderUI.getString("footer.terms.href") %> target="_blank">
      <%=ResLoaderUI.getString("footer.terms.txt") %>
    </A>
    |
    <A href=<%=ResLoaderUI.getString("footer.feedback.href") %> title="Signet feedback">
      <%=ResLoaderUI.getString("footer.feedback.txt") %>
    </A>
  </P>
</DIV>
