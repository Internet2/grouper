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
package edu.internet2.middleware.subject.provider;

import edu.internet2.middleware.subject.provider.SourceManager.SourceManagerStatusBean;
import junit.framework.TestCase;
import junit.textui.TestRunner;


public class SubjectStatusProcessorTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new SubjectStatusProcessorTest("testProcessSearch"));
  }
  
  /**
   * @param name
   */
  public SubjectStatusProcessorTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public SubjectStatusProcessorTest() {
    
  }
  
  private static SubjectStatusConfig subjectStatusConfigLessGreaterInactive = new SubjectStatusConfig();
  
  static {
    subjectStatusConfigLessGreaterInactive.setSourceId("sourceIdLessGreaterInactive");
    subjectStatusConfigLessGreaterInactive.setStatusAllFromUser("All");
    subjectStatusConfigLessGreaterInactive.setStatusDatastoreFieldName("statusField");
    subjectStatusConfigLessGreaterInactive.setStatusLabel("status");
    subjectStatusConfigLessGreaterInactive.setStatusSearchDefault("status<>inactive");
    subjectStatusConfigLessGreaterInactive.getStatusesFromUser().add("active");
    subjectStatusConfigLessGreaterInactive.getStatusesFromUser().add("inactive");
    subjectStatusConfigLessGreaterInactive.getStatusesFromUser().add("temporary");

    subjectStatusConfigLessGreaterInactive.getStatusTranslateUserToDatastore().put("active", "activeDb");

  }
  
  private static SubjectStatusConfig subjectStatusConfigNoDefault = new SubjectStatusConfig();
  
  static {
    subjectStatusConfigNoDefault.setSourceId("sourceIdNoDefault");
    subjectStatusConfigNoDefault.setStatusAllFromUser("All");
    subjectStatusConfigNoDefault.setStatusDatastoreFieldName("statusField");
    subjectStatusConfigNoDefault.setStatusLabel("status");
    subjectStatusConfigNoDefault.getStatusesFromUser().add("active");
    subjectStatusConfigNoDefault.getStatusesFromUser().add("inactive");
    subjectStatusConfigNoDefault.getStatusesFromUser().add("temporary");

    subjectStatusConfigNoDefault.getStatusTranslateUserToDatastore().put("active", "activeDb");

  }

  private static SubjectStatusConfig subjectStatusConfigNone = new SubjectStatusConfig();
  
  static {
    subjectStatusConfigNone.setSourceId("sourceIdConfigNone");
  }
  
  private static SubjectStatusConfig subjectStatusConfigPoundNotEqualInactive = new SubjectStatusConfig();
  
  static {
    subjectStatusConfigPoundNotEqualInactive.setSourceId("sourceIdPoundNotEqualInactive");
    subjectStatusConfigPoundNotEqualInactive.setStatusAllFromUser("All");
    subjectStatusConfigPoundNotEqualInactive.setStatusDatastoreFieldName("statusField");
    subjectStatusConfigPoundNotEqualInactive.setStatusLabel("status");
    subjectStatusConfigPoundNotEqualInactive.setStatusSearchDefault("status!=inactive");
    subjectStatusConfigPoundNotEqualInactive.getStatusesFromUser().add("active");
    subjectStatusConfigPoundNotEqualInactive.getStatusesFromUser().add("inactive");
    subjectStatusConfigPoundNotEqualInactive.getStatusesFromUser().add("temporary");

    subjectStatusConfigPoundNotEqualInactive.getStatusTranslateUserToDatastore().put("inactive", "inactiveDb");
  }

  private static SubjectStatusConfig subjectStatusConfigEqualActive = new SubjectStatusConfig();
  
  static {
    subjectStatusConfigEqualActive.setSourceId("sourceIdEqualActive");
    subjectStatusConfigEqualActive.setStatusAllFromUser("All");
    subjectStatusConfigEqualActive.setStatusDatastoreFieldName("statusField");
    subjectStatusConfigEqualActive.setStatusLabel("status");
    subjectStatusConfigEqualActive.setStatusSearchDefault("status=active");
    subjectStatusConfigEqualActive.getStatusesFromUser().add("active");
    subjectStatusConfigEqualActive.getStatusesFromUser().add("inactive");
    subjectStatusConfigEqualActive.getStatusesFromUser().add("temporary");

    subjectStatusConfigEqualActive.getStatusTranslateUserToDatastore().put("active", "activeDb");
    subjectStatusConfigEqualActive.getStatusTranslateUserToDatastore().put("inactive", "inactiveDb");
  }
  
  static {
    SourceManagerStatusBean sourceManagerStatusBean = SourceManager.getInstance().getSourceManagerStatusBean();
    
    sourceManagerStatusBean.getSourceIdToStatusConfigs().put(subjectStatusConfigEqualActive.getSourceId(), 
        subjectStatusConfigEqualActive);
    sourceManagerStatusBean.getSourceIdToStatusConfigs().put(subjectStatusConfigLessGreaterInactive.getSourceId(), 
        subjectStatusConfigLessGreaterInactive);
    sourceManagerStatusBean.getSourceIdToStatusConfigs().put(subjectStatusConfigNoDefault.getSourceId(), 
        subjectStatusConfigNoDefault);
    sourceManagerStatusBean.getSourceIdToStatusConfigs().put(subjectStatusConfigNone.getSourceId(), 
        subjectStatusConfigNone);
    sourceManagerStatusBean.getSourceIdToStatusConfigs().put(subjectStatusConfigPoundNotEqualInactive.getSourceId(), 
        subjectStatusConfigPoundNotEqualInactive);

    sourceManagerStatusBean.processConfigBeans();
    
  }
  
  /**
   * test
   */
  public void testProcessOriginalQuery() {
    
    SubjectStatusProcessor subjectStatusProcessor = null;
    
    subjectStatusProcessor = new  SubjectStatusProcessor("smith", subjectStatusConfigLessGreaterInactive);
    
    subjectStatusProcessor.processOriginalQuery();
    
    assertEquals("smith status<>inactive" , subjectStatusProcessor.getQueryWithDefault());
    assertEquals("smith" , subjectStatusProcessor.getOriginalQuery());
    assertEquals("inactive" , subjectStatusProcessor.getStatusValueFromUser());
    assertEquals("smith" , subjectStatusProcessor.getStrippedQuery());
    assertTrue(!subjectStatusProcessor.isEqualsFromUser());

    //#####################################
    
    subjectStatusProcessor = new  SubjectStatusProcessor("smith", subjectStatusConfigPoundNotEqualInactive);
    
    subjectStatusProcessor.processOriginalQuery();
    
    assertEquals("smith status!=inactive" , subjectStatusProcessor.getQueryWithDefault());
    assertEquals("smith" , subjectStatusProcessor.getOriginalQuery());
    assertEquals("inactive" , subjectStatusProcessor.getStatusValueFromUser());
    assertEquals("smith" , subjectStatusProcessor.getStrippedQuery());
    assertTrue(!subjectStatusProcessor.isEqualsFromUser());

    //#####################################
    
    subjectStatusProcessor = new  SubjectStatusProcessor("smith", subjectStatusConfigEqualActive);
    
    subjectStatusProcessor.processOriginalQuery();
    
    assertEquals("smith status=active" , subjectStatusProcessor.getQueryWithDefault());
    assertEquals("smith" , subjectStatusProcessor.getOriginalQuery());
    assertEquals("active" , subjectStatusProcessor.getStatusValueFromUser());
    assertEquals("smith" , subjectStatusProcessor.getStrippedQuery());
    assertTrue(subjectStatusProcessor.isEqualsFromUser());

    //#####################################
    
    subjectStatusProcessor = new  SubjectStatusProcessor("smith status=all", subjectStatusConfigEqualActive);
    
    subjectStatusProcessor.processOriginalQuery();
    
    assertEquals("smith status=all" , subjectStatusProcessor.getQueryWithDefault());
    assertEquals("smith status=all" , subjectStatusProcessor.getOriginalQuery());
    assertEquals("all" , subjectStatusProcessor.getStatusValueFromUser());
    assertEquals("smith" , subjectStatusProcessor.getStrippedQuery());
    assertTrue(subjectStatusProcessor.isEqualsFromUser());

    //#####################################
    
    subjectStatusProcessor = new  SubjectStatusProcessor("smith status=all", subjectStatusConfigLessGreaterInactive);
    
    subjectStatusProcessor.processOriginalQuery();
    
    assertEquals("smith status=all" , subjectStatusProcessor.getQueryWithDefault());
    assertEquals("smith status=all" , subjectStatusProcessor.getOriginalQuery());
    assertEquals("all" , subjectStatusProcessor.getStatusValueFromUser());
    assertEquals("smith" , subjectStatusProcessor.getStrippedQuery());
    assertTrue(subjectStatusProcessor.isEqualsFromUser());

    //#####################################
    
    subjectStatusProcessor = new  SubjectStatusProcessor("status=all smith", subjectStatusConfigPoundNotEqualInactive);
    
    subjectStatusProcessor.processOriginalQuery();
    
    assertEquals("status=all smith" , subjectStatusProcessor.getQueryWithDefault());
    assertEquals("status=all smith" , subjectStatusProcessor.getOriginalQuery());
    assertEquals("all" , subjectStatusProcessor.getStatusValueFromUser());
    assertEquals("smith" , subjectStatusProcessor.getStrippedQuery());
    assertTrue(subjectStatusProcessor.isEqualsFromUser());

    //#####################################
    
    subjectStatusProcessor = new  SubjectStatusProcessor("john status=active smith", subjectStatusConfigPoundNotEqualInactive);
    
    subjectStatusProcessor.processOriginalQuery();
    
    assertEquals("john status=active smith" , subjectStatusProcessor.getQueryWithDefault());
    assertEquals("john status=active smith" , subjectStatusProcessor.getOriginalQuery());
    assertEquals("active" , subjectStatusProcessor.getStatusValueFromUser());
    assertEquals("john smith" , subjectStatusProcessor.getStrippedQuery());
    assertTrue(subjectStatusProcessor.isEqualsFromUser());

    //#####################################
    
    subjectStatusProcessor = new  SubjectStatusProcessor("john status!=inactive smith", subjectStatusConfigEqualActive);
    
    subjectStatusProcessor.processOriginalQuery();
    
    assertEquals("john status!=inactive smith" , subjectStatusProcessor.getQueryWithDefault());
    assertEquals("john status!=inactive smith" , subjectStatusProcessor.getOriginalQuery());
    assertEquals("inactive" , subjectStatusProcessor.getStatusValueFromUser());
    assertEquals("john smith" , subjectStatusProcessor.getStrippedQuery());
    assertTrue(!subjectStatusProcessor.isEqualsFromUser());

    //#####################################
    
    subjectStatusProcessor = new  SubjectStatusProcessor("john status!=inactive smith status=whatever", subjectStatusConfigEqualActive);
    
    subjectStatusProcessor.processOriginalQuery();
    
    //equals is parsed before !=...
    assertEquals("john status!=inactive smith status=whatever" , subjectStatusProcessor.getQueryWithDefault());
    assertEquals("john status!=inactive smith status=whatever" , subjectStatusProcessor.getOriginalQuery());
    assertEquals("inactive" , subjectStatusProcessor.getStatusValueFromUser());
    assertEquals("john smith" , subjectStatusProcessor.getStrippedQuery());
    assertTrue(!subjectStatusProcessor.isEqualsFromUser());

    //#####################################
    
    subjectStatusProcessor = new  SubjectStatusProcessor("john something=whatever jason status!=inactive smith status=whatever", subjectStatusConfigEqualActive);
    
    subjectStatusProcessor.processOriginalQuery();
    
    //equals is parsed before !=...
    assertEquals("john something=whatever jason status!=inactive smith status=whatever" , subjectStatusProcessor.getQueryWithDefault());
    assertEquals("john something=whatever jason status!=inactive smith status=whatever" , subjectStatusProcessor.getOriginalQuery());
    assertEquals("inactive" , subjectStatusProcessor.getStatusValueFromUser());
    assertEquals("john something=whatever jason smith" , subjectStatusProcessor.getStrippedQuery());
    assertTrue(!subjectStatusProcessor.isEqualsFromUser());

  
  }

  /**
   * test
   */
  public void testProcessSearch() {
    
    SubjectStatusProcessor subjectStatusProcessor = null;
    
    subjectStatusProcessor = new SubjectStatusProcessor("smith", subjectStatusConfigLessGreaterInactive);
    
    SubjectStatusResult subjectStatusResult = subjectStatusProcessor.processSearch();
    
    assertTrue(!subjectStatusResult.isEquals());
    assertEquals("statusField", subjectStatusResult.getDatastoreFieldName());
    assertEquals("inactive", subjectStatusResult.getDatastoreValue());
    assertEquals("smith" , subjectStatusResult.getStrippedQuery());
  
    //#####################################
    
    subjectStatusProcessor = new SubjectStatusProcessor("smith", subjectStatusConfigNone);
    
    subjectStatusResult = subjectStatusProcessor.processSearch();
    
    assertTrue(!subjectStatusResult.isEquals());
    assertNull(subjectStatusResult.getDatastoreFieldName());
    assertNull(subjectStatusResult.getDatastoreValue());
    assertEquals("smith" , subjectStatusResult.getStrippedQuery());
  
    //#####################################
    
    subjectStatusProcessor = new SubjectStatusProcessor("smith status = active ", subjectStatusConfigNone);
    
    subjectStatusResult = subjectStatusProcessor.processSearch();
    
    assertFalse(subjectStatusResult.isEquals());
    assertNull(subjectStatusResult.getDatastoreFieldName());
    assertNull(subjectStatusResult.getDatastoreValue());
    assertEquals("smith" , subjectStatusResult.getStrippedQuery());
  
    //#####################################
    
    subjectStatusProcessor = new SubjectStatusProcessor("smith", subjectStatusConfigPoundNotEqualInactive);
    
    subjectStatusResult = subjectStatusProcessor.processSearch();
    
    assertFalse(subjectStatusResult.isEquals());
    assertEquals("statusField", subjectStatusResult.getDatastoreFieldName());
    assertEquals("inactiveDb", subjectStatusResult.getDatastoreValue());
    assertEquals("smith" , subjectStatusResult.getStrippedQuery());
  
  
  }

  /**
   * test
   */
  public void testValidate() {
    
    subjectStatusConfigEqualActive.validate();
    subjectStatusConfigLessGreaterInactive.validate();
    subjectStatusConfigNoDefault.validate();
    subjectStatusConfigNone.validate();
    subjectStatusConfigPoundNotEqualInactive.validate();
    
  }
  
}
