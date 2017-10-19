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
package org.xmind.core.internal;

import org.xmind.core.IEntryStreamNormalizer;
import org.xmind.core.ISerializingBase;

/**
 * @author Frank Shaka
 *
 */
public abstract class AbstractSerializingBase implements ISerializingBase {

    private String creatorName;
    private String creatorVersion;
    private IEntryStreamNormalizer encryptionHandler;

    /**
     * 
     */
    protected AbstractSerializingBase() {
        this.creatorName = null;
        this.creatorVersion = null;
        this.encryptionHandler = null;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getCreatorVersion() {
        return creatorVersion;
    }

    public void setCreatorName(String name) {
        this.creatorName = name;
    }

    public void setCreatorVersion(String version) {
        this.creatorVersion = version;
    }

    public IEntryStreamNormalizer getEntryStreamNormalizer() {
        return encryptionHandler;
    }

    public void setEntryStreamNormalizer(
            IEntryStreamNormalizer encryptionHandler) {
        this.encryptionHandler = encryptionHandler;
    }

}
