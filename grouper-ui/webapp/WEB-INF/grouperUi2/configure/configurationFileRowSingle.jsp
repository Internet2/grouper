<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<%-- GRP-7351: render a single config property row from the request container, for re-rendering a
     row in place after an edit. configure.jsp includes configurationFileRowContents.jsp directly
     from its loop instead, where the variables come from the loop. --%>
<c:set var="guiConfigProperty" value="${grouperRequestContainer.configurationContainer.currentGuiConfigProperty}" />
<c:set var="i" value="${grouperRequestContainer.configurationContainer.currentIndex}" />
<c:set var="configItemMetadata" value="${guiConfigProperty.configItemMetadata}" />
<%@ include file="configurationFileRowContents.jsp"%>
