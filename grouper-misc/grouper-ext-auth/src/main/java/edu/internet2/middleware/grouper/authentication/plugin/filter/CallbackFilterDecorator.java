package edu.internet2.middleware.grouper.authentication.plugin.filter;

import edu.internet2.middleware.grouper.authentication.plugin.ConfigUtils;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import org.pac4j.jee.filter.CallbackFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class CallbackFilterDecorator extends CallbackFilter implements Reinitializable {
    private Timer timer = new Timer();

    private static boolean isCallbackUrlCalled(HttpServletRequest request) {
        return getRequestPathInContext(request).matches(ConfigUtils.getBestGrouperConfiguration().propertyValueString("external.authentication.callbackUrl", "/callback"));
    }

    private static String getRequestPathInContext(HttpServletRequest request) {
        return request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        this.initDecorator();
        TimerTask timerTask = new ReinitializingTimer(this);
        int period = ConfigUtils.getBestGrouperConfiguration().propertyValueInt("external.authentication.config.reload.milliseconds", 60 * 1000);
        this.timer.schedule(timerTask, period, period);
    }

    public void initDecorator() {
        this.setDefaultUrl(ConfigUtils.getBestGrouperConfiguration().propertyValueString("external.authentication.defaultUrl", "/"));
        this.setRenewSession(true);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (FilterDecoratorUtils.isExternalAuthenticationEnabled() && isCallbackUrlCalled((HttpServletRequest) request)) {
            super.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    protected void internalFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            super.internalFilter(request, response, chain);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }
}
