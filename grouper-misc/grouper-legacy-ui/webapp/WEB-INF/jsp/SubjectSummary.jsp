<%-- @annotation@ 
			Displays summary of a subject and provides links for 
			the maintenance of the subject
--%><%--
  @author Gary Brown.
  @version $Id: SubjectSummary.jsp,v 1.9 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<div class="subjectSummary section">
  <div class="sectionBody">
	<tiles:insert definition="dynamicTileDef">
		<tiles:put name="viewObject" beanName="subject" />
		<tiles:put name="view" value="subjectInfo"/>
	</tiles:insert>

<c:if test="${!empty grouperForm.map.callerPageId && grouperForm.map.subjectId!=lastSubjectSummaryForm.subjectId}">
<div class="linkButton">
			<c:if test="${!empty subject}">
				<html:link page="/addSavedSubject.do" name="saveParams">
					<grouper:message key="saved-subjects.add.subject"/>
				</html:link>
			</c:if>
		<tiles:insert definition="callerPageButtonDef"/>
		<c:set target="${subject}" property="changeMode" value="true"/>
		<html:link page="/populateSubjectSummary.do" name="subject">
					<grouper:message key="subject.summary.start-again-here"/>
		</html:link>
		</div>


 </c:if>
<c:if test="${empty grouperForm.map.callerPageId || grouperForm.map.subjectId==lastSubjectSummaryForm.subjectId}">
<tiles:insert definition="subjectLinksDef"/>


</div>
</div>
<div class="section">
  <grouper:subtitle key="entity.search.results" />
  <div class="sectionBody">
    <tiles:insert definition="changeSubjectSummaryScopeDef"/>
    <br />
    <div class="midSectionTitle"><grouper:message key="${scopeListData.titleKey}" /></div>
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
			<tiles:put name="view" value="${scopeListData.view}"/>
			<tiles:put name="headerView" value="${scopeListData.headerView}"/>
			<tiles:put name="itemView" value="${scopeListData.itemView}"/>
			<tiles:put name="linkSeparator">  
				<tiles:insert definition="linkSeparatorDef" flush="false">
					<tiles:put name="separatorScopeDef" value="${scopeListData.itemView}"/>
				</tiles:insert>
			</tiles:put>
			<tiles:put name="footerView" value="${scopeListData.footerView}"/>
			<tiles:put name="pager" beanName="pager"/>
		</tiles:insert>
		
	<c:if test="${pager.count==0}">
		<div class="searchCountZero"><grouper:message key="${scopeListData.noResultsKey}"/></div>
	</c:if>
</c:if>
  </div>
</div><!-- subjectSummary-->