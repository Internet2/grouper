<%-- @annotation@
		  Contains standard imports, tag libraries so they don't have
		  to be repeated oon every page
		  
--%><%--
  @author Gary Brown.
  @version $Id: include.jsp,v 1.3 2008-04-09 14:59:42 isgwb Exp $
--%><%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%><c:set var="_trash">

<%@page import="java.util.*"%>
<%@page import="edu.internet2.middleware.grouper.ui.util.*"%>
<%@page import="edu.internet2.middleware.grouper.ui.*"%>
<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.logging.LogFactory"%>
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