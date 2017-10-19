package org.xmind.ui.blackbox;

import java.io.File;

/**
 * @author Jason Wong
 */
public class BlackBoxVersion implements IBlackBoxVersion {

    private String mapID;

    private String timestamp;

    private boolean reliable;

    private File file;

    public BlackBoxVersion(String mapID, String timestamp, boolean reliable) {
        this.mapID = mapID;
        this.timestamp = timestamp;
        this.reliable = reliable;
    }

    public String getMapID() {
        return mapID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isReliable() {
        return reliable;
    }

    public IBlackBoxMap getMap() {
        return BlackBoxManager.getInstance().findMapByID(mapID);
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public int compareTo(IBlackBoxVersion anotherVersion) {
        long timestamp1 = Long.parseLong(this.timestamp);
        long timestamp2 = Long.parseLong(anotherVersion.getTimestamp());
        if (timestamp1 == timestamp2)
            return 0;
        if (timestamp1 > timestamp2)
            return 1;
        return -1;
    }

}
