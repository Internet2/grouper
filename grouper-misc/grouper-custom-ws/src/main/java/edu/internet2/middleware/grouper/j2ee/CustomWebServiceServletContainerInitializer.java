package edu.internet2.middleware.grouper.j2ee;

import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.ws.rest.CustomGrouperRestServlet;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Set;

public class CustomWebServiceServletContainerInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        boolean runGrouperWs = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ws", false);

        if (runGrouperWs) {
            String customRestServletName = "CustomRestServlet";
            Class customRestServletClass = CustomGrouperRestServlet.class;
            ServletRegistration.Dynamic customRestServlet = servletContext.addServlet(customRestServletName, customRestServletClass);
            customRestServlet.addMapping("/servicesRest/custom/*");
            customRestServlet.setLoadOnStartup(1);
        }
    }
}
