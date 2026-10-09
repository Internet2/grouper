<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('aiAgentBreadcrumb')}

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>

    <li class="active">${textContainer.text['aiAgentBreadcrumb'] }</li>
  </ul>

  <div class="page-header blue-gradient">
    <div class="row-fluid">
      <div class="span10 pull-left">
        <h1>${textContainer.text['aiAgentTitle'] }</h1>
        <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['aiAgentDescription'] }</p>
      </div>
    </div>
  </div>
</div>

<div class="row-fluid">
  <div class="span12">
    <div id="aiAgentScopeDivId">
      <%@ include file="aiAgentScope.jsp"%>
    </div>
  </div>
</div>

<div class="row-fluid">
  <div class="span12">
    <%-- redrawn whole when a turn starts and ends; the working indicator inside it is redrawn on
         its own while the turn runs --%>
    <div id="aiAgentConversationDivId">
      <%@ include file="aiAgentConversation.jsp"%>
    </div>
    <%-- what screen reader users hear when a turn starts and ends.  outside the conversation, which
         is redrawn whole, so the conversation itself is not read out again each time --%>
    <div id="aiAgentAnnounceId" class="visually-hidden" role="status" aria-live="polite"></div>
  </div>
</div>

<div class="row-fluid">
  <div class="span12">
    <form id="aiAgentMessageFormId" onsubmit="ajax('../app/UiV2AiAgent.aiAgentSend', {formIds: 'aiAgentMessageFormId'}); return false;">
      <label for="aiAgentMessageId"><strong>${textContainer.text['aiAgentMessageLabel'] }</strong></label>
      <textarea id="aiAgentMessageId" name="aiAgentMessage" rows="3" style="width: 95%;"></textarea>
      <br />
      <input type="submit" class="btn btn-primary" aria-controls="aiAgentConversationDivId"
        value="${textContainer.text['aiAgentSend'] }" />
      &nbsp;
      <button type="button" class="btn btn-cancel" aria-controls="aiAgentConversationDivId"
        onclick="if (confirm('${textContainer.textEscapeSingleDouble['aiAgentNewConversationConfirm']}')) { ajax('../app/UiV2AiAgent.aiAgentNewConversation'); } return false;"
        >${textContainer.text['aiAgentNewConversation'] }</button>
    </form>
  </div>
</div>
