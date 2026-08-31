---
title: "Grouper Training - AI - Lesson: Calling AI web service"
space: Grouper
pageId: 28545192
version: 10
lastUpdated: 2025-12-08T17:17:32.235Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545192/Grouper+Training+-+AI+-+Lesson+Calling+AI+web+service
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

1. Go to Miscellaneous → Daemon Jobs → Filter for: OTHER_JOB_dataProviderHR → Job actions → Run job now
2. Review the ABAC data fields
3. Make a skeleton GSH template to build ABAC scripts using AI
  
  1. Miscellaneous → Configure → Configuration files → Import → Copy/paste → grouper.properties (input below text)   
      
    
    ```
    grouperGshTemplate.gteAbacAi.displayErrorOutput = true
    grouperGshTemplate.gteAbacAi.gshTemplate = //
    grouperGshTemplate.gteAbacAi.input.0.description = You can ask for roles (faculty, staff, work_study, affiliate), departments, and orgs
    grouperGshTemplate.gteAbacAi.input.0.label = Prompt for AI
    grouperGshTemplate.gteAbacAi.input.0.maxLength = 4000
    grouperGshTemplate.gteAbacAi.input.0.name = gsh_input_prompt
    grouperGshTemplate.gteAbacAi.input.0.required = true
    grouperGshTemplate.gteAbacAi.input.0.validationType = none
    grouperGshTemplate.gteAbacAi.numberOfInputs = 1
    grouperGshTemplate.gteAbacAi.runAsType = GrouperSystem
    grouperGshTemplate.gteAbacAi.securityRunType = wheel
    grouperGshTemplate.gteAbacAi.templateDescription = Let AI write an ABAC script for you
    grouperGshTemplate.gteAbacAi.templateName = GTE ABAC AI
    grouperGshTemplate.gteAbacAi.templateType = abac
    grouperGshTemplate.gteAbacAi.templateVersion = V2
    ```
  2. Review GSH template... we need to write a script to call an OpenAI web service (will be done later)
4. Go to OpenAI playground (can google it): [https://platform.openai.com/playground](https://platform.openai.com/playground)
5. Click on Assistants
6. + Create
  
  1. Name  
      
    
    ```
    Grouper GTE ABAC
    ```
  2. Instructions  
      
    
    ```
    Write an Internet2 Grouper ABAC script for a user.
    
    For instance, if a user sends the message: 
    
    abac script for staff in the art museum org
    
    Then the script is:
    
    entity.hasRow('hr_positions', "role == 'staff' && org_code == 'AMU' ")
    
    If the user asks for: 
    
    fac or staff from alumni relations
    
    Then the script is: 
    
    entity.hasRow('hr_positions', " ( role == 'faculty' || role == 'staff' ) && dept_code == 'ALMR' ")
    
    could also use this operator: 
    
    entity.hasRow('hr_positions', " role =~ [ 'faculty', 'staff' ] && dept_code == 'ALMR' ")
    
    Do not hallucinate more rows or attributes, if you do not know the answer, tell the user it is not possible or to read the Entity Data Field Data Dictionary: https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2EntityDataFields.viewDataFieldAndRowDictionary
    
    When checking equality of an attribute always use == and never use =
    
    Do not have scripts that are too long.  You can have a line break before an && or an || for example.
    
    The possible attributes for hr_positions are: role, dept_code, org_code
    
    These are the role values (must be one of these): faculty, staff, work_study, affiliate.  
    work_study means a student with a work study job.
    
    These are the departments (dept_code: description): 
    AAAS: African and African American Studies
    ALMR: Alumni Relations
    AMAT: Applied Mathematics
    AMUS: Art Museum
    ANTH: Anthropology
    APCS: Applied Computation
    APHY: Applied Physics
    ARCH: Architecture, Landscape Arch, and Urban Planning
    ART: Art
    AS: American Studies
    ASTR: Astronomy
    ATHL: Athletics
    BF: Budget & Finance
    BIOL: Biological Sciences
    BIOS: Biostatistics
    BPHY: Biophysics
    BT: Board of Trustees
    BUS: School of Business
    CAS: College of Arts and Sciences
    CELT: Celtic Languages and Literatures
    CFR: Corporate & Foundational Relations
    CHEM: Chemistry
    CLSS: Classics
    CNSL: Counseling Center
    COMM: Communications
    CPLT: Comparative Literature
    CS: Computer Science
    CSL: Center for Spiritual Life
    CTRS: Centers & Institutes
    DCOM: Digital Communications
    DCS: Department of Community Services
    DEI: Diversity Equity & Inclusion
    DR: Donor Relations
    DS: Dean of Students
    DSA: Department of Student Activities
    EA: Enterprise Applications
    EALC: East Asian Languages and Civilizations
    ECOM: Economics
    ENG: Engineering
    ENGL: English
    ENV: Environmental Science
    FA: Finance & Administration
    GC: General Counsel
    GERM: Germanic Languages and Literatures
    GHP: Global Health Policy
    GS: Graduate School of Arts and Sciences
    GSS: Gender and Sexuality Studies
    GVMT: Government
    HC: Health Center
    HIST: History
    HR: Human Resources
    HUM: Humanities
    IADV: Institutional Advancement
    IAM: Identity and Access Management
    ICFE: International Center for Ethics
    IMED: Integrated Media
    INFR: Infrastructure
    INTC: Intercultural Center
    INTD: Integrated Design
    INVE: Investment
    ISSR: Institute for Social Science Research
    ITS: Information Technology Services
    LDAG: Leadership Annual Giving
    LIB: Library
    LING: Linguistics
    LIT: Literature
    MAT: Mathematics
    MCB: Molecular and Cellular Biology
    MEDS: Medieval Studies
    MES: Middle Eastern Studies
    MPG: Major & Planned Giving
    MUS: Music
    NELC: Near Eastern Languages and Civilizations
    NET: Networking
    OEO: Office of Equal Opportunity
    OIR: Office of Institutional Research
    OMBU: University Ombuds
    OPS: Campus Operations
    PARC: Prevention Advocacy & Resource Center
    PHIL: Philosophy
    PHY: Physics
    POLI: Political Science
    PRES: President
    PROV: Provost
    PSCI: Planetary Sciences
    PSYC: Psychology
    RA: Office of Research Administration
    RC: Research Computing
    RCWS: Research Center for Women's Studies
    REL: Religion
    ROML: Romance Languages and Literatures
    SA: Software Acquisitions
    SAF: Student Affairs
    SAS: South Asian Studies
    SCOM: Strategic Communications
    SCS: School of Continuing Studies
    SEC: Security
    SLAV: Slavic Languages and Literatures
    SOC: Sociology
    SOCS: Social Studies
    SPM: School for Social Policy and Management
    SRCS: Student Rights & Community Standards
    STAT: Statistics
    THEA: Theater
    TP: Technology and Planning
    USE: User Support
    VPR: Office of the Vice Provost of Research
    WRI: Creative Writing
    
    These are the orgs (org_code: description):
    AMU: Art Museum
    AS: College of Arts and Sciences
    BT: Board of Trustees
    CIS: Centers & Institutes
    CSTU: School of Continuing Studies
    Comm: Communications
    DEIS: Diversity Equity & Inclusion
    FA: Finance & Administration
    GC: General Counsel
    GS: Graduate School of Arts and Sciences
    IA: Institutional Advancement
    IRA: Office of Institutional Research
    ITS: Information Technology Services
    LIB: Library
    ORA: Office of Research Administration
    PRES: President
    PROV: Provost
    SA: Student Affairs
    SB: School of Business
    SPM: School for Social Policy and Management
    VPR: Office of the Vice Provost of Research
    
    
    ```
  3. Model: gpt-4.1-nano
  4. No tools, temperature and top-p = 0.2
  5. Click "Edit", and try it out with  
      
    
    ```
    i want affiliates in the art department or astronomy department.  union that set of people with staff in the provost organization
    ```
  6. It should respond with something like:  
      
    
    ```
    entity.hasRow('hr_positions', " ( role == 'affiliate' && ( dept_code == 'ART' || dept_code == 'ASTR' ) ) || ( role == 'staff' && org_code == 'PROV' ) ")
    ```
7. Create a group: testAbac in the test folder
  
  1. Group actions → Loader
  2. Loader actions → Edit loader configuration
  3. Yes has loader
  4. Scripted group
  5. Paste script from openai to the jexl script field
  6. Analyze, should have 8 results
  7. Save
8. Click on API keys on left of Openai Playground
  
  1. Name: GTE
  2. Create Secret Key
  3. Save the secret key in a text file somewhere
9. Create external system in Grouper to point to that WS endpoint
  
  1. Go to Grouper tab
  2. Miscellaneous → External Systems
  3. Actions → Add
  4. Config ID:  
      
    
    ```
    openAiGte
    ```
  5. Type: web service
  6. Endpoint URL:  
      
    
    ```
    https://api.openai.com
    ```
  7. Secret: paste secret from openAI
  8. Test URL suffix  
      
    
    ```
    /v1/assistants
    ```
  9. Test response code  
      
    
    ```
    400
    ```
  10. Test response body regex:  
      
    
    ```
    .*OpenAI-Beta.*
    ```
  11. Save and test the external system
10. Get the assistant ID
  
  1. Click on assistants in openai playground and copy and keep the assistant ID, looks like this: asst_N3habc123abc123abc123
11. Go to another tab to [the Internet2 GSH agent](https://chatgpt.com/g/g-68488c7d982881918b33d1eed434991c-internet2-grouper-gsh-agent)
  
  1. Prompt  
      
    
    ```
    open a canvas with empty GSH template
    ```
  2. If it is not in canvas mode, click the "edit" button in the upperish rightish
  3. Type on left each in succession and craft the GSH template  
      
    
    ```
    take in an input named: gsh_input_prompt
    ```
    
      
    
    ```
    make a variable for the AI external system id: openAiGte
    ```
    
      
    
    ```
    get a thread id from openai for that external system
    ```
    
      
    
    ```
    send the prompt to the openai thread
    ```
    
      
    Use the assistant ID copied from step above (or go get it from the openai assistants page)  
      
    
    ```
    make a variable for openai assistant id: asst_N3hE3abc123abc123abc123abc123
    ```
    
      
    
    ```
    run the thread on the assistant
    ```
    
      
    
    ```
    loop and check the thread until the openai status is success. 
    each loop should sleep for a second. if its not done in 3 
    minute just throw a descriptive exception
    ```
    
      
    
    ```
    get the response from ai into a variable
    ```
    
      
    
    ```
    assign the ai response to the gsh template output abac script
    ```
    
      
    
    ```
    take out the output lines
    ```
12. Copy the script and try it out. Miscellaneous → GSH templates → Edit the gteAbacAi template, paste into the GSH script textarea, save
13. Configure scripted group
  
  1. Make a group in the test folder named  
      
    
    ```
    testAbacAi
    ```
  2. Group actions → loader
  3. Loader actions → Edit loader configuration
  4. Yes has loader
  5. Source type: Scripted group
  6. Construct script: Pattern
  7. Pattern: GTE ABAC AI
  8. Prompt:  
      
    
    ```
    include faculty in political science department.  also include (with 'or') staff in the org for general counsel
    ```
  9. Should make a script that might looks something similar to this (with 14 people)  
      
    
    ```
    entity.hasRow('hr_positions', " role == 'faculty' && dept_code == 'POLI' ") 
    or entity.hasRow('hr_positions', " role == 'staff' && org_code == 'GC' ")
    ```
  10. If you got an error, edit the scripted group script with this (but you MUST put in your assistant ID in the script)  
      
    
    ```
    // Empty GSH template skeleton
    // Customize this class with your own logic and input variables.
    
    import java.sql.Timestamp;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.*;
    
    import org.apache.commons.lang3.*;
    import org.apache.commons.logging.Log;
    
    import com.fasterxml.jackson.databind.*;
    import com.fasterxml.jackson.databind.node.*;
    
    import edu.internet2.middleware.grouper.*;
    import edu.internet2.middleware.grouper.CompositeSave;
    import edu.internet2.middleware.grouper.ai.openai.*;
    import edu.internet2.middleware.grouper.app.attestation.*;
    import edu.internet2.middleware.grouper.app.externalSystem.*;
    import edu.internet2.middleware.grouper.app.grouperTypes.*;
    import edu.internet2.middleware.grouper.app.gsh.template.*;
    import edu.internet2.middleware.grouper.app.loader.*;
    import edu.internet2.middleware.grouper.app.provisioning.*;
    import edu.internet2.middleware.grouper.attr.finder.*;
    import edu.internet2.middleware.grouper.attr.value.*;
    import edu.internet2.middleware.grouper.attr.*;
    import edu.internet2.middleware.grouper.attr.assign.*;
    import edu.internet2.middleware.grouper.authentication.*;
    import edu.internet2.middleware.grouper.exception.*;
    import edu.internet2.middleware.grouper.group.*;
    import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
    import edu.internet2.middleware.grouper.ldap.*;
    import edu.internet2.middleware.grouper.membership.*;
    import edu.internet2.middleware.grouper.misc.*;
    import edu.internet2.middleware.grouper.util.*;
    import edu.internet2.middleware.grouperClient.jdbc.*;
    import edu.internet2.middleware.grouperClient.jdbc.tableSync.*;
    import edu.internet2.middleware.subject.*;
    import edu.internet2.middleware.grouper.privs.*;
    
    // Simple empty template: extend GshTemplateV2 so it can be registered and run
    public class Test145gteAbacAi extends GshTemplateV2 {
    
      /** logger (always use GshTemplateV2.class so Groovy class names don't matter) */
      private static final Log LOG = GrouperUtil.getLog(GshTemplateV2.class);
    
      @Override
      public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
          GshTemplateV2output gshTemplateV2output) {
    
        // Built‑in output object used for validations, messages, redirects, etc.
        GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();
    
        // AI external system config id (must match the configured external system id)
        String aiExternalSystemConfigId = "openAiGte";
    
        // OpenAI Assistant id
        String aiAssistantId = "asst_N3hE3Nopatm8DDueeLEGMLlY";
    
        // 1) Read input variables here (must be named gsh_input_*)
        String gsh_input_prompt = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_prompt");
    
        // 2) Do validations *before* doing any work.
        if (StringUtils.isBlank(gsh_input_prompt)) {
          gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_prompt", "Prompt is required");
        }
    
        //    Additional validations can go here. *before* doing any work.
        //    Example:
        //    if (StringUtils.isBlank(gsh_input_groupName)) {
        //      gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_groupName", "Group name is required");
        //    }
    
        // If there are validation problems, mark as error and return before doing work.
        if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
          gsh_builtin_gshTemplateOutput.assignIsError(true);
          return;
        }
    
        // 3) Perform template logic here.
        //    Retrieve (or create) an OpenAI thread id for the configured external system.
    
        String aiThreadId = GrouperOpenAiApiCommands.retrieveOpenAiThreadId(aiExternalSystemConfigId);
    
        // Send the user prompt into the OpenAI thread.
        GrouperOpenAiApiCommands.sendMessageToOpenAi(aiExternalSystemConfigId, aiThreadId, gsh_input_prompt);
    
        // Run the thread on the configured assistant and get the run id.
        String aiRunId = GrouperOpenAiApiCommands.runThreadOnAssistant(aiExternalSystemConfigId, aiThreadId, aiAssistantId);
    
        // Poll the thread status until the run is finished or until 3 minutes have passed.
        int maxWaitSeconds = 180;
        boolean aiRunFinished = false;
        int aiWaitCount = 0;
    
        while (!aiRunFinished && aiWaitCount < maxWaitSeconds) {
          boolean currentStatus = GrouperOpenAiApiCommands.retrieveThreadStatus(aiExternalSystemConfigId, aiThreadId, aiRunId);
          if (currentStatus) {
            aiRunFinished = true;
            break;
          }
          // Sleep 1 second between status checks.
          GrouperUtil.sleep(1000);
          aiWaitCount = aiWaitCount + 1;
        }
    
        if (!aiRunFinished) {
          throw new RuntimeException("OpenAI run did not complete within 3 minutes (threadId=" + aiThreadId + ", runId=" + aiRunId + ").");
        }
    
        // Retrieve the AI response text from the thread into a variable.
        String aiResponseText = GrouperOpenAiApiCommands.retrieveFirstMessageFromThread(aiExternalSystemConfigId, aiThreadId);
    
        // Optionally show the AI response to the user.
    
        // Assign the AI response as the ABAC script so the loader can use it.
        gsh_builtin_gshTemplateOutput.assignAbacScript(aiResponseText);
    
        // 4) Optionally add output lines to show results to the user.
        //    Example: show the thread id so it can be reused or inspected.
        //    Example:
        //    gsh_builtin_gshTemplateOutput.addOutputLine("Success: template completed.");
        //    Example:
        //    gsh_builtin_gshTemplateOutput.addOutputLine("Success: template completed.");
    
        // 5) Optionally control redirect behavior at the end.
        //    To stay on the same screen (no redirect), uncomment:
        //    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");
    
        //    To redirect to a specific group, you can do something like:
        //    String groupName = "test:testGroup";
        //    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("operation=UiV2Group.viewGroup&groupName=" + groupName);
    
      }
    }
    ```
  11. sdf
