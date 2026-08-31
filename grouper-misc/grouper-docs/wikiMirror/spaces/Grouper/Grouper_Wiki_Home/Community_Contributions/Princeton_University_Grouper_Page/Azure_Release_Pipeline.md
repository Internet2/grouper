---
title: "Azure Release Pipeline"
space: Grouper
pageId: 28544772
version: 6
lastUpdated: 2026-07-01T05:48:03.062Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544772/Azure+Release+Pipeline
---

Our Azure release pipeline takes our Terraform code stored within an Azure repo, performs variable replacement based off of the stage, and deploys to the appropriate environment.

Within the above screenshot, one can see our artifact (Terraform code) is called *grouper_IaC, basically a zip file. We have two stages, Sandbox IaC Deploy and Dev IaC Deploy; these represent different environments which can comprise of different Azure subscriptions and resource groups. We use service connections to connect to the different subs/ resource groups. None of our stages automatically run when a commit is made to the IaC Terraform repo. Rather, one must manually start one or many stages, which could initiate an approval process built into the stage.*

*Within a given stage, we have 3 jobs and 7 tasks, shown below.*

**

## *The 3 jobs are:*

1. Plan
  
  1. Performs variable replacement within our tfvars file and installs Terraform via a Task Group (reusable Azure release pipeline component)
  2. Terraform init: connects to our Terraform state file (stored in Azure, in a different RG than our Grouper deployment...but doesn't have to be)
  3. Terraform plan: show what would change. Note, that we pass in a variable file within the additional command arguments section; e.g. -no-color -var-file=$(System.DefaultWorkingDirectory)/_grouper_IaC/drop/z.variables.tfvars
2. Review:
  
  1. Manual intervention step: basically pauses the release pipeline, instructs (via email) folks to review the Terraform plan carefully before proceeding. We have a default action of timing out in 60 minutes.
3. Do:
  
  1. Performs variable replacement within our tfvars file and installs Terraform via a Task Group (reusable Azure release pipeline component); we need to repeat this step as we lost our Azure VM when the plan job concluded.
  2. Terraform init: connects to our Terraform state file (stored in Azure, in a different RG than our Grouper deployment...but doesn't have to
  3. Terraform apply: applies the plan

## Variable replacement within z.variables.tfvars

Our Terraform has a file called z.variables.tfvars which uses token replacement and variables specific to stages to swap out the variables during a stage deployment.

Here is what the task group looks like for variable replacement.

In the picture above, we are only swapping out tokens that exist within *.tfvars files.

Our variable values are within the release pipeline itself.

The variables in the picture above are used by the Terraform code to create the infrastructure and are used by KeyVault to store runtime container variable values. For example, the variable above grouper_url is stored within KeyVault and passed to the container during runtime as GROUPER_APACHE_SERVER_NAME whereas the variable postgres_sku defines our Postgres DB size within Azure (General purpose Gen 5, 2 CPUs).
