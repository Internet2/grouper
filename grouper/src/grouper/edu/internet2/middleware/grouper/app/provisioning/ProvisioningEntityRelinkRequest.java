package edu.internet2.middleware.grouper.app.provisioning;

/**
 * A request to re-point a provisioned entity from the target account it is currently linked to
 * (which could not be updated) onto a different, already-existing target account that holds the
 * desired identity, and to dispose of the now-orphaned old account per provisioner settings.
 *
 * <p>This is the framework-agnostic hand-off produced when a target dao detects that an update
 * would collide with a pre-existing account (e.g. a SCIM PATCH rejected with HTTP 409 scimType
 * "uniqueness").  The dao only detects the collision and records this request; the generic
 * provisioning logic drains the requests near end-of-run ({@code drainEntityRelinks}) and does the
 * actual work: it re-links the entity's sync member to the new account and runs the old account
 * through the standard entity-delete decision, so "never delete", "disable instead of delete", and
 * "delete only if grouper created it" are all honored centrally rather than in any one dao.</p>
 */
public class ProvisioningEntityRelinkRequest {

  /** the wrapper for the entity being re-linked (carries the sync member and current target link) */
  private ProvisioningEntityWrapper provisioningEntityWrapper;

  /**
   * the pre-existing target account that already holds the desired identity, as a generic target
   * entity (with its target id and link attributes populated), which the entity is re-linked onto.
   */
  private ProvisioningEntity newTargetEntity;

  /** the target id of the old account we were linked to and failed to update; becomes the orphan */
  private String oldTargetId;

  public ProvisioningEntityRelinkRequest() {
  }

  public ProvisioningEntityRelinkRequest(ProvisioningEntityWrapper provisioningEntityWrapper,
      ProvisioningEntity newTargetEntity, String oldTargetId) {
    this.provisioningEntityWrapper = provisioningEntityWrapper;
    this.newTargetEntity = newTargetEntity;
    this.oldTargetId = oldTargetId;
  }

  public ProvisioningEntityWrapper getProvisioningEntityWrapper() {
    return this.provisioningEntityWrapper;
  }

  public void setProvisioningEntityWrapper(ProvisioningEntityWrapper provisioningEntityWrapper) {
    this.provisioningEntityWrapper = provisioningEntityWrapper;
  }

  public ProvisioningEntity getNewTargetEntity() {
    return this.newTargetEntity;
  }

  public void setNewTargetEntity(ProvisioningEntity newTargetEntity) {
    this.newTargetEntity = newTargetEntity;
  }

  public String getOldTargetId() {
    return this.oldTargetId;
  }

  public void setOldTargetId(String oldTargetId) {
    this.oldTargetId = oldTargetId;
  }

}
