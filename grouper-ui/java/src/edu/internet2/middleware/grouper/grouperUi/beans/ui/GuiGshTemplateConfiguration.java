package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateCompileStatus;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateCompileStatus.GshTemplateCompileStatusResult;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GuiGshTemplateConfiguration {

  private GshTemplateConfiguration gshTemplateConfiguration;

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

  // GRP-7034: inventory columns (type, mode, source location, compile status).

  /**
   * @return the template type (gsh, abac, provisioner, daemon, ...); defaults to gsh
   */
  public String getTemplateType() {
    String value = this.gshTemplateConfiguration.retrieveAttributeValueFromConfig("templateType", false);
    return StringUtils.defaultIfBlank(value, "gsh");
  }

  /**
   * @return the template mode (interpreted or compiled); defaults to interpreted
   */
  public String getTemplateMode() {
    String value = this.gshTemplateConfiguration.retrieveAttributeValueFromConfig("templateMode", false);
    return StringUtils.defaultIfBlank(value, "interpreted");
  }

  /**
   * @return true if this template runs in compiled-Java mode
   */
  public boolean isCompiledMode() {
    return StringUtils.equalsIgnoreCase("compiled", this.getTemplateMode());
  }

  /**
   * @return source location token: "file" (container file) or "inline" (config)
   */
  public String getSourceLocation() {
    String sourceType = this.gshTemplateConfiguration.retrieveAttributeValueFromConfig("gshTemplateSourceType", false);
    return StringUtils.equals("file", sourceType) ? "file" : "inline";
  }

  /**
   * lazily-computed compile-status token: "" (interpreted, not applicable),
   * "ok", "failed", or "fileMissing"
   */
  private String compileStatusToken;

  /**
   * lazily-computed compile-status result (null for interpreted / file-missing)
   */
  private GshTemplateCompileStatusResult compileStatusResult;

  /**
   * whether the compile status has been computed yet (results are cached per bean)
   */
  private boolean compileStatusComputed;

  /**
   * Compute the compile-status token and result once per bean.
   */
  private void computeCompileStatus() {
    if (this.compileStatusComputed) {
      return;
    }
    this.compileStatusComputed = true;

    if (!this.isCompiledMode()) {
      this.compileStatusToken = "";
      return;
    }

    String sourceType = this.gshTemplateConfiguration.retrieveAttributeValueFromConfig("gshTemplateSourceType", false);
    String javaSource;
    if (StringUtils.equals("file", sourceType)) {
      String fileName = this.gshTemplateConfiguration.retrieveAttributeValueFromConfig("gshTemplateFileName", false);
      if (StringUtils.isBlank(fileName)) {
        this.compileStatusToken = "";
        return;
      }
      File file = new File(fileName);
      if (!file.exists()) {
        this.compileStatusToken = "fileMissing";
        return;
      }
      javaSource = GrouperUtil.readFileIntoString(file);
    } else {
      javaSource = this.gshTemplateConfiguration.retrieveAttributeValueFromConfig("gshTemplate", false);
    }

    if (StringUtils.isBlank(javaSource)) {
      this.compileStatusToken = "";
      return;
    }

    this.compileStatusResult = GshTemplateCompileStatus.statusForSource(
        this.gshTemplateConfiguration.getConfigId(), javaSource);
    this.compileStatusToken = this.compileStatusResult.isSuccess() ? "ok" : "failed";
  }

  /**
   * @return compile-status token for display: "" (interpreted / unknown),
   *   "ok", "failed", or "fileMissing"
   */
  public String getCompileStatus() {
    this.computeCompileStatus();
    return this.compileStatusToken;
  }

  /**
   * @return compiler/parse diagnostics when the compile failed; empty otherwise
   */
  public String getCompileStatusDetail() {
    this.computeCompileStatus();
    if (this.compileStatusResult == null || this.compileStatusResult.getDiagnostics() == null) {
      return "";
    }
    return this.compileStatusResult.getDiagnostics();
  }

  /**
   * @return when the compile status was last computed, formatted, or empty if
   *   not applicable
   */
  public String getLastCompiled() {
    this.computeCompileStatus();
    if (this.compileStatusResult == null) {
      return "";
    }
    return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(this.compileStatusResult.getLastCompiledMillis()));
  }

}
