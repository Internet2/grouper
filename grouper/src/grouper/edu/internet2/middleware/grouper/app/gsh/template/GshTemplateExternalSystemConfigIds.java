package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * Dropdown option driver for an external system field that points at a GSH template, e.g.
 * grouper.wsBearerToken.&lt;id&gt;.dynamicGshTemplateConfigId.  It lists every GSH template config
 * id whose templateType is "externalSystem", so the config editor shows a dropdown of valid
 * templates instead of a free-text field.  externalSystem templates have no legacy interpreted
 * path, so every id returned here is a compiled template.
 *
 * Modeled on GshTemplateDaemonConfigIds.  Wired via optionValuesFromClass in
 * grouper-loader.base.properties.
 *
 * GRP-7593
 */
public class GshTemplateExternalSystemConfigIds implements OptionValueDriver {

  public GshTemplateExternalSystemConfigIds() {
  }

  /**
   * matches the templateType key of every configured GSH template, capturing
   * the template config id in group 1
   */
  private static final Pattern configIdPattern = Pattern.compile("^grouperGshTemplate\\.([^.]+)\\.templateType$");

  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> propertyConfigIds = grouperConfig.propertyConfigIds(configIdPattern);
    List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
    for (String propertyConfigId : propertyConfigIds) {
      String templateType = grouperConfig.propertyValueString("grouperGshTemplate." + propertyConfigId + ".templateType");
      // externalSystem templates are always compiled, so no templateMode check is needed
      if (StringUtils.equals(GshTemplateType.externalSystem.name(), templateType)) {
        keysAndLabels.add(new MultiKey(propertyConfigId, propertyConfigId));
      }
    }
    return keysAndLabels;
  }

}
