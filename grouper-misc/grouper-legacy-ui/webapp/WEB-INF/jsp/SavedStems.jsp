<%-- @annotation@ Top level JSP which displays a list of saved groups --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:message key="saved-stems.intro"/>
<div class="savedGroupsList section">
<div class="sectionBody">
<c:if test="${savedStemsSize==0}">
<grouper:message key="saved-stems.none"/>
</c:if>

<c:if test="${savedStemsSize>0}">
	<html:form action="/populateListSavedStems.do" method="post">
  <table class="formTable formTableSpaced">
	<tiles:insert definition="searchStemResultFieldChoiceDef"/>
  <c:if test="${!empty mediaMap['search.stem.result-field']}">
    <tr class="formTableRow">
    <td class="formTableLeft">&nbsp;</td>
    <td class="formTableRight">
    <html:submit styleClass="blueButton" property="x" value="${navMap['saved-stems.change-field']}"/>
    </td>
    </tr>
  </c:if>
  </table>
	</html:form>
	<html:form action="/removeSavedStems.do" method="post">
	<ul class="savedStems">
	<c:forEach var="stem" items="${savedStems}">
		<li><input name="stemIds" type="checkbox" value="<c:out value="${stem.id}"/>"/>
		<tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="stem"/>
		  <tiles:put name="view" value="savedStem"/>
	  </tiles:insert> 
		</li>
	</c:forEach>
	</ul>
		<html:submit styleClass="blueButton" property="x" value="${navMap['saved-stems.remove-selected']}"/> 
	</html:form>
</c:if>
</div>
</div>



