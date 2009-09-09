<%-- @annotation@
		  Contains standard imports, tag libraries so they don't have
		  to be repeated oon every page
		  
--%><%--
  @author Gary Brown.
  @version $Id: include.jsp,v 1.4 2009-09-09 15:10:03 mchyzer Exp $
--%><%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %><c:set var="_trash">

<%@page import="java.util.*"%>
<%@page import="edu.internet2.middleware.grouper.ui.util.*"%>
<%@page import="edu.internet2.middleware.grouper.ui.*"%>
<%@page import="org.apache.commons.logging.Log"%>
<%@page import="org.apache.commons.logging.LogFactory"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean-el.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic-el.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles-el.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/x.tld" prefix="x"%>

<%@ taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tld/taglibs-datetime.tld" prefix="dt" %>
<%@ taglib uri="/WEB-INF/tld/taglibs-request.tld" prefix="req" %>
<%@ taglib uri="/WEB-INF/tld/grouper-el.tld" prefix="grouper"%>

<%-- @ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn" --%>
<%-- @ taglib uri="/WEB-INF/tld/grouperGui.tld" prefix="grouperGui" --%>

<html:xhtml/></c:set>