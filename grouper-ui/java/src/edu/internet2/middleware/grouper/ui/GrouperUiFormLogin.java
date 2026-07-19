/*******************************************************************************
 * Grouper UI form-based login (development convenience).
 ******************************************************************************/
package edu.internet2.middleware.grouper.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.authentication.GrouperPassword;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.j2ee.Authentication;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Simple form-based login for the Grouper UI, as a DEVELOPMENT convenience (for
 * example so an automated/headless browser can log in without a native HTTP
 * BASIC popup, which such browsers cannot drive).
 *
 * <p>This is wired dynamically inside {@link GrouperUiFilter} (which is itself
 * registered programmatically in CommonServletContainerInitializer), NOT via a
 * web.xml login-config -- the Servlet API does not allow setting an auth-method
 * at runtime, so container FORM auth is not an option here.</p>
 *
 * <p>It is gated two ways and BOTH must hold:</p>
 * <ul>
 *   <li>config: grouper.is.ui.formAuthn = true (in grouper.hibernate.properties)</li>
 *   <li>environment: NOT production (see {@link #isProductionEnvironment()})</li>
 * </ul>
 *
 * <p>Credentials are checked by the same {@link Authentication} path used for the
 * BASIC auth mode, so the grouperPasswordConfigOverride_UI_&lt;user&gt;_pass
 * entries (and the grouper_password table) are what accept a login.</p>
 */
public class GrouperUiFormLogin {

  /** request parameter names for the login form */
  private static final String PARAM_USERNAME = "grouperFormLoginUsername";

  /** request parameter for the password */
  private static final String PARAM_PASSWORD = "grouperFormLoginPassword";

  /** request parameter for where to send the user after a successful login */
  private static final String PARAM_TARGET = "grouperFormLoginTarget";

  /**
   * true if this looks like a production environment, in which case form login
   * must never run. Mirrors the logic used for the environment banner
   * (IndexContainer): production when grouper.env.name is blank, prod, or
   * production.
   * @return true if production
   */
  public static boolean isProductionEnvironment() {
    String environmentName = GrouperConfig.retrieveConfig().propertyValueString("grouper.env.name");
    return StringUtils.isBlank(environmentName)
        || StringUtils.equalsIgnoreCase("production", environmentName)
        || StringUtils.equalsIgnoreCase("prod", environmentName);
  }

  /**
   * Handle the form login for a request that is not yet authenticated.
   *
   * @param request the request
   * @param response the response
   * @param session the http session
   * @return true if the user is authenticated and the filter chain should
   *   continue; false if this method has already written the response (the login
   *   form or a post-login redirect) and the caller should return immediately.
   * @throws IOException on write error
   */
  public static boolean handleFormLogin(HttpServletRequest request, HttpServletResponse response,
      HttpSession session) throws IOException {

    // already logged in this session
    if (session.getAttribute("REMOTE_USER") != null) {
      return true;
    }

    String username = request.getParameter(PARAM_USERNAME);
    String password = request.getParameter(PARAM_PASSWORD);

    // this is a login submission
    if (!StringUtils.isBlank(username) && password != null) {

      String basicHeader = "Basic " + Base64.getEncoder().encodeToString(
          (username + ":" + password).getBytes(StandardCharsets.UTF_8));

      boolean isValid = new Authentication().authenticate(basicHeader,
          GrouperPassword.Application.UI, request.getRemoteAddr());

      if (isValid) {
        session.setAttribute("REMOTE_USER", username);
        // post/redirect/get so credentials do not linger in the request
        response.sendRedirect(safeTarget(request, request.getParameter(PARAM_TARGET)));
        return false;
      }

      // bad credentials: show the form again with an error
      writeLoginForm(request, response, "Invalid username or password", request.getParameter(PARAM_TARGET));
      return false;
    }

    // not logged in and not submitting: show the login form, remembering where
    // the user was trying to go
    String target = request.getRequestURI();
    if (!StringUtils.isBlank(request.getQueryString())) {
      target = target + "?" + request.getQueryString();
    }
    writeLoginForm(request, response, null, target);
    return false;
  }

  /**
   * only allow redirect targets inside this webapp, otherwise use a default,
   * to avoid an open redirect.
   * @param request the request
   * @param target the requested target
   * @return a safe target url
   */
  private static String safeTarget(HttpServletRequest request, String target) {
    String defaultTarget = request.getContextPath() + "/grouperUi/app/UiV2Main.index";
    if (StringUtils.isBlank(target)) {
      return defaultTarget;
    }
    // must be a path within this context (no scheme, no protocol-relative, in-context)
    if (!target.startsWith(request.getContextPath() + "/") || target.startsWith("//")) {
      return defaultTarget;
    }
    return target;
  }

  /**
   * write the minimal login form html.
   * @param request the request
   * @param response the response
   * @param errorMessage optional error message to show
   * @param target where to send the user after login
   * @throws IOException on write error
   */
  private static void writeLoginForm(HttpServletRequest request, HttpServletResponse response,
      String errorMessage, String target) throws IOException {

    response.setContentType("text/html; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");

    String action = GrouperUtil.xmlEscape(request.getRequestURI());
    String targetEscaped = GrouperUtil.xmlEscape(StringUtils.defaultString(target));
    String environmentName = GrouperUtil.xmlEscape(
        StringUtils.defaultString(GrouperConfig.retrieveConfig().propertyValueString("grouper.env.name")));

    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html>\n");
    html.append("<html><head><meta charset=\"UTF-8\"/><title>Grouper login</title>\n");
    html.append("<style>body{font-family:sans-serif;margin:60px auto;max-width:320px;}"
        + "h1{font-size:20px;}label{display:block;margin-top:12px;}"
        + "input[type=text],input[type=password]{width:100%;padding:6px;box-sizing:border-box;}"
        + "button{margin-top:16px;padding:8px 16px;}.err{color:#b00;}.env{color:#888;font-size:12px;}</style>\n");
    html.append("</head><body>\n");
    html.append("<h1>Grouper login</h1>\n");
    if (!StringUtils.isBlank(environmentName)) {
      html.append("<p class=\"env\">Environment: ").append(environmentName).append("</p>\n");
    }
    if (!StringUtils.isBlank(errorMessage)) {
      html.append("<p class=\"err\">").append(GrouperUtil.xmlEscape(errorMessage)).append("</p>\n");
    }
    html.append("<form method=\"POST\" action=\"").append(action).append("\">\n");
    html.append("<label>Username<input type=\"text\" name=\"").append(PARAM_USERNAME).append("\" autofocus=\"autofocus\"/></label>\n");
    html.append("<label>Password<input type=\"password\" name=\"").append(PARAM_PASSWORD).append("\"/></label>\n");
    html.append("<input type=\"hidden\" name=\"").append(PARAM_TARGET).append("\" value=\"").append(targetEscaped).append("\"/>\n");
    html.append("<button type=\"submit\">Log in</button>\n");
    html.append("</form>\n");
    html.append("</body></html>\n");

    // Write via the character writer normally; but on some dispatches (e.g. a
    // welcome-file forward where the container already took the output stream)
    // response.getWriter() throws IllegalStateException -- fall back to the
    // output stream in that case.
    byte[] bytes = html.toString().getBytes(StandardCharsets.UTF_8);
    try {
      PrintWriter out = response.getWriter();
      out.write(html.toString());
      out.flush();
    } catch (IllegalStateException getWriterNotAvailable) {
      response.getOutputStream().write(bytes);
      response.getOutputStream().flush();
    }
  }

}
