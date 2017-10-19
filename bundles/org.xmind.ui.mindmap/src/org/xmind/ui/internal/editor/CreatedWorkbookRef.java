package org.xmind.ui.internal.editor;

import static org.xmind.ui.internal.editor.TempWorkbookRefFactory.URI_SCHEME;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IMemento;
import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.core.style.IStyle;
import org.xmind.ui.internal.editor.URIParser.QueryParameter;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.mindmap.WorkbookInitializer;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class CreatedWorkbookRef extends AbstractWorkbookRef {

    protected static final String URI_PATH = "/create"; //$NON-NLS-1$
    private static final String PARAM_NAME = "name"; //$NON-NLS-1$
    private static final String PARAM_STRUCTURE_CLASS = "structureClass"; //$NON-NLS-1$
    private static final String PARAM_THEME_URI = "themeURI"; //$NON-NLS-1$

    private CreatedWorkbookRef(URI uri, IMemento state) {
        super(uri, state);
    }

    @Override
    public String getName() {
        String name = URIParser.getQueryParameter(getURI(), PARAM_NAME);
        if (name != null)
            return name;

        return null;
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    protected IWorkbook doLoadWorkbookFromURI(IProgressMonitor monitor, URI uri)
            throws InterruptedException, InvocationTargetException {
        IWorkbook workbook = Core.getWorkbookBuilder()
                .createWorkbook(getTempStorage());
        toInitializer(getURI()).initWorkbook(workbook);
        return workbook;
    }

    private static WorkbookInitializer toInitializer(URI uri) {
        WorkbookInitializer initializer = WorkbookInitializer.getDefault();
        Iterator<QueryParameter> it = URIParser.iterateQueryParameters(uri);
        while (it.hasNext()) {
            QueryParameter p = it.next();
            if (PARAM_STRUCTURE_CLASS.equals(p.name)) {
                initializer = initializer.withStructureClass(p.value);
            } else if (PARAM_THEME_URI.equals(p.name)) {
                Object theme = MindMapUI.getResourceManager()
                        .findResource(p.value);
                if (theme != null && theme instanceof IStyle) {
                    initializer = initializer.withTheme((IStyle) theme);
                }
            }
        }
        return initializer;
    }

    public static IWorkbookRef create(URI uri, IMemento state) {
        Assert.isNotNull(uri);
        Assert.isLegal(URI_SCHEME.equals(uri.getScheme()));
        Assert.isLegal(URI_PATH.equals(uri.getPath()));
        return new CreatedWorkbookRef(uri, state);
    }

    public static IWorkbookRef createFromWorkbookInitializer(
            WorkbookInitializer initializer, String name) {
        Assert.isNotNull(initializer);
        URI uri = URI.create(URI_SCHEME + ":" + URI_PATH); //$NON-NLS-1$
        if (name != null) {
            uri = URIParser.appendQueryParameter(uri, PARAM_NAME, name);
        }
        if (initializer.getStructureClass() != null) {
            uri = URIParser.appendQueryParameter(uri, PARAM_STRUCTURE_CLASS,
                    initializer.getStructureClass());
        }
        if (initializer.getTheme() != null) {
            uri = URIParser.appendQueryParameter(uri, PARAM_THEME_URI,
                    MindMapUI.getResourceManager()
                            .toResourceURI(initializer.getTheme()));
        }
        return new CreatedWorkbookRef(uri, null);
    }

}