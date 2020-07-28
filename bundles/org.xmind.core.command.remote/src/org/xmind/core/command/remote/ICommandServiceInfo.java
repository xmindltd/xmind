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

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;

/**
 * This class provides information and metadata of a remote/local command
 * service.
 * 
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @author Frank Shaka
 */
public interface ICommandServiceInfo extends IAdaptable {

    /**
     * A metadata key for the version of this command service (value is
     * <code>"ver"</code>).
     */
    String VERSION = "ver"; //$NON-NLS-1$

    /**
     * A metadata key for the name of the command service provider (value is
     * <code>"cli"</code>).
     */
    String CLIENT_NAME = "cli"; //$NON-NLS-1$

    /**
     * A metadata key for the version of the command service provider (value is
     * <code>"cliver"</code>).
     */
    String CLIENT_VERSION = "cliver"; //$NON-NLS-1$

    /**
     * A metadata key for the symbolic name of the command service provider
     * (value is <code>"cliid"</code>).
     */
    String CLIENT_SYMBOLIC_NAME = "cliid"; //$NON-NLS-1$

    /**
     * A metadata key for the build id of the command service provider (value is
     * <code>"clibid"</code>).
     */
    String CLIENT_BUILD_ID = "clibid"; //$NON-NLS-1$

    /**
     * The metadata value for the current local service version (value is
     * <code>"1"</code>).
     */
    String CURRENT_VERSION = "1"; //$NON-NLS-1$

    /**
     * Gets the identifier of this command service. An identifier should be
     * unique within a specific domain (e.g. a local area network). To obtain a
     * human-readable name, use {@link #getName()}.
     * 
     * @return the identifier of this service, or <code>null</code> if the
     *         command service is not identified (e.g. an unreigstered local
     *         command server)
     */
    IIdentifier getId();

    /**
     * Gets the name of this command service. This name is suitable to display
     * to end users, but it's not guaranteed to be unique. To obtain a unique
     * identifier, use {@link #getId()}.
     * 
     * @return the name of this service, possibly an empty string, but never
     *         <code>null</code>
     */
    String getName();

    /**
     * Retrieves the metadata value of this command service corresponding to the
     * given key.
     * 
     * @param key
     *            the key of the metadata
     * @return the value of the metadata, or </code>null</code> if the specified
     *         metadata is not found
     */
    String getMetadata(String key);

    /**
     * Returns an iterator that iterates over all existing metadata keys.
     * 
     * @return an iterator that iterates over all existing metadata keys
     */
    Iterator<String> metadataKeys();

}
