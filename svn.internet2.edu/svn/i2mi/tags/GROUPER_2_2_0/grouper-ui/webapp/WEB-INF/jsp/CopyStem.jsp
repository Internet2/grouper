<%-- @annotation@ 
			Form for copying stems to another folder.
--%>
<%--
  @author shilen
  @version $Id: CopyStem.jsp,v 1.2 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<div class="section">
  <div class="sectionBody">
    <tiles:insert definition="showStemsLocationDef" /> 
    <html:form styleId="StemFormBean" action="/copyStem" method="post">
      <fieldset>
        <html:hidden property="stemId" />

        <table class="formTable formTableSpaced" cellspacing="2">
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message key="stems.copy.destinationStem" />
            </td>
            <td class="formTableRight">
              <table>
                <c:forEach var="stem" items="${savedStems}">
                  <tr>
                    <td><input name="stemSelection" type="radio" value="<c:out value="${stem.name}"/>"/></td>
                    <td>
                      <tiles:insert definition="dynamicTileDef" flush="false">
                        <tiles:put name="viewObject" beanName="stem"/>
                        <tiles:put name="view" value="savedStem"/>
                      </tiles:insert>
                    </td>
                  </tr>
                </c:forEach>
                <tr>
                  <c:choose>
                    <c:when test="${savedStemsSize==0}">
                      <td><input name="stemSelection" type="hidden" value="other"/></td>
                    </c:when>
                    <c:otherwise>
                      <td><input name="stemSelection" type="radio" value="other" checked="checked"/></td>
                    </c:otherwise>
                  </c:choose>
                  <td><input name="otherStemSelection" type="text" size="50" maxlength="50" /></td>
                </tr>
              </table>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message key="stems.copy.copyAttributes" />
            </td>
            <td class="formTableRight">
              <input checked="checked" type="checkbox" name="selections" value="copyAttributes"/>
            </td>
          </tr> 
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message key="stems.copy.copyListMembersOfGroup" />
            </td>
            <td class="formTableRight">
              <input checked="checked" type="checkbox" name="selections" value="copyListMembersOfGroup"/>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message key="stems.copy.copyPrivilegesOfGroup" />
            </td>
            <td class="formTableRight">
              <input checked="checked" type="checkbox" name="selections" value="copyPrivilegesOfGroup"/>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message key="stems.copy.copyListGroupAsMember" />
            </td>
            <td class="formTableRight">
              <input checked="checked" type="checkbox" name="selections" value="copyListGroupAsMember"/>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message key="stems.copy.copyGroupAsPrivilege" />
            </td>
            <td class="formTableRight">
              <input checked="checked" type="checkbox" name="selections" value="copyGroupAsPrivilege"/>
            </td>
          </tr>
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message key="stems.copy.copyPrivilegesOfStem" />
            </td>
            <td class="formTableRight">
              <input checked="checked" type="checkbox" name="selections" value="copyPrivilegesOfStem"/>
            </td>
          </tr>
        </table>

        <html:submit styleClass="blueButton" property="submit.save" value="${navMap['stems.action.copy']}" /> 
      </fieldset>
    </html:form>

    <div class="linkButton">
      <html:link page="/populate${linkBrowseMode}Groups.do">
        <grouper:message key="stems.movesandcopies.cancel"/>
      </html:link>
    </div>

  </div>
</div>

