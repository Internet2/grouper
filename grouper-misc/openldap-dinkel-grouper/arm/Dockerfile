FROM centos:centos7
RUN yum update -y && yum install -y rsync sudo wget tar unzip dos2unix patch file net-tools mlocate && yum clean all
    
RUN yum install -y openldap openldap-clients openldap-servers && yum clean all

COPY ldap-seed-data/ /seed-data
COPY entrypoint.sh /entrypoint.sh
COPY buildScript.sh /buildScript.sh

RUN dos2unix /entrypoint.sh && chmod +x /entrypoint.sh && dos2unix /buildScript.sh && chmod +x /buildScript.sh && /buildScript.sh

EXPOSE 389

VOLUME ["/etc/ldap", "/var/lib/ldap"]

CMD ["/entrypoint.sh"]

#ENTRYPOINT ["ping"]
#CMD ["google.com"]