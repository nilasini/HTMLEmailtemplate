package org.wso2.carbon.custom.email.notification.sender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.mgt.mail.AbstractEmailSendingModule;
import org.wso2.carbon.identity.mgt.mail.Notification;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class CustomEmailSendingModule extends AbstractEmailSendingModule {

    private static Log log = LogFactory.getLog(CustomEmailSendingModule.class);
    private static final int ENTITY_EXPANSION_LIMIT = 0;
    private Notification notification;

    /**
     * Send email notification to the user
     */
    public void sendEmail() {

        String user_name = null;
        try {
            if (this.notification == null) {
                throw new IllegalStateException("Notification not set. " +
                        "Please set the notification before sending messages");
            }

            PrivilegedCarbonContext.startTenantFlow();
            if (notificationData != null) {
                String tenantDomain = notificationData.getDomainName();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantDomain(tenantDomain, true);
                user_name = notificationData.getUserId();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("notification data not found. Tenant might not be loaded correctly");
                }
            }

            // Get the mail body
            String body = notification.getBody();

            if (log.isDebugEnabled()) {
                log.debug("Sending " + "user mail to " + this.notification.getSendTo());
            }

            // Send the HTML content mail.
            sendHtmlEmail(retrieveCreatePasswordLink(body), user_name);

            if (log.isDebugEnabled()) {
                log.debug("Email content : " + body);
            }

            log.info("Email has been sent to " + this.notification.getSendTo());
        } catch (Exception e) {
            log.error("Failed Sending Email ", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void setNotification(Notification notification) {

        this.notification = notification;
    }

    @Override
    public Notification getNotification() {

        return this.notification;
    }

    /**
     * Send html type email.
     *
     * @param link     redirection link used for assign a password
     * @param userName
     */
    private void sendHtmlEmail(String link, String userName) throws IOException, SAXException,
            ParserConfigurationException, EmailException {

        HtmlEmail email = new HtmlEmail();

        HashMap<String, String> mailProperties = readMailPropertiesFromFile();

        email.setHostName(mailProperties.get("host"));
        email.setSmtpPort(Integer.valueOf(mailProperties.get("port")));
        email.setSSLOnConnect(true);
        email.setAuthentication(mailProperties.get("from"), mailProperties.get("password"));
        email.setCharset("UTF-8");
        email.addTo(this.notification.getSendTo(), this.notification.getSendTo());

        email.setFrom(mailProperties.get("from"), mailProperties.get("from"));
        email.setSubject(this.notification.getSubject());

        email.setHtmlMsg("<html>" + "<p>Hi " + userName + "</p>" + "\n<p>change your password for the newly " +
                "created" +
                " account : " + userName + "\nClick the link below to<br /> create the password.</p>" +
                "\n<p>" + link + "</p>" +
                "\n<p>If clicking the link doesn't seem to work, you can copy and paste the<br /> link into your " +
                "browser's address " +
                "window.</p>" + "</html>");
        email.setTextMsg("Email client does not support HTML messages");

        email.send();

    }

    /**
     * Retrieve the redirection link from the mail body.
     *
     * @param body email body
     * @return redirection link used for assign a password
     */
    private String retrieveCreatePasswordLink(String body) {

        List<String> bodyList;
        String link = null;

        bodyList = Arrays.asList(body.trim().split(" "));
        for (String s : bodyList) {
            if (s.startsWith("http")) {
                link = s;
            }
        }
        return link;
    }

    /**
     * Read the config file which has the mail properties.
     *
     * @return email properties
     */
    private HashMap<String, String> readMailPropertiesFromFile() throws ParserConfigurationException, IOException,
            SAXException {

        File identityConfigXml = new File(CarbonUtils.getCarbonConfigDirPath(),
                "output-event-adapters.xml");
        HashMap<String, String> mailProperties = new HashMap<String, String>();

        if (identityConfigXml.exists()) {

            DocumentBuilder dBuilder = getSecuredDocumentBuilder().newDocumentBuilder();
            Document doc = dBuilder.parse(identityConfigXml);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("adapterConfig");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                Element eElement = (Element) nNode;
                if (eElement.getAttribute("type").equals("email")) {

                    mailProperties.put("from", eElement.getElementsByTagName("property").item(0)
                            .getTextContent());
                    mailProperties.put("password", eElement.getElementsByTagName("property").item(2)
                            .getTextContent());
                    mailProperties.put("host", eElement.getElementsByTagName("property").item(3)
                            .getTextContent());
                    mailProperties.put("port", eElement.getElementsByTagName("property").item(4)
                            .getTextContent());
                }
            }
        }
        return mailProperties;
    }

    /**
     * Secure the DocumentBuilderFactory to avoid XML External Entity attack.
     *
     * @return documentBuilderFactory
     */
    private DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX +
                    Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX +
                    Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX +
                    Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or "
                    + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX +
                Constants.SECURITY_MANAGER_PROPERTY, securityManager);
        return dbf;
    }
}