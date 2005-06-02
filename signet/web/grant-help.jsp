<!--
   $Id: grant-help.jsp,v 1.4 2005-06-02 06:26:04 jvine Exp $
   $Date: 2005-06-02 06:26:04 $

   Copyright 2004, 2005 Internet2 and Stanford University.  All Rights 
Reserved.
   Licensed under the Signet License, Version 1,
   see doc/license.txt in this distribution.
--> 


<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
         
   Subsystem currentSubsystem
     = (Subsystem)
         (request.getSession().getAttribute("currentSubsystem"));
%>


	<P>Granting authority takes place in a series of steps. You will select: </P>
	<UL>
		<LI> the privilege you want to grant </LI>
		<LI> the scope to which the privilege applies</LI>
		<LI> any limits attached to the privilege, such as spending amounts, populations, etc.</LI>
  </UL>
  <P>Only the privileges, scopes, and limits  you are authorized to grant are listed. </P>