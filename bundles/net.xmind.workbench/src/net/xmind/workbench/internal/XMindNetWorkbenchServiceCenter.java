/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package net.xmind.workbench.internal;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;

import net.xmind.workbench.internal.notification.SiteEventNotificationService;

public class XMindNetWorkbenchServiceCenter
        implements IStartup, IWorkbenchListener {

    private static XMindNetWorkbenchServiceCenter INSTANCE = null;

    private SiteEventNotificationService eventService = null;

    private NewsletterSubscriptionReminder newsletterSubscriptionReminder = null;

    private XMindNetEvaluationHelper evaluationHelper = null;

    public void earlyStartup() {
        INSTANCE = this;
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench != null) {
            startServices(workbench);
            workbench.addWorkbenchListener(this);
        }
    }

    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        return true;
    }

    public void postShutdown(IWorkbench workbench) {
        stopServices();
        INSTANCE = null;
    }

    private void startServices(IWorkbench workbench) {
        if (eventService == null) {
            eventService = new SiteEventNotificationService(workbench);
        }
        eventService.start();
        newsletterSubscriptionReminder = new NewsletterSubscriptionReminder(
                workbench);
        newsletterSubscriptionReminder.start();

        if (evaluationHelper == null) {
            evaluationHelper = new XMindNetEvaluationHelper(workbench);
        }
    }

    private void stopServices() {
        if (evaluationHelper != null) {
            evaluationHelper.shutdown();
            evaluationHelper = null;
        }
        if (newsletterSubscriptionReminder != null) {
            newsletterSubscriptionReminder.stop();
            newsletterSubscriptionReminder = null;
        }
        if (eventService != null) {
            eventService.shutdown();
            eventService = null;
        }
    }

    public static SiteEventNotificationService getSiteEventNotificationService() {
        return INSTANCE == null ? null : INSTANCE.eventService;
    }

    public static NewsletterSubscriptionReminder getNewsletterSubscriptionReminder() {
        return INSTANCE == null ? null
                : INSTANCE.newsletterSubscriptionReminder;
    }

    public static XMindNetWorkbenchServiceCenter getInstance() {
        return INSTANCE;
    }

}
