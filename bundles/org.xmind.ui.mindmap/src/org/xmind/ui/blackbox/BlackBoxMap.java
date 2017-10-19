package org.xmind.ui.blackbox;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jason Wong
 */
public class BlackBoxMap implements IBlackBoxMap {

    private IBlackBoxLibrary library;

    private String id;

    private Set<IBlackBoxVersion> versions;

    private String source;

    public BlackBoxMap(IBlackBoxLibrary library, String id) {
        this.library = library;
        this.id = id;
    }

    public IBlackBoxLibrary getBlackBoxLibrary() {
        return library;
    }

    public String getID() {
        return id;
    }

    public Set<IBlackBoxVersion> getVersions() {
        if (versions == null)
            versions = new HashSet<IBlackBoxVersion>();
        return versions;
    }

    public String getSource() {
        if (source == null)
            return ""; //$NON-NLS-1$
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

}
