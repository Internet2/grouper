package edu.internet2.middleware.grouper.plugins.testImplementation;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.plugins.testInterface.SamplePluginProviderInput;
import edu.internet2.middleware.grouper.plugins.testInterface.SamplePluginProviderOutput;
import edu.internet2.middleware.grouper.plugins.testInterface.SamplePluginProviderService;

/**
 * some implementation
 * @author mchyzer
 *
 */
public class ProviderServiceImpl implements SamplePluginProviderService {

  /**
   * 
   */
  @Override
  public SamplePluginProviderOutput provide(SamplePluginProviderInput providerInput) {
    SamplePluginProviderOutput providerOutput = new SamplePluginProviderOutput();
    if (StringUtils.equals(providerInput.getInput1(), "hey")) {
      providerOutput.setOutput1(providerInput.getInput1() + " hey output");
    } else {
      providerOutput.setOutput1(providerInput.getInput1() + " nonhey output");
    }
    return providerOutput;
  }
 
}