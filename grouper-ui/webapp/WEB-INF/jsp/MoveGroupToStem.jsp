<%-- @annotation@ 
			Form for moving groups.
--%>
<%--
  @author shilen
  @version $Id: MoveGroupToStem.jsp,v 1.2 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<div class="section">
  <div class="sectionBody">
    <tiles:insert definition="showStemsLocationDef" /> 
    <html:form styleId="GroupFormBean" action="/moveGroupToStem" method="post">
      <fieldset>
        <html:hidden property="stemId" />

        <table class="formTable formTableSpaced" cellspacing="2">
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message key="groups.move.groupToMove" />
            </td>
            <td class="formTableRight">
              <table>
                <c:forEach var="group" items="${savedSubjects}">
                  <tr>
                    <td><input name="groupSelection" type="radio" value="<c:out value="${group.name}"/>"/></td>
                    <td>
                      <tiles:insert definition="dynamicTileDef" flush="false">
                        <tiles:put name="viewObject" beanName="group"/>
                        <tiles:put name="view" value="savedGroup"/>
                      </tiles:insert>
                    </td>
                  </tr>
                </c:forEach>
                <tr>
                  <c:choose>
                    <c:when test="${savedSubjectsSize==0}">
                      <td><input name="groupSelection" type="hidden" value="other"/></td>
                    </c:when>
                    <c:otherwise>
                      <td><input name="groupSelection" type="radio" value="other" checked="checked"/></td>
                    </c:otherwise>
                  </c:choose>
                  <td><input name="otherGroupSelection" type="text" size="50" maxlength="50" /></td>
                </tr>
              </table>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message key="groups.move.assignAlternateName" />
            </td>
            <td class="formTableRight">
              <input checked="checked" type="checkbox" name="selections" value="assignAlternateName"/>
            </td>
          </tr> 
        </table>

        <html:submit styleClass="blueButton" property="submit.save" value="${navMap['stems.action.move-group-to-stem']}" /> 
      </fieldset>
    </html:form>

    <div class="linkButton">
      <html:link page="/populate${linkBrowseMode}Groups.do">
        <grouper:message key="stems.movesandcopies.cancel"/>
      </html:link>
    </div>

  </div>
</div>

