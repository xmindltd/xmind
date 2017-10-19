package org.xmind.core.internal.runtime;

import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.xmind.core.io.BundleResource;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class BundleResourceFinder {

    public static BundleResource resolve(BundleResource res) {
        IPath path = res.getPath();
        if (path.isEmpty() || path.isRoot()) {
            path = Path.EMPTY;
        }

        BundleResource result;
        String first = path.segment(0);
        if (first == null || !first.startsWith("$")) { //$NON-NLS-1$
            result = find(res.getBundle(), path);
        } else {
            path = path.removeFirstSegments(1);
            if ("$nl$".equalsIgnoreCase(first)) { //$NON-NLS-1$
                result = resolveByNL(res.getBundle(), path);
            } else if ("$os$".equalsIgnoreCase(first)) { //$NON-NLS-1$
                result = resolveByOS(res.getBundle(), path);
            } else if ("$ws$".equalsIgnoreCase(first)) { //$NON-NLS-1$
                result = resolveByWS(res.getBundle(), path);
            } else {
                result = find(res.getBundle(), path);
            }
        }
        return result;
    }

    private static BundleResource find(Bundle bundle, IPath path) {
        URL entry = bundle.getEntry(path.toString());
        if (entry != null) {
            return new BundleResource(bundle, new Path(entry.getPath()));
        }

        Bundle[] fragments = Platform.getFragments(bundle);
        if (fragments == null)
            return null;

        for (int i = 0; i < fragments.length; i++) {
            BundleResource result = find(fragments[i], path);
            if (result != null)
                return result;
        }

        return null;
    }

    private static BundleResource resolveByNL(Bundle bundle, IPath path) {
        String nl = Platform.getNL();
        String[] nlParts = nl.split("_"); //$NON-NLS-1$

        for (int i = 0; i < nlParts.length; i++) {
            int count = nlParts.length - i;
            IPath p = Path.ROOT.append("nl"); //$NON-NLS-1$
            for (int j = 0; j < count; j++) {
                p = p.append(nlParts[j]);
            }
            p = p.append(path);
            BundleResource result = find(bundle, p);
            if (result != null)
                return result;
        }

        return find(bundle, path);
    }

    private static BundleResource resolveByOS(Bundle bundle, IPath path) {
        // TODO
        return null;
    }

    private static BundleResource resolveByWS(Bundle bundle, IPath path) {
        // TODO
        return null;
    }

}
