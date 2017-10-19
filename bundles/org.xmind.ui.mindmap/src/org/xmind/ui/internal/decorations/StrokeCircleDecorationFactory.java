package org.xmind.ui.internal.decorations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.decorations.IDecorationFactory;
import org.xmind.ui.internal.svgsupport.SvgFileLoader;
import org.xmind.ui.mindmap.IBranchPart;

public class StrokeCircleDecorationFactory
        implements IDecorationFactory, IExecutableExtension {

    private String[] svgFilePaths;

    private String innerPath;

    private String outerPath;

    public StrokeCircleDecorationFactory() {
    }

    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
        if (data instanceof String) {
            String svgFilePath = (String) data;
            svgFilePaths = svgFilePath.split(","); //$NON-NLS-1$
        }

    }

    public IDecoration createDecoration(String id, IGraphicalPart part) {
        if (innerPath == null) {
            SvgFileLoader loader = SvgFileLoader.getInstance();
            innerPath = loader.loadSvgFile(svgFilePaths[0]);
        }

        if (outerPath == null && svgFilePaths.length > 1) {
            SvgFileLoader loader = SvgFileLoader.getInstance();
            outerPath = loader.loadSvgFile(svgFilePaths[1]);
        }

        return new StrokeCircleTopicDecoration(id, (IBranchPart) part,
                innerPath, outerPath);
    }

}
