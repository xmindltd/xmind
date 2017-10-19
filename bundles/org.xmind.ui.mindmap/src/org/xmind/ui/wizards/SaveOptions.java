package org.xmind.ui.wizards;

import java.net.URI;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class SaveOptions {

    private String proposalName = null;

    private URI oldURI = null;

    private SaveOptions() {
    }

    private SaveOptions copy() {
        SaveOptions that = new SaveOptions();
        that.proposalName = this.proposalName;
        that.oldURI = this.oldURI;
        return that;
    }

    public SaveOptions proposalName(String proposalName) {
        SaveOptions that = copy();
        that.proposalName = trimName(proposalName);
        return that;
    }

    private String trimName(String name) {
        return name.replaceAll("\\r\\n|\\n|\\r", // $NON-NLS-1$ //$NON-NLS-1$
                " "); //$NON-NLS-1$
    }

    public String proposalName() {
        return proposalName;
    }

    public SaveOptions oldURI(URI uri) {
        SaveOptions that = copy();
        this.oldURI = uri;
        return that;
    }

    public URI oldURI() {
        return oldURI;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null || !(obj instanceof SaveOptions))
            return false;

        SaveOptions that = (SaveOptions) obj;

        if (!objEquals(this.proposalName, that.proposalName))
            return false;

        if (!objEquals(this.oldURI, that.oldURI))
            return false;

        return true;
    }

    private static boolean objEquals(Object o1, Object o2) {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }

    @Override
    public int hashCode() {
        return 37 ^ objHash(this.proposalName) ^ objHash(this.oldURI);
    }

    private static int objHash(Object o) {
        return o == null ? 37 : o.hashCode();
    }

    public static SaveOptions getDefault() {
        return new SaveOptions();
    }

}
