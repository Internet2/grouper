---
title: "Grouper Training - AI - Lesson: Trained model"
space: Grouper
pageId: 28544815
version: 4
lastUpdated: 2025-12-07T21:04:27.211Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544815/Grouper+Training+-+AI+-+Lesson+Trained+model
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson**

1. Make a fine tuned trained model
  
  1. Go to ChatGPT (base model 5.1) and enter this prompt  
      
    
    ```
    Make 10 jsonl openAI fine tuning model training JSON based on this
    
    Example question (dont use this one)
    Question: abac script for staff in the art museum org
    Answer: entity.hasRow('hr_positions', "role == 'staff' && org_code == 'AMU' ")
    
    Example question (dont use this one)
    Question: fac or staff from alumni relations
    Answer: entity.hasRow('hr_positions', " ( role == 'faculty' || role == 'staff' ) && dept_code == 'ALMR' ")
    
    These would be two lines of the JSONL file but come up with ten more not including these
    
    
    {"messages":[{"role":"user","content":"abac script for staff in the art museum org"},{"role":"assistant","content":"entity.hasRow('hr_positions', \"role == 'staff' && org_code == 'AMU' \")"}]}
    {"messages":[{"role":"user","content":"fac or staff from alumni relations"},{"role":"assistant","content":"entity.hasRow('hr_positions', \" ( role == 'faculty' || role == 'staff' ) && dept_code == 'ALMR' \")"}]}
    
    
    Do not hallucinate more rows or attributes, if you do not know the answer, tell the user it is not possible or to read the Entity Data Field Data Dictionary: https://localhost:8443/grouper/grouperUi/app/UiV2Main.index?operation=UiV2EntityDataFields.viewDataFieldAndRowDictionary
    
    When checking equality of an attribute always use == and never use =
    
    Do not have scripts that are too long. You can have a line break before an && or an || for example.
    
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
    ```
  2. If it doesnt prompt you to download a file, enter this prompt  
      
    
    ```
    give me a valid JSONL file to download preferably named: gteAiAbacTraining.jsonl
    ```
  3. Make a gteAiAbacTraining.jsonl file
  4. Go to [openai playground](https://platform.openai.com/playground)
  5. Click on "fine tuning"
  6. Create
  7. Supervised
  8. Base model 4.1 nano
  9. Suffix:   
      
    
    ```
    gte-ai-abac
    ```
  10. Upload the jsonl file above
  11. Create
2. Look at billing and see where you are at. Click on "Usage"
3. Edit the assistant to use the trained model
  
  1. Assistants → Assistant → Model → Pick the gte-ai-abac on at bottom
4. Try out the abac prompt again
  
  1. Create a group in your GTE ([https://localhost:8443/grouper](https://localhost:8443/grouper)): testAbacAi2 in the test folder
  2. Group actions → Loader
  3. Loader actions → Edit loader configuration
  4. Yes has loader
  5. Source type: Scripted group
  6. Construct script: Pattern
  7. Pattern: GTE ABAC AI
  8. Prompt:  
      
    
    ```
    include faculty in political science department. also include staff in the org for general counsel
    ```
  9. Analyze resulting script (e.g.)  
      
    
    ```
    entity.hasRow('hr_positions', "role == 'faculty' && dept_code == 'POLI'") 
    || entity.hasRow('hr_positions', "role == 'staff' && org_code == 'GC'")
    ```
  10. Should have 14 people (wait a minute for incremental ABAC JEXL daemon to run)
5. Check usage, see how much that cost ([https://platform.openai.com/settings](https://platform.openai.com/settings) → Billing)
