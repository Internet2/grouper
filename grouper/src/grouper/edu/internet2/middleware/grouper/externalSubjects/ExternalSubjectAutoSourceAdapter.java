/**
 * 
 */
package edu.internet2.middleware.grouper.externalSubjects;

import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig.ExternalSubjectAttributeConfigBean;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig.ExternalSubjectConfigBean;
import edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter2;


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
          newInstance.setId("grouperExternal");
          newInstance.setName("Grouper external subjects");
          newInstance.addSubjectType("person");
          newInstance.addInitParam("jdbcConnectionProvider", "edu.internet2.middleware.grouper.subj.GrouperJdbcConnectionProvider");
          newInstance.addInitParam("dbTableOrView", "grouper_ext_subj_v");

          newInstance.addInitParam("subjectIdCol", "uuid");
          newInstance.addInitParam("nameCol", "name");
          newInstance.addInitParam("descriptionCol", "description");
          newInstance.addInitParam("lowerSearchCol", "search_string_lower");
          newInstance.addInitParam("subjectIdentifierCol0", "identifier");
          
          ExternalSubjectConfigBean externalSubjectConfigBean = ExternalSubjectConfig.externalSubjectConfigBean();
          
          newInstance.addInitParam("subjectAttributeCol0", "identifier");
          newInstance.addInitParam("subjectAttributeName0", "identifier");

          int index = 1;
          for (ExternalSubjectAttributeConfigBean externalSubjectAttributeConfigBean : 
              externalSubjectConfigBean.getExternalSubjectAttributeConfigBeans()) {
            newInstance.addInitParam("subjectAttributeCol" + index, externalSubjectAttributeConfigBean.getSystemName());
            newInstance.addInitParam("subjectAttributeName" + index, externalSubjectAttributeConfigBean.getSystemName());

            index++;
          }

          instance = newInstance;
          
        }
      }
    }
    return instance;
  }

}
