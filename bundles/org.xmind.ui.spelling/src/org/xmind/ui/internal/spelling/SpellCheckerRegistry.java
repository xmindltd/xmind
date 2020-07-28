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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;

/**
 * @author Frank Shaka
 * 
 */
public class SpellCheckerRegistry {

    private static class FileSpellCheckerDescriptor
            implements ISpellCheckerDescriptor {

        private File file;

        /**
         * Creates a new instance.
         */
        public FileSpellCheckerDescriptor(File file) {
            this.file = file;
        }

        /**
         * @return the file
         */
        public File getFile() {
            return file;
        }

        public String getName() {
            return file.getName();
        }

        public InputStream openStream() throws IOException {
            return new FileInputStream(file);
        }

        public void setEnabled(boolean enabled) {
            getPreferenceStore().setValue(getName(), enabled);
        }

        public boolean isEnabled() {
            return getPreferenceStore().getBoolean(getName());
        }

        private IPreferenceStore getPreferenceStore() {
            return SpellingPlugin.getDefault().getPreferenceStore();
        }

    }

    private static SpellCheckerRegistry instance = null;

    private List<ISpellCheckerDescriptor> descriptors = null;

    /**
     * @return the descriptors
     */
    public List<ISpellCheckerDescriptor> getDescriptors() {
        lazyLoad();
        return descriptors;
    }

    private void lazyLoad() {
        if (descriptors != null)
            return;
        doLazyLoad();
        if (descriptors == null)
            descriptors = Collections.emptyList();
    }

    private void doLazyLoad() {
        File dir = getUserDictDir();
        if (!dir.exists() || !dir.isDirectory() || !dir.canRead())
            return;

        File[] dictFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.startsWith("."); //$NON-NLS-1$
            }
        });
        for (File dictFile : dictFiles) {
            if (dictFile.isFile() && dictFile.canRead()) {
                if (descriptors == null)
                    descriptors = new ArrayList<ISpellCheckerDescriptor>();
                descriptors.add(new FileSpellCheckerDescriptor(dictFile));
            }
        }
    }

    private static File getUserDictDir() {
        return new File(SpellingPlugin.getBundleDataPath("user")); //$NON-NLS-1$
//        return new File(Core.getWorkspace().getAbsolutePath("spelling/user")); //$NON-NLS-1$
    }

    public String getImportableDictFileName(File sourceDictFile,
            List<String> nameExclusions) {
        String name = sourceDictFile.getName();
        int sepIndex = name.lastIndexOf('.');
        String prefix, suffix;
        if (sepIndex < 0) {
            prefix = name;
            suffix = ""; //$NON-NLS-1$
        } else {
            prefix = name.substring(0, sepIndex);
            suffix = name.substring(sepIndex);
        }
        File targetDictFile = createFile(getUserDictDir(), prefix, suffix,
                nameExclusions);

        FileSpellCheckerDescriptor descriptor = new FileSpellCheckerDescriptor(
                targetDictFile);
        return descriptor.getName();
    }

    public ISpellCheckerDescriptor importDictFile(File sourceDictFile,
            String name) throws IOException {
        if (name == null) {
            name = sourceDictFile.getName();
        }
        int sepIndex = name.lastIndexOf('.');
        String prefix, suffix;
        if (sepIndex < 0) {
            prefix = name;
            suffix = ""; //$NON-NLS-1$
        } else {
            prefix = name.substring(0, sepIndex);
            suffix = name.substring(sepIndex);
        }
//        File targetFile = FileUtils.ensureFileParent(createFile(
//                getUserDictDir(), FileUtils.getNoExtensionFileName(name),
//                FileUtils.getExtension(name)));
        File targetDictFile = createFile(getUserDictDir(), prefix, suffix,
                null);
        if (targetDictFile.getParentFile() != null) {
            targetDictFile.getParentFile().mkdirs();
        }
//        FileUtils.copy(sourceDictFile, targetFile);
        try {
            InputStream inp = new FileInputStream(sourceDictFile);
            try {
                OutputStream out = new FileOutputStream(targetDictFile);
                try {
                    byte[] buffer = new byte[4096];
                    int numRead;
                    while ((numRead = inp.read(buffer)) > 0) {
                        out.write(buffer, 0, numRead);
                    }
                } finally {
                    out.close();
                }
            } finally {
                inp.close();
            }
        } catch (IOException e) {
            SpellingPlugin.log(e,
                    "Failed to copy dict file into workspace while importing it."); //$NON-NLS-1$
        }
        if (descriptors == null || descriptors.isEmpty()) {
            descriptors = new ArrayList<ISpellCheckerDescriptor>();
        }
        FileSpellCheckerDescriptor descriptor = new FileSpellCheckerDescriptor(
                targetDictFile);
        descriptor.setEnabled(false);
        descriptors.add(descriptor);
        return descriptor;
    }

    private File createFile(File dir, String prefix, String suffix,
            List<String> nameExclusions) {
        int i = 1;
        File file = new File(dir, prefix + suffix);
        while (nameExclusions != null
                && nameExclusions.contains(file.getName())) {
            i++;
            file = new File(dir, prefix + " (" + i + ")" + suffix); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return file;
    }

    public void removeDictionary(ISpellCheckerDescriptor descriptor) {
        FileSpellCheckerDescriptor fileDescriptor = (FileSpellCheckerDescriptor) descriptor;
        fileDescriptor.setEnabled(false);
        if (descriptors != null && !descriptors.isEmpty()) {
            descriptors.remove(descriptor);
        }
        File file = fileDescriptor.getFile();
        file.delete();
        SpellCheckerAgent.resetSpellChecker();
    }

    public static void migrateUserDictDir() {
        File newDir = getUserDictDir();
        if (newDir.exists() && newDir.isDirectory())
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

        File oldDir = new File(new File(instanceDir, ".xmind"), //$NON-NLS-1$
                "spelling/user"); //$NON-NLS-1$
        if (oldDir.exists() && oldDir.isDirectory()) {
            moveUserDictDir(oldDir, newDir);
            return;
        }

        oldDir = new File(instanceDir, "spelling/user.dict"); //$NON-NLS-1$
        if (oldDir.exists() && oldDir.isDirectory()) {
            moveUserDictDir(oldDir, newDir);
            return;
        }

    }

    private static void moveUserDictDir(File oldDir, File newDir) {
        if (newDir.getParentFile() != null) {
            newDir.getParentFile().mkdirs();
        }
        boolean moved = oldDir.renameTo(newDir);
        if (!moved) {
            SpellingPlugin.getDefault().getLog()
                    .log(new Status(IStatus.WARNING, SpellingPlugin.PLUGIN_ID,
                            "Failed to migrate old user added dict directory: " //$NON-NLS-1$
                                    + oldDir.getAbsolutePath()));
        }
    }

    /**
     * @return the registry
     */
    public static SpellCheckerRegistry getInstance() {
        if (instance == null)
            instance = new SpellCheckerRegistry();
        return instance;
    }

}
