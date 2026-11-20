package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.util.GrouperHttpClient;

/**
 * Base class for compiled Java external system templates
 * (<code>templateType=externalSystem</code>, <code>templateMode=compiled</code>).
 *
 * An external system can hand part of its work to one of these templates, for logic that
 * cannot be expressed with configuration alone.  Each hook method has a no-op default, so a
 * template overrides only the hooks its external system calls, and new hooks can be added
 * later without breaking existing templates.
 *
 * Today the web service external system (WsBearerTokenExternalSystem) calls
 * {@link #decorateHttpClient(GrouperHttpClient, String)} when
 * <code>grouper.wsBearerToken.&lt;id&gt;.httpAuthnType = dynamic</code>, for example to log in
 * through SSO and attach a short-lived bearer token and session cookies.
 *
 * Lifecycle: the caller keeps one instance per external system config id and reuses it, so
 * instance fields can cache tokens between calls.  The instance is replaced when the template
 * source changes (hot reload) or the JVM restarts.  Calls can come from several threads at
 * once (e.g. provisioner threads), so a template that caches state must be thread safe.
 *
 * The template keeps its own configuration (target host, credentials, etc.).  Authorization,
 * cookie, auth and key headers are already redacted from the HTTP log; the template marks any other
 * secret header with addDoNotLogHeader.  HTTP calls the template makes itself (e.g. a login or token
 * refresh) must set their own do-not-log headers, parameters and bodies, and exception messages must
 * not include tokens.
 *
 * The caller caches the instance for up to a minute between checks, so disabling or editing the
 * template takes effect within a minute.  A new instance is used if the external system's
 * endpoint changes, so a token is not sent to a different host.
 *
 * GRP-7593
 */
public abstract class GrouperTemplateExternalSystem {

  /**
   * Add authentication (headers, cookies, etc.) to an outgoing web service request.  Called by
   * the web service external system before every attempt of every request, including retries,
   * so an expired token can be refreshed here.  Use
   * {@link GrouperHttpClient#addHeader(String, String)} and
   * {@link GrouperHttpClient#addDoNotLogHeader(String)} for secrets.  Throw an exception if
   * authentication cannot be obtained; the request then fails with that error.
   *
   * @param grouperHttpClient the request about to be sent
   * @param externalSystemConfigId config id of the web service external system making the call,
   *   e.g. the "myWs" in grouper.wsBearerToken.myWs.endpoint
   */
  public void decorateHttpClient(GrouperHttpClient grouperHttpClient, String externalSystemConfigId) {
    // default: nothing to add
  }

}
