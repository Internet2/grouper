package edu.internet2.middleware.grouper.app.provisioning;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * bean to hold a provisioning validation problem.
 * @author mchyzer
 *
 */
public class ProvisioningValidationIssue {

  /**
   * error message
   */
  private String message;

  /**
   * can indicate on UI where the issue is
   */
  private String jqueryHandle;
  
  /**
   * if this should stop the runtime from running.  i.e. fatal error
   */
  private boolean runtimeError;

  /**
   * error message
   * @return error message
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * error message
   * @param message1
   * @return this for chaining
   */
  public ProvisioningValidationIssue assignMessage(String message1) {
    this.message = message1;
    return this;
  }

  /**
   * can indicate on UI where the issue is
   * @return jqeury handle
   */
  public String getJqueryHandle() {
    return this.jqueryHandle;
  }

  /**
   * can indicate on UI where the issue is
   * @param jqueryHandle1
   * @return this for chaining
   */
  public ProvisioningValidationIssue assignJqueryHandle(String jqueryHandle1) {
    this.jqueryHandle = htmlJqueryHandle(jqueryHandle1);
    return this;
  }

  /**
   * 
   * @param suffix
   * @return html jquery handle
   */
  public String htmlJqueryHandle(String suffix) {
    if (suffix != null && !suffix.startsWith("#") && !StringUtils.equals("class", suffix)) {
      suffix = "#config_" + suffix + "_spanid";
    }
    return suffix;
  }


  /**
   * if this should stop the runtime from running.  i.e. fatal error
   * @return runtime error
   */
  public boolean isRuntimeError() {
    return this.runtimeError;
  }

  /**
   * if this should stop the runtime from running.  i.e. fatal error
   * @param runtimeError1
   * @return this for chaining
   */
  public ProvisioningValidationIssue assignRuntimeError(boolean runtimeError1) {
    this.runtimeError = runtimeError1;
    return this;
  }

  /**
  *
  */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ProvisioningValidationIssue)) {
      return false;
    }
    ProvisioningValidationIssue other = (ProvisioningValidationIssue) obj;

    return new EqualsBuilder()
        .append(this.jqueryHandle, other.jqueryHandle)
        .append(this.message, other.message)
        .append(this.runtimeError, other.runtimeError).isEquals();

  }

  /**
   * 
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(this.jqueryHandle)
        .append(this.message)
        .append(this.runtimeError).toHashCode();
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
  }

}
