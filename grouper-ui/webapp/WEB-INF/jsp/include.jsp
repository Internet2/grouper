<%-- @annotation@
		  Contains standard imports, tag libraries so they don't have
		  to be repeated oon every page
		  
--%><%--
  @author Gary Brown.
  @version $Id: include.jsp,v 1.2 2006-07-17 10:04:36 isgwb Exp $
--%><%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%><c:set var="_trash">

<%@page import="java.util.*"%>
<%@page import="edu.internet2.middleware.grouper.ui.util.*"%>
<%@page import="edu.internet2.middleware.grouper.ui.*"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jstl/xml" prefix="x"%>

<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/request-1.0" prefix="req" %>
<%@ taglib uri="http://middleware.internet2.edu/dir/groups/grouper" prefix="grouper"%>
<html:xhtml/></c:set>