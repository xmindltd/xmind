package org.xmind.ui.blackbox;

import java.io.File;

/**
 * @author Jason Wong
 */
public interface IBlackBoxVersion {

    String getMapID();

    String getTimestamp();

    boolean isReliable();

    IBlackBoxMap getMap();

    File getFile();

    int compareTo(IBlackBoxVersion anotherVersion);

}
