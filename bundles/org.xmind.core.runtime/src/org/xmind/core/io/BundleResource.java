package org.xmind.core.io;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.xmind.core.internal.runtime.BundleResourceFinder;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public final class BundleResource {

    private static final String SCHEME_PLATFORM = "platform"; //$NON-NLS-1$
    private static final String TYPE_PLUGIN = "plugin"; //$NON-NLS-1$

    private final Bundle bundle;

    private final IPath path;

    public BundleResource(Bundle bundle, IPath path) {
        Assert.isNotNull(bundle);
        this.bundle = bundle;
        this.path = path == null ? Path.EMPTY : path;
    }

    public BundleResource(URL platformURL) {
        Assert.isNotNull(platformURL);
        Assert.isLegal(SCHEME_PLATFORM.equals(platformURL.getProtocol()));

        String fullPathString = platformURL.getPath();
        Assert.isLegal(fullPathString != null && !"".equals(fullPathString)); //$NON-NLS-1$

        IPath fullPath = new Path(fullPathString);
        String type = fullPath.segment(0);
        Assert.isLegal(TYPE_PLUGIN.equals(type));

        fullPath = fullPath.removeFirstSegments(1);
        String bundleId = fullPath.segment(0);
        Assert.isLegal(bundleId != null && !"".equals(bundleId)); //$NON-NLS-1$

        Bundle bundle = Platform.getBundle(bundleId);
        Assert.isLegal(bundle != null);

        this.bundle = bundle;
        this.path = fullPath.removeFirstSegments(1);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public IPath getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof BundleResource))
            return false;
        BundleResource that = (BundleResource) obj;
        return this.bundle.equals(that.bundle) && this.path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return 37 ^ bundle.hashCode() ^ path.hashCode();
    }

    @Override
    public String toString() {
        return toPlatformURL().toExternalForm();
    }

    public URL toPlatformURL() {
        IPath urlPath = Path.ROOT.append(TYPE_PLUGIN).addTrailingSeparator()
                .append(bundle.getSymbolicName()).addTrailingSeparator()
                .append(path);
        try {
            return new URL(SCHEME_PLATFORM, null, urlPath.toString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(String.format(
                    "Failed to construct platform URL: bundle=%s, path=%s", //$NON-NLS-1$
                    bundle.getSymbolicName(), path), e);
        }
    }

    /**
     * Replace variables like '$nl$', '$os$', '$ws$'.
     * 
     * @return resolved bundle resource, or <code>null</code> if not resolved
     */
    public BundleResource resolve() {
        return BundleResourceFinder.resolve(this);
    }

}
