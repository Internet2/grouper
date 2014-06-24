/*******************************************************************************
 * Copyright 2014 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.poc;


public class AutoCreateObjects {

  public AutoCreateObjects() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    
    System.out.println("grouperSession = GrouperSession.startRootSession();");
    
    for (int i=0;i<39;i++) {
      System.out.println("new StemSave(grouperSession).assignName(\"test2:testStem" + i + "\").assignCreateParentStemsIfNotExist(true).save();");
    }

    for (int i=0;i<139;i++) {
      System.out.println("group = new GroupSave(grouperSession).assignName(\"test2:testGroup" + i + "\").assignCreateParentStemsIfNotExist(true).save();");
    }

    System.out.println("attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.attr).assignName(\"test2:testAttrName\").assignToStem(true).save();");

    for (int i=0;i<39;i++) {
      System.out.println("new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.attr).assignName(\"test2:testAttrName" + i + "\").assignToStem(true).save();");
    }

    for (int i=0;i<39;i++) {
      System.out.println("new AttributeDefNameSave(grouperSession, attributeDef).assignCreateParentStemsIfNotExist(true).assignName(\"test2:testAttrDefName" + i + "\").save();");
    }
  }

}
