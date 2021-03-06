package org.wso2.carbon.custom.email.notification.sender.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="custom.email.notification.sender.dscomponent" immediate=true
 */

public class CustomEmailNotificationSenderDSComponent {
    private static Log log = LogFactory.getLog(CustomEmailNotificationSenderDSComponent.class);

    private static RealmService realmService;

    protected void activate(ComponentContext ctxt) {

        log.info("CustomEmailNotificationSenderDSComponent bundle activated successfully..");
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("CustomEmailNotificationSenderDSComponent is deactivated ");
        }
    }
}
