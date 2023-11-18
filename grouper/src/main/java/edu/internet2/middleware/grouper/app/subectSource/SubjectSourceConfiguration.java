package edu.internet2.middleware.grouper.app.subectSource;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;

public abstract class SubjectSourceConfiguration extends GrouperConfigurationModuleBase {
  
  public final static Set<String> sourceConfigClassNames = new LinkedHashSet<String>();
  
  static {
    sourceConfigClassNames.add(LdapSubjectSourceConfiguration.class.getName());
    sourceConfigClassNames.add(SqlSubjectSourceConfiguration.class.getName());
  }
  
  /**
   * list of systems that can be configured
   * @return
   */
  public static List<SubjectSourceConfiguration> retrieveAllSubjectSourceConfigurationTypes() {
    return (List<SubjectSourceConfiguration>) (Object) retrieveAllConfigurationTypesHelper(sourceConfigClassNames);
  }
  
  /**
   * list of configured provisioner systems
   * @return
   */
  public static List<SubjectSourceConfiguration> retrieveAllSubjectSourceConfigurations() {
   return (List<SubjectSourceConfiguration>) (Object) retrieveAllConfigurations(sourceConfigClassNames);
  }
  
  @Override
  protected String getConfigurationTypePrefix() {
    return "subjectSourceConfiguration";
  }
  
  @Override
  protected String getGenericConfigId() {
    return "genericSource";
  }

}
