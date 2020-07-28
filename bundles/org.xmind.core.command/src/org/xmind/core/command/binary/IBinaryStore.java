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
package org.xmind.core.command.binary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A binary store stores binary entries with unique names. It may use local
 * memory as the cache pool, but local files are more recommended.
 * 
 * @author Frank Shaka
 * 
 */
public interface IBinaryStore {

    /**
     * Returns a binary entry associated with the given name.
     * 
     * @param entryName
     *            the name of a binary entry
     * @return a binary entry associated with the given name, or
     *         <code>null</code> if the entry is not found
     */
    IBinaryEntry getEntry(String entryName);

    /**
     * Determines whether a binary entry associated with the given name exists
     * in this binary store.
     * 
     * @param entryName
     *            the name of a binary entry
     * @return <code>true</code> if the name if found, or <code>false</code>
     *         otherwise
     */
    boolean hasEntry(String entryName);

    /**
     * Clears this binary store and disposes all binary entries. All caches will
     * be flushed.
     */
    void clear();

    /**
     * Determines whether this binary store contains any binary entry or not.
     * 
     * @return <code>true</code> if this binary store has no entries, or
     *         <code>false</code> otherwise
     */
    boolean isEmpty();

    /**
     * Returns the number of binary entries exists in this binary store.
     * 
     * @return the integer number of binary entries
     */
    int size();

    /**
     * Returns an iterator that iterates over all existing entry names.
     * 
     * @return an iterator instance, never <code>null</code>
     */
    Iterator<String> entryNames();

    /**
     * Removes and disposes a binary entry associated with the given name from
     * this binary store.
     * 
     * @param entryName
     *            the name of a binary entry
     * @return <code>true</code> if the binary entry existed in this binary
     *         store and has been removed successfully, or <code>false</code>
     *         otherwise
     */
    boolean removeEntry(String entryName);

    /**
     * Adds a binary entry into this binary store and associate it with the
     * given name. Note that any existing binary entry associated with the given
     * will be removed and disposed in prior.
     * 
     * @param entryName
     *            the name to be associated with the binary entry
     * @param entry
     *            the binary entry to be added into this binary store
     */
    void addEntry(String entryName, IBinaryEntry entry);

    /**
     * Caches the contents of the given input stream in this binary store as a
     * new binary entry. A unique name will be generated for the new binary
     * entry.
     * 
     * @param monitor
     *            the progress monitor, or <code>null</code> if progress
     *            monitoring is not required
     * @param source
     *            the input stream providing binary contents
     * @return a new binary entry with name associated, whose name can be
     *         retrieved via {@link INamedEntry#getName()}
     * @throws IOException
     *             if any IO error occurs
     * @throws InterruptedException
     *             if the process is canceled by detecting that
     *             {@link IProgressMonitor#isCanceled()} returns
     *             <code>true</code>
     */
    INamedEntry addEntry(IProgressMonitor monitor, InputStream source)
            throws IOException, InterruptedException;

    /**
     * Caches the contents of the given input stream in this binary store as a
     * new binary entry and associate it with the given name. Note that any
     * existing binary entry associated with the given name will be removed and
     * disposed in prior.
     * 
     * @param monitor
     *            the progress monitor, or <code>null</code> if progress
     *            monitoring is not required
     * @param entryName
     *            the name to be associated with the new binary entry
     * @param source
     *            the input stream providing binary contents
     * @return a new binary entry
     * @throws IOException
     *             if any IO error occurs
     * @throws InterruptedException
     *             if the process is canceled by detecting that
     *             {@link IProgressMonitor#isCanceled()} returns
     *             <code>true</code>
     */
    IBinaryEntry addEntry(IProgressMonitor monitor, String entryName,
            InputStream source) throws IOException, InterruptedException;

}
