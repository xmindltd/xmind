package org.xmind.ui.blackbox;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.prefs.PrefConstants;

/**
 * @author Jason Wong
 */
public class BlackBoxManager {

    private static BlackBoxManager instance = new BlackBoxManager();

    public static final String FILE_BLACKBOX = "blackbox"; //$NON-NLS-1$

    public static final String FILE_INFO_EXT = ".xbkinfo"; //$NON-NLS-1$

    public static final int MD5_LENGTH = 32;

    public static final int TIMESTAMP_LENGTH = 13;

    public static final String FILE_XMIND_EXT = ".xbkmap"; //$NON-NLS-1$

    private static final String BREAK = "."; //$NON-NLS-1$

    private static List<IBlackBoxVersion> versionTrashes = new ArrayList<IBlackBoxVersion>();

    private static List<IBlackBoxMap> mapTrashes = new ArrayList<IBlackBoxMap>();

    private static String pluginPath;

    private static String blackboxPath;

    private IBlackBoxLibrary library;

    private BlackBoxManager() {
    }

    public static BlackBoxManager getInstance() {
        return instance;
    }

    public static void log(String message, Throwable e) {
        IStatus status = new Status(
                e == null ? IStatus.WARNING : IStatus.ERROR, message,
                BlackBox.ID, e);
        MindMapUIPlugin.getDefault().getLog().log(status);
    }

    public static String getPluginPath() {
        if (pluginPath != null)
            return pluginPath;

        Bundle bundle = MindMapUIPlugin.getDefault().getBundle();
        String parentFile = Platform.getStateLocation(bundle).toFile()
                .getParent();
        pluginPath = new File(parentFile, BlackBox.ID).getAbsolutePath();
        return pluginPath;
    }

    public static String getBlackboxPath() {
        if (blackboxPath != null && new File(blackboxPath).exists())
            return blackboxPath;

        File blackbox = new File(getPluginPath(), FILE_BLACKBOX);
        FileUtils.ensureDirectory(blackbox);
        if (blackbox.exists())
            blackboxPath = blackbox.getAbsolutePath();
        return blackboxPath;
    }

    public static void addTrash(IBlackBoxVersion version) {
        if (version == null)
            return;
        versionTrashes.add(version);
    }

    public static void addTrash(IBlackBoxMap map) {
        if (map == null)
            return;
        mapTrashes.add(map);
    }

    public static void clearTrashes() {
        if (mapTrashes.size() > 0) {
            for (IBlackBoxMap m : mapTrashes) {
                Set<IBlackBoxVersion> versions = m.getVersions();
                for (IBlackBoxVersion v : versions) {
                    if (!versionTrashes.contains(v)) {
                        versionTrashes.add(v);
                    }
                }
                File metaFile = new File(getBlackboxPath(), m.getID()
                        + FILE_INFO_EXT);
                if (metaFile != null && metaFile.exists()) {
                    metaFile.delete();
                }
            }
            mapTrashes.clear();
        }

        if (versionTrashes.size() > 0) {
            for (IBlackBoxVersion v : versionTrashes) {
                File trash = v.getFile();
                if (trash != null && trash.exists()) {
                    trash.delete();
                }
            }
            versionTrashes.clear();
        }
    }

    public static String convertToMD5(String source) {
        if (source == null)
            return null;

        String plainText = source;
        try {
            StringBuffer buf = new StringBuffer();
            MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$

            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0"); //$NON-NLS-1$
                }
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (Exception e) {
            BlackBoxManager.log("Error occurred while generate map id.", e); //$NON-NLS-1$
            return null;
        }
    }

    public void doBackup(String source) {
        if (!isEnable() || source == null)
            return;

        IBlackBoxLibrary library = getLibrary();
        IBlackBoxMap map = library.findMapBySource(source);
        String mapID = map == null ? getMapID(source) : map.getID();
        String timestamp = String.valueOf(System.currentTimeMillis());
        boolean isReliable = !getLibrary().isSavedMap(source);
        File dest = null;
        try {
            dest = backup(source, mapID, timestamp, isReliable);
        } catch (IOException e) {
            BlackBoxManager.log("Error occurred while backup src file.", //$NON-NLS-1$
                    e);
            return;
        }

        if (dest != null) {
            BlackBoxVersion version = new BlackBoxVersion(mapID, timestamp,
                    isReliable);
            version.setFile(dest);
            library.addVersion(source, mapID, version);
        }
    }

    public IBlackBoxMap[] getMaps() {
        return getLibrary().getMaps();
    }

    public boolean removeMap(IBlackBoxMap map) {
        if (map == null)
            return false;
        return getLibrary().removeMap(map);
    }

    public boolean removeVersion(String source, String timestamp) {
        return removeVersion(findMapBySource(source), timestamp);
    }

    public boolean removeVersion(IBlackBoxMap map, String timestamp) {
        if (map == null)
            return false;
        return getLibrary().removeVersion(map, timestamp);
    }

    public IBlackBoxMap findMapBySource(String source) {
        if (source != null)
            return getLibrary().findMapBySource(source);
        return null;
    }

    public IBlackBoxMap findMapByID(String id) {
        if (id != null)
            return getLibrary().findMapByID(id);
        return null;
    }

    public IBlackBoxLibrary getLibrary() {
        if (library == null)
            library = new BlackBoxLibrary();
        return library;
    }

    public void removeSavedMap(String source) {
        if (!isEnable() || source == null)
            return;

        getLibrary().removeSavedMap(source);
    }

    /**
     * @return dest file.
     */
    private File backup(String source, String mapID, String timestamp,
            boolean isReliable) throws IOException {
        File src = new File(source);
        if (!src.exists() || mapID == null)
            return null;

        String destName = getDestName(mapID, timestamp, isReliable);
        File dest = new File(getDestPath(mapID), destName);
        if (!dest.exists())
            dest.createNewFile();

        FileUtils.copy(src, dest);
        return dest;
    }

    private String getDestName(String mapID, String timestamp,
            boolean isReliable) {
        if (isReliable)
            return mapID + BREAK + "a" + BREAK + timestamp + FILE_XMIND_EXT; //$NON-NLS-1$

        return mapID + BREAK + "b" + BREAK + timestamp + FILE_XMIND_EXT; //$NON-NLS-1$
    }

    private String getDestPath(String mapID) throws IOException {
        return getBlackboxPath();
    }

    private String getMapID(String source) {
        return convertToMD5(source);
    }

    private boolean isEnable() {
        return MindMapUIPlugin.getDefault().getPreferenceStore()
                .getBoolean(PrefConstants.AUTO_BACKUP_ENABLE);
    }

}
