<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <c:set var="guiInstance" value="${grouperRequestContainer.adminContainer.guiInstrumentationDataInstances[0]}" />
            
            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['adminInstrumentationHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.instrumentation');">${textContainer.text['adminInstrumentationBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${guiInstance.instrumentationDataInstance.uuid}</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['adminInstrumentationTitle'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <table class="table table-condensed table-striped">
              	  <tbody>
              	    <tr>
                      <td><strong>${textContainer.text['adminInstrumentationInstanceUuidColumn'] }</strong></td>
                      <td>${guiInstance.instrumentationDataInstance.uuid}</td>
                     </tr>
              	    <tr>
                      <td><strong>${textContainer.text['adminInstrumentationInstanceEngineNameColumn'] }</strong></td>
                      <td>${grouper:escapeHtml(guiInstance.instrumentationDataInstance.engineName)}</td>
                     </tr>
              	    <tr>
                      <td><strong>${textContainer.text['adminInstrumentationInstanceServerLabelColumn'] }</strong></td>
                      <td>${grouper:escapeHtml(guiInstance.instrumentationDataInstance.serverLabel)}</td>
                     </tr>
              	    <tr>
                      <td><strong>${textContainer.text['adminInstrumentationInstanceLastUpdateColumn'] }</strong></td>
                      <td>${guiInstance.instrumentationDataInstance.lastUpdate}</td>
                     </tr>
              	  </tbody>
              	</table>

                <form class="form-inline form-small form-filter" id="instrumentationFilterFormId" action="#" onsubmit="return guiV2link('operation=UiV2Admin.instrumentation&instanceId=${guiInstance.instrumentationDataInstance.uuid}', {optionalFormElementNamesToSend: 'filterDate'});">
                  <label for="date-filter">${textContainer.text['adminInstrumentationFilterByDate'] }</label>&nbsp;
                  <select id="date-filter" class="span2" name="filterDate">
                    <option value="">&nbsp;</option>
                    <c:forEach items="${grouperRequestContainer.adminContainer.guiInstrumentationDaysWithData}" var="dayString">
                      <option ${grouperRequestContainer.adminContainer.guiInstrumentationFilterDate == dayString ? 'selected="selected"' : '' } value="${dayString}">${dayString}</option>
                    </c:forEach>
                  </select>
                  <button type="submit" class="btn" id="instrumentationFilterSubmitButtonId" onclick="return guiV2link('operation=UiV2Admin.instrumentation&instanceId=${guiInstance.instrumentationDataInstance.uuid}', {optionalFormElementNamesToSend: 'filterDate'});">${textContainer.text['adminInstrumentationFilterButton']}</button>
                </form>	
              	
                <c:forEach items="${grouperRequestContainer.adminContainer.guiInstrumentationGraphResults}" var="type" >
                  <br />
                  <c:set var="typeTextKey" value="adminInstrumentationDataType_${type.key}" />
                  <div id="chart_${type.key}"></div>
                  <br /><br />
                  <script type="text/javascript">
                  var dates = ["x"];
                  var counts = ["${textContainer.text[typeTextKey]}"];
                  <c:forEach items="${type.value}" var="item" >
              	    dates.push("${item.key}");
              	    counts.push(${item.value});
                  </c:forEach>
                  
                  
                    var chart = c3.generate({
                      bindto: '#chart_${type.key}',
                        data: {
                          x: 'x',
                          xFormat : '%Y-%m-%d %H:%M:%S',
                          columns: [
                            dates,
                            counts
                          ]
                        },
                        axis: {
                          x: {
                            type: 'timeseries',
                            tick: {
                              format: '%Y-%m-%d %H:%M UTC',
                              rotate: 45,
                              multiline: false
                            }
                          }
                        }
                  });
                </script>
                </c:forEach> 
              </div>
            </div>
