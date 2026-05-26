package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript;

/**
 * Base class for compiled Java GSH change-log daemon templates
 * (<code>templateType=daemonChangeLog</code>,
 * <code>templateMode=compiled</code>).
 *
 * The dispatcher — a branch inside EsbPublisherChangeLogScript's
 * dispatchEventList — reads the configured GSH template id, resolves the
 * compiled class through GshTemplateClassLoaderRegistry, instantiates a
 * subclass of this class, and calls processRecords with the
 * EsbPublisherChangeLogScript reference itself. The reference carries the
 * batch's events (esbEventContainers), the change-log processor metadata
 * (which exposes the hib3GrouperLoaderLog), and the
 * ProvisioningSyncConsumerResult used to advance the cursor.
 *
 * The author returns the sequence number of the last event in the batch
 * that was successfully processed. The dispatcher sets this on the
 * ProvisioningSyncConsumerResult so the change-log cursor advances
 * correctly. Returning -1 signals "no advance" (no events successfully
 * processed; the same batch will be retried on the next dispatch).
 *
 * Throw any exception to signal hard failure; the dispatcher records it
 * and the cursor does NOT advance.
 *
 * GRP-7011
 */
public abstract class GrouperTemplateDaemonChangeLog {

  /**
   * Override to implement the change-log consumer's work. Called once per
   * event batch.
   *
   * @param esbPublisherChangeLogScript the bridge instance — carries the
   *   events, metadata, result object, and shared script context. Same
   *   instance that EsbPublisherChangeLogScript.retrieveFromThreadLocal()
   *   returns for helper code that uses the ThreadLocal pattern.
   * @return the sequence number of the last successfully processed event
   *   from the current batch, or -1 if no advance should happen
   */
  public abstract long processRecords(EsbPublisherChangeLogScript esbPublisherChangeLogScript);

}
