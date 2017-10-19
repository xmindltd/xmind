package org.xmind.ui.blackbox;

/**
 * @author Jason Wong
 */
public class BlackBox {

    public static final String ID = "org.xmind.ui.blackbox"; //$NON-NLS-1$

    public static void doBackup(String source) {
        BlackBoxManager.getInstance().doBackup(source);
    }

    public static IBlackBoxMap findMapBySource(String source) {
        return BlackBoxManager.getInstance().findMapBySource(source);
    }

    public static IBlackBoxMap findMapByID(String id) {
        return BlackBoxManager.getInstance().findMapByID(id);
    }

    public static IBlackBoxMap[] getMaps() {
        return BlackBoxManager.getInstance().getMaps();
    }

    public static boolean removeMap(IBlackBoxMap map) {
        return BlackBoxManager.getInstance().removeMap(map);
    }

    public static boolean removeVersion(String source, String timestamp) {
        return BlackBoxManager.getInstance().removeVersion(source, timestamp);
    }

    public static boolean removeVersion(IBlackBoxMap map, String timestamp) {
        return BlackBoxManager.getInstance().removeVersion(map, timestamp);
    }

    public static void removeSavedMap(String source) {
        BlackBoxManager.getInstance().removeSavedMap(source);
    }

}
