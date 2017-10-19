package org.xmind.cathy.internal.dashboard;

import java.net.URI;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.xmind.cathy.internal.ICathyConstants;
import org.xmind.cathy.internal.dashboard.RecentFilesGalleryPartFactory.RecentFilesFramePart;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.ui.internal.SpaceCollaborativeEngine;
import org.xmind.gef.util.Properties;
import org.xmind.ui.editor.IEditorHistory;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GalleryNavigablePolicy;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.resources.ColorUtils;

public class RecentFileViewer extends GalleryViewer {

    private static final int FRAME_WIDTH = 210;
    private static final int FRAME_HEIGHT = 130;
    private static final String COLOR_CONTENT_BORDER = "#cccccc"; //$NON-NLS-1$

    private IEditorHistory editorHistory;

    private LocalResourceManager resources;
    private Control viewerControl;
    private RecentFilesContentProvider contentProvider;

    public RecentFileViewer(Composite parent) {
        editorHistory = PlatformUI.getWorkbench()
                .getService(IEditorHistory.class);
        initViewer(parent);
        registerHelper(parent.getShell());
    }

    private void registerHelper(Shell shell) {
        shell.setData(ICathyConstants.HELPER_RECENTFILE_PIN, new Runnable() {
            public void run() {
                final ISelection selection = getSelection();
                if (selection instanceof IStructuredSelection) {
                    final List list = ((IStructuredSelection) selection)
                            .toList();
                    for (final Object element : list) {
                        if (element instanceof URI) {
                            final boolean isChecked = editorHistory
                                    .isPinned((URI) element);
                            if (isChecked) {
                                unPinRecentFile((URI) element);
                            } else {
                                pinRecentFile((URI) element);
                            }
                        }
                    }
                }

            }
        });
        shell.setData(ICathyConstants.HELPER_RECENTFILE_DELETE, new Runnable() {
            public void run() {
                final ISelection selection = getSelection();
                if (selection instanceof IStructuredSelection) {
                    final List list = ((IStructuredSelection) selection)
                            .toList();
                    for (final Object element : list) {
                        if (element instanceof URI) {
                            deleteRecentFile((URI) element);
                        }
                    }
                }
            }
        });
        shell.setData(ICathyConstants.HELPER_RECENTFILE_CLEAR, new Runnable() {
            public void run() {
                clearRecentFile();
            }
        });
    }

    private void unregisterHelper(Shell shell) {
        shell.setData(ICathyConstants.HELPER_RECENTFILE_CLEAR, null);
        shell.setData(ICathyConstants.HELPER_RECENTFILE_DELETE, null);
        shell.setData(ICathyConstants.HELPER_RECENTFILE_PIN, null);
    }

    private void handleDispose() {
        if (viewerControl != null && !viewerControl.isDisposed())
            unregisterHelper(viewerControl.getShell());
        if (editorHistory != null)
            editorHistory.removeEditorHistoryListener(contentProvider);
    }

    @SuppressWarnings("restriction")
    private void initViewer(final Composite parent) {
        if (resources == null)
            resources = new LocalResourceManager(JFaceResources.getResources(),
                    parent);

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        editDomain.installEditPolicy(GalleryViewer.POLICY_NAVIGABLE,
                new GalleryNavigablePolicy());
        setEditDomain(editDomain);

        Properties properties = getProperties();
        properties.set(GalleryViewer.Horizontal, Boolean.TRUE);
        properties.set(GalleryViewer.Wrap, Boolean.TRUE);
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SingleClickToOpen, Boolean.FALSE);
        properties.set(GalleryViewer.SolidFrames, true);
        properties.set(GalleryViewer.FlatFrames, true);
        properties.set(GalleryViewer.ImageConstrained, Boolean.TRUE);
        properties.set(GalleryViewer.ImageStretched, Boolean.TRUE);
        properties.set(GalleryViewer.ContentPaneBorderWidth, 1);
        properties.set(GalleryViewer.CustomContentPaneDecorator, true);
        properties.set(GalleryViewer.ContentPaneBorderColor,
                resources.get(ColorUtils.toDescriptor(COLOR_CONTENT_BORDER)));

        properties.set(GalleryViewer.FrameContentSize,
                new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_TOPLEFT,
                        GalleryLayout.ALIGN_TOPLEFT, 30, 0,
                        new Insets(10, 65, 20, 65)));
        properties.set(GalleryViewer.ContentPaneSpaceCollaborativeEngine,
                new SpaceCollaborativeEngine());

        contentProvider = new RecentFilesContentProvider();
        final RecentFilesLabelProvider labelProvider = new RecentFilesLabelProvider(
                parent);
        contentProvider.addContentChangeListener(new Runnable() {
            public void run() {
                handleRecentFileListChanged(contentProvider, labelProvider,
                        true);
                parent.layout(true);
            }
        });

        viewerControl = createControl(parent);
        viewerControl.setBackground(parent.getBackground());
        viewerControl.setForeground(parent.getForeground());
        viewerControl
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewerControl.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                handleDispose();
            }
        });

        setPartFactory(new RecentFilesGalleryPartFactory());
        setContentProvider(contentProvider);
        setLabelProvider(labelProvider);

        editorHistory.addEditorHistoryListener(contentProvider);
        setInput(editorHistory);
        handleRecentFileListChanged(contentProvider, labelProvider, true);
    }

    @Override
    public Control getControl() {
        if (viewerControl != null)
            return viewerControl;
        return super.getControl();
    }

    private void handleRecentFileListChanged(
            RecentFilesContentProvider contentProvider,
            RecentFilesLabelProvider labelProvider, boolean refresh) {
        if (refresh) {
            setInput(getInput());
        }
    }

    private void clearRecentFile() {
        editorHistory.clear();
    }

    private void deleteRecentFile(URI fileURI) {
        editorHistory.remove(fileURI);
    }

    private void pinRecentFile(URI fileURI) {
        editorHistory.pin(fileURI);
        updateRecentFilePart(fileURI);
    }

    private void unPinRecentFile(URI fileURI) {
        editorHistory.unPin(fileURI);
        updateRecentFilePart(fileURI);
    }

    private void updateRecentFilePart(URI pinURI) {
        RecentFilesFramePart part = findRecentFilePart(pinURI);
        if (part != null)
            part.update();
    }

    private RecentFilesFramePart findRecentFilePart(URI pinURI) {
        if (pinURI == null)
            return null;
        return (RecentFilesFramePart) getPartRegistry().getPartByModel(pinURI);
    }
}
