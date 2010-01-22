<%-- @annotation@
		Tile which displays a summary of group attributes  - currently inserted
		in GroupSummaryDef
--%>
<%--
  @author Gary Brown.
  @version $Id: groupInfo.jsp,v 1.9 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic"
  tile="${requestScope['javax.servlet.include.servlet_path']}"
>
  <table class="formTable formTableSpaced" cellspacing="2">
    <tr class="formTableRow">
      <td class="formTableLeft"><grouper:message key="field.displayName.displayExtension" /> </td>
      <td class="formTableRight"><c:out value="${group.displayExtension}" /></td>
    </tr>
    <tr class="formTableRow">
      <td class="formTableLeft"><grouper:message key="field.displayName.displayName" /> 
      </td>
      <td class="formTableRight"><c:out value="${group.displayName}" /></td>
    </tr>
    <%-- c:if test="${!empty group.description}" --%>
      <tr class="formTableRow">
        <td class="formTableLeft"><grouper:message key="field.displayName.description" /> </td>
        <td class="formTableRight"><c:out value="${group.description}" /></td>
      </tr>
    <%-- /c:if --%>
    <tr class="formTableRow">
      <td class="formTableLeft"><grouper:message key="field.displayName.extension" /> 
      </td>

      <td class="formTableRight"><c:out value="${group.extension}" /></td>
    </tr>
    <tr class="formTableRow">
      <td class="formTableLeft"><grouper:message key="field.displayName.name" /> </td>
      <td class="formTableRight"><c:out value="${group.name}" /></td>
    </tr>
    <tr class="formTableRow">
      <td class="formTableLeft"><grouper:message key="field.displayName.alternateName" /> </td>
      <td class="formTableRight"><c:out value="${group.alternateName}" /></td>
    </tr>
    <tr class="formTableRow">
      <td class="formTableLeft"><grouper:message 
        key="groups.summary.id"
      /></td>
      <td class="formTableRight"><c:out value="${group.id}" /></td>
    </tr>
    <c:if test="${!empty group.types}">
      <tr class="formTableRow">
        <td class="formTableLeft"><grouper:message
          key="groups.summary.types"
        /></td>
        <td class="formTableRight">
        <table class="groupTypes formTable"><tiles:insert definition="dynamicTileDef">
          <tiles:put name="viewObject" beanName="group" beanProperty="types" />
          <tiles:put name="view" value="groupSummaryGroupTypes" />
          <tiles:put name="itemView" value="groupSummary" />
          <tiles:put name="listless" value="TRUE" />
        </tiles:insert></table>
        </td>
      </tr>
    </c:if>
  </table>
</grouper:recordTile>
