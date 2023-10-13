package edu.internet2.middleware.grouper.plugin.test;

import edu.internet2.middleware.grouper.plugin.test.filter.TestFilter;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

public class TestPluginServletContainerInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        TestFilter testFilter = new TestFilter();
        FilterRegistration.Dynamic testFilterRegistration = servletContext.addFilter("testFilter", testFilter);
        testFilterRegistration.addMappingForUrlPatterns(null, false, "/*");
    }
}
