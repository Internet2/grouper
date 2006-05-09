<!--
  $Id: footer.jsp,v 1.4 2006-05-09 01:33:33 ddonn Exp $
  $Date: 2006-05-09 01:33:33 $
  
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
    <A href="@FEEDBACK_URL@" title="Signet feedback">
      <%=ResLoaderUI.getString("footer.feedback.txt") %>
    </A>
  </P>
</DIV>
