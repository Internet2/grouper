<!--
   $Id: personview-help.jsp,v 1.5 2005-06-02 06:26:04 jvine Exp $
   $Date: 2005-06-02 06:26:04 $

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
		This page shows the privileges granted to
				(<%=subjectName%>), filtered by type (<%=(currentSubsystem == null ? "NO ASSIGNED" : currentSubsystem.getName())%>).</P>
	<P>
		To view another privilege type, select it from the drop-down menu and click &quot;Show.&quot; If a privilege type is not shown, this subject does not have any privileges of that type.</P>
  <P>To grant more privileges to this subject, select the type of privilege you want to grant, and click &quot;Start.&quot; The menu of privilege types shows only those you can grant. </P>
