/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package org.xmind.core.usagedata;

public interface IUsageDataSampler {

    /**
     * A singleton instance to do nothing on sampling. This helps ensure that
     * sampling operations are always error-free no matter a dedicated sampling
     * service exists or not.
     */
    IUsageDataSampler NULL = new IUsageDataSampler() {

        public void trackEvent(String category, String action, String name,
                String value) {
            // do nothing
        }

        public void trackEvent(String category, String action) {
            // do nothing
        }

        public void trackView(String actionName, long cost, String actionIds) {
            // do nothing
        }

        public void trackView(String actionName) {
            // do nothing
        }

        public void trackVerifySource(String featureKey) {
            // do nothing
        }
    };

    void trackEvent(String category, String action, String name, String value);

    void trackEvent(String category, String action);

    void trackView(String actionName, long cost, String actionIds);

    void trackView(String actionName);

    void trackVerifySource(String featureKey);

}
