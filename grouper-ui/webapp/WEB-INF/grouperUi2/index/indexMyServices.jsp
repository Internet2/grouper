<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexMyServices.jsp -->
                    <h4>My Services</h4>
                    <ul class="unstyled list-widget">
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiAttributeDefNamesMyServices}" var="guiAttributeDefName">
                        <%-- TODO work on this, should be from text file, and by attr def name --%>
                        <li><a href="view-group.html" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" 
                          <%-- &lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. --%>
                          title="${grouper:escapeHtml(guiAttributeDefName.title)}"><i class="icon-group"></i> ${grouper:escapeHtml(guiAttributeDefName.attributeDefName.displayExtension) }</a><br/><small class="indent">${grouper:escapeHtml(guiAttributeDefName.pathColonSpaceSeparated) }</small>
                        </li>
                      </c:forEach>
                    </ul>
                    <p><strong><a href="my-services.html">View all services</a></strong></p>
                    <!-- start indexMyServices.jsp -->
                    