package edu.internet2.middleware.grouper.app.gshTemplateProvisioner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GshTemplateProvisionerConfigIds implements OptionValueDriver {

  public GshTemplateProvisionerConfigIds() {
  }

  private static final Pattern configIdPattern = Pattern.compile("^grouperGshTemplate\\.([^.]+)\\.templateType$");
  
  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> propertyConfigIds = grouperConfig.propertyConfigIds(configIdPattern);
    List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
    for (String propertyConfigId : propertyConfigIds) {
      String templateType = grouperConfig.propertyValueString("grouperGshTemplate." + propertyConfigId + ".templateType");
      if (StringUtils.equals(GshTemplateType.provisioner.name(), templateType)) {
        keysAndLabels.add(new MultiKey(propertyConfigId, propertyConfigId));
      }
    }
    return keysAndLabels;
  }

}
