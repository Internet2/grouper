                      <table class="table table-condensed table-striped">
                        <tbody>
                        
                        <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningTargetNameLabel'] }</strong></td>
                            <td>
                              ${grouperRequestContainer.provisioningContainer.targetName}
                            </td>
                          </tr>
                         
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningGroupsCountLabel'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.groupsCount}
                              <br />
                              <span class="description">${textContainer.text['provisioningGroupsInStemCountHint']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningUsersCountLabel'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.usersCount}
                              <br />
                              <span class="description">${textContainer.text['provisioningUsersInStemCountHint']}</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['provisioningMembershipsCountLabel'] }</strong></td>
                            <td>
                            ${grouperRequestContainer.provisioningContainer.membershipsCount}
                              <br />
                              <span class="description">${textContainer.text['provisioningMembershipsCountHint']}</span>
                            </td>
                          </tr>
                          
                        </tbody>
                      </table>
