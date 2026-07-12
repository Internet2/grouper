---
title: "SLAC Grouper container in Amazon AWS ECS Elastic Container Service"
space: Grouper
pageId: 28544144
version: 16
lastUpdated: 2026-07-12T15:26:12.578Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544144/SLAC+Grouper+container+in+Amazon+AWS+ECS+Elastic+Container+Service
---

## Description

This page provides an Amazon Elastic Container Services (Amazon ECS) Grouper example for a swift and simple setup. It presumes the existence of a functional VPC, its associated network components [S3 Bucket](https://docs.aws.amazon.com/AmazonS3/latest/userguide/create-bucket-overview.html), an [SSL cert](https://repost.aws/knowledge-center/import-ssl-certificate-to-iam), and an Application Load Balancer (ALB). Should these prerequisites not be met, we recommend visiting the following foundational networking workshop: [AWS Networking Fundamentals Workshop](https://catalog.workshops.aws/networking/en-US/foundational/fundamentals). Additionally, the presence of an operational PostgreSQL Cluster is assumed.

Before executing the provided templates, it is crucial to thoroughly examine and substitute any placeholders marked with "<>" with the appropriate string values.

This AWS CloudFormation template creates an ECS Task Execution Role and an ECS Cluster, laying the groundwork for your ECS environment.

```
AWSTemplateFormatVersion: 2010-09-09
Description: A CloudFormation template for creating Fargate ECS Cluster and an ECS Task Execution Role.

Resources:  
  # Create Task Execution Role. This should provide most of the permissions you would need.
  ECSTaskExecutionRole:
    Type: AWS::IAM::Role
    Properties:
      RoleName: ecsTaskExecutionRole
      AssumeRolePolicyDocument:
        Version: "2012-10-17"
        Statement:
          - Effect: Allow
            Principal:
              Service:
                - ecs-tasks.amazonaws.com
            Action:
              - sts:AssumeRole

  AmazonECSTaskExecutionRolePolicy:
    Type: AWS::IAM::Policy
    Properties:
      PolicyName: AmazonECSTaskExecutionRolePolicy
      Roles:
        - !Ref ECSTaskExecutionRole
      PolicyDocument:
        Version: "2012-10-17"
        Statement:
          - Effect: Allow
            Action:
              - ecr:GetAuthorizationToken
              - ecr:BatchCheckLayerAvailability
              - ecr:GetDownloadUrlForLayer
              - ecr:BatchGetImage
              - logs:CreateLogStream
              - logs:PutLogEvents
            Resource: "*"

  AWSAppMeshEnvoyAccess:
    Type: AWS::IAM::Policy
    Properties:
      PolicyName: AWSAppMeshEnvoyAccess
      Roles:
        - !Ref ECSTaskExecutionRole
      PolicyDocument:
        Version: "2012-10-17"
        Statement:
          - Effect: Allow
            Action:
              - appmesh:StreamAggregatedResources
            Resource: "*"

  AWSXRayDaemonWriteAccess:
    Type: AWS::IAM::Policy
    Properties:
      PolicyName: AWSXRayDaemonWriteAccess
      Roles:
        - !Ref ECSTaskExecutionRole
      PolicyDocument:
        Version: "2012-10-17"
        Statement:
          - Effect: Allow
            Action:
              - xray:PutTraceSegments
              - xray:PutTelemetryRecords
              - xray:GetSamplingRules
              - xray:GetSamplingTargets
              - xray:GetSamplingStatisticSummaries
            Resource: "*"

  CloudWatchFullAccess:
    Type: AWS::IAM::Policy
    Properties:
      PolicyName: CloudWatchFullAccess
      Roles:
        - !Ref ECSTaskExecutionRole
      PolicyDocument:
        Version: "2012-10-17"
        Statement:
          - Effect: Allow
            Action:
              - autoscaling:Describe*
              - cloudwatch:*
              - logs:*
              - sns:*
              - iam:GetPolicy
              - iam:GetPolicyVersion
              - iam:GetRole
              - oam:ListSinks
            Resource: "*"
          - Effect: Allow
            Action:
              - iam:CreateServiceLinkedRole
            Resource: "arn:aws:iam::*:role/aws-service-role/events.amazonaws.com/AWSServiceRoleForCloudWatchEvents*"
            Condition:
              StringLike:
                iam:AWSServiceName: "events.amazonaws.com"
          - Effect: Allow
            Action:
              - oam:ListAttachedLinks
            Resource: "arn:aws:oam:*:*:sink/*"

  ECSParameterSource:
    Type: AWS::IAM::Policy
    Properties:
      PolicyName: ECSParameterSource
      Roles:
        - !Ref ECSTaskExecutionRole
      PolicyDocument:
        Version: "2012-10-17"
        Statement:
          - Effect: Allow
            Action:
              - secretsmanager:GetSecretValue
              - ssm:GetParameters
              - kms:Decrypt
            Resource:
              - !Sub "arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:*"
              - !Sub "arn:aws:ssm:${AWS::Region}:${AWS::AccountId}:parameter/*"
              - !Sub "arn:aws:kms:${AWS::Region}:${AWS::AccountId}:*"
          - Effect: Allow
            Action:
              - s3:GetObject
            Resource:
              - "arn:aws:s3:::<YOUR BUCKET NAME>/environment-grouper.env"
          - Effect: Allow
            Action:
              - s3:GetBucketLocation
            Resource:
              - "<YOUR BUCKET ARN>"

 # Create an ECS Cluster
  ECSCluster:
    Type: AWS::ECS::Cluster
    Properties:
      ClusterName: "my-dev-ecs-cluster"
      CapacityProviders:
        - FARGATE
        - FARGATE_SPOT
      DefaultCapacityProviderStrategy:
        - CapacityProvider: FARGATE
          Weight: 1
        - CapacityProvider: FARGATE_SPOT
          Weight: 1
      Configuration:
        ExecuteCommandConfiguration:
          Logging: DEFAULT
      ClusterSettings:
        - Name: containerInsights
          Value: enabled
      Tags:
        - Key: name
          Value:  "my-dev-ecs-cluster"

Outputs:
  ECSCluster:
    Description: A reference to the security group for ECS hosts
    Value: !Ref ECSCluster
    Export:
      Name: dev-ECSCluster

  ECSTaskExecutionRole:
    Description: ECS Task Execution Role
    Value: !Ref ECSTaskExecutionRole
    Export:
      Name: dev-ECSTaskExecutionRole

```

This template creates various components, including a Grouper Daemon Task Definition, a service, a Security Group, and a Target Group, offering a comprehensive setup for your ECS Grouper Daemon deployment.

```
AWSTemplateFormatVersion: '2010-09-09'
Description: "Creates an ECS Grouper Daemon Task, SG, and Service."

Resources:

  GrouperDaemonTaskDefinition:
    Type: AWS::ECS::TaskDefinition
    Properties:
      Family: Grouper_Daemon
      ContainerDefinitions:
        - Name: grouper_daemon
          Image: !Sub ${AWS::AccountId}.dkr.ecr.${AWS::Region}.amazonaws.com/grouper:<VERSION>
          Cpu: 2048
          Memory: 6144
          MemoryReservation: 6144
          PortMappings:
            - Name: grouper_daemon-443-tcp
              ContainerPort: 443
              HostPort: 443
              Protocol: tcp
              AppProtocol: http
          Essential: true
          Command:
            - daemon
          Environment:
            - Name: GROUPERSYSTEM_QUICKSTART_PASS
              Value: Pass
            - Name: GROUPER_RUN_SHIB_SP
              Value: "false"
            - Name: GROUPER_LOG_TO_PIPE
              Value: "true"
            - Name: TZ
              Value: America/Los_Angeles
            - Name: GROUPER_RUN_APACHE
              Value: "true"
            - Name: GROUPER_DAEMON
              Value: "true"
            - Name: GROUPER_MAX_MEMORY
              Value: 6g
            - Name: GROUPER_RUN_HSQLDB
              Value: "false"
            - Name: GROUPER_RUN_TOMCAT
              Value: "true"
            - Name: GROUPER_MORPHSTRING_ENCRYPT_KEY
              Value: abc123
            - Name: GROUPER_SELF_SIGNED_CERT
              Value: "true"
            - Name: USERTOKEN
              Value: daemon
            - Name: GROUPER_TOMCAT_LOG_ACCESS
              Value: "true"
            - Name: GROUPER_DATABASE_USERNAME
              Value: grouper
            - Name: GROUPER_LOG_TO_HOST
              Value: "true"
            - Name: GROUPER_AUTO_DDL_UPTOVERSION
              Value: v4.*.* # Modify this as needed.
            - Name: GROUPER_DATABASE_URL
              Value:  <DATABASE URL>
            - Name: GROUPER_DATABASE_PASSWORD
              Value:  <DATABASE PASSWORD>
          EnvironmentFiles: []
          MountPoints: []
          VolumesFrom: []
          Ulimits: []
          LogConfiguration:
            LogDriver: awslogs
            Options:
              awslogs-create-group: "true"
              awslogs-group: /ecs/Grouper_Daemon
              awslogs-region: !Sub ${AWS::Region}
              awslogs-stream-prefix: ecs
            SecretOptions: []
      TaskRoleArn: !ImportValue dev-ECSTaskExecutionRole
      ExecutionRoleArn: !ImportValue dev-ECSTaskExecutionRole
      NetworkMode: awsvpc
      RequiresCompatibilities:
        - FARGATE
      Cpu: "2048"
      Memory: "6144"
      Tags: []

  GrouperDaemonSecurityGroup:
    Type: AWS::EC2::SecurityGroup
    Properties:
      VpcId:  <ADD YOUR VPCID>
      GroupName: "SG-Grouper-Daemon"
      GroupDescription: "ECS SecurityGroup for Grouper Daemon Service"
      SecurityGroupIngress:
        - IpProtocol: tcp
          FromPort: 443
          ToPort: 443
          SourceSecurityGroupId: <ADD YOUR LOADBALANCER SECURITYGROUP>
      Tags:
        - Key: Name
          Value: "SG-Grouper-Daemon"

  PostgresSecurityGroupIngress:
    Type: "AWS::EC2::SecurityGroupIngress"
    Properties:
      GroupId:  <ADD YOUR DATABASE SECURITYGROUP>
      IpProtocol: "tcp"
      FromPort: 5432
      ToPort: 5432
      SourceSecurityGroupId: !Ref GrouperDaemonSecurityGroup

  DaemonECSService:
    Type: AWS::ECS::Service
    Properties:
      Cluster: !ImportValue dev-ECSCluster
      TaskDefinition: !Ref GrouperDaemonTaskDefinition
      ServiceName: grouper_daemon-service
      SchedulingStrategy: REPLICA
      DesiredCount: 1
      NetworkConfiguration:
        AwsvpcConfiguration:
          AssignPublicIp: ENABLED
          SecurityGroups:
            - !Ref GrouperDaemonSecurityGroup
          Subnets: 
            - <ADD YOUR VPC SUBNETS>

      PlatformVersion: LATEST
      DeploymentConfiguration:
        MaximumPercent: 200
        MinimumHealthyPercent: 100
        DeploymentCircuitBreaker:
          Enable: true
          Rollback: true
      DeploymentController:
        Type: ECS
      ServiceConnectConfiguration:
        Enabled: false
      Tags: []
      EnableECSManagedTags: true

Outputs:
  DaemonECSService:
    Description: Grouper Daemon service.
    Value: !Ref DaemonECSService
```

This template creates various components, including a Grouper UI Task Definition, a Service, a Security Group, a Target Group, and an SSL-configured ALB Listener, offering a comprehensive setup for your ECS Grouper UI deployment.

```
AWSTemplateFormatVersion: "2010-09-09"
Description: "Create an ECS Grouper UI Task Definition, SG, and Service."

Resources:

  GrouperUITaskDefinition:
    Type: AWS::ECS::TaskDefinition
    Properties:
      Family: Grouper_UI
      ContainerDefinitions:
        - Name: grouper_ui
          Image: !Sub ${AWS::AccountId}.dkr.ecr.${AWS::Region}.amazonaws.com/grouper:<VERSION>
          Cpu: 2048
          Memory: 4096
          MemoryReservation: 4096
          PortMappings:
            - Name: grouper_ui-443-tcp
              ContainerPort: 443
              HostPort: 443
              Protocol: tcp
              AppProtocol: http
          Essential: true
          Command:
            - ui
          Environment:
            - Name: GROUPERSYSTEM_QUICKSTART_PASS
              Value: Pass
            - Name: GROUPER_RUN_SHIB_SP
              Value: "false"
            - Name: GROUPER_LOG_TO_PIPE
              Value: "true"
            - Name: GROUPER_UI_GROUPER_AUTH
              Value: "true"
            - Name: TZ
              Value: America/Los_Angeles
            - Name: GROUPER_RUN_APACHE
              Value: "true"
            - Name: GROUPER_UI
              Value: "true"
            - Name: GROUPER_MAX_MEMORY
              Value: 4g
            - Name: GROUPER_RUN_HSQLDB
              Value: "false"
            - Name: GROUPER_RUN_TOMCAT
              Value: "true"
            - Name: GROUPER_MORPHSTRING_ENCRYPT_KEY
              Value: abc123
            - Name: GROUPER_SELF_SIGNED_CERT
              Value: "true"
            - Name: USERTOKEN
              Value: ui
            - Name: GROUPER_TOMCAT_LOG_ACCESS
              Value: "true"
            - Name: GROUPER_DATABASE_USERNAME
              Value: grouper
            - Name: GROUPER_LOG_TO_HOST
              Value: "true"
            - Name: GROUPER_AUTO_DDL_UPTOVERSION
              Value: v4.*.* # Modify this as needed.
            - Name: GROUPER_DATABASE_URL
              Value:  <DATABASE URL>
            - Name: GROUPER_DATABASE_PASSWORD
              Value:  <DATABASE PASSWORD>
          EnvironmentFiles: []
          MountPoints: []
          VolumesFrom: []
          Ulimits: []
          LogConfiguration:
            LogDriver: awslogs
            Options:
              awslogs-create-group: "true"
              awslogs-group: /ecs/Grouper_UI
              awslogs-region: !Sub ${AWS::Region}
              awslogs-stream-prefix: ecs
            SecretOptions: []
      TaskRoleArn: !ImportValue dev-ECSTaskExecutionRole
      ExecutionRoleArn: !ImportValue dev-ECSTaskExecutionRole
      NetworkMode: awsvpc
      RequiresCompatibilities:
        - FARGATE
      Cpu: "2048"
      Memory: "4096"
      Tags: []

  GrouperUISecurityGroup:
    Type: AWS::EC2::SecurityGroup
    Properties:
      VpcId: <ADD YOUR VPCID>
      GroupName: "SG-Grouper-UI"
      GroupDescription: "ECS SecurityGroup for Grouper UI Service"
      SecurityGroupIngress:
        - IpProtocol: tcp
          FromPort: 443
          ToPort: 443
          SourceSecurityGroupId: <ADD YOUR LOADBALANCER SECURITYGROUP>
      Tags:
        - Key: Name
          Value: "SG-Grouper-UI"

  PostgresSecurityGroupIngress:
    Type: "AWS::EC2::SecurityGroupIngress"
    Properties:
      GroupId: <ADD YOUR DATABASE SECURITYGROUP>
      IpProtocol: "tcp"
      FromPort: 5432
      ToPort: 5432
      SourceSecurityGroupId: !Ref GrouperUISecurityGroup

  GrouperUIECSService:
    Type: AWS::ECS::Service
    Properties:
      Cluster: !ImportValue dev-ECSCluster
      TaskDefinition: !Ref GrouperUITaskDefinition
      ServiceName: grouper-ui-service
      SchedulingStrategy: "REPLICA"
      DesiredCount: 1

      LoadBalancers:
        - ContainerName: "grouper_ui"
          ContainerPort: 443
          TargetGroupArn: !Ref GrouperUITargetGroup

      NetworkConfiguration:
        AwsvpcConfiguration:
          AssignPublicIp: "ENABLED" # If you are using secrets and/or ECR make sure this is enabled. only do this for dev Environmens (not for Production).
          SecurityGroups: 
            - !Ref GrouperUISecurityGroup
          Subnets: 
            - <ADD YOUR VPC SUBNETS>

      PlatformVersion: "LATEST"
      DeploymentConfiguration:
        MaximumPercent: 200
        MinimumHealthyPercent: 100
        DeploymentCircuitBreaker:
          Enable: true
          Rollback: true
      DeploymentController:
        Type: "ECS"
      ServiceConnectConfiguration:
        Enabled: false
      Tags: []
      EnableECSManagedTags: true
    DependsOn:
      - GrouperUIListener
      - GrouperUISecurityGroup

  GrouperUITargetGroup:
    Type: AWS::ElasticLoadBalancingV2::TargetGroup
    Properties:
      HealthCheckPath: "/status_grouper/status?diagnosticType=db"
      Name: "grouper-ui-target-group"
      Port: 443
      Protocol: "HTTPS"
      TargetType: "ip"
      HealthCheckProtocol: "HTTPS"
      VpcId:  <ADD YOUR VPCID>

  GrouperUIListener:
    Type: "AWS::ElasticLoadBalancingV2::Listener"
    Properties:
      DefaultActions:
        - Type: "forward"
          TargetGroupArn: !Ref "GrouperUITargetGroup"
      LoadBalancerArn: <ADD YOUR LOADBALANCER ARN>
      Port: "443"
      Protocol: "HTTPS"
      Certificates:
        - CertificateArn: <ADD YOUR SSL Cert ARN>

Outputs:
  GrouperUIECSService:
    Description: "The created service."
    Value: !Ref GrouperUIECSService

  GrouperUIListener:
    Description: "The created listener."
    Value: !Ref GrouperUIListener
    Export:
      Name: dev-HTTPS-Listener

  GrouperUITargetGroup:
    Description: "The created target group."
    Value: !Ref GrouperUITargetGroup

```

This template creates various components, including a Grouper WS Task Definition, a Service, a Security Group, a Target Group, and an SSL-configured ALB Listener, offering a comprehensive setup for your ECS Grouper WS deployment.

```
AWSTemplateFormatVersion: "2010-09-09"
Description: "Create an ECS Grouper WS Task Definition, SG, and Service."

Resources:

  GrouperWSTaskDefinition:
    Type: AWS::ECS::TaskDefinition
    Properties:
      Family: Grouper_WS
      ContainerDefinitions:
        - Name: grouper_ws
          Image: !Sub ${AWS::AccountId}.dkr.ecr.${AWS::Region}.amazonaws.com/grouper:<VERSION>
          Cpu: 2048
          Memory: 4096
          MemoryReservation: 4096
          PortMappings:
            - Name: grouper_ws-443-tcp
              ContainerPort: 443
              HostPort: 443
              Protocol: tcp
              AppProtocol: http
          Essential: true
          Command:
            - ws
          Environment:
            - Name: GROUPERSYSTEM_QUICKSTART_PASS
              Value: Pass
            - Name: GROUPER_RUN_SHIB_SP
              Value: "false"
            - Name: GROUPER_LOG_TO_PIPE
              Value: "true"
            - Name: TZ
              Value: America/Los_Angeles
            - Name: GROUPER_RUN_APACHE
              Value: "true"
            - Name: GROUPER_WS
              Value: "true"
            - Name: GROUPER_WS_GROUPER_AUTH
              Value: "true"
            - Name: GROUPER_MAX_MEMORY
              Value: 4g
            - Name: GROUPER_RUN_HSQLDB
              Value: "false"
            - Name: GROUPER_RUN_TOMCAT
              Value: "true"
            - Name: GROUPER_MORPHSTRING_ENCRYPT_KEY
              Value: abc123
            - Name: GROUPER_SELF_SIGNED_CERT
              Value: "true"
            - Name: USERTOKEN
              Value: ws
            - Name: GROUPER_TOMCAT_LOG_ACCESS
              Value: "true"
            - Name: GROUPER_DATABASE_USERNAME
              Value: grouper
            - Name: GROUPER_LOG_TO_HOST
              Value: "true"
            - Name: GROUPER_AUTO_DDL_UPTOVERSION
              Value: v4.*.* # Modify this as needed.
            - Name: GROUPER_DATABASE_URL
              Value:  <DATABASE URL>
            - Name: GROUPER_DATABASE_PASSWORD
              Value:  <DATABASE PASSWORD>
          EnvironmentFiles: []
          MountPoints: []
          VolumesFrom: []
          Ulimits: []
          LogConfiguration:
            LogDriver: awslogs
            Options:
              awslogs-create-group: "true"
              awslogs-group: /ecs/Grouper_WS
              awslogs-region: !Sub ${AWS::Region}
              awslogs-stream-prefix: ecs
            SecretOptions: []
      TaskRoleArn: !ImportValue dev-ECSTaskExecutionRole
      ExecutionRoleArn: !ImportValue dev-ECSTaskExecutionRole
      NetworkMode: awsvpc
      RequiresCompatibilities:
        - FARGATE
      Cpu: "2048"
      Memory: "4096"
      Tags: []

  GrouperWSSecurityGroup:
    Type: AWS::EC2::SecurityGroup
    Properties:
      VpcId:  <ADD YOUR VPCID>
      GroupName: "SG-GrouperWS"
      GroupDescription: "ECS SecurityGroup for Grouper WS Service"
      SecurityGroupIngress:
        - IpProtocol: tcp
          FromPort: 443
          ToPort: 443
          SourceSecurityGroupId: <ADD YOUR LOADBALANCER SECURITYGROUP>
      Tags:
        - Key: Name
          Value: "SG-GrouperWS"

  PostgresSecurityGroupIngress:
    Type: "AWS::EC2::SecurityGroupIngress"
    Properties:
      GroupId: <ADD YOUR DATABASE SECURITYGROUP>
      IpProtocol: "tcp"
      FromPort: 5432
      ToPort: 5432
      SourceSecurityGroupId: !Ref GrouperWSSecurityGroup

  GrouperWSECSService:
    Type: AWS::ECS::Service
    Properties:
      Cluster: !ImportValue dev-ECSCluster
      TaskDefinition: !Ref GrouperWSTaskDefinition
      ServiceName: grouper_ws-service
      SchedulingStrategy: "REPLICA"
      DesiredCount: 1

      LoadBalancers:
        - ContainerName: "grouper_ws"
          ContainerPort: 443
          TargetGroupArn: !Ref GrouperWSTargetGroup

      NetworkConfiguration:
        AwsvpcConfiguration:
          AssignPublicIp: "ENABLED" # If you are using secrets and/or ECR make sure this is enabled. only do this for dev Environmens (not for Production).
          SecurityGroups: 
            - !Ref GrouperWSSecurityGroup
          Subnets: 
            - <ADD YOUR VPC SUBNETS>

      PlatformVersion: "LATEST"
      DeploymentConfiguration:
        MaximumPercent: 200
        MinimumHealthyPercent: 100
        DeploymentCircuitBreaker:
          Enable: true
          Rollback: true
      DeploymentController:
        Type: "ECS"
      ServiceConnectConfiguration:
        Enabled: false
      Tags: []
      EnableECSManagedTags: true
    DependsOn:
      - GrouperWSSecurityGroup

  GrouperWSTargetGroup:
    Type: AWS::ElasticLoadBalancingV2::TargetGroup
    Properties:
      HealthCheckPath: "/grouper-ws/status?diagnosticType=trivial"
      Name: "grouper-ws-target-group"
      Port: 443
      Protocol: "HTTPS"
      TargetType: "ip"
      HealthCheckProtocol: "HTTPS"
      VpcId: <ADD YOUR VPCID>

  ListenerRule:
    Type: AWS::ElasticLoadBalancingV2::ListenerRule
    Properties:
      Actions:
        - Type: forward
          TargetGroupArn: !Ref GrouperWSTargetGroup
      Conditions:
        - Field: path-pattern
          Values:
            - /grouper-ws*
      ListenerArn: !ImportValue dev-HTTPS-Listener
      Priority: '2'

Outputs:
  GrouperWSECSService:
    Description: "The created Grouper WS service."
    Value: !Ref GrouperWSECSService

  GrouperWSTargetGroup:
    Description: "The created Grouper WS target group."
    Value: !Ref GrouperWSTargetGroup
```

**See Also**:

[SLAC Grouper Contribution page from 2020](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543439/SLAC+National+Accelerator+Laboratory+at+Stanford+University)
