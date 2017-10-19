package org.xmind.ui.blackbox;

import java.util.Set;

/**
 * @author Jason Wong
 */
public interface IBlackBoxMap {

    IBlackBoxLibrary getBlackBoxLibrary();

    String getID();

    Set<IBlackBoxVersion> getVersions();

    String getSource();

}
