/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.core.usagedata;

/**
 * This interface provides abilities to record usage data.
 * 
 * <p>
 * Usage data are recorded in the form of a collection of key-value pairs, where
 * the key is a unique string defined by the feature being used. Keys may
 * consist of sub-keys separated by slashes('<code>/</code>').
 * </p>
 * 
 * <p>
 * Features that wish to provide usage data should obtain a dedicated instance
 * of this interface via OSGi's service tracker. See below for example. If no
 * such instance is available, the default instance {@link #NULL} should be used
 * instead to prevent {@link NullPointerException}.
 * 
 * <pre>
 * public class MyBundleActivator implements BundleActivator {
 * 
 *     private ServiceTracker&lt;IUsageDataSampler, IUsageDataSampler&gt; usageDataService;
 * 
 *     public void start(BundleContext context) throws Exception {
 *         usageDataService = new ServiceTracker&lt;IUsageDataSampler, IUsageDataSampler&gt;(
 *                 context, IUsageDataSampler.class.getName(), null);
 *         usageDataService.open();
 *     }
 * 
 *     public void stop(BundleContext context) throws Exception {
 *         usageDataService.close();
 *         usageDataService = null;
 *     }
 * 
 *     public IUsageDataSampler getUsageDataSampler() {
 *         IUsageDataSampler sampler = usageDataService.get();
 *         return sampler == null ? IUsageDataSampler.NULL : sampler;
 *     }
 * }
 * </pre>
 * </p>
 * 
 * @author Frank Shaka
 */
public interface IUsageDataSampler {

    /**
     * A singleton instance to do nothing on sampling. This helps ensure that
     * sampling operations are always error-free no matter a dedicated sampling
     * service exists or not.
     */
    IUsageDataSampler NULL = new IUsageDataSampler() {
        @Override
        public void increase(String key) {
            // do nothing
        }

        @Override
        public void put(String key, String value) {
            // do nothing
        }

        @Override
        public void put(String key, long value) {
            // do nothing
        }
    };

    /**
     * Increases the numeric value of a specified item by one. This method is
     * useful when counting how many times a feature is triggered.
     * 
     * @param key
     *            the key of the item, must NOT be <code>null</code>
     * @throws IllegalArgumentException
     *             if the key is <code>null</code>
     */
    void increase(String key);

    /**
     * Put an item with the specified key and value directly into the item set.
     * 
     * @param key
     *            the key of the item, must NOT be <code>null</code>
     * @param value
     *            the value of the item, or <code>null</code> to delete the item
     * @throws IllegalArgumentException
     *             if the key is <code>null</code>
     */
    void put(String key, String value);

    /**
     * Put an item with the specified key and value directly into the item set.
     * 
     * @param key
     *            the key of the item, must NOT be <code>null</code>
     * @param value
     *            the value of the item
     * @throws IllegalArgumentException
     *             if the key is <code>null</code>
     */
    void put(String key, long value);

}
