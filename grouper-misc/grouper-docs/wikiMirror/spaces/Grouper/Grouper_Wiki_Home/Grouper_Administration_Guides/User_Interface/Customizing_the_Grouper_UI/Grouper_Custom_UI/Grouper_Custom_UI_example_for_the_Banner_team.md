---
title: "Grouper Custom UI example for the Banner team"
space: Grouper
pageId: 28554867
version: 4
lastUpdated: 2020-09-02T21:15:01.609Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554867/Grouper+Custom+UI+example+for+the+Banner+team
---

The intent of this application is:

1. Error page for the non-prod banner SP to help people troubleshoot their access
2. Administrative tool to analyze a team member's access
3. Self-service access analysis

## Screenshots for various situations

Can use banner  
[https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=true&cu_bannerNonprodEligible=true&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=true&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=true&cu_twoStepUsers=true&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=](https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=true&cu_bannerNonprodEligible=true&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=true&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=true&cu_twoStepUsers=true&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=)

Pending access to team  
[https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=true&cu_bannerNonprodEligible=true&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=true&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=true&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=](https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=true&cu_bannerNonprodEligible=true&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=true&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=true&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=)

Pending access to banner  
[https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=true&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=true&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=true&cu_twoStepUsers=true&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=](https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=true&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=true&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=true&cu_twoStepUsers=true&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=)

Needs FERPA  
[https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=true&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=false&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/06/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=](https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=true&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=false&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/06/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=)

Not employee  
[https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=true&cu_bannerNonprodActiveEmployee=false&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodActiveEmployeeEndDate=2020/06/07%2011:23&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=](https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=true&cu_bannerNonprodActiveEmployee=false&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodActiveEmployeeEndDate=2020/06/07%2011:23&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=)

Not in two step  
[https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=false&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodActiveEmployeeEndDate=2020/11/07%2011:23&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=](https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=false&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodActiveEmployeeEndDate=2020/11/07%2011:23&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=)

Needs security training  
[https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=false&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=false&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=](https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=false&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=false&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=)

Needs confluence  
[https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=false&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=false&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=](https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=false&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=false&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=)

Needs vpn  
[https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=false&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=false&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=](https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=false&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=false&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=true&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=)

Needs box  
[https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=false&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=false&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=](https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2CustomUi.customUiGroup&groupId=efbf8445b85946b997c85a40dd4a6a72&cu_bannerNonprodAllowedToManage=false&cu_grouperTurnOffManager=true&cu_bannerNonprodEligible30min=false&cu_bannerNonprodEligible=false&cu_bannerNonprodReprieved=false&cu_bannerNonprodReprieveEnd=&cu_bannerNonprodReprieveAnalysis=&cu_bannerNonprodReprieveLastAudit=&cu_bannerNonprodInTeamAll=false&cu_bannerNonprodInTeamAllAnalysis=&cu_bannerNonprodInTeamAll30min=false&cu_twoStepUsers=false&cu_bannerNonprodActiveEmployee=true&cu_bannerNonprodActiveEmployeeAnalysis=&cu_bannerNonprodFerpa=true&cu_bannerNonprodFerpaAnalysis=&cu_bannerNonprodFerpaEndDate=2020/10/21%2007:45&cu_bannerNonprodSecurityTraining=true&cu_bannerNonprodSecurityTrainingAnalysis=&cu_bannerNonprodIscContractor=false&cu_bannerNonprodClient=true&cu_bannerNonprodIscStaff=false&cu_bannerNonprodTeamAnalysis=&cu_bannerNonprodConfluence=true&cu_bannerNonprodConfluenceAnalysis=&cu_bannerNonprodJira=true&cu_bannerNonprodJiraAnalysis=&cu_bannerNonprodBannerVpn=true&cu_bannerNonprodBannerVpn30min=true&cu_bannerNonprodBannerVpnAnalysis=&cu_bannerNonprodEmail=true&cu_bannerNonprodEmail30min=true&cu_bannerNonprodEmailAnalysis=&cu_bannerNonprodBox=false&cu_bannerNonprodBox30min=true&cu_bannerNonprodBoxAnalysis=)

## Admin interface

## Configuration of variables

```
{
   "variableToAssign":"cu_bannerNonprodPersonDescription",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"pennCommunity",
   "bindVar0type":"string",
   "query":"select description from PCDADMIN.computed_person cp where cp.char_PENN_ID = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodPersonDescription']}",
   "order":10
}
{
   "variableToAssign":"cu_bannerNonprodEligible30min",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"boolean",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select case when is_user_for_30_min = 'T' then 1 else 0 end as the_result from penn_custom_ui_mships_v where group_name in ('penn:isc:ait:apps:ngss:banner:nonprod:ngssBannerNonprodCanLogin' ) and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodEligible30min']}",
   "order":20
}
{
   "variableToAssign":"cu_bannerNonprodEligible",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:ngss:banner:nonprod:ngssBannerNonprodCanLogin",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodEligible']}",
   "order":30
}
{
   "variableToAssign":"cu_bannerNonprodReprieved",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:ngss:knowledgeLink:ngssTeamMembersExemptedFromTraining",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodReprieved']}",
   "order":40
}
{
   "variableToAssign":"cu_bannerNonprodReprieveEnd",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"String",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select disabled_time from penn_imm_mship_disabled_date_v where group_name = 'penn:isc:ait:apps:ngss:knowledgeLink:ngssTeamMembersExemptedFromTraining' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodReprieveEnd']}",
   "order":50
}
{
   "variableToAssign":"cu_bannerNonprodReprieveAnalysis",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:knowledgeLink:ngssTeamMembersExemptedFromTraining' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodReprieveAnalysis']}",
   "order":55
}
{
   "variableToAssign":"cu_bannerNonprodReprieveLastAudit",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select 'The last reprieve was assigned by ' || user_using_grouper_name || ' on ' || to_char(the_timestamp, 'YYYY/MM/DD HH24:MI') as reprieve_audit from penn_audit_add_membership_help_v paamv1 where group_name = 'penn:isc:ait:apps:ngss:knowledgeLink:ngssTeamMembersExemptedFromTraining' and user_acted_on_subject_id = ? and user_acted_on_subject_source_id = 'pennperson' and paamv1.the_timestamp = (select max(paamv2.the_timestamp) from penn_audit_add_membership_help_v paamv2 where paamv1.group_name = paamv2.group_name and paamv1.user_acted_on_member_id = paamv2.user_acted_on_member_id) limit 1",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodReprieveLastAudit']}",
   "order":57
}
{
   "variableToAssign":"cu_bannerNonprodInTeamAll",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:ngss:team:ngssTeamAll",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodInTeamAll']}",
   "order":60
}
{
   "variableToAssign":"cu_bannerNonprodInTeamAllAnalysis",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:team:ngssTeamAll' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodInTeamAllAnalysis']}",
   "order":65
}
{
   "variableToAssign":"cu_bannerNonprodInTeamAll30min",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"boolean",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select case when is_user_for_30_min = 'T' then 1 else 0 end as the_result from penn_custom_ui_mships_v where group_name in ('penn:isc:ait:apps:ngss:team:ngssTeamAll' ) and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodInTeamAll30min']}",
   "order":66
}
{
   "variableToAssign":"cu_twoStepUsers",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:community:authentication:twoStepUsers",
   "label":"${textContainer.text['penn_o365twoStep_cu_twoStepUsers']}",
   "order":70
}
{
   "variableToAssign":"cu_bannerNonprodActiveEmployee",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:community:employeeOrContractor",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodActiveEmployee']}",
   "order":77
}
{
   "variableToAssign":"cu_bannerNonprodActiveEmployeeAnalysis",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:community:employeeOrContractor' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodActiveEmployeeAnalysis']}",
   "order":80
}
{
   "variableToAssign":"cu_bannerNonprodActiveEmployeeEndDate",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"pennCommunity",
   "bindVar0type":"string",
   "query":"select expire_message from employee_affil_expire_date_v where penn_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodActiveEmployeeEndDate']}",
   "order":87
}
{
   "variableToAssign":"cu_bannerNonprodFerpa",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:knowledgeLink:completedWhenRequired:UP_87024_ITEM_FERPA001",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodFerpa']}",
   "order":90
}
{
   "variableToAssign":"cu_bannerNonprodFerpaAnalysis",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:knowledgeLink:completedWhenRequired:UP_87024_ITEM_FERPA001' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodFerpaAnalysis']}",
   "order":100
}
{
   "variableToAssign":"cu_bannerNonprodFerpaEndDate",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"warehouse",
   "bindVar0type":"string",
   "query":"select to_char(max(end_date), 'YYYY/MM/DD HH24:MI') as the_end_date from AUTHZ_KL_COURSEYR_EXPIRE_V where penn_id = ? and item_id = 'UP.87024.ITEM.FERPA001'",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodFerpaEndDate']}",
   "order":105
}
{
   "variableToAssign":"cu_bannerNonprodSecurityTraining",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:knowledgeLink:completed:UP_91028_ITEM_InfoSecEssGen",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodSecurityTraining']}",
   "order":110
}
{
   "variableToAssign":"cu_bannerNonprodSecurityTrainingAnalysis",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:knowledgeLink:completed:UP_91028_ITEM_InfoSecEssGen' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodSecurityTrainingAnalysis']}",
   "order":120
}
{
   "variableToAssign":"cu_bannerNonprodIscContractor",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:ngss:team:ngssIscContractors",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodIscContractor']}",
   "order":122
}
{
   "variableToAssign":"cu_bannerNonprodClient",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:ngss:team:ngssClients",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodClient']}",
   "order":124
}
{
   "variableToAssign":"cu_bannerNonprodIscStaff",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:ngss:team:ngssIscStaff",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodIscStaff']}",
   "order":126
}
{
   "variableToAssign":"cu_bannerNonprodTeamAnalysis",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:team:ngssTeamAllBeforeCheckingStatus' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodTeamAnalysis']}",
   "order":128
}
{
   "variableToAssign":"cu_bannerNonprodConfluence",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"boolean",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select case when is_user = 'F' then 0 when is_user_for_24_hr = 'T' then 1 when extract(day from now()) > extract(day from user_start_timestamp) and extract(hour from now()) > 6 then 1 when extract(hour from now()) < 6 then 0 when extract(hour from now()) > 6 and extract(hour from user_start_timestamp) < 6 then 1 else 0 end as allowed from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:confluence:ngss_confluence_contributors' and subject_id = ? and subject_source_id = 'pennperson'",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodConfluence']}",
   "order":130
}
{
   "variableToAssign":"cu_bannerNonprodConfluenceAnalysis",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:confluence:ngss_confluence_contributors' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodConfluenceAnalysis']}",
   "order":132
}
{
   "variableToAssign":"cu_bannerNonprodJira",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"boolean",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select case when is_user = 'F' then 0 when is_user_for_24_hr = 'T' then 1 when extract(day from now()) > extract(day from user_start_timestamp) and extract(hour from now()) > 6 then 1 when extract(hour from now()) < 6 then 0 when extract(hour from now()) > 6 and extract(hour from user_start_timestamp) < 6 then 1 else 0 end as allowed from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:jira:ngss_jira_team_members' and subject_id = ? and subject_source_id = 'pennperson'",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodJira']}",
   "order":134
}
{
   "variableToAssign":"cu_bannerNonprodJiraAnalysis",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:jira:ngss_jira_team_members' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodJiraAnalysis']}",
   "order":136
}
{
   "variableToAssign":"cu_bannerNonprodBannerVpn",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:ngss:vpn:vpn_CONR_VPN_Banner_profile",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodBannerVpn']}",
   "order":138
}
{
   "variableToAssign":"cu_bannerNonprodBannerVpn30min",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"boolean",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select case when is_user_for_30_min = 'T' then 1 else 0 end as the_result from penn_custom_ui_mships_v where group_name in ('penn:isc:ait:apps:ngss:vpn:vpn_CONR_VPN_Banner_profile' ) and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodBannerVpn30min']}",
   "order":139
}
{
   "variableToAssign":"cu_bannerNonprodBannerVpnAnalysis",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:vpn:vpn_CONR_VPN_Banner_profile' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodBannerVpnAnalysis']}",
   "order":140
}
{
   "variableToAssign":"cu_bannerNonprodEmail",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:ngss:email:ngssTeamEmailList",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodEmail']}",
   "order":142
}
{
   "variableToAssign":"cu_bannerNonprodEmail30min",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"boolean",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select case when is_user_for_30_min = 'T' then 1 else 0 end as the_result from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:email:ngssTeamEmailList' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodEmail30min']}",
   "order":144
}
{
   "variableToAssign":"cu_bannerNonprodEmailAnalysis",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:ait:apps:ngss:email:ngssTeamEmailList' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodEmailAnalysis']}",
   "order":146
}
{
   "variableToAssign":"cu_bannerNonprodBox",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:nandt:apps:pennbox:boxgroups:group_isc_ngss",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodBox']}",
   "order":148
}
{
   "variableToAssign":"cu_bannerNonprodBox30min",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"boolean",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select case when is_user_for_30_min = 'T' then 1 else 0 end as the_result from penn_custom_ui_mships_v where group_name = 'penn:isc:nandt:apps:pennbox:boxgroups:group_isc_ngss' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodBox30min']}",
   "order":150
}
{
   "variableToAssign":"cu_bannerNonprodBoxAnalysis",
   "bindVar0":"${subject.id}",
   "userQueryType":"sql",
   "variableType":"string",
   "configId":"grouper",
   "bindVar0type":"string",
   "query":"select mship_desc from penn_custom_ui_mships_v where group_name = 'penn:isc:nandt:apps:pennbox:boxgroups:group_isc_ngss' and subject_source_id = 'pennperson' and subject_id = ?",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodBoxAnalysis']}",
   "order":152
}
{
   "variableToAssign":"cu_bannerNonprodAllowedToManage",
   "fieldNames":"members",
   "userQueryType":"grouper",
   "variableType":"boolean",
   "groupName":"penn:isc:ait:apps:ngss:banner:nonprod:nonprodBannerAdmin",
   "label":"${textContainer.text['penn_bannerNonprod_cu_bannerNonprodAllowedToManage']}",
   "order":160,
   "forLoggedInUser":true
}

```

## Text config

```
{
   "endIfMatches":true,
   "customUiTextType":"header",
   "index":10,
   "defaultText":true,
   "text":"${textContainer.text['penn_bannerNonprod_header']}"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":0,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_eligible']}",
   "script":"${ cu_bannerNonprodEligible }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":10,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_ineligible']}",
   "script":"${ ! cu_bannerNonprodEligible }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":20,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_eligible30']}",
   "script":"${ cu_bannerNonprodEligible && cu_bannerNonprodEligible30min }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":30,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_eligibleNot30']}",
   "script":"${ cu_bannerNonprodEligible && !cu_bannerNonprodEligible30min }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":40,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_inTeamAll']}",
   "script":"${ cu_bannerNonprodInTeamAll }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":50,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_notInTeamAll']}",
   "script":"${ ! cu_bannerNonprodInTeamAll }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":60,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_inTeamAll30']}",
   "script":"${ cu_bannerNonprodInTeamAll && cu_bannerNonprodInTeamAll30min }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":70,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_notInTeamAll30']}",
   "script":"${ cu_bannerNonprodInTeamAll && !cu_bannerNonprodInTeamAll30min }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":80,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_reprievedNoEnd']}",
   "script":"${ cu_bannerNonprodReprieved && cu_bannerNonprodReprieveEnd == null }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":90,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_reprievedEnd']}",
   "script":"${ cu_bannerNonprodReprieved && cu_bannerNonprodReprieveEnd != null }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":100,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_twoStep']}",
   "script":"${ cu_twoStepUsers }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":110,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_noTwoStep']}",
   "script":"${ ! cu_twoStepUsers }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":120,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_activeEmployee']}",
   "script":"${ cu_bannerNonprodActiveEmployee }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":130,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_notActiveEmployee']}",
   "script":"${ ! cu_bannerNonprodActiveEmployee }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":140,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_activeEmployeeEndDate']}",
   "script":"${ cu_bannerNonprodActiveEmployeeEndDate != null }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":150,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_ferpa']}",
   "script":"${ cu_bannerNonprodFerpa }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":160,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_noFerpa']}",
   "script":"${ ! cu_bannerNonprodFerpa }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":170,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_ferpaEndDate']}",
   "script":"${ cu_bannerNonprodFerpaEndDate != null }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":173,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_security']}",
   "script":"${ cu_bannerNonprodSecurityTraining }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":174,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_noSecurity']}",
   "script":"${ ! cu_bannerNonprodSecurityTraining }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":180,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_contractor']}",
   "script":"${ cu_bannerNonprodIscContractor }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":190,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_client']}",
   "script":"${ cu_bannerNonprodClient }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":200,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_isc']}",
   "script":"${ cu_bannerNonprodIscStaff }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":202,
   "defaultText":false,
   "text":"${cu_bannerNonprodTeamAnalysis}",
   "script":"${ !cu_bannerNonprodIscContractor && !cu_bannerNonprodClient && !cu_bannerNonprodIscStaff}"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":210,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_confluence']}",
   "script":"${ cu_bannerNonprodConfluence }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":220,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_noConfluence']}",
   "script":"${ ! cu_bannerNonprodConfluence }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":230,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_jira']}",
   "script":"${ cu_bannerNonprodJira }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":240,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_noJira']}",
   "script":"${ ! cu_bannerNonprodJira }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":250,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_vpnNoTwoStep']}",
   "script":"${ cu_bannerNonprodBannerVpn && !cu_twoStepUsers }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":255,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_vpn']}",
   "script":"${ cu_bannerNonprodBannerVpn && cu_bannerNonprodBannerVpn30min && cu_twoStepUsers }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":260,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_vpnNot30']}",
   "script":"${ cu_bannerNonprodBannerVpn && !cu_bannerNonprodBannerVpn30min && cu_twoStepUsers }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":262,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_vpnNot']}",
   "script":"${ ! cu_bannerNonprodBannerVpn }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":265,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_email']}",
   "script":"${ cu_bannerNonprodEmail }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":266,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_emailNot']}",
   "script":"${ ! cu_bannerNonprodEmail }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":270,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_boxNot']}",
   "script":"${ !cu_bannerNonprodBox }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":271,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_box30']}",
   "script":"${ cu_bannerNonprodBox && cu_bannerNonprodBox30min }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":272,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_instructions_boxNot30']}",
   "script":"${ cu_bannerNonprodBox && !cu_bannerNonprodBox30min }"
}
{
   "endIfMatches":false,
   "customUiTextType":"instructions1",
   "index":280,
   "defaultText":false,
   "text":"<br />",
   "script":"true"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":0,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_enrollLabel_activeTeamMember']}",
   "script":"${cu_bannerNonprodEligible && cu_bannerNonprodEligible30min && cu_bannerNonprodInTeamAll && cu_bannerNonprodInTeamAll30min}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":10,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_enrollLabel_pendingTeamMember']}",
   "script":"${cu_bannerNonprodEligible && cu_bannerNonprodInTeamAll}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":20,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_enrollLabel_activeNonprodBanner']}",
   "script":"${cu_bannerNonprodEligible && cu_bannerNonprodEligible30min && !cu_bannerNonprodInTeamAll}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":30,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_enrollLabel_pendingNonprodBanner']}",
   "script":"${cu_bannerNonprodEligible && !cu_bannerNonprodInTeamAll }"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":40,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_enrollLabel_activeNonprodTeamResources']}",
   "script":"${!cu_bannerNonprodEligible && cu_bannerNonprodInTeamAll && cu_bannerNonprodInTeamAll30min }"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":50,
   "defaultText":false,
   "text":"${textContainer.text['penn_bannerNonprod_enrollLabel_pendingNonprodTeamResources']}",
   "script":"${!cu_bannerNonprodEligible && cu_bannerNonprodInTeamAll && !cu_bannerNonprodInTeamAll30min }"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollmentLabel",
   "index":60,
   "defaultText":true,
   "text":"${textContainer.text['penn_bannerNonprod_enrollLabel_cannotUseBanner']}"
}
{
   "endIfMatches":true,
   "customUiTextType":"canSeeUserEnvironment",
   "index":0,
   "text":"${cu_bannerNonprodAllowedToManage}"
}
{
   "endIfMatches":true,
   "customUiTextType":"canSeeScreenState",
   "index":0,
   "text":"false"
}
{
   "endIfMatches":true,
   "customUiTextType":"canAssignVariables",
   "index":0,
   "text":"${cu_bannerNonprodAllowedToManage}"
}
{
   "endIfMatches":true,
   "customUiTextType":"enrollButtonShow",
   "index":0,
   "text":"false"
}
{
   "endIfMatches":true,
   "customUiTextType":"unenrollButtonShow",
   "index":0,
   "text":"false"
}
{
   "endIfMatches":true,
   "customUiTextType":"managerInstructions",
   "index":10,
   "defaultText":true,
   "text":"${textContainer.text['penn_bannerNonprod_managerInstructions']}"
}
```

## Externalized text

```
############# banner nonprod

penn_bannerNonprod_header = <h1>NGSS and Non-prod Banner account analysis</h1>
penn_bannerNonprod_helpText = NGSS access is analyzed and described to help troubleshoot issues.
penn_bannerNonprod_managerInstructions = Search for a person to analyze their NGSS status

penn_bannerNonprod_cu_bannerNonprodPersonDescription = Person
penn_bannerNonprod_cu_bannerNonprodEligible30min = Eligible for 30 min (provisioned to weblogin)
penn_bannerNonprod_cu_bannerNonprodEligible = Eligible (active employee, trained, two-step, or reprieved)
penn_bannerNonprod_cu_bannerNonprodEligibleAnalysis = Eligibility analysis
penn_bannerNonprod_cu_bannerNonprodReprieved = Reprieved (exempt from eligibility requirements)
penn_bannerNonprod_cu_bannerNonprodReprieveEnd = Reprieve end date
penn_bannerNonprod_cu_bannerNonprodReprieveAnalysis = Reprieve analysis
penn_bannerNonprod_cu_bannerNonprodReprieveLastAudit = Latest reprieve audit
penn_bannerNonprod_cu_bannerNonprodHasAccount = Has Banner account
penn_bannerNonprod_cu_bannerNonprodHasAccountAnalysis = Banner account analysis
penn_bannerNonprod_cu_bannerNonprodActiveEmployee = Active employee
penn_bannerNonprod_cu_bannerNonprodActiveEmployeeAnalysis = Active employee analysis
penn_bannerNonprod_cu_bannerNonprodActiveEmployeeEndDate = Employee end date
penn_bannerNonprod_cu_bannerNonprodFerpa = <a href="https://url/ngssFerpa">FERPA</a> trained
penn_bannerNonprod_cu_bannerNonprodFerpaAnalysis = FERPA analysis
penn_bannerNonprod_cu_bannerNonprodFerpaEndDate = FERPA expire date
penn_bannerNonprod_cu_bannerNonprodAllowedToManage = Allowed to view other users
penn_bannerNonprod_cu_bannerNonprodInTeamAll = Has access to NGSS resources
penn_bannerNonprod_cu_bannerNonprodInTeamAllAnalysis = Has access to NGSS resources analysis
penn_bannerNonprod_cu_bannerNonprodInTeamAll30min = Has had access to NGSS resources for more than 30 minutes

penn_bannerNonprod_cu_bannerNonprodSecurityTraining = <a href="https://url/ngssSecurity">Security</a> trained
penn_bannerNonprod_cu_bannerNonprodSecurityTrainingAnalysis = Security training analysis

penn_bannerNonprod_cu_bannerNonprodIscContractor = Contractor
penn_bannerNonprod_cu_bannerNonprodClient = Non-ISC employee
penn_bannerNonprod_cu_bannerNonprodIscStaff = ISC employee
penn_bannerNonprod_cu_bannerNonprodTeamAnalysis = Team role analysis

penn_bannerNonprod_cu_bannerNonprodConfluence = Confluence
penn_bannerNonprod_cu_bannerNonprodJira = Jira
penn_bannerNonprod_cu_bannerNonprodConfluenceAnalysis = Confluence analysis
penn_bannerNonprod_cu_bannerNonprodJiraAnalysis = Jira analysis

penn_bannerNonprod_cu_bannerNonprodBannerVpn = Banner VPN
penn_bannerNonprod_cu_bannerNonprodBannerVpn30min = Banner VPN eligible for 30min at least (provisioned to Kite)
penn_bannerNonprod_cu_bannerNonprodBannerVpnAnalysis = Banner VPN analysis

penn_bannerNonprod_cu_bannerNonprodEmail = NGSS email list
penn_bannerNonprod_cu_bannerNonprodEmail30min = NGSS email list for 30 minutes
penn_bannerNonprod_cu_bannerNonprodEmailAnalysis = NGSS email list analysis

penn_bannerNonprod_cu_bannerNonprodBox = NGSS Box access
penn_bannerNonprod_cu_bannerNonprodBox30min = NGSS Box access for 30 minutes
penn_bannerNonprod_cu_bannerNonprodBoxAnalysis = NGSS Box access analysis

penn_bannerNonprod_enrollLabel_activeTeamMember = <b>NGSS status:</b> <b style="color: green">Can access team resources and can use non-prod Banner</b>

penn_bannerNonprod_enrollLabel_pendingTeamMember = <b>NGSS status:</b> <b style="color: brown">Pending: access to team resources and non-prod Banner should work in less than 30 minutes, try again soon</b>

penn_bannerNonprod_enrollLabel_activeNonprodBanner = <b>NGSS status:</b> <b style="color: brown">Can access non-prod Banner, but not team resources</b>

penn_bannerNonprod_enrollLabel_pendingNonprodBanner = <b>NGSS status:</b> <b style="color: brown">Pending: access to non-prod Banner should work in less than 30 minutes, try again soon, but cannot access team resources</b>

penn_bannerNonprod_enrollLabel_activeNonprodTeamResources = <b>NGSS status:</b> <b style="color: brown">Can access team resources, but not non-prod Banner</b>

penn_bannerNonprod_enrollLabel_pendingNonprodTeamResources = <b>NGSS status:</b> <b style="color: brown">Pending: access to team resources, should work in less than 30 minutes, try again soon, but cannot access non-prod Banner</b>

# not enrolled
penn_bannerNonprod_enrollLabel_cannotUseBanner = <b>NGSS status:</b> <b style="color: brown">You cannot access team resources or non-prod Banner</b>

penn_bannerNonprod_instructions_eligible = You are eligible to use non-prod Banner.$space$
penn_bannerNonprod_instructions_ineligible = <b style="color: brown">You are not eligible to use non-prod Banner.</b>$space$ 
penn_bannerNonprod_instructions_eligible30 = You have been eligible for more than 30 minutes, so Weblogin should allow you through to non-prod Banner.$space$
penn_bannerNonprod_instructions_eligibleNot30 = <b style="color: brown">You have been eligible for fewer than 30 minutes so you will have to wait for your Weblogin Banner non-prod access.  Try again soon.</b>$space$

penn_bannerNonprod_instructions_inTeamAll = You have access to the NGSS team resources (e.g. Jira, Confluence, Box, Clarizen, email list). $space$
penn_bannerNonprod_instructions_notInTeamAll = <b style="color: brown">You do not have access to the NGSS team resources (e.g. Jira, Confluence, Box, Clarizen, email list).</b>$space$
penn_bannerNonprod_instructions_inTeamAll30 = You have been eligible for more than 30 minutes, so Weblogin should allow you through to NGSS team resources (e.g. Jira, Confluence, Box, Clarizen, email list).$space$
penn_bannerNonprod_instructions_notInTeamAll30 = <b style="color: brown">You have been eligible for fewer than 30 minutes so you will have to wait for your Weblogin team resource access  (e.g. Jira, Confluence, Box, Clarizen, email list).  Try again soon.</b>$space$

penn_bannerNonprod_instructions_reprievedEnd = <b style="color: brown">You have an eligibility reprieve that ends on ${cu_bannerNonprodReprieveEnd}.</b>$space$
penn_bannerNonprod_instructions_reprievedNoEnd = You have an eligibility reprieve with no end date.$space$

penn_bannerNonprod_instructions_twoStep = You are enrolled in Two-Step Verification.$space$
penn_bannerNonprod_instructions_noTwoStep = <b style="color: brown">You are not enrolled in Two-Step Verification, please <a href="https://mfa.school.edu" style="text-decoration:underline !important">enroll</a>.</b>$space$

penn_bannerNonprod_instructions_activeEmployee = You are an active employee.$space$
penn_bannerNonprod_instructions_notActiveEmployee = <b style="color: brown">Your data does not reflect that you are an active employee.</b>$space$
penn_bannerNonprod_instructions_activeEmployeeEndDate = <b style="color: brown">Your employment record ends on ${cu_bannerNonprodActiveEmployeeEndDate}.  If you need Banner access after that date be sure to coordinate with your Business Administrator (e.g. SamB).</b>.$space$

penn_bannerNonprod_instructions_ferpa = You are trained in FERPA.$space$
penn_bannerNonprod_instructions_noFerpa = <b style="color: brown">You are required to take <a href="https://url/ngssFerpa" style="text-decoration:underline !important">FERPA training</a>.</b>.$space$

penn_bannerNonprod_instructions_ferpaEndDate = Your FERPA end date is ${cu_bannerNonprodFerpaEndDate}.  Be sure to <a href="https://url/ngssFerpa" style="text-decoration:underline !important">take the FERPA training again</a> before that date.$space$

penn_bannerNonprod_instructions_security = You are trained in Security.$space$
penn_bannerNonprod_instructions_noSecurity = <b style="color: brown">You are required to take <a href="https://url/ngssSecurity" style="text-decoration:underline !important">Security training</a>.</b>.$space$

penn_bannerNonprod_instructions_contractor = You are listed as a contractor on the NGSS project.$space$
penn_bannerNonprod_instructions_client = You are listed as an employee on the NGSS project (not in ISC).$space$
penn_bannerNonprod_instructions_isc = You are listed as an employee on the NGSS project (in ISC).$space$

penn_bannerNonprod_instructions_confluence = You have been eligible for Confluence since the last user sync (6am), so you should have access to Confluence.  If you are having issues slack Chris or Isobel to have your account enabled.$space$
penn_bannerNonprod_instructions_noConfluence = <b style="color: brown">You probably do not have access to NGSS Confluence since you have not had access to team resources since before the last Confluence user sync.  Fix your team resources issues and wait until after the next 6am sync.</b>$space$

penn_bannerNonprod_instructions_jira = You have been eligible for Jira since the last user sync (6am), so you should have access to Jira.  If you are having issues slack Chris or Isobel to have your account enabled.$space$
penn_bannerNonprod_instructions_noJira = <b style="color: brown">You probably do not have access to NGSS Jira since you have not had access to team resources since before the last Jira user sync.  Fix your team resources issues and wait until after the next 6am sync.</b>$space$

penn_bannerNonprod_instructions_vpnNoTwoStep = <b style="color: brown">You cannot use the banner VPN until you <a href="https://mfa.school" style="text-decoration:underline !important">enroll in Two-step Verification</a>.</b>$space$
penn_bannerNonprod_instructions_vpn = You have had banner VPN access for at least 30 minutes, so you should be able to use the VPN.$space$
penn_bannerNonprod_instructions_vpnNot30 = <b style="color: brown">You recently gained access to the VPN.  Usually it takes 10-30 minutes to provision to Kite.  Try it again soon if it does not work.</b>$space$
penn_bannerNonprod_instructions_vpnNot = <b style="color: brown">You do not have access to the VPN.  Ask SamB or DaveT to enable your access.</b>$space$

penn_bannerNonprod_instructions_email = You are on the NGSS email list.$space$
penn_bannerNonprod_instructions_emailNot = <b style="color: brown">You are not on the NGSS email list.</b>$space$

penn_bannerNonprod_instructions_boxNot = <b style="color: brown">You do not have access to Box for NGSS.</b>$space$
penn_bannerNonprod_instructions_box30 = You have had NGSS box access for at least 30 minutes, so you should be able to use NGSS box resources.$space$
penn_bannerNonprod_instructions_boxNot30 = <b style="color: brown">You recently gained access to NGSS Box.  Usually it takes 10-30 minutes to provision to Box.  Try it again soon if it does not work.</b>$space$

```
