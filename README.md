# HTMLEmailtemplate
Allow html type email in IS5.2.0

Please follow the steps to allow html type emails in wso2 identity server 5.2.0

1) First you need to build the project using maven. Then copy the build jar file and commons-email jar file(You can get this from[1]) to <IS_HOME>/repository/components/dropins directory.
2) Then we need to disable the default notification sender module and engage the custom notification sender module we have written. For that,in <IS_HOME>/repository/conf/identity/identity-mgt.properties file, you have to comment the DefaultEmailSendingModule class name and add the fully qualified class name of the custom component written.
#Identity.Mgt.Notification.Sending.Module.1=org.wso2.carbon.identity.mgt.mail.DefaultEmailSendingModule 
Identity.Mgt.Notification.Sending.Module.1=org.wso2.carbon.custom.email.notification.sender.CustomEmailSendingModule

3) Then restart the Identity Server. Login to management console and create a user then that user will get the notification mail which we have specified in html format in our custom code.

[1] http://commons.apache.org/proper/commons-email/download_email.cgi
