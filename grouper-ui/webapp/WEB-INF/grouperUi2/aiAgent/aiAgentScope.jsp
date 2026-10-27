<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- only the categories the user's groups allow are offered.  the saved choice can only narrow
     what the groups allow, never add to it.  there is no Save button: each box saves when it
     changes.  ajax blocks the page until the answer comes back, so saves do not overlap --%>
<form id="aiAgentScopeFormId" onsubmit="aiAgentScopeSave(); return false;">
  <fieldset>
    <legend style="font-size: 1.1em; margin-bottom: 0.25em;">${textContainer.text['aiAgentScopeHeader'] }</legend>
    <c:choose>
      <c:when test="${empty grouperRequestContainer.aiAgentContainer.scopeOptions}">
        <p>${textContainer.text['aiAgentScopeNone'] }</p>
      </c:when>
      <c:otherwise>
        <%-- like the OAuth consent screen, only worth offering with more than one box.  it has no
             name, so it is never sent: the boxes it sets are what is saved --%>
        <c:if test="${fn:length(grouperRequestContainer.aiAgentContainer.scopeOptions) > 1}">
          <label class="checkbox" style="margin-bottom: 0.5em; padding-bottom: 0.25em; border-bottom: 1px solid #ddd;">
            <input type="checkbox" id="aiAgentScopeSelectAll" onchange="aiAgentScopeSetAll(this.checked);" />
            <strong>${textContainer.text['aiAgentScopeSelectAll'] }</strong>
          </label>
        </c:if>
        <c:forEach items="${grouperRequestContainer.aiAgentContainer.scopeOptions}" var="scopeOption">
          <c:set var="aiAgentScopeTextKey" value="aiAgentScope_${scopeOption.category}" />
          <label class="checkbox">
            <input type="checkbox" name="aiAgentScope" id="aiAgentScope_${grouper:escapeHtml(scopeOption.category)}"
              value="${grouper:escapeHtml(scopeOption.category)}" onchange="aiAgentScopeSyncImplied(); aiAgentScopeUpdateSelectAll(); aiAgentScopeSave();"
              <c:if test="${scopeOption.selected}">checked="checked"</c:if> />
            ${textContainer.text[aiAgentScopeTextKey] }
          </label>
        </c:forEach>
        <script type="text/javascript">
          // the status is cleared first, so a save which fails does not leave an earlier one's
          // "Saved" showing
          function aiAgentScopeSave() {
            $('#aiAgentScopeStatusId').empty();
            ajax('../app/UiV2AiAgent.aiAgentScopeSubmit', {formIds: 'aiAgentScopeFormId'});
          }

          // read and write includes read-only, and admin read and write includes admin read-only, as
          // on the OAuth consent screen: while the larger one is checked, the one it includes is
          // checked and disabled.  a disabled box is not sent, so the server adds it when saving
          function aiAgentScopeSyncImplied() {
            var pairs = [['aiAgentScope_readwrite', 'aiAgentScope_readonly'],
              ['aiAgentScope_admin_readwrite', 'aiAgentScope_admin_readonly']];
            for (var i = 0; i < pairs.length; i++) {
              var writeBox = document.getElementById(pairs[i][0]);
              var readBox = document.getElementById(pairs[i][1]);
              if (writeBox && readBox) {
                if (writeBox.checked) {
                  readBox.checked = true;
                  readBox.disabled = true;
                } else {
                  readBox.disabled = false;
                }
              }
            }
          }
          // check or clear every box, then save them all in one call.  the included boxes are
          // enabled first so clearing reaches them, then the rule above disables them again.  not
          // named after the box's id: an inline handler looks names up on the form first, and the
          // form has the box under its id, so the call would reach the box instead
          function aiAgentScopeSetAll(checked) {
            var boxes = document.querySelectorAll('input[name=aiAgentScope]');
            for (var i = 0; i < boxes.length; i++) {
              boxes[i].disabled = false;
              boxes[i].checked = checked;
            }
            aiAgentScopeSyncImplied();
            aiAgentScopeSave();
          }

          // checked only while every box is.  a box checked and disabled because a larger one
          // includes it counts as checked
          function aiAgentScopeUpdateSelectAll() {
            var selectAll = document.getElementById('aiAgentScopeSelectAll');
            if (!selectAll) {
              return;
            }
            var boxes = document.querySelectorAll('input[name=aiAgentScope]');
            var allChecked = boxes.length > 0;
            for (var i = 0; i < boxes.length; i++) {
              if (!boxes[i].checked) {
                allChecked = false;
              }
            }
            selectAll.checked = allChecked;
          }

          aiAgentScopeSyncImplied();
          aiAgentScopeUpdateSelectAll();
        </script>
      </c:otherwise>
    </c:choose>
  </fieldset>
</form>
