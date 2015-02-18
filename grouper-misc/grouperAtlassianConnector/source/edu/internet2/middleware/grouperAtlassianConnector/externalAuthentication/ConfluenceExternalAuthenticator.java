/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.externalAuthentication;

import static com.atlassian.seraph.auth.DefaultAuthenticator.LOGGED_IN_KEY;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.confluence.security.seraph.ConfluenceUserPrincipal;
import com.atlassian.confluence.user.ConfluenceAuthenticator;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.auth.RoleMapper;
import com.atlassian.seraph.config.SecurityConfig;
import com.atlassian.seraph.elevatedsecurity.ElevatedSecurityGuard;
import com.atlassian.seraph.interceptor.LogoutInterceptor;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.atlassian.seraph.util.SecurityUtils;

/**
 *
 */
public class ConfluenceExternalAuthenticator extends ConfluenceAuthenticator {

  /**
   * @see com.atlassian.confluence.user.ConfluenceAuthenticator#authenticate(java.security.Principal, java.lang.String)
   */
  @Override
  protected boolean authenticate(Principal arg0, String arg1) throws AuthenticatorException {
    if (arg0 != null && arg0.getName() != null && arg1 != null && arg0.getName().equals(arg1)) {
      return true;
    }
    System.out.println("ConfluenceExternalAuthenticator: authenticate(Principal arg0, String arg1)");
    return super.authenticate(arg0, arg1);
  }

  /**
   * @see com.atlassian.confluence.user.ConfluenceAuthenticator#authoriseUserAndEstablishSession(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.security.Principal)
   */
  @Override
  protected boolean authoriseUserAndEstablishSession(HttpServletRequest arg0,
      HttpServletResponse arg1, Principal arg2) {
    System.out.println("ConfluenceExternalAuthenticator: authoriseUserAndEstablishSession(HttpServletRequest arg0, HttpServletResponse arg1, Principal arg2)");
    return super.authoriseUserAndEstablishSession(arg0, arg1, arg2);
  }

  /**
   * @see com.atlassian.confluence.user.ConfluenceAuthenticator#getUser(java.lang.String)
   */
  @Override
  protected ConfluenceUser getUser(String uid) {
    System.out.println("ConfluenceExternalAuthenticator: getUser(String uid)");
    return super.getUser(uid);
  }

  /**
   * @see com.atlassian.confluence.user.ConfluenceAuthenticator#getUserAccessor()
   */
  @Override
  protected UserAccessor getUserAccessor() {
    System.out.println("ConfluenceExternalAuthenticator: getUserAccessor()");
    return super.getUserAccessor();
  }

  /**
   * @see com.atlassian.confluence.user.ConfluenceAuthenticator#isPrincipalAlreadyInSessionContext(javax.servlet.http.HttpServletRequest, java.security.Principal)
   */
  @Override
  protected boolean isPrincipalAlreadyInSessionContext(HttpServletRequest httpServletRequest,
      Principal principal) {
    System.out.println("ConfluenceExternalAuthenticator: isPrincipalAlreadyInSessionContext(HttpServletRequest httpServletRequest, Principal principal)");
    return super.isPrincipalAlreadyInSessionContext(httpServletRequest, principal);
  }

  /**
   * @see com.atlassian.confluence.user.ConfluenceAuthenticator#login(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public boolean login(HttpServletRequest arg0, HttpServletResponse arg1, String arg2, String arg3,
      boolean arg4) throws AuthenticatorException {
    System.out.println("ConfluenceExternalAuthenticator: login(HttpServletRequest arg0, HttpServletResponse arg1, String arg2, String arg3, boolean arg4)");
    return super.login(arg0, arg1, arg2, arg3, arg4);
  }

  /**
   * @see com.atlassian.confluence.user.ConfluenceAuthenticator#logout(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  public boolean logout(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticatorException {
    System.out.println("ConfluenceExternalAuthenticator: logout(HttpServletRequest request, HttpServletResponse response)");
    return super.logout(request, response);
  }

  /**
   * @see com.atlassian.confluence.user.ConfluenceAuthenticator#putPrincipalInSessionContext(javax.servlet.http.HttpServletRequest, java.security.Principal)
   */
  @Override
  protected void putPrincipalInSessionContext(HttpServletRequest httpServletRequest,
      Principal principal) {
    System.out.println("ConfluenceExternalAuthenticator: putPrincipalInSessionContext(HttpServletRequest httpServletRequest, Principal principal)");
    super.putPrincipalInSessionContext(httpServletRequest, principal);
  }

  /**
   * @see com.atlassian.confluence.user.ConfluenceAuthenticator#refreshPrincipalObtainedFromSession(javax.servlet.http.HttpServletRequest, java.security.Principal)
   */
  @Override
  protected Principal refreshPrincipalObtainedFromSession(HttpServletRequest httpServletRequest,
      Principal principal) {
    System.out.println("ConfluenceExternalAuthenticator: refreshPrincipalObtainedFromSession(HttpServletRequest httpServletRequest, Principal principal)");
    return super.refreshPrincipalObtainedFromSession(httpServletRequest, principal);
  }

  /**
   * @see com.atlassian.confluence.user.ConfluenceAuthenticator#setUserAccessor(com.atlassian.confluence.user.UserAccessor)
   */
  @Override
  public void setUserAccessor(UserAccessor userAccessor) {
    System.out.println("ConfluenceExternalAuthenticator: setUserAccessor(UserAccessor userAccessor)");
    super.setUserAccessor(userAccessor);
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#getAuthType()
   */
  @Override
  public String getAuthType() {
    System.out.println("ConfluenceExternalAuthenticator: getAuthType()");
    return super.getAuthType();
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#getElevatedSecurityGuard()
   */
  @Override
  protected ElevatedSecurityGuard getElevatedSecurityGuard() {
    System.out.println("ConfluenceExternalAuthenticator: getElevatedSecurityGuard()");
    return super.getElevatedSecurityGuard();
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#getLogoutInterceptors()
   */
  @Override
  protected List<LogoutInterceptor> getLogoutInterceptors() {
    System.out.println("ConfluenceExternalAuthenticator: getLogoutInterceptors()");
    return super.getLogoutInterceptors();
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#getRememberMeService()
   */
  @Override
  protected RememberMeService getRememberMeService() {
    System.out.println("ConfluenceExternalAuthenticator: getRememberMeService()");
    return super.getRememberMeService();
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#getRoleMapper()
   */
  @Override
  protected RoleMapper getRoleMapper() {
    System.out.println("ConfluenceExternalAuthenticator: getRoleMapper()");
    return super.getRoleMapper();
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#getUserFromBasicAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected Principal getUserFromBasicAuthentication(HttpServletRequest arg0,
      HttpServletResponse arg1) {
    System.out.println("ConfluenceExternalAuthenticator: getUserFromBasicAuthentication(HttpServletRequest arg0, HttpServletResponse arg1)");
    return super.getUserFromBasicAuthentication(arg0, arg1);
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#getUserFromCookie(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected Principal getUserFromCookie(HttpServletRequest arg0, HttpServletResponse arg1) {
    System.out.println("ConfluenceExternalAuthenticator: getUserFromCookie(HttpServletRequest arg0, HttpServletResponse arg1)");
    return super.getUserFromCookie(arg0, arg1);
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#getUserFromSession(javax.servlet.http.HttpServletRequest)
   */
  @Override
  protected Principal getUserFromSession(HttpServletRequest arg0) {
    System.out.println("ConfluenceExternalAuthenticator: getUserFromSession(HttpServletRequest arg0)");
    return super.getUserFromSession(arg0);
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#init(java.util.Map, com.atlassian.seraph.config.SecurityConfig)
   */
  @Override
  public void init(Map<String, String> params, SecurityConfig config) {
    System.out.println("ConfluenceExternalAuthenticator: init(Map<String, String> params, SecurityConfig config)");
    super.init(params, config);
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#isAuthorised(javax.servlet.http.HttpServletRequest, java.security.Principal)
   */
  @Override
  protected boolean isAuthorised(HttpServletRequest httpServletRequest, Principal principal) {
    System.out.println("ConfluenceExternalAuthenticator: isAuthorised(HttpServletRequest httpServletRequest, Principal principal)");
    return super.isAuthorised(httpServletRequest, principal);
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#isUserInRole(javax.servlet.http.HttpServletRequest, java.lang.String)
   */
  @Override
  public boolean isUserInRole(HttpServletRequest request, String role) {
    System.out.println("ConfluenceExternalAuthenticator: isUserInRole(HttpServletRequest request, String role)");
    return super.isUserInRole(request, role);
  }

  /**
   * @see com.atlassian.seraph.auth.DefaultAuthenticator#removePrincipalFromSessionContext(javax.servlet.http.HttpServletRequest)
   */
  @Override
  protected void removePrincipalFromSessionContext(HttpServletRequest httpServletRequest) {
    System.out.println("ConfluenceExternalAuthenticator: removePrincipalFromSessionContext(HttpServletRequest httpServletRequest)");
    super.removePrincipalFromSessionContext(httpServletRequest);
  }

  /**
   * @see com.atlassian.seraph.auth.AbstractAuthenticator#destroy()
   */
  @Override
  public void destroy() {
    System.out.println("ConfluenceExternalAuthenticator: destroy()");
    super.destroy();
  }

  /**
   * @see com.atlassian.seraph.auth.AbstractAuthenticator#getConfig()
   */
  @Override
  protected SecurityConfig getConfig() {
    System.out.println("ConfluenceExternalAuthenticator: getConfig()");
    return super.getConfig();
  }

  /**
   * @see com.atlassian.seraph.auth.AbstractAuthenticator#getRemoteUser(javax.servlet.http.HttpServletRequest)
   */
  @Override
  public String getRemoteUser(HttpServletRequest request) {
    System.out.println("ConfluenceExternalAuthenticator: getRemoteUser(HttpServletRequest request)");
    return super.getRemoteUser(request);
  }

  /**
   * @see com.atlassian.seraph.auth.AbstractAuthenticator#getUser(javax.servlet.http.HttpServletRequest)
   */
  @Override
  public Principal getUser(HttpServletRequest request) {
    System.out.println("ConfluenceExternalAuthenticator: getUser(HttpServletRequest request)");
    return super.getUser(request);
  }

  /**
   * @see com.atlassian.seraph.auth.AbstractAuthenticator#login(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.String)
   */
  @Override
  public boolean login(HttpServletRequest request, HttpServletResponse response, String username,
      String password) throws AuthenticatorException {
    System.out.println("ConfluenceExternalAuthenticator: login(HttpServletRequest request, HttpServletResponse response, String username, String password)");
    return super.login(request, response, username, password);
  }

  /**
   * 
   */
  public ConfluenceExternalAuthenticator() {
    super();
    System.out.println("ConfluenceExternalAuthenticator: ConfluenceExternalAuthenticator()");
  }

  
  
  /**
   * 
   * @see com.atlassian.confluence.user.ConfluenceAuthenticator#getUser(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  public Principal getUser(HttpServletRequest request, HttpServletResponse httpServletResponse) {
    System.out.println("ConfluenceExternalAuthenticator: getUser(HttpServletRequest request, HttpServletResponse httpServletResponse)");
    Principal principal = loggedInUser(request);
    if (principal != null && principal.getName() != null && !"".equals(principal.getName().trim())) {
      boolean dothis = false;
      if (dothis) {
        //trying to get BaseLoginFilter to not freak out
        SecurityUtils.disableSeraphFiltering(request);
      }
      dothis = true;
      if (dothis) {
        return getUser(principal.getName());
      }
    }
    return principal;
  }

  /**
   * @param request
   * @return principal
   */
  public static Principal loggedInUser(HttpServletRequest request) {
    ExternalAuthenticator externalAuthenticator = new ExternalAuthenticator();

    return ExternalAuthenticator.getUser(externalAuthenticator, request, 
        "confluence", DefaultAuthenticator.LOGGED_IN_KEY, null);
  }

}
