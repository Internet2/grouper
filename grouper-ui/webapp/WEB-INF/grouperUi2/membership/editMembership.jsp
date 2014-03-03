<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />


            <div class="bread-header-container">
              <ul class="breadcrumb">
                <%-- <li><a href="index.html">Home </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li><a href="#">Root </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li><a href="view-folder.html">Applications </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li><a href="view-folder.html">Wiki </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li><a href="view-group.html">Editors</a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li class="active">Trace membership</li> --%>
                ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbBullets}
                <li class="active">${grouperRequestContainer.subjectContainer.guiSubject.screenLabelShort2noLink}</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1> <i class="icon-group icon-header"> </i> ${grouperRequestContainer.subjectContainer.guiSubject.screenLabelShort2noLink}
                <br /><small>${textContainer.text['membershipEditSubHeader']}</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <form class="form-horizontal">

                  <div class="control-group">
                    <label class="control-label">${textContainer.text['subjectViewLabelId'] }</label>
                    <div class="controls">
                      ${grouper:escapeHtml(grouperRequestContainer.subjectContainer.guiSubject.subject.id)}
                    </div>
                  </div>
                  <c:if test="${grouperRequestContainer.subjectContainer.guiSubject.hasEmailAttributeInSource }">
                    <div class="control-group">
                      <label class="control-label">${textContainer.text['subjectViewLabelEmail']}</label>
                      <div class="controls">
                        ${grouperRequestContainer.subjectContainer.guiSubject.email}
                      </div>
                    </div>
                  </c:if>
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['subjectViewLabelName'] }</label>
                    <div class="controls">
                      ${grouper:escapeHtml(grouperRequestContainer.subjectContainer.guiSubject.subject.name)}
                    </div>
                  </div>
                  <div class="control-group">
                    <label class="control-label">${textContainer.text['subjectViewLabelDescription'] }</label>
                    <div class="controls">
                      ${grouper:escapeHtml(grouperRequestContainer.subjectContainer.guiSubject.subject.description)}
                    </div>
                  </div>
                  <div class="control-group">
                    <label class="control-label hide">${textContainer.text['membershipEditLabelMembership'] }</label>
                    <div class="controls">
                      <label class="checkbox">
                        <c:choose>
                          <c:when test="${grouperRequestContainer.membershipGuiContainer.directMembership}">
                            <input type="checkbox" name="hasMembership" checked="checked" value="true" /> 
                              ${textContainer.text['membershipEditHasDirectMembership']}
                          </c:when>
                          <c:otherwise>
                            <input type="checkbox" name="hasMembership" value="true" />
                              ${textContainer.text['membershipEditHasDirectMembership']}
                          </c:otherwise>
                        </c:choose>
                      </label>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="member-start-date" class="control-label">Start Date:</label>
                    <div class="controls">
                      <input type="text" value="12/13/2012" id="member-start-date"><span class="help-block">The date on which this entity's membership begins.</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="member-end-date" class="control-label">End Date:</label>
                    <div class="controls">
                      <input type="text" value="11/1/2015" id="member-end-date"><span class="help-block">The date on which this entity's membership expires.</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label class="control-label">This member has the following privileges in this group:</label>
                    <div class="controls">
                      <label class="checkbox inline">
                        <input type="checkbox" id="inlineCheckbox1" value="admin">ADMIN
                      </label>
                      <label class="checkbox inline">
                        <input type="checkbox" id="inlineCheckbox2" value="update">UPDATE
                      </label>
                      <label class="checkbox inline">
                        <input type="checkbox" id="inlineCheckbox3" value="read" checked disabled>READ
                      </label>
                      <label class="checkbox inline">
                        <input type="checkbox" id="inlineCheckbox4" value="view" checked disabled>VIEW
                      </label>
                      <label class="checkbox inline">
                        <input type="checkbox" id="inlineCheckbox5" value="optin" checked>OPTIN
                      </label>
                      <label class="checkbox inline">
                        <input type="checkbox" id="inlineCheckbox6" value="optout">OPTOUT
                      </label>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary">Save</a> <a href="view-group.html" class="btn">Cancel</a></div>
                </form>
              </div>
            </div>
