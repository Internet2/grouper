package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GuiGshTemplateConfiguration {

  private static final Log LOG = GrouperUtil.getLog(GuiGshTemplateConfiguration.class);

  private GshTemplateConfiguration gshTemplateConfiguration;

  /** whether the default Run button target has been resolved */
  private boolean defaultRunButtonTargetComputed;

  /** resolved group or folder id for the default Run button */
  private String defaultRunButtonTargetId;

  /** whether resolving the configured default Run button target failed */
  private boolean defaultRunButtonResolutionError;

  private GuiGshTemplateConfiguration(GshTemplateConfiguration gshTemplateConfiguration) {
    this.gshTemplateConfiguration = gshTemplateConfiguration;
  }
  
  public GshTemplateConfiguration getGshTemplateConfiguration() {
    return gshTemplateConfiguration;
  }

  public static GuiGshTemplateConfiguration convertFromGshTemplateConfiguration(GshTemplateConfiguration gshTemplateConfiguration) {
    return new GuiGshTemplateConfiguration(gshTemplateConfiguration);
  }
  
  public static List<GuiGshTemplateConfiguration> convertFromGshTemplateConfiguration(List<GshTemplateConfiguration> gshTemplateConfigurations) {
    
    List<GuiGshTemplateConfiguration> guiGshTemplateConfigs = new ArrayList<GuiGshTemplateConfiguration>();
    
    for (GshTemplateConfiguration gshTemplateConfiguration: gshTemplateConfigurations) {
      guiGshTemplateConfigs.add(convertFromGshTemplateConfiguration(gshTemplateConfiguration));
    }
    
    return guiGshTemplateConfigs;
    
  }

  /**
   * Resolve the optional default Run button target once per row.  The underlying
   * configuration getters intentionally throw when a configured group or folder
   * cannot be found.  The inventory screen must isolate that failure to the
   * affected row so an administrator can still edit the configuration.
   */
  private void computeDefaultRunButtonTarget() {
    if (this.defaultRunButtonTargetComputed) {
      return;
    }
    this.defaultRunButtonTargetComputed = true;

    String defaultRunButtonType = this.gshTemplateConfiguration.getDefaultRunButtonType();
    try {
      if (StringUtils.equals("group", defaultRunButtonType)) {
        this.defaultRunButtonTargetId = this.gshTemplateConfiguration.getGroupId();
      } else if (StringUtils.equals("folder", defaultRunButtonType)) {
        this.defaultRunButtonTargetId = this.gshTemplateConfiguration.getFolderId();
      }
    } catch (RuntimeException e) {
      this.defaultRunButtonResolutionError = true;
      LOG.warn("Could not resolve default Run button target for GSH template configId "
          + this.gshTemplateConfiguration.getConfigId(), e);
    }
  }

  /**
   * @return resolved group or folder id for the default Run button, or null
   */
  public String getDefaultRunButtonTargetId() {
    this.computeDefaultRunButtonTarget();
    return this.defaultRunButtonTargetId;
  }

  /**
   * @return true if the configured default Run button group or folder could not be resolved
   */
  public boolean isDefaultRunButtonResolutionError() {
    this.computeDefaultRunButtonTarget();
    return this.defaultRunButtonResolutionError;
  }
  
}
