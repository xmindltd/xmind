/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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
package org.xmind.core.command.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * An implementation of {@link ICommandServiceInfo} with abilities to set
 * information and metadata. Note that all <code>set-</code> methods are for
 * internal use only and are not recommended for direct invocation by clients.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @author Frank Shaka
 */
public class CommandServiceInfo implements ICommandServiceInfo {

    private IIdentifier id = null;

    private String name = ""; //$NON-NLS-1$

    private Map<String, String> metadata = new HashMap<String, String>();

    /**
     * Construct a new command service information object.
     */
    public CommandServiceInfo() {
        metadata.put(VERSION, CURRENT_VERSION);
        IProduct product = Platform.getProduct();
        if (product != null) {
            metadata.put(CLIENT_NAME, product.getName());
            metadata.put(CLIENT_SYMBOLIC_NAME, product.getId());
            Bundle bundle = product.getDefiningBundle();
            if (bundle != null) {
                Version version = bundle.getVersion();
                metadata.put(
                        CLIENT_VERSION,
                        String.format(
                                "%s.%s.%s", //$NON-NLS-1$
                                version.getMajor(), version.getMinor(),
                                version.getMicro()));
                metadata.put(CLIENT_BUILD_ID, version.toString());
            }
        }
    }

    /**
     * Constructs a new command service information object and copy all
     * attributes from the given source.
     * 
     * @param copy
     */
    public CommandServiceInfo(ICommandServiceInfo source) {
        this.id = source.getId();
        this.name = source.getName();
        Iterator<String> keys = source.metadataKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            metadata.put(key, source.getMetadata(key));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.command.remote.IRemoteCommandServiceInfo#getId()
     */
    public IIdentifier getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public synchronized String getMetadata(String key) {
        return metadata.get(key);
    }

    public synchronized Iterator<String> metadataKeys() {
        return new ArrayList<String>(metadata.keySet()).iterator();
    }

    public void setId(IIdentifier id) {
        this.id = id;
    }

    public void setName(String name) {
        if (name == null)
            name = ""; //$NON-NLS-1$
        this.name = name;
    }

    public void setMetadata(String key, String value) {
        if (value == null) {
            metadata.remove(key);
        } else {
            metadata.put(key, value);
        }
    }

    protected Map<String, String> getMetadataImpl() {
        return metadata;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CommandServiceInfo{id="); //$NON-NLS-1$
        sb.append(id);
        sb.append(",name="); //$NON-NLS-1$
        sb.append(name);
        sb.append(",metadata="); //$NON-NLS-1$
        sb.append(metadata);
        sb.append('}');
        return sb.toString();
    }
}
