package org.xmind.ui.blackbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.event.CoreEventSupport;

/**
 * @author Jason Wong
 */
public class BlackBoxLibrary implements IBlackBoxLibrary, ICoreEventSource {

    private static final String MAP_REMOVE = "mapRemove"; //$NON-NLS-1$

    private static final String VERSION_ADD = "versionAdd"; //$NON-NLS-1$

    private static final String VERSION_REMOVE = "versionRemove"; //$NON-NLS-1$

    private List<IBlackBoxMap> maps = new ArrayList<IBlackBoxMap>();

    private List<String> savedSrcs = new ArrayList<String>();

    private File blackbox = null;

    private ICoreEventSupport coreEventSupport;

    public BlackBoxLibrary() {
        load();
    }

    public synchronized void addVersion(String source, String mapID,
            IBlackBoxVersion version) {
        IBlackBoxMap map = findMapBySource(source);
        if (map == null) {
            BlackBoxMap m = new BlackBoxMap(this, mapID);
            m.setSource(source);
            map = m;
            this.maps.add(map);
        }

        Set<IBlackBoxVersion> versions = map.getVersions();
        if (versions.size() == 0) {
            versions.add(version);
        } else if (versions.size() < 3 && !version.isReliable()) {
            versions.add(version);
        } else {
            IBlackBoxVersion trash = null;
            trash = findTrashVersion(versions, version.isReliable());
            versions.remove(trash);
            versions.add(version);
            BlackBoxManager.addTrash(trash);
        }

        if (!isSavedMap(source)) {
            this.savedSrcs.add(source);
        }

        save(map);
        fireTargetEvent(VERSION_ADD, version);
    }

    public synchronized boolean removeMap(IBlackBoxMap map) {
        if (this.maps.remove(map)) {
            BlackBoxManager.addTrash(map);
            save(map);
            fireTargetEvent(MAP_REMOVE, map);
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean removeVersion(IBlackBoxMap map, String timestamp) {
        Set<IBlackBoxVersion> versions = map.getVersions();
        for (IBlackBoxVersion v : versions) {
            if (v.getTimestamp().equals(timestamp)) {
                if (versions.remove(v)) {
                    BlackBoxManager.addTrash(v);
                    save(map);
                    if (versions.size() == 0) {
                        removeMap(map);
                        fireTargetEvent(MAP_REMOVE, map);
                    }
                    fireTargetEvent(VERSION_REMOVE, v);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public synchronized IBlackBoxMap[] getMaps() {
        load();
        return this.maps.toArray(new IBlackBoxMap[this.maps.size()]);
    }

    public synchronized IBlackBoxMap findMapBySource(String source) {
        for (IBlackBoxMap map : this.maps) {
            if (source.equals(map.getSource()))
                return map;
        }
        return null;
    }

    public synchronized IBlackBoxMap findMapByID(String id) {
        for (IBlackBoxMap map : this.maps) {
            if (map.getID().equals(id))
                return map;
        }
        return null;
    }

    public synchronized boolean isSavedMap(String source) {
        if (source == null)
            return false;

        return this.savedSrcs.contains(source);
    }

    public synchronized void removeSavedMap(String source) {
        if (!(savedSrcs.size() > 0) || source == null)
            return;
        if (isSavedMap(source)) {
            savedSrcs.remove(source);
        }
    }

    private void load() {
        File blackbox = new File(BlackBoxManager.getBlackboxPath());
        if (!blackbox.exists())
            return;

        maps.clear();
        File[] infos = blackbox.listFiles();
        for (File info : infos) {
            String fileName = info.getName();
            String[] parts = fileName.split("\\."); //$NON-NLS-1$s

            if (fileName.contains(BlackBoxManager.FILE_INFO_EXT)) {
                try {
                    loadBackupInfo(info, parts);
                } catch (IOException e) {
                    BlackBoxManager
                            .log("Error occurred while loading maps.", e); //$NON-NLS-1$
                }
            } else if (fileName.contains(BlackBoxManager.FILE_XMIND_EXT)) {
                loadMapInfo(info, parts);
            }
        }
    }

    private synchronized void save(IBlackBoxMap map) {
        File metaFile = getMetaFile(map.getID());
        if (!metaFile.exists()) {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(metaFile));
                writer.write(map.getSource());
                writer.flush();
            } catch (IOException e) {
            } finally {
                try {
                    if (writer != null)
                        writer.close();
                } catch (IOException e) {
                }
            }
        }
        BlackBoxManager.clearTrashes();
    }

    private File getMetaFile(String mapID) {
        if (blackbox == null) {
            blackbox = new File(BlackBoxManager.getBlackboxPath());
        }

        File metaFile = new File(blackbox, mapID
                + BlackBoxManager.FILE_INFO_EXT);
        return metaFile;
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerGlobalListener(type, listener);
    }

    private void fireTargetEvent(String eventType, Object target) {
        getCoreEventSupport().dispatchTargetChange(this, eventType, target);
    }

    public ICoreEventSupport getCoreEventSupport() {
        if (coreEventSupport == null) {
            coreEventSupport = new CoreEventSupport();
        }
        return coreEventSupport;
    }

    /**
     * If isReliable value is true, returns the last saved the reliable version.
     * else returns the older save unreliable version.
     * 
     * @param isReliable
     * @return The IBlackBoxVersion will be replaced
     */
    private IBlackBoxVersion findTrashVersion(Set<IBlackBoxVersion> versions,
            boolean isReliable) {
        if (isReliable) {
            for (IBlackBoxVersion v : versions) {
                if (v.isReliable()) {
                    return v;
                }
            }
        }

        IBlackBoxVersion trash = null;
        for (IBlackBoxVersion v : versions) {
            if (v.isReliable())
                continue;

            if (trash == null) {
                trash = v;
                continue;
            }
            trash = trash.compareTo(v) > 0 ? v : trash;
        }
        return trash;
    }

    private void loadBackupInfo(File info, String[] parts) throws IOException {
        BlackBoxMap map = null;
        String mapID = null;

        for (String part : parts) {
            if (part.length() == BlackBoxManager.MD5_LENGTH) {
                mapID = part;
                break;
            }
        }
        if (mapID == null)
            return;

        map = (BlackBoxMap) findMapByID(mapID);
        if (map == null) {
            map = new BlackBoxMap(this, mapID);
            maps.add(map);
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(info));
            StringBuffer sb = new StringBuffer();
            while (reader.ready()) {
                sb.append(reader.readLine());
            }
            String source = sb.toString();
            map.setSource(source);
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    private void loadMapInfo(File info, String[] parts) {
        BlackBoxMap map = null;
        String mapID = null;
        boolean reliable = false;
        String timestamp = null;

        for (String part : parts) {
            if (part.length() == BlackBoxManager.MD5_LENGTH)
                mapID = part;
            else if (part.length() == 1)
                reliable = "a".equals(part) ? true : false; //$NON-NLS-1$
            else if (part.length() == BlackBoxManager.TIMESTAMP_LENGTH)
                timestamp = part;
        }
        if (mapID == null || timestamp == null)
            return;

        map = (BlackBoxMap) findMapByID(mapID);
        if (map == null) {
            map = new BlackBoxMap(this, mapID);
            maps.add(map);
        }

        Set<IBlackBoxVersion> versions = map.getVersions();
        if (versions.size() < 3) {
            BlackBoxVersion v = new BlackBoxVersion(mapID, timestamp, reliable);
            v.setFile(info);
            versions.add(v);
        }
    }
}
