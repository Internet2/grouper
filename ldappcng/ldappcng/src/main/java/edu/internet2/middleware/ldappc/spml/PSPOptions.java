/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.spml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.SchemaEntityRef;

import edu.internet2.middleware.ldappc.spml.request.BulkCalcRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkDiffRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkProvisioningRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkSyncRequest;
import edu.internet2.middleware.ldappc.spml.request.CalcRequest;
import edu.internet2.middleware.ldappc.spml.request.DiffRequest;
import edu.internet2.middleware.ldappc.spml.request.ProvisioningRequest;
import edu.internet2.middleware.ldappc.spml.request.SyncRequest;

public class PSPOptions {

  /** Represent cli args as requests. */
  public enum Mode {

    /** BulkCalcRequest */
    bulkCalc {

      /**
       * {@inheritDoc}
       */
      public ProvisioningRequest getNewProvisioningRequest() {
        return new BulkCalcRequest();
      }

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        return new Option("bulkCalc", "Calculate provisioning for all identifiers.");
      }
    },

    /** BulkDiffRequest */
    bulkDiff {

      /**
       * {@inheritDoc}
       */
      public ProvisioningRequest getNewProvisioningRequest() {
        return new BulkDiffRequest();
      }

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        return new Option("bulkDiff", "Determine provisioning difference for all identifiers.");
      }
    },

    /** BulkSyncRequest */
    bulkSync {

      /**
       * {@inheritDoc}
       */
      public ProvisioningRequest getNewProvisioningRequest() {
        return new BulkSyncRequest();
      }

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        return new Option("bulkSync", "Synchronize provisioning for all identifiers.");
      }
    },

    /** CalcRequest */
    calc {

      /**
       * {@inheritDoc}
       */
      public ProvisioningRequest getNewProvisioningRequest() {
        return new CalcRequest();
      }

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("calc", "Calculate provisioning for an identifier.");
        option.setArgs(Option.UNLIMITED_VALUES);
        option.setArgName("id");
        return option;
      }
    },

    /** DiffRequest */
    diff {

      /**
       * {@inheritDoc}
       */
      public ProvisioningRequest getNewProvisioningRequest() {
        return new DiffRequest();
      }

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("diff", "Determine provisioning difference for an identifier.");
        option.setArgs(Option.UNLIMITED_VALUES);
        option.setArgName("id");
        return option;
      }
    },

    /** SyncRequest */
    sync {

      /**
       * {@inheritDoc}
       */
      public ProvisioningRequest getNewProvisioningRequest() {
        return new SyncRequest();
      }

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("sync", "Synchronize provisioning for an identifier.");
        option.setArgs(Option.UNLIMITED_VALUES);
        option.setArgName("id");
        return option;
      }
    };

    /**
     * Return a newly created <code>ProvisioningRequest</code> appropriate for the mode of
     * operation.
     * 
     * @return the newly instantiated <code>ProvisioningRequest</code>
     */
    public abstract ProvisioningRequest getNewProvisioningRequest();

    /**
     * Return the <code>Option</code> name via getOption().getOpt().
     * 
     * @return the name of the option
     */
    public String getOpt() {
      return getOption().getOpt();
    }

    /**
     * Return the commons-cli option.
     * 
     * @return the <code>Option<code>
     */
    public abstract Option getOption();

    /**
     * Return the <code>ProvisioningRequest</code>s resulting from the processing of the
     * command line.
     * 
     * @param line
     *          the <code>CommandLine</code>
     * @return the SPML requests
     */
    public List<ProvisioningRequest> getRequests(CommandLine line) {

      List<ProvisioningRequest> requests = new ArrayList<ProvisioningRequest>();
      if (this.getOption().hasArg()) {
        for (String id : line.getOptionValues(this.getOpt())) {
          ProvisioningRequest request = this.getNewProvisioningRequest();
          request.setId(id);
          requests.add(request);
        }
      } else {
        requests.add(this.getNewProvisioningRequest());
      }
      return requests;
    }
  }

  /** cli options */
  public enum Opts {

    /** configuration directory */
    conf {

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("conf", true, "Configuration directory.");
        option.setArgName("dir");
        return option;
      }

      /**
       * {@inheritDoc}
       */
      public void handle(PSPOptions pspOptions, CommandLine line) {
        pspOptions.setConfDir(line.getOptionValue(this.getOpt()));
      }
    },

    /** SPML entityName */
    entityName {

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("entityName", true, "Entity name or provisioned object ID.");
        option.setArgName("id");
        return option;
      }

      /**
       * {@inheritDoc}
       */
      public void handle(PSPOptions pspOptions, CommandLine line) {
        String entityName = line.getOptionValue(this.getOpt());
        for (ProvisioningRequest request : pspOptions.getRequests()) {
          if (request.getSchemaEntities().isEmpty()) {
            request.addSchemaEntity(new SchemaEntityRef());
          }
          if (request.getSchemaEntities().size() > 1) {
            throw new IllegalArgumentException("Only one schema entity is supported.");
          }
          request.getSchemaEntities().get(0).setEntityName(entityName);
        }
      }
    },

    /** polling interval */
    interval {

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("interval", true,
            "Number of seconds between the start of recurring provisioning iterations. If omitted, only one provisioning cycle is performed.");
        option.setArgName("seconds");
        return option;
      }

      /**
       * {@inheritDoc}
       */
      public void handle(PSPOptions pspOptions, CommandLine line) {
        pspOptions.setInterval(Integer.parseInt(line.getOptionValue(this.getOpt())));
      }
    },

    /** polling interval */
    lastModifyTime {

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("lastModifyTime", true, "Select objects changed since this time.");
        option.setArgName("yyyy-MM-dd[_hh:mm:ss]");
        return option;
      }

      /**
       * {@inheritDoc}
       */
      public void handle(PSPOptions pspOptions, CommandLine line) {

        for (ProvisioningRequest request : pspOptions.getRequests()) {
          if (request instanceof BulkProvisioningRequest) {
            try {
              ((BulkProvisioningRequest) request).setUpdatedSince(line.getOptionValue(this.getOpt()));
              if (pspOptions.getLastModifyTime() == null) {
                pspOptions.setLastModifyTime(((BulkProvisioningRequest) request).getUpdatedSinceAsDate());
              }
            } catch (java.text.ParseException e) {
              throw new IllegalArgumentException(e);
            }
          }
        }
      }
    },

    /** log spml requests and responses */
    logSpml {

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        return new Option("logSpml", "Log SPML requests and responses.");
      }

      /**
       * {@inheritDoc}
       */
      public void handle(PSPOptions pspOptions, CommandLine line) {
        if (line.hasOption(this.getOpt())) {
          pspOptions.setLogSpml(true);
        }
      }
    },

    /** output file */
    output {

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("output", true, "Output file.");
        option.setArgName("file");
        return option;
      }

      /**
       * {@inheritDoc}
       */
      public void handle(PSPOptions pspOptions, CommandLine line) {
        pspOptions.setOutputFile(line.getOptionValue(this.getOpt()));
      }
    },

    /** whether or not to print requests */
    printRequests {

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("printRequests", "Print SPML requests as well as responses.");
        return option;
      }

      /**
       * {@inheritDoc}
       */
      public void handle(PSPOptions pspOptions, CommandLine line) {
        pspOptions.setPrintRequests(true);
      }
    },

    /** SPML request ID */
    requestID {

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("requestID", true, "Request ID.");
        option.setArgName("id");
        return option;
      }

      /**
       * {@inheritDoc}
       */
      public void handle(PSPOptions pspOptions, CommandLine line) {
        for (ProvisioningRequest request : pspOptions.getRequests()) {
          request.setRequestID(line.getOptionValue(this.getOpt()));
        }
      }
    },

    /** SPML returnData data */
    returnData {

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("returnData", "Return data.");
        return option;
      }

      /**
       * {@inheritDoc}
       */
      public void handle(PSPOptions pspOptions, CommandLine line) {
        for (ProvisioningRequest request : pspOptions.getRequests()) {
          request.setReturnData(ReturnData.DATA);
        }
      }
    },

    /** SPML returnData data */
    returnEverything {

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("returnEverything", "Return everything.");
        return option;
      }

      /**
       * {@inheritDoc}
       */
      public void handle(PSPOptions pspOptions, CommandLine line) {
        for (ProvisioningRequest request : pspOptions.getRequests()) {
          request.setReturnData(ReturnData.EVERYTHING);
        }
      }
    },
    /** SPML returnData data */
    returnIdentifier {

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("returnIdentifier", "Return identifier.");
        return option;
      }

      /**
       * {@inheritDoc}
       */
      public void handle(PSPOptions pspOptions, CommandLine line) {
        for (ProvisioningRequest request : pspOptions.getRequests()) {
          request.setReturnData(ReturnData.IDENTIFIER);
        }
      }
    },

    /** SPML target IDs */
    targetID {

      /**
       * {@inheritDoc}
       */
      public Option getOption() {
        Option option = new Option("targetID", true, "Target ID.");
        option.setArgName("id");
        return option;
      }

      /**
       * {@inheritDoc}
       */
      public void handle(PSPOptions pspOptions, CommandLine line) {
        String targetId = line.getOptionValue(this.getOpt());
        for (ProvisioningRequest request : pspOptions.getRequests()) {
          if (request.getSchemaEntities().isEmpty()) {
            request.addSchemaEntity(new SchemaEntityRef());
          }
          if (request.getSchemaEntities().size() > 1) {
            throw new IllegalArgumentException("Only one schema entity is supported.");
          }
          request.getSchemaEntities().get(0).setTargetID(targetId);
        }
      }
    };

    /**
     * Return the <code>Option</code> name via getOption().getOpt().
     * 
     * @return the name of the option
     */
    public String getOpt() {
      return getOption().getOpt();
    }

    /**
     * Get the <code>Option</code>
     * 
     * @return the <code>Option</code>
     */
    public abstract Option getOption();

    /**
     * Handle the cli arguments. For example, set the bean parameters matching the cli
     * args.
     * 
     * @param pspOptions
     *          the <code>PSPOptions</code>
     * @param line
     *          the <code>CommandLine<code>
     */
    public abstract void handle(PSPOptions pspOptions, CommandLine line);
  }

  /** Program name. */
  public static final String NAME = "ldappc-ng";

  /** Command line arguments. */
  private String[] args;

  /** The bean name for the <code>PSP<code>, probably "ldappc". */
  private String beanName;

  /** The location where configuration files are found. */
  private String confDir;

  /** The interval in seconds between polling actions. */
  private int interval = 0;

  /** The lastModifyTime. */
  private Date lastModifyTime;

  /** Whether or not to log SPML requests and responses */
  private boolean logSpml;

  /** Commons-cli options. */
  private Options options;

  /** Path to the output file. */
  private String outputFile;

  /** Whether or not to print the SPML requests as well as the responses. */
  private boolean printRequests;

  /** The cli args represented as SPML requests. */
  private List<ProvisioningRequest> requests;

  /**
   * Constructor
   * 
   * @param args
   *          command line arguments
   */
  public PSPOptions(String[] args) {
    this.args = args;
    initOptions();
  }

  /**
   * @return Returns the beanName.
   */
  public String getBeanName() {
    return beanName;
  }

  /**
   * @return Returns the confDir.
   */
  public String getConfDir() {
    return confDir;
  }

  /**
   * @return Returns the interval.
   */
  public int getInterval() {
    return interval;
  }

  /**
   * @return Returns the lastModifyTime.
   */
  public Date getLastModifyTime() {
    return lastModifyTime;
  }

  /**
   * @return Returns the outputFile.
   */
  public String getOutputFile() {
    return outputFile;
  }

  /**
   * Returns the result of parsing the command line arguments in the form of
   * <code>ProvisioningRequest</code>s ready to be executed by the <code>PSP</code>.
   * 
   * @return the <code>ProvisioningRequest</code>s
   */
  public List<ProvisioningRequest> getRequests() {
    return requests;
  }

  /**
   * Construct the <code>Options</code>.
   */
  private void initOptions() {

    options = new Options();

    OptionGroup requestOp = new OptionGroup();
    requestOp.setRequired(true);
    requestOp.addOption(Mode.calc.getOption());
    requestOp.addOption(Mode.diff.getOption());
    requestOp.addOption(Mode.sync.getOption());
    requestOp.addOption(Mode.bulkCalc.getOption());
    requestOp.addOption(Mode.bulkDiff.getOption());
    requestOp.addOption(Mode.bulkSync.getOption());
    options.addOptionGroup(requestOp);

    OptionGroup returnData = new OptionGroup();
    returnData.addOption(Opts.returnData.getOption());
    returnData.addOption(Opts.returnEverything.getOption());
    returnData.addOption(Opts.returnIdentifier.getOption());
    returnData.setRequired(false);
    options.addOptionGroup(returnData);

    options.addOption(Opts.conf.getOption());
    options.addOption(Opts.entityName.getOption());
    options.addOption(Opts.interval.getOption());
    options.addOption(Opts.lastModifyTime.getOption());
    options.addOption(Opts.logSpml.getOption());
    options.addOption(Opts.output.getOption());
    options.addOption(Opts.requestID.getOption());
    options.addOption(Opts.printRequests.getOption());
    options.addOption(Opts.targetID.getOption());
  }

  /**
   * Whether or not the <code>PSP</code> should log SPML request and responses.
   * 
   * @return <code>boolean</code>
   */
  public boolean isLogSpml() {
    return logSpml;
  }

  /**
   * Whether or not to print SPML requests as well as responses.
   * 
   * @return <code>boolean</code>
   */
  public boolean isPrintRequests() {
    return printRequests;
  }

  /**
   * Process cli args.
   * 
   * @throws ParseException
   *           if an error occurs parsing the args
   */
  public void parseCommandLineOptions() throws ParseException {

    CommandLineParser parser = new GnuParser();

    CommandLine line = parser.parse(options, args);

    requests = new ArrayList<ProvisioningRequest>();

    for (Mode mode : Mode.values()) {
      if (line.hasOption(mode.getOpt())) {
        requests.addAll(mode.getRequests(line));
      }
    }

    for (Opts opts : Opts.values()) {
      if (line.hasOption(opts.getOpt())) {
        opts.handle(this, line);
      }
    }
  }

  /**
   * Print usage to stdout.
   */
  public void printUsage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(NAME, options, true);
  }

  /**
   * @param beanName
   *          The beanName to set.
   */
  public void setBeanName(String beanName) {
    this.beanName = beanName;
  }

  /**
   * @param confDir
   *          The confDir to set.
   */
  public void setConfDir(String confDir) {
    this.confDir = confDir;
  }

  /**
   * @param interval
   *          The interval to set.
   */
  public void setInterval(int interval) {
    this.interval = interval;
  }

  /**
   * @param lastModifyTime
   *          The lastModifyTime to set.
   */
  public void setLastModifyTime(Date lastModifyTime) {
    this.lastModifyTime = lastModifyTime;
  }

  /**
   * Whether or not the <code>PSP</code> should log SPML request and responses.
   * 
   * @param logSpml
   *          <code>boolean</code>
   */
  public void setLogSpml(boolean logSpml) {
    this.logSpml = logSpml;
  }

  /**
   * @param outputFile
   *          The outputFile to set.
   */
  public void setOutputFile(String outputFile) {
    this.outputFile = outputFile;
  }

  /**
   * Set whether or not to print SPML requests as well as response.
   * 
   * @param <code>boolean</code>
   */
  public void setPrintRequests(boolean printRequests) {
    this.printRequests = printRequests;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.append("args", args);
    toStringBuilder.append("beanName", beanName);
    toStringBuilder.append("confDir", confDir);
    toStringBuilder.append("interval", interval);
    toStringBuilder.append("logSpml", logSpml);
    toStringBuilder.append("outputFile", outputFile);
    toStringBuilder.append("printRequests", printRequests);
    return toStringBuilder.toString();
  }

}
