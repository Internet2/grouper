<%-- @annotation@
		Tile which displays the simple subject search form  - allows any configured source to be selected
--%>
<%--
  @author Gary Brown.
  @version $Id: SimpleSubjectSearch.jsp,v 1.11 2009-10-30 15:06:34 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic"
  tile="${requestScope['javax.servlet.include.servlet_path']}"
>

  <div class="section searchSubjects"><grouper:subtitle key="find.heading.search">
    
    <a href="#" onclick="return grouperHideShow(event, 'advancedSubjectSearch');" 
      class="underline subtitleLink"><grouper:message key="find.search.subjects.specifySource"  ignoreTooltipStyle="true" /></a>
        
    <c:if test="${mediaMap['allow.self-subject-summary'] == 'true'}">
      <html:link styleClass="underline subtitleLink" page="/populateSubjectSummary.do"
        name="AuthSubject"
      >
        <grouper:message key="subject.view.yourself" />
      </html:link>
    </c:if>
    
  </grouper:subtitle>
  <div class="sectionBody"><!--<p><a class="underline" href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=true"><grouper:message key="find.action.select.groups-advanced-search"/></a></p>
--> <html:form styleId="SearchFormBean" action="/doSearchSubjects" method="post">
    <html:hidden property="searchInNameOrExtension" />
    <html:hidden property="searchInDisplayNameOrExtension" />
    <input type="hidden" name="callerPageId" value="${thisPageId}"/>
    <fieldset>

    <table class="formTable">
      <tr class="formTableRow">
        <td class="formTableLeft"><label for="searchTerm" class="noCSSOnly"><grouper:message
          key="find.search-term"
        /></label> <input name="searchTerm" type="text" id="searchTerm" /></td>
        <td class="formTableRight"><html:submit property="submit.group.member" styleClass="blueButton"
          value="${navMap['find.action.search']}"
        /></td>
      </tr>

      <tiles:insert definition="subjectSearchFragmentDef" />

    </table>

    <input type="hidden" name="newSearch" value="Y" /></fieldset>
  </html:form></div>
  </div>
  <a name="endSearch" id="endSearch"></a>
</grouper:recordTile>