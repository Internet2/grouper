<%-- @annotation@ 
			Displays summary of a subject and provides links for 
			the maintenance of the subject
--%><%--
  @author Gary Brown.
  @version $Id: SubjectSummary.jsp,v 1.7 2008-04-11 14:49:36 mchyzer Exp $
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
					<fmt:message bundle="${nav}" key="saved-subjects.add.subject"/>
				</html:link>
			</c:if>
		<tiles:insert definition="callerPageButtonDef"/>
		<c:set target="${subject}" property="changeMode" value="true"/>
		<html:link page="/populateSubjectSummary.do" name="subject">
					<fmt:message bundle="${nav}" key="subject.summary.start-again-here"/>
		</html:link>
		</div>


 </c:if>
<c:if test="${empty grouperForm.map.callerPageId || grouperForm.map.subjectId==lastSubjectSummaryForm.subjectId}">
<tiles:insert definition="subjectLinksDef"/>


	<div class="changeSubjectSummaryScope" style="clear:left;">
		<tiles:insert definition="changeSubjectSummaryScopeDef"/>
	</div>
</div>
</div>
<div class="section">
  <grouper:subtitle key="${scopeListData.titleKey}" />
  <div class="sectionBody">
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
		<div class="searchCountZero"><grouper:message bundle="${nav}" key="${scopeListData.noResultsKey}"/></div>
	</c:if>
</c:if>
  </div>
</div><!-- subjectSummary-->