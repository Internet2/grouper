/*******************************************************************************
 * Copyright 2016 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.corebeans;

import edu.internet2.middleware.tierApiAuthzServer.contentType.AsasRestContentType;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;


/**
 * Multiple groups
 * @author mchyzer
 *
 */
public class AsasGroupSearchContainer extends AsasResponseBeanBase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    AsasGroupSearchContainer asasGroupSearchContainer = new AsasGroupSearchContainer();
    AsasGroup asasGroup = new AsasGroup();
    asasGroup.setId("id");
    asasGroup.setName("name");
    asasGroupSearchContainer.setGroups(new AsasGroup[]{asasGroup});
    
    String string = StandardApiServerUtils.indent(AsasRestContentType.json.writeString(asasGroupSearchContainer), true);
    
    System.out.println(string);
    
    
  }

  
  /**
   * list of groups
   */
  private AsasGroup[] groups = null;

  
  /**
   * @return the groups
   */
  public AsasGroup[] getGroups() {
    return this.groups;
  }

  
  /**
   * @param groups the groups to set
   */
  public void setGroups(AsasGroup[] groups1) {
    this.groups = groups1;
  }
  
  
  
}
