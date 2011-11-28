<%-- @annotation@ 
			Form for creating new groups or editing existing ones.
--%>
<%--
  @author Gary Brown.
  @version $Id: EditGroup.jsp,v 1.15 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<div class="editForm section">
<div class="sectionBody"><tiles:insert definition="showStemsLocationDef" /> <html:form
  styleId="GroupFormBean" action="/saveGroup" method="post"
>
  <fieldset><html:hidden property="groupId" />
  <table class="formTable formTableSpaced" cellspacing="2">
    <tr class="formTableRow">
      <td class="formTableLeft"><label for="groupDisplayName">
      <grouper:message key="field.displayName.displayExtension" /></label></td>
      <td class="formTableRight"><html:text property="groupDisplayName" size="50"
        maxlength="50" styleId="groupDisplayName"
      /></td>
    </tr>
    <tr class="formTableRow">
      <td class="formTableLeft"><label for="groupName">
      <grouper:message key="field.displayName.extension" />
      </label></td>
      <td class="formTableRight"><html:text property="groupName" size="50"
        maxlength="50" styleId="groupName"
      /></td>
    </tr>

    <c:if test="${!empty GroupFormBean.map.groupId}">
      <tr class="formTableRow">
        <td class="formTableLeft"><label for="groupAlternateName">
        <grouper:message key="field.displayName.alternateName" />
        </label></td>
        <td class="formTableRight"><html:text property="groupAlternateName" size="50"
          maxlength="1024" styleId="groupAlternateName"
        /></td>
      </tr>
    </c:if>

    <tr class="formTableRow">
      <td class="formTableLeft"><label for="groupDescription">
      <grouper:message key="field.displayName.description" /></label></td>
      <td class="formTableRight"><html:text property="groupDescription" size="50"
        maxlength="100" styleId="groupDescription"
      /></td>
    </tr>

    <tr class="formTableRow">
      <td class="formTableLeft"><grouper:message
        key="groups.create.privs-for-all"
      /></td>
      <td class="formTableRight"><c:forEach var="priv" items="${privileges}">
        <span class="checkbox"> <input
          <c:if test="${!empty preSelected[priv]}">checked="checked"</c:if>
          type="checkbox" name="privileges" value="<c:out value="${priv}"/>"
          id="priv<c:out value="${priv}"/>"
        /> <label for="priv<c:out value="${priv}"/>"><grouper:message
          key="priv.${priv}"
        /></label></span>
      </c:forEach></td>
    </tr>


    <tr class="formTableRow">
      <td class="formTableLeft"><grouper:message
        key="groups.edit.type"
      /></td>
      <td class="formTableRight"><tiles:insert definition="multiOptionDef">
        <tiles:put name="items" beanName="allGroupTypes" />
        <tiles:put name="selected" beanName="selectedGroupTypes" />
        <tiles:put name="name" value="groupTypes" />
        <tiles:put name="property" value="name" />
        <tiles:put name="columns" value="3" />
      </tiles:insert></td>
    </tr>

  </table>

  <!--<tr>
    <td valign="top"><grouper:message key="groups.edit.type"/></td>
    <td valign="top"><html:select property="groupType" disabled="${editMode}">
    <htmlx:options name="groupTypes" />
   </html:select>
    </td>
</tr>--> <input type="hidden" name="groupType" value="base" /> <html:submit
    styleClass="blueButton" property="submit.save" value="${navMap['groups.action.save']}"
  /> <c:if test="${empty GroupFormBean.map.groupId}">
    <html:submit styleClass="blueButton" property="submit.saveAndAssign"
      value="${navMap['groups.action.save-assign']}"
    />
    <html:submit styleClass="blueButton" property="submit.saveAndAddComposite"
      value="${navMap['groups.action.save-add-composite']}"
    />
  </c:if></fieldset>
</html:form>
<div class="linkButton"><c:if test="${! editMode}">
  <html:link page="/populate${linkBrowseMode}Groups.do">
    <grouper:message key="groups.create.cancel" />
  </html:link>
</c:if> <c:if test="${editMode}">
  <c:set var="groupAttr" value="${GroupFormBean.map}" />
  <html:link page="/populateGroupSummary.do" paramId="groupId" paramName="groupAttr"
    paramProperty="groupId"
  >
    <grouper:message key="groups.edit.cancel" />
  </html:link>
</c:if></div>
</div>
</div>

