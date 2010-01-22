<%-- @annotation@
		Tile which displays the simple search form for audit entries
--%><%--
  @author Gary Brown.
  @version $Id: auditSearch.jsp,v 1.3 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><grouper:message key="page.skip.search"/></a>

 <html:form styleId="SearchFormBean" action="/userAudit" method="post">
 		<input type="hidden" name="viaForm" value="true"/>
 		<html:hidden property="groupId"/>
		<html:hidden property="stemId"/>
		<html:hidden property="memberId"/>
		<html:hidden property="subjectId"/>
		<html:hidden property="subjectType"/>
		<html:hidden property="sourceId"/>
		<html:hidden property="filterType"/>
		<html:hidden property="pageSize"/>
		<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
<fieldset>

<table class="formTable formTableSpaced">
<tr class="formTableRow">
<td class="formTableLeft">
	<label for="searchTerm" class="noCSSOnly"><grouper:message
          key="find.search-term"
        /></label><grouper:message key="audit.query.filter-by-date"/> 
        
  </td>
<td class="formTableRight">
<html:select property="dateQualifier">
        	<html:option value="on"><grouper:message key="audit.query.filter-by-date.on"/></html:option>
        	<html:option value="before"><grouper:message key="audit.query.filter-by-date.before"/></html:option>
        	<html:option value="after"><grouper:message key="audit.query.filter-by-date.after"/></html:option>
        	<html:option value="between"><grouper:message key="audit.query.filter-by-date.between"/></html:option>
        	</html:select> <html:text size="15" property="date1" /> (<grouper:message key="audit.query.filter-by-date.and"/> <html:text size="15" property="date2" />)
	
    </td>
</tr>

<tr class="formTableRow">
<td class="formTableLeft">
	<label for="searchTerm" class="noCSSOnly"><grouper:message
          key="find.search-term"
        /></label><grouper:message key="audit.query.sort"/> 
        
  </td>
<td class="formTableRight">
<html:select property="sort">
        	<html:option value="desc"><grouper:message key="audit.query.desc"/></html:option>
        	<html:option value="asc"><grouper:message key="audit.query.asc"/></html:option>
 	</html:select>
    </td>
</tr>
<tr class="formTableRow">
<td class="formTableLeft">
	<grouper:message key="audit.query.extended-results"/> 
        
  </td>
<td class="formTableRight">
<html:checkbox property="extendedResults"></html:checkbox>
    </td>
</tr>
<tr class="formTableRow">
<td class="formTableLeft">

        
  </td>
<td class="formTableRight">
	<input type="submit" class="blueButton" value="<c:out value="${navMap['find.action.audit-query']}"/>"/>
    </td>
</tr>

</table>





<input type="hidden" name="newSearch" value="Y"/>
</fieldset>
</html:form>
</div>

<a name="endSearch" id="endSearch"></a>
</grouper:recordTile>
