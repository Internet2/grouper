package edu.internet2.middleware.grouper.app.scim2Provisioning;

/**
 * One attribute parsed from a SCIM /Schemas response.  Complex attributes are flattened, so a
 * sub-attribute is represented with a dotted name (e.g. the givenName sub-attribute of name becomes
 * "name.givenName").  Used by provisioning diagnostics to cross-check the provisioner's configured
 * target attributes against what the target's schema actually advertises (mutability, required).
 */
public class GrouperScim2SchemaAttribute {

  /** SCIM resource this attribute belongs to, e.g. "User" or "Group" */
  private String resourceName;

  /** flattened attribute name, e.g. "userName", "name.givenName", "emails.value" */
  private String name;

  /** SCIM mutability: readOnly, readWrite, immutable, or writeOnly (may be null if not advertised) */
  private String mutability;

  /** whether the target schema marks this attribute as required */
  private boolean required;

  /** SCIM type: string, boolean, complex, etc. (may be null) */
  private String type;

  public GrouperScim2SchemaAttribute() {
  }

  public GrouperScim2SchemaAttribute(String resourceName, String name, String mutability, boolean required, String type) {
    this.resourceName = resourceName;
    this.name = name;
    this.mutability = mutability;
    this.required = required;
    this.type = type;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMutability() {
    return mutability;
  }

  public void setMutability(String mutability) {
    this.mutability = mutability;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "GrouperScim2SchemaAttribute[resource=" + resourceName + ", name=" + name
        + ", mutability=" + mutability + ", required=" + required + ", type=" + type + "]";
  }
}
