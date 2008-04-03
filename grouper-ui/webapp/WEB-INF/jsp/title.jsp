<%-- @annotation@
		 Standard tile which displays a page title and possibly subtitle
		 below the subheader
--%>
<%--
  @author Gary Brown.
  @version $Id: title.jsp,v 1.5 2008-04-03 07:48:21 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic"
  tile="${requestScope['javax.servlet.include.servlet_path']}"
>
  <c:if test="${!empty title || !empty subtitle}">
    <div><c:if test="${!empty title}">
      <h1 id="title"><grouper:message bundle="${nav}" key="${title}"
        tooltipDisable="true"
      /><span id="subtitle"> <c:choose>
        <%-- if there is a title and subtitle --%>
        <c:when test="${!empty subtitle}">
          <%-- this is the nav.properties id for the infodot body for this title/subtitle combo --%>
          <c:set var="titleInfodotName" scope="request" value="infodot.title.${title}.${subtitle}" />
          <!-- trying title infodot with nav.properties key: <c:out value="${titleInfodotName}" /> -->
        		&nbsp;-&nbsp;<grouper:message bundle="${nav}" key="${subtitle}">
            <c:forEach var="arg" items="${subtitleArgs}">
              <grouper:param value="${arg}" />
            </c:forEach>
          </grouper:message>
        </c:when>
        <c:otherwise>
          <%-- only the title is set --%>
          <%-- this is the nav.properties id for the infodot body for this title/subtitle combo --%>
          <c:set var="titleInfodotName" scope="request" value="infodot.title.${title}" />
          <!-- trying title infodot with nav.properties key: <c:out value="${titleInfodotName}" /> -->
        </c:otherwise>
      </c:choose> </span>
      <%-- c:out value="Map: ${navNullMap[titleInfodotName]}, titleName: ${titleInfodotName}, title: ${title}" / --%>
      <%-- if we are title.subtitle, and not there, then try just title --%>
      <%-- this isnt valid since it is usually misleading... c:if
        test="${empty navNullMap[titleInfodotName] && !empty subtitle}"
      >
        <c:set var="titleInfodotName" scope="request" value="infodot.title.${title}" />
        <!-- trying title infodot with nav.properties key: <c:out value="${titleInfodotName}" /> -->
      </c:if --%>
      
      <%-- CH 20080325 only show if there is one --%> <c:if
        test="${!empty navNullMap[titleInfodotName]}"
      >
        &nbsp;<grouper:infodot hideShowHtmlId="titleHideShow_${titleInfodotName}" />
      </c:if></h1>
      <c:if test="${!empty navNullMap[titleInfodotName]}">
        <div class="helpText"
          <grouper:hideShowTarget hideShowHtmlId="titleHideShow_${titleInfodotName}"  />
        ><grouper:message bundle="${nav}" key="${titleInfodotName}"
          useNewTermContext="true"
        /></div>
      </c:if>
    </c:if></div>
  </c:if>
</grouper:recordTile>

