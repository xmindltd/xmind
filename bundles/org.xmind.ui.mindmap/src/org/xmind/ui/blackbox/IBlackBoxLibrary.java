package org.xmind.ui.blackbox;

/**
 * @author Jason Wong
 */
public interface IBlackBoxLibrary {

    public IBlackBoxMap[] getMaps();

    public IBlackBoxMap findMapBySource(String source);

    public IBlackBoxMap findMapByID(String id);

    public void addVersion(String source, String mapID, IBlackBoxVersion version);

    public boolean removeMap(IBlackBoxMap map);

    public boolean removeVersion(IBlackBoxMap map, String timestamp);

    public boolean isSavedMap(String id);

    public void removeSavedMap(String source);

}
