package org.xmind.ui.internal.editor;

import static org.xmind.ui.internal.editor.TempWorkbookRefFactory.URI_SCHEME;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.IMemento;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.ISerializer;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.CloneHandler;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IWorkbookRef;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class ClonedWorkbookRef extends AbstractWorkbookRef {

    protected static final String URI_PATH = "/clone"; //$NON-NLS-1$
    private static final String PARAM_SOURCE_URI = "sourceURI"; //$NON-NLS-1$

    private String name;

    private ClonedWorkbookRef(URI uri, IMemento state) {
        this(uri, state, null);
    }

    private ClonedWorkbookRef(URI uri, IMemento state, String name) {
        super(uri, state);
        this.name = name;
    }

    public URI getSourceWorkbookURI() {
        return getSourceWorkbookURI(getURI());
    }

    private static URI getSourceWorkbookURI(URI uri) {
        String sourceURIString = URIParser.getQueryParameter(uri,
                PARAM_SOURCE_URI);
        return URI.create(sourceURIString);
    }

    @Override
    public String getName() {
        if (name != null) {
            return name;
        }

        String path = getSourceWorkbookURI().getPath();
        int suffixIndex = path.lastIndexOf("."); //$NON-NLS-1$
        if (suffixIndex > 0) {
            path = path.substring(0, suffixIndex);
        }
        int nameIndex = path.lastIndexOf("/"); //$NON-NLS-1$
        if (nameIndex > 0 && nameIndex < path.length() - 1) {
            path = path.substring(nameIndex + 1);
        }
        return path;
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    protected IWorkbook doLoadWorkbookFromURI(IProgressMonitor monitor, URI uri)
            throws InterruptedException, InvocationTargetException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

        subMonitor.newChild(5);
        URI sourceWorkbookURI = getSourceWorkbookURI(uri);
        IWorkbookRef sourceWorkbookRef = MindMapUIPlugin.getDefault()
                .getWorkbookRefFactory()
                .createWorkbookRef(sourceWorkbookURI, null);
        Assert.isTrue(sourceWorkbookRef != null);
        if (monitor.isCanceled())
            throw new InterruptedException();

        sourceWorkbookRef.open(subMonitor.newChild(20));
        try {
            IWorkbook sourceWorkbook = sourceWorkbookRef.getWorkbook();
            Assert.isTrue(sourceWorkbook != null);
            return doCloneWorkbook(subMonitor.newChild(70), sourceWorkbook);
        } finally {
            subMonitor.setWorkRemaining(5);
            sourceWorkbookRef.close(subMonitor.newChild(5));
        }
    }

    private IWorkbook doCloneWorkbook(IProgressMonitor monitor,
            IWorkbook sourceWorkbook)
            throws InterruptedException, InvocationTargetException {
        try {
            IWorkbook workbook = Core.getWorkbookBuilder()
                    .createWorkbook(getTempStorage());
            ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
            serializer.setWorkbook(workbook);
            serializer.setWorkbookStorageAsOutputTarget();
            serializer.setEntryStreamNormalizer(getEncryptionHandler());
            serializer.serialize(null);

            new CloneHandler().withWorkbooks(sourceWorkbook, workbook)
                    .copyWorkbookContents();

            return workbook;
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        }
    }

    public static IWorkbookRef create(URI uri, IMemento state) {
        Assert.isNotNull(uri);
        Assert.isLegal(URI_SCHEME.equals(uri.getScheme()));
        Assert.isLegal(URI_PATH.equals(uri.getPath()));
        return new ClonedWorkbookRef(uri, state);
    }

    public static IWorkbookRef createFromSourceWorkbookURI(
            URI sourceWorkbookURI, String name) {
        Assert.isNotNull(sourceWorkbookURI);
        URI uri = URI.create(URI_SCHEME + ":" + URI_PATH); //$NON-NLS-1$
        uri = URIParser.appendQueryParameter(uri, PARAM_SOURCE_URI,
                sourceWorkbookURI.toString());
        return new ClonedWorkbookRef(uri, null, name);
    }

}
