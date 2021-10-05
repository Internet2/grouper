<%@ include file="../assetsJsp/commonTaglib.jsp"%>

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcommon.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcombo.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcalendar.js"></script>

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxmenu.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/menu/ext/dhtmlxmenu_ext.js"></script>
    <link rel="stylesheet" type="text/css" href="../../grouperExternal/public/assets/dhtmlx/menu/skins/dhtmlxmenu_dhx_blue.css" />

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/ext/dhtmlxcombo_extra.js"></script>

    <c:choose>
      <c:when test="${grouperRequestContainer.grouperPasswordContainer.guiGrouperPassword == null}">
        ${textContainer.text['localEntityViewWsJwtKeyNoAssociatedKey']}
      </c:when>
      <c:otherwise>
      
       <c:if test="${grouperRequestContainer.grouperPasswordContainer.privateKey != null}">
        <table class="table table-condensed table-striped"> 
        <tbody>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['localEntityWsJwtPrivateKeyLabel']}</label></strong></td>
          <td>
          <textarea rows="6" cols="60" style="width: 100%">${grouperRequestContainer.grouperPasswordContainer.privateKey}</textarea>
            <br />
           <span class="description">${textContainer.text['localEntityWsJwtPrivateKeyHint']}</span>
          </td>
        </tr>
        </tbody>
        </table>
      </c:if>
      
       <c:set var="guiGrouperPassword" value="${grouperRequestContainer.grouperPasswordContainer.guiGrouperPassword}" />
       <table class="table table-condensed table-striped">
        <tbody>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['localEntityWsJwtCreatedTimestampLabel']}</label></strong></td>
          <td>${guiGrouperPassword.createdString}</td>
        </tr>
        
       <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['localEntityWsJwtExpiresOnDateLabel']}</label></strong></td>
          <td>${guiGrouperPassword.expiresAtString}</td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['localEntityWsJwtLastEditedTimestampLabel']}</label></strong></td>
          <td>${guiGrouperPassword.lastEditedString}</td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['localEntityWsJwtLastAuthenticatedTimestampLabel']}</label></strong></td>
          <td>${guiGrouperPassword.lastAuthenticatedString}</td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['localEntityWsJwtMemberWhoSetPasswordLabel']}</label></strong></td>
          <td>${guiGrouperPassword.memberWhoSetPasswordShortLink}</td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['localEntityWsJwtAllowedFromCidrsLabel']}</label></strong></td>
          <td>${guiGrouperPassword.grouperPassword.allowedFromCidrs}</td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['localEntityWsJwtRecentSourceAddressesLabel']}</label></strong></td>
          <td>${guiGrouperPassword.recentSourceAddresses}</td>
        </tr>
        
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['localEntityWsJwtFailedSourceAddressesLabel']}</label></strong></td>
          <td>${guiGrouperPassword.failedSourceAddresses}</td>
        </tr>
                
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['localEntityWsJwtSampleAuthorizationHeaderLabel']}</label></strong></td>
           <td>${guiGrouperPassword.sampleAuthorizationHeader}</td>
        </tr>
        
      </tbody>
    </table>
       
         
      </c:otherwise>
    </c:choose>
