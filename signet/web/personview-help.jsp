<!--
   $Id: personview-help.jsp,v 1.4 2005-03-01 21:07:36 acohen Exp $
   $Date: 2005-03-01 21:07:36 $

   Copyright 2004, 2005 Internet2 and Stanford University.  All Rights 
Reserved.
   Licensed under the Signet License, Version 1,
   see doc/license.txt in this distribution.
--> 


<%@ page import="edu.internet2.middleware.signet.Signet" %>
<%@ page import="edu.internet2.middleware.signet.PrivilegedSubject" %>
<%@ page import="edu.internet2.middleware.signet.Subsystem" %>
<%@ page import="edu.internet2.middleware.subject.Subject" %>

<% 
  Signet signet
     = (Signet)
         (request.getSession().getAttribute("signet"));
   
   PrivilegedSubject currentGranteePrivilegedSubject
     = (PrivilegedSubject)
         (request.getSession().getAttribute("currentGranteePrivilegedSubject"));
         
   Subsystem currentSubsystem
     = (Subsystem)
         (request.getSession().getAttribute("currentSubsystem"));
         
   String subjectTypeId = currentGranteePrivilegedSubject.getSubjectTypeId();
   String subjectId = currentGranteePrivilegedSubject.getSubjectId();
   Subject subject = signet.getSubject(subjectTypeId, subjectId);
   String subjectName = subject.getName();
%>

	<P>
		This page shows all the privileges of the selected type
		(<%=(currentSubsystem == null ? "NO ASSIGNED" : currentSubsystem.getName())%>)
		assigned to the selected person
		(<%=subjectName%>).
	</P>
	<P>
		To view other types of privilege assigned to this person, select a privilege
		type from the &quot;assigned privilege types&quot; drop-down menu and click
		"Show." If a privilege type is not listed, this person does not have an
		privileges of that type.
	</P>
