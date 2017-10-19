package org.xmind.ui.internal.decorations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.decorations.IDecorationFactory;
import org.xmind.ui.internal.svgsupport.SvgFileLoader;

public class CloudDecorationFactory
        implements IDecorationFactory, IExecutableExtension {

    private String svgFilePath;

    private String path;

    public CloudDecorationFactory() {
    }

    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
        if (data instanceof String) {
            svgFilePath = (String) data;
        }

    }

    public IDecoration createDecoration(String id, IGraphicalPart part) {
        if (path == null) {
            SvgFileLoader loader = SvgFileLoader.getInstance();
            path = loader.loadSvgFile(svgFilePath);
        }

        return new CloudTopicDecoration(id, path);
    }

}
