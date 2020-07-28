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
package org.xmind.cathy.internal;

import java.util.ArrayList;
import java.util.List;

import org.xmind.core.licensing.ILicenseAgent;
import org.xmind.core.licensing.ILicenseChangedListener;
import org.xmind.core.licensing.ILicenseKeyHeader;

/**
 * @author Frank Shaka
 *
 */
public class LicenseAgentProxy implements ILicenseAgent {

    private ILicenseAgent delegate;

    private final List<ILicenseChangedListener> listeners;

    /**
     * 
     */
    public LicenseAgentProxy() {
        this.delegate = null;
        this.listeners = new ArrayList<ILicenseChangedListener>(1);
    }

    /**
     * @param delegate
     *            the delegate to set
     */
    public void setDelegate(ILicenseAgent delegate) {
        ILicenseAgent oldDelegate = this.delegate;
        if (delegate == oldDelegate)
            return;

        if (oldDelegate != null) {
            for (ILicenseChangedListener listener : listeners) {
                oldDelegate.removeLicenseChangedListener(listener);
            }
        }
        this.delegate = delegate;
        if (delegate != null) {
            for (ILicenseChangedListener listener : listeners) {
                delegate.addLicenseChangedListener(listener);
            }
        }

        for (ILicenseChangedListener listener : listeners) {
            listener.licenseChanged(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.licensing.ILicenseAgent#getLicenseType()
     */
    @Override
    public int getLicenseType() {
        return delegate == null ? NOT_LICENSED : delegate.getLicenseType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.licensing.ILicenseAgent#getLicenseeName()
     */
    @Override
    public String getLicenseeName() {
        return delegate == null ? null : delegate.getLicenseeName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.licensing.ILicenseAgent#getLicenseKeyHeader()
     */
    @Override
    public ILicenseKeyHeader getLicenseKeyHeader() {
        return delegate == null ? null : delegate.getLicenseKeyHeader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.licensing.ILicenseAgent#addLicenseChangedListener(org.
     * xmind.core.licensing.ILicenseChangedListener)
     */
    @Override
    public void addLicenseChangedListener(ILicenseChangedListener listener) {
        listeners.add(listener);
        if (delegate != null) {
            delegate.addLicenseChangedListener(listener);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.licensing.ILicenseAgent#removeLicenseChangedListener(org.
     * xmind.core.licensing.ILicenseChangedListener)
     */
    @Override
    public void removeLicenseChangedListener(ILicenseChangedListener listener) {
        listeners.remove(listener);
        if (delegate != null) {
            delegate.removeLicenseChangedListener(listener);
        }
    }

}
