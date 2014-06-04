<%-- @annotation@
		 Standard tile which displays a page title and possibly subtitle
		 below the subheader
--%>
<%--
  @author Gary Brown.
  @version $Id: title.jsp,v 1.9 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic"
  tile="${requestScope['javax.servlet.include.servlet_path']}"
>
  <c:if test="${!empty title || !empty subtitle}">
    <%-- title1 is the breadcrumb, title2 is the title --%>
    <c:choose>
      <c:when test="${!empty title && !empty subtitle}">
        <c:set var="title1" scope="request">
          <grouper:message key="${title}"
            tooltipDisable="true" />
        </c:set>
        <c:set var="title2" scope="request">
        <grouper:message key="${subtitle}" tooltipDisable="true">
            <c:forEach var="arg" items="${subtitleArgs}">
              <grouper:param value="${arg}" />
            </c:forEach>
          </grouper:message>
        </c:set>
        <%-- this is the nav.properties id for the infodot body for this title/subtitle combo --%>
        <c:set var="titleInfodotName" scope="request" value="infodot.title.${title}.${subtitle}" />
        
        <%-- also try just the subtitle, which is probably a better choice --%>
        <c:choose>
          <c:when test="${empty navNullMap[titleInfodotName]}">
            <%-- try more general one --%>
            <!-- trying specific title infodot (another general one below) with nav.properties key: <c:out value="${titleInfodotName}" /> -->
            <c:set var="titleInfodotName" scope="request" value="infodot.title.${subtitle}" />
            
          </c:when>
          <c:otherwise>
             <!-- could also use more general title infodot with nav.properties key: <c:out value="infodot.title.${subtitle}" /> -->
          </c:otherwise>
        </c:choose>
        
      </c:when>
      <c:when test="${!empty title}">
        <%-- blank it out so it isnt carried over from another set --%>
        <c:set var="title1" scope="request" value="" />
        <c:set var="title2" scope="request">
          <grouper:message key="${title}"
            tooltipDisable="true" />
        </c:set>
        <%-- this is the nav.properties id for the infodot body for this title/subtitle combo --%>
        <c:set var="titleInfodotName" scope="request" value="infodot.title.${title}" />
        
      </c:when>
      <c:when test="${!empty subtitle}">
        <c:set var="title1" scope="request" value="" />
        <c:set var="title2" scope="request">
        <grouper:message key="${subtitle}" tooltipDisable="true">
            <c:forEach var="arg" items="${subtitleArgs}">
              <grouper:param value="${arg}" />
            </c:forEach>
          </grouper:message>
        </c:set>
        <%-- this is the nav.properties id for the infodot body for this title/subtitle combo --%>
        <c:set var="titleInfodotName" scope="request" value="infodot.title.${subtitle}" />
      </c:when>
    </c:choose>  
    <!-- trying title infodot with nav.properties key: <c:out value="${titleInfodotName}" /> -->
    <c:if test="${!empty title1}"><div class="breadcrumb"><c:out value="${title1}" escapeXml="false"/></div></c:if>
      <h1 id="title"><c:out value="${title2}" escapeXml="false"/>
      
      <%-- CH 20080325 only show if there is one --%> <c:if
        test="${!empty navNullMap[titleInfodotName]}"
      >
        &nbsp;<grouper:infodot hideShowHtmlId="titleHideShow_${titleInfodotName}" />
      </c:if></h1>
      <c:if test="${!empty navNullMap[titleInfodotName]}">
        <div class="helpText"
          <grouper:hideShowTarget hideShowHtmlId="titleHideShow_${titleInfodotName}"  />
        ><grouper:message key="${titleInfodotName}"
          useNewTermContext="true"
        /></div>
      </c:if>
  
  </c:if>
</grouper:recordTile>

