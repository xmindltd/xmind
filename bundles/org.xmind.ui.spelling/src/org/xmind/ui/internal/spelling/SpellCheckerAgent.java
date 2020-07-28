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
package org.xmind.ui.internal.spelling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;

import com.swabunga.spell.engine.Configuration;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;

public class SpellCheckerAgent {

    private static final SpellCheckerAgent instance = new SpellCheckerAgent();

    private SpellChecker spellChecker;

    private Configuration configuration;

    private List<ISpellCheckerVisitor> listeners = new ArrayList<ISpellCheckerVisitor>();

    public static void resetSpellChecker() {
        getInstance().spellChecker = null;
    }

    public static void visitSpellChecker(ISpellCheckerVisitor visitor) {
        getInstance().doVisitSpellChecker(visitor);
    }

    public static void setConfigurations(IPreferenceStore prefStore) {
        getInstance().doSetConfigurations(prefStore);
    }

    public static void migrateUserDictFile() {
        getInstance().doMigrateUserDictFile();
    }

    public static void addListener(ISpellCheckerVisitor listener) {
        getInstance().listeners.add(listener);
    }

    public static void removeListener(ISpellCheckerVisitor listener) {
        getInstance().listeners.remove(listener);
    }

    private synchronized void doVisitSpellChecker(
            final ISpellCheckerVisitor visitor) {
        if (spellChecker != null) {
            visitor.handleWith(spellChecker);
            return;
        }

        new Job(Messages.loadingSpellChecker) {
            protected IStatus run(IProgressMonitor monitor) {
                return loadSpellChecker(monitor, visitor);
            }
        }.schedule();
    }

    public static void updateSpellChecker() {
        getInstance().doUpdateSpellChecker();
    }

    private synchronized void doUpdateSpellChecker() {
        new Job(Messages.loadingSpellChecker) {
            protected IStatus run(IProgressMonitor monitor) {
                return loadSpellChecker(monitor, null);
            }
        }.schedule();
    }

    private IStatus loadSpellChecker(IProgressMonitor monitor,
            ISpellCheckerVisitor visitor) {
        monitor.beginTask(null, 4);

        monitor.subTask(Messages.creatingSpellCheckerInstance);
        SpellChecker spellChecker = new SpellChecker();
        monitor.worked(1);

        // Load system dictionaries
        monitor.subTask(Messages.addingSystemDictionary);
        addSystemDictionaries(spellChecker);
        monitor.worked(1);

        // Load user dictionaries and words
        monitor.subTask(Messages.addingUserDictionary);
        addUserDictionaries(spellChecker);
        monitor.worked(1);

        // Load configurations
        monitor.subTask(Messages.initializingSpellingSettings);
        setConfigurations(spellChecker);
        monitor.worked(1);

        this.spellChecker = spellChecker;

        monitor.subTask(Messages.notifyingSpellingVisitors);

        notifyVisitor(visitor);
        notifyListeners();

        monitor.done();

        return new Status(IStatus.OK, SpellingPlugin.PLUGIN_ID,
                "Finish loading spell checker"); //$NON-NLS-1$
    }

    private void notifyVisitor(ISpellCheckerVisitor visitor) {
        if (visitor != null) {
            visitor.handleWith(spellChecker);
        }
    }

    private void notifyListeners() {
        if (listeners != null) {
            for (ISpellCheckerVisitor listener : listeners) {
                if (listener != null) {
                    listener.handleWith(spellChecker);
                }
            }
        }
    }

    private void setConfigurations(SpellChecker spellChecker) {
        configuration = spellChecker.getConfiguration();
        doSetConfigurations(SpellingPlugin.getDefault().getPreferenceStore());
    }

    private void addUserDictionaries(SpellChecker spellChecker) {
        for (ISpellCheckerDescriptor descriptor : SpellCheckerRegistry
                .getInstance().getDescriptors()) {
            try {
                if (descriptor.isEnabled()) {
                    spellChecker.addDictionary(
                            new SpellDictionaryHashMap(new InputStreamReader(
                                    descriptor.openStream(), "utf-16le"))); //$NON-NLS-1$
                }
            } catch (IOException e) {
                SpellingPlugin.log(e);
            }
        }

//        File userDict = FileUtils.ensureFileParent(new File(Core.getWorkspace()
//                .getAbsolutePath("spelling/user.dict"))); //$NON-NLS-1$
        File userDictFile = getUserDictFile();
        if (!userDictFile.exists()) {
            if (userDictFile.getParentFile() != null) {
                userDictFile.getParentFile().mkdirs();
            }
            try {
                new FileOutputStream(userDictFile).close();
            } catch (IOException ignore) {
            }
        }
        try {
            spellChecker.setUserDictionary(
                    new SpellDictionaryHashMap(userDictFile));
        } catch (IOException e) {
            SpellingPlugin.log(e);
        }
    }

    private void addSystemDictionaries(SpellChecker spellChecker) {
        if ((!SpellingPlugin.getDefault().getPreferenceStore()
                .getBoolean(SpellingPlugin.DEFAULT_SPELLING_CHECKER_INVISIBLE))
                && (!SpellingPlugin.getDefault().getPreferenceStore()
                        .getBoolean(
                                SpellingPlugin.DEFAULT_SPELLING_CHECKER_DISABLED))) {
            loadDictionariesFromBundle(spellChecker, "net.sourceforge.jazzy", //$NON-NLS-1$
                    "dict/"); //$NON-NLS-1$
        }

        loadDictionariesFromBundle(spellChecker, SpellingPlugin.PLUGIN_ID,
                "dict/"); //$NON-NLS-1$
    }

    private void loadDictionariesFromBundle(SpellChecker spellChecker,
            String pluginId, String dirPath) {
        Bundle bundle = Platform.getBundle(pluginId);
        if (bundle != null) {
            Enumeration<String> dicts = bundle.getEntryPaths(dirPath);
            while (dicts.hasMoreElements()) {
                String path = dicts.nextElement();
                if (path.endsWith(".dic") || path.endsWith(".dict")) { //$NON-NLS-1$ //$NON-NLS-2$
                    URL uri = bundle.getEntry(path);
                    if (uri != null) {
                        try {
                            loadDictionaryFromURL(spellChecker, uri);
                        } catch (IOException e) {
                            SpellingPlugin.log(e,
                                    "Failed to load dictionary from platform://" //$NON-NLS-1$
                                            + bundle.getSymbolicName() + "/" //$NON-NLS-1$
                                            + path);
                        }
                    }
                } else if (path.endsWith("/")) { //$NON-NLS-1$
                    loadDictionariesFromBundle(spellChecker, pluginId, path);
                }
            }
        }
    }

    private void loadDictionaryFromURL(SpellChecker spellChecker, URL uri)
            throws IOException {
        InputStream dictStream = uri.openStream();
        try {
            InputStreamReader dictReader = new InputStreamReader(dictStream,
                    "utf-8"); //$NON-NLS-1$
            try {
                SpellDictionary dict = new SpellDictionaryHashMap(dictReader);
                spellChecker.addDictionary(dict);
            } finally {
                dictReader.close();
            }
        } finally {
            dictStream.close();
        }
    }

    private void doSetConfigurations(IPreferenceStore ps) {
        if (configuration == null)
            return;

        configuration.setBoolean(Configuration.SPELL_IGNOREDIGITWORDS,
                ps.getBoolean(Configuration.SPELL_IGNOREDIGITWORDS));
        configuration.setBoolean(Configuration.SPELL_IGNOREINTERNETADDRESSES,
                ps.getBoolean(Configuration.SPELL_IGNOREINTERNETADDRESSES));
        configuration.setBoolean(Configuration.SPELL_IGNOREMIXEDCASE,
                ps.getBoolean(Configuration.SPELL_IGNOREMIXEDCASE));
        configuration.setBoolean(
                Configuration.SPELL_IGNORESENTENCECAPITALIZATION, ps.getBoolean(
                        Configuration.SPELL_IGNORESENTENCECAPITALIZATION));
        configuration.setBoolean(Configuration.SPELL_IGNOREUPPERCASE,
                ps.getBoolean(Configuration.SPELL_IGNOREUPPERCASE));
    }

    private void doMigrateUserDictFile() {
        File newFile = getUserDictFile();
        if (newFile.exists())
            return;

        Location instanceLocation = Platform.getInstanceLocation();
        if (instanceLocation == null)
            return;

        URL instanceURL = instanceLocation.getURL();
        if (instanceURL == null)
            return;

        try {
            instanceURL = FileLocator.toFileURL(instanceURL);
        } catch (IOException e) {
        }
        File instanceDir = new File(instanceURL.getFile());
        if (!instanceDir.exists())
            return;

        File oldFile = new File(new File(instanceDir, ".xmind"), //$NON-NLS-1$
                "spelling/user.dict"); //$NON-NLS-1$
        if (oldFile.exists()) {
            moveUserDictFile(oldFile, newFile);
            return;
        }

        oldFile = new File(instanceDir, "spelling/user.dict"); //$NON-NLS-1$
        if (oldFile.exists()) {
            moveUserDictFile(oldFile, newFile);
            return;
        }
    }

    private static void moveUserDictFile(File oldFile, File newFile) {
        if (newFile.getParentFile() != null) {
            newFile.getParentFile().mkdirs();
        }
        boolean moved = oldFile.renameTo(newFile);
        if (!moved) {
            SpellingPlugin.getDefault().getLog()
                    .log(new Status(IStatus.WARNING, SpellingPlugin.PLUGIN_ID,
                            "Failed to migrate old user dict file: " //$NON-NLS-1$
                                    + oldFile.getAbsolutePath()));
        }
    }

    private static File getUserDictFile() {
        return new File(SpellingPlugin.getBundleDataPath("user.dict")); //$NON-NLS-1$
    }

    private static SpellCheckerAgent getInstance() {
        return instance;
    }

}
