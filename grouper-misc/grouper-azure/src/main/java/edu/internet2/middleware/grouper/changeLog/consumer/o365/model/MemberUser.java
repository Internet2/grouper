package edu.internet2.middleware.grouper.changeLog.consumer.o365.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class MemberUser {
    @SerializedName("@odata.type")
    private String type;
    private String id;
    private String[] businessPhones;
    private String displayName;
    public String givenName;
    private String jobTitle;
    private String mail;
    private String mobilePhone;
    private String officeLocation;
    private String preferredLanguage;
    private String surname;
    private String userPrincipalName;

    public MemberUser(String type, String id, String[] businessPhones, String displayName, String givenName, String jobTitle, String mail, String mobilePhone, String officeLocation, String preferredLanguage, String surname, String userPrincipalName) {
        this.type = type;
        this.id = id;
        this.businessPhones = businessPhones;
        this.displayName = displayName;
        this.givenName = givenName;
        this.jobTitle = jobTitle;
        this.mail = mail;
        this.mobilePhone = mobilePhone;
        this.officeLocation = officeLocation;
        this.preferredLanguage = preferredLanguage;
        this.surname = surname;
        this.userPrincipalName = userPrincipalName;
    }

    public MemberUser() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getBusinessPhones() {
        return businessPhones;
    }

    public void setBusinessPhones(String[] businessPhones) {
        this.businessPhones = businessPhones;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUserPrincipalName() {
        return userPrincipalName;
    }

    public void setUserPrincipalName(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
    }

    @Override
    public String toString() {
        return "MemberUser{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", businessPhones=" + Arrays.toString(businessPhones) +
                ", displayName='" + displayName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", mail='" + mail + '\'' +
                ", mobilePhone='" + mobilePhone + '\'' +
                ", officeLocation='" + officeLocation + '\'' +
                ", preferredLanguage='" + preferredLanguage + '\'' +
                ", surname='" + surname + '\'' +
                ", userPrincipalName='" + userPrincipalName + '\'' +
                '}';
    }
}
