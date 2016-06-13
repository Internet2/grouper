package edu.internet2.middleware.grouper.ws.scim;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import io.swagger.util.Json;
import io.swagger.util.Yaml;

@WebListener
public class SwaggerJaxrsConfig implements ServletContextListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerJaxrsConfig.class);

  public void contextInitialized(ServletContextEvent event) {
    LOGGER.debug("Initializing swagger...");

    try {
      Json.mapper().registerModule(new JaxbAnnotationModule());
      Yaml.mapper().registerModule(new JaxbAnnotationModule());

    } catch (Exception e) {
      LOGGER.error("Error initializing swagger", e);
    }
  }

  public void contextDestroyed(ServletContextEvent event) {
    // do on application destroy
  }
}