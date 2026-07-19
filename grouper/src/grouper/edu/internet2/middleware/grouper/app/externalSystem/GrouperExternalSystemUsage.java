/**
 * @author Grouper - external system references feature
 */
package edu.internet2.middleware.grouper.app.externalSystem;

/**
 * One place in Grouper where an external system is referenced (used), either by
 * config editor config (grouper_config) or by attribute data (loaders, reports).
 * These are rendered as rows in the References section of the external system
 * view details screen (Miscellaneous -&gt; External systems -&gt; Actions -&gt; View details).
 */
public class GrouperExternalSystemUsage {

  /**
   * link kind constants.  These decide whether/how the name is rendered as a link.
   * A null linkType means no link (show the name as plain text) -- used for
   * reference types that do not have their own view page.
   */
  public static final String LINK_TYPE_PROVISIONER = "provisioner";

  /** link kind: link to a group view page */
  public static final String LINK_TYPE_GROUP = "group";

  /** link kind: link to a folder (stem) view page */
  public static final String LINK_TYPE_STEM = "stem";

  /**
   * human readable type of the reference, e.g. Provisioner, SQL group loader.
   * Rows are grouped by this in the UI and capped per type.
   */
  private String usageType;

  /**
   * display name of the referencing object, e.g. the provisioner config id or
   * the group/folder name.
   */
  private String name;

  /** human readable description of how the external system is used */
  private String description;

  /**
   * one of the LINK_TYPE_* constants, or null when the name should render as
   * plain text (no view page exists for this reference type).
   */
  private String linkType;

  /**
   * no-arg constructor
   */
  public GrouperExternalSystemUsage() {
  }

  /**
   * convenience constructor
   * @param usageType human readable type
   * @param name display name of the referencing object
   * @param description how the external system is used
   * @param linkType one of LINK_TYPE_* or null for plain text
   */
  public GrouperExternalSystemUsage(String usageType, String name, String description, String linkType) {
    this.usageType = usageType;
    this.name = name;
    this.description = description;
    this.linkType = linkType;
  }

  /**
   * @return human readable type of the reference
   */
  public String getUsageType() {
    return usageType;
  }

  /**
   * @param usageType human readable type of the reference
   */
  public void setUsageType(String usageType) {
    this.usageType = usageType;
  }

  /**
   * @return display name of the referencing object
   */
  public String getName() {
    return name;
  }

  /**
   * @param name display name of the referencing object
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return how the external system is used
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description how the external system is used
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return one of LINK_TYPE_* or null for plain text
   */
  public String getLinkType() {
    return linkType;
  }

  /**
   * @param linkType one of LINK_TYPE_* or null for plain text
   */
  public void setLinkType(String linkType) {
    this.linkType = linkType;
  }

}
