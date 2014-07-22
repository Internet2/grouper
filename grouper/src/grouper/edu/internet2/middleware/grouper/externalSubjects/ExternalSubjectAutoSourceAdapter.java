/**
 * Copyright 2012 Internet2
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
 */
/**
 * 
 */
package edu.internet2.middleware.grouper.externalSubjects;

import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig.ExternalSubjectAttributeConfigBean;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig.ExternalSubjectConfigBean;
import edu.internet2.middleware.grouper.subj.GrouperJdbcConnectionProvider;
import edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter2;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * if we are auto creating the source, then this is the class based on the view
 * @author mchyzer
 */
public class ExternalSubjectAutoSourceAdapter extends GrouperJdbcSourceAdapter2 {

  /** instance */
  private static ExternalSubjectAutoSourceAdapter instance = null;

  /**
   * instance
   * @return instance
   */
  public static ExternalSubjectAutoSourceAdapter instance() {
    if (instance == null) {
      synchronized (ExternalSubjectAutoSourceAdapter.class) {
        if (instance == null) {
          ExternalSubjectAutoSourceAdapter newInstance = new ExternalSubjectAutoSourceAdapter();
          newInstance.setId(ExternalSubject.sourceId());
          newInstance.setName(ExternalSubject.sourceId());
          newInstance.addSubjectType("person");
          newInstance.addInitParam("jdbcConnectionProvider", GrouperJdbcConnectionProvider.class.getName());
          newInstance.addInitParam("dbTableOrView", "grouper_ext_subj_v");

          newInstance.addInitParam("subjectIdCol", "uuid");
          newInstance.addInitParam("nameCol", "name");
          newInstance.addInitParam("descriptionCol", "description");
          newInstance.addInitParam("lowerSearchCol", "search_string_lower");

          newInstance.addInitParam("maxPageSize", "100");
          
          
          newInstance.addInitParam("subjectIdentifierCol0", "identifier");
          ExternalSubjectConfigBean externalSubjectConfigBean = ExternalSubjectConfig.externalSubjectConfigBean();
          
          newInstance.addInitParam("subjectAttributeCol0", "identifier");
          newInstance.addInitParam("subjectAttributeName0", "identifier");
          
          newInstance.addInitParam("subjectAttributeCol1", "institution");
          newInstance.addInitParam("subjectAttributeName1", "institution");
          
          newInstance.addInitParam("subjectAttributeCol2", "email");
          newInstance.addInitParam("subjectAttributeName2", "email");
          
          int index = 3;
          for (ExternalSubjectAttributeConfigBean externalSubjectAttributeConfigBean : 
              externalSubjectConfigBean.getExternalSubjectAttributeConfigBeans()) {
            newInstance.addInitParam("subjectAttributeCol" + index, externalSubjectAttributeConfigBean.getSystemName());
            newInstance.addInitParam("subjectAttributeName" + index, externalSubjectAttributeConfigBean.getSystemName());

            index++;
          }
          {
            boolean foundSortAttribute = false;
            for (int i = 0; i < externalSubjectConfigBean.getSortAttributeEl().size(); i++) {
              if (!GrouperUtil.isEmpty(externalSubjectConfigBean.getSortAttributeEl().get(i))) {
                newInstance.addInitParam("subjectVirtualAttribute_" + i + "_sortAttribute" + i, externalSubjectConfigBean.getSortAttributeEl().get(i));
                newInstance.addInternalAttribute("sortAttribute" + i);
                newInstance.addInitParam("sortAttribute" + i, "sortAttribute" + i);
                foundSortAttribute = true;
              }
            }
            
            if (!foundSortAttribute) {
              newInstance.addInitParam("sortAttribute0", "description");
            }
          }
          
          {
            boolean foundSearchAttribute = false;
            for (int i = 0; i < externalSubjectConfigBean.getSearchAttributeEl().size(); i++) {
              if (!GrouperUtil.isEmpty(externalSubjectConfigBean.getSearchAttributeEl().get(i))) {
                newInstance.addInitParam("subjectVirtualAttribute_" + i + "_searchAttribute" + i, externalSubjectConfigBean.getSearchAttributeEl().get(i));
                newInstance.addInternalAttribute("searchAttribute" + i);
                newInstance.addInitParam("searchAttribute" + i, "searchAttribute" + i);
                foundSearchAttribute = true;
              }
            }
            if (!foundSearchAttribute) {
              newInstance.addInitParam("searchAttribute0", "search_string_lower");
            }
          }          
          instance = newInstance;
          
        }
      }
    }
    return instance;
  }
}
