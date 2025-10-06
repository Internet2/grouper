<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <ul class="nav nav-tabs">
                  <%-- if grouperCurrentTab is attributeNames then this one is selected --%>
                  <c:choose>
                    <c:when test="${ grouperCurrentTab == 'attributeNames' }">
                      <li class="active"><a role="tab"  aria-selected="true" href="#" onclick="return false;" >${textContainer.text['attributeDefAttributeDefNameTab'] }</a></li>
                    </c:when>
                    <c:otherwise>
                      <li><a role="tab" href="?operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});" >${textContainer.text['attributeDefAttributeDefNameTab'] }</a></li>
                    </c:otherwise>
                  </c:choose>

                  <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb == 'perm'}">
                    <%-- if grouperCurrentTab is actions then this one is selected --%>
                    <c:choose>
                      <c:when test="${ grouperCurrentTab == 'actions' }">
                        <li class="active"><a role="tab"  aria-selected="true" href="#" onclick="return false;" >${textContainer.text['attributeDefAttributeDefActionTab'] }</a></li>
                      </c:when>
                      <c:otherwise>
                        <li><a role="tab" href="?operation=UiV2AttributeDefAction.attributeDefActions&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDefAction.attributeDefActions&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});">${textContainer.text['attributeDefAttributeDefActionTab'] }</a></li>
                      </c:otherwise>
                    </c:choose>
                    
                  </c:if>
 
                  <c:if test="${grouperRequestContainer.attributeDefContainer.canAdmin}">
                    <%-- if grouperCurrentTab is privileges then this one is selected --%>
                    <c:choose>
                      <c:when test="${ grouperCurrentTab == 'privileges' }">
                        <li class="active"><a role="tab"  aria-selected="true" href="#" onclick="return false;" >${textContainer.text['attributeDefPrivilegesTab'] }</a></li>
                      </c:when>
                      <c:otherwise>
                        <li><a role="tab" href="?operation=UiV2AttributeDef.attributeDefPrivileges&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDef.attributeDefPrivileges&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});" >${textContainer.text['attributeDefPrivilegesTab'] }</a></li>
                      </c:otherwise>
                    </c:choose>
                  </c:if>
    
                  <c:if test="${grouperRequestContainer.attributeDefContainer.canReadPrivilegeInheritance}">
                    <%@ include file="attributeDefMoreTab.jsp" %>
                  </c:if>
                </ul>
