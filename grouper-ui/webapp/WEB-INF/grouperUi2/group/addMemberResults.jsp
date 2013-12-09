<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                          <table class="table table-hover table-bordered table-striped table-condensed">
                            <thead>
                              <tr>
                                <th class="sorted">Entity Name</th>
                              </tr>
                            </thead>
                            <tbody>

                              <c:forEach items="${grouperRequestContainer.groupContainer.guiSubjectsAddMember}" 
                                var="guiSubject" >

                                <tr>
                                 <td><a href="#" data-dismiss="modal">${guiSubject.screenLabelShort2noLinkWithIcon }</a></td>
                                </tr>


                              </c:forEach>
<%--                               <tr>
                                <td><a href="#" data-dismiss="modal"><i class="icon-user"></i> Smith, Jane</a></td>
                              </tr>
                              --%>
                            </tbody>
                          </table>
