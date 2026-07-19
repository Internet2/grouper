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
 * Dropdown option driver for the "gshTemplateConfigId" field of a compiledJava
 * otherJob (script daemon) config. It enumerates every GSH template config id
 * whose templateType is "daemon" so the config editor can present a dropdown of
 * valid daemon templates instead of a free-text field. daemon templates have no
 * legacy interpreted path (see GshTemplateMode), so every id returned here is a
 * compiled daemon template -- exactly the set an otherJob.scriptType=compiledJava
 * daemon can point at.
 *
 * Modeled on GshTemplateProvisionerConfigIds (which does the same for
 * templateType=provisioner). Wired via optionValuesFromClass on the
 * otherJob...gshTemplateConfigId metadata in grouper-loader.base.properties.
 */
public class GshTemplateDaemonConfigIds implements OptionValueDriver {

  public GshTemplateDaemonConfigIds() {
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
      // daemon templates are always compiled, so no templateMode check is needed
      if (StringUtils.equals(GshTemplateType.daemon.name(), templateType)) {
        keysAndLabels.add(new MultiKey(propertyConfigId, propertyConfigId));
      }
    }
    return keysAndLabels;
  }

}
