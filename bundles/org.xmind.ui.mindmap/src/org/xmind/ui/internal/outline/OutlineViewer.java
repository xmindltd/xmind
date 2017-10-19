package org.xmind.ui.internal.outline;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.outline.resource.IAZResource;
import org.xmind.ui.internal.outline.resource.IAssigneeResource;
import org.xmind.ui.internal.outline.resource.ILabelResource;
import org.xmind.ui.internal.outline.resource.IMarkerResource;
import org.xmind.ui.internal.outline.resource.IOutlineResource;
import org.xmind.ui.internal.outline.resource.ITaskDateResource;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MarkerImageDescriptor;

public class OutlineViewer extends TreeViewer {

    public static final String VIEWERTYPE_SINGLECOLUMN = "singleColumn"; //$NON-NLS-1$

    public static final String VIEWERTYPE_MULITCOLUMN = "multiColumn"; //$NON-NLS-1$

    private class OutlineContentProvider implements ITreeContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
            if (newInput instanceof ITaskDateResource) {
                int taskDateType = ((ITaskDateResource) newInput)
                        .getTaskDateResourceType();
                Tree tree = getTree();
                if (tree != null && !tree.isDisposed()) {
                    TreeColumn column = tree.getColumn(1);
                    if (column != null && !column.isDisposed())
                        if (taskDateType == OutlineIndexModelPart.OUTLINE_TYPE_BY_STARTDATE) {
                            column.setText(
                                    MindMapMessages.OutlineViewer_StartDate_col);
                        } else if (taskDateType == OutlineIndexModelPart.OUTLINE_TYPE_BY_ENDDATE)
                            column.setText(
                                    MindMapMessages.OutlineViewer_EndData_col);
                }
            }
        }

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof ITaskDateResource) {
                return ((ITaskDateResource) inputElement)
                        .getAllTopicsForTaskDate().toArray();
            }
            if (inputElement instanceof IOutlineResource) {
                IOutlineResource input = (IOutlineResource) inputElement;
                return new Object[] { input.getSource() };
            }
            return null;
        }

        public Object[] getChildren(Object parentElement) {
            Object input = getInput();
            if (input instanceof IMarkerResource) {
                IMarkerResource resource = (IMarkerResource) input;
                return getChildrenForMarkerResource(resource, parentElement);
            } else if (input instanceof ILabelResource) {
                ILabelResource resource = (ILabelResource) input;
                return getChildrenForLabelResource(resource, parentElement);
            } else if (input instanceof IAssigneeResource) {
                return getChildrenForAssigneeResource((IAssigneeResource) input,
                        parentElement);
            } else if (input instanceof ITaskDateResource) {
                return null;
            } else if (input instanceof IAZResource) {
                if (parentElement instanceof IWorkbookRef
                        || parentElement instanceof ISheet)
                    return ((IAZResource) input).getTopics().toArray();
            }
            return null;
        }

        public Object getParent(Object element) {
            Object input = getInput();
            if (input instanceof IMarkerResource) {
                IMarkerResource resource = (IMarkerResource) input;
                return getParentForMarkerResource(resource, element);
            } else if (input instanceof ILabelResource) {
                ILabelResource resource = (ILabelResource) input;
                return getParentForLabelResource(resource, element);
            } else if (input instanceof IAssigneeResource) {
                return getParentForAssigneeResource((IAssigneeResource) input,
                        element);
            } else if (input instanceof ITaskDateResource) {
                return null;
            } else if (input instanceof IAZResource) {
                if (element instanceof ITopic)
                    return ((IAZResource) input).getSource();
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            Object input = getInput();
            if (input instanceof IMarkerResource) {
                return (element instanceof IWorkbookRef)
                        || (element instanceof ISheet)
                        || (element instanceof IMarkerGroup)
                        || (element instanceof IMarker);
            } else if (input instanceof ILabelResource) {
                return (element instanceof IWorkbookRef)
                        || (element instanceof ISheet)
                        || (element instanceof String);
            } else if (input instanceof IAssigneeResource) {
                return (element instanceof IWorkbookRef)
                        || (element instanceof ISheet)
                        || (element instanceof String);
            } else if (input instanceof IAZResource) {
                return (element instanceof IWorkbookRef)
                        || (element instanceof ISheet);
            }
            return false;
        }

        private Object[] getChildrenForMarkerResource(IMarkerResource resource,
                Object parentElement) {
            Set<String> ids = resource.getMarkerIds();
            if (parentElement instanceof IMarkerGroup) {
                Set<IMarker> childrenIds = new HashSet<IMarker>();
                for (String id : ids) {
                    IMarker marker = resource.getMarker(id);
                    if (marker.getParent() == parentElement) {
                        childrenIds.add(marker);
                    }
                }
                return childrenIds.toArray();
            } else if (parentElement instanceof IMarker) {
                return resource.getTopics(((IMarker) parentElement).getId())
                        .toArray();
            } else if ((parentElement instanceof IWorkbookRef)
                    || (parentElement instanceof ISheet)) {
                Set<IMarkerGroup> group = new HashSet<IMarkerGroup>();
                for (String markerId : ids) {
                    IMarker marker = resource.getMarker(markerId);
                    group.add(marker.getParent());
                }
                return group.toArray();
            }
            return null;
        }

        private Object[] getChildrenForLabelResource(ILabelResource resource,
                Object parentElement) {
            if (parentElement instanceof IWorkbookRef
                    || parentElement instanceof ISheet) {
                return resource.getLabels().toArray();
            } else if (parentElement instanceof String) {
                return resource.getTopics((String) parentElement).toArray();
            }
            return null;
        }

        private Object[] getChildrenForAssigneeResource(
                IAssigneeResource resource, Object parentElement) {
            if (parentElement instanceof IWorkbookRef
                    || parentElement instanceof ISheet) {
                return resource.getAssignees().toArray();
            } else if (parentElement instanceof String) {
                return resource.getTopics((String) parentElement).toArray();
            }
            return null;
        }

        private Object getParentForMarkerResource(IMarkerResource resource,
                Object element) {
            if (element instanceof IMarkerGroup) {
                return resource.getSource();
            } else if (element instanceof IMarker) {
                return ((IMarker) element).getParent();
            } else if (element instanceof ITopic) {
                Set<String> ids = resource.getMarkerIds();
                for (String markerId : ids) {
                    if (resource.getTopics(markerId).contains(element)) {
                        IMarker marker = resource.getMarker(markerId);
                        return marker;
                    }
                }
            }
            return null;
        }

        private Object getParentForLabelResource(ILabelResource resource,
                Object element) {
            if (element instanceof String) {
                return resource.getSource();
            } else if (element instanceof ITopic) {
                Set<String> labels = resource.getLabels();
                for (String lab : labels) {
                    if (resource.getTopics(lab).contains(element))
                        return lab;
                }
            }
            return null;
        }

        private Object getParentForAssigneeResource(IAssigneeResource resource,
                Object element) {
            if (element instanceof String) {
                return resource.getSource();
            } else if (element instanceof ITopic) {
                Set<String> assignees = resource.getAssignees();
                for (String assignee : assignees) {
                    if (resource.getTopics(assignee).contains(element))
                        return assignee;
                }
            }
            return null;
        }
    }

    private class OutlineLabelProvider extends LabelProvider {

        public String getText(Object element) {
            if (element instanceof IWorkbookRef) {
                return ((IWorkbookRef) element).getName();
            } else if (element instanceof ISheet) {
                return ((ISheet) element).getTitleText();
            } else if (element instanceof IMarkerGroup) {
                return ((IMarkerGroup) element).getName();
            } else if (element instanceof IMarker) {
                return ((IMarker) element).getName();
            } else if (element instanceof ITopic) {
                return ((ITopic) element).getTitleText();
            } else if (element instanceof String) {
                return (String) element;
            }
            return super.getText(element);
        }

        public Image getImage(Object element) {
            if (element instanceof IWorkbookRef) {
                ImageDescriptor imageDescriptor = MindMapUI.getImages()
                        .get(IMindMapImages.WORKBOOK, true);
                return localResourceManager.createImage(imageDescriptor);
            } else if (element instanceof ISheet) {
                return localResourceManager.createImage(
                        MindMapUI.getImages().get(IMindMapImages.SHEET, true));
            } else if (element instanceof ITopic) {
//                return localResourceManager.createImage(MindMapUI.getImages()
//                        .getTopicIcon((ITopic) element, true));
            } else if (element instanceof IMarker) {
                ImageDescriptor imageDescriptor = MarkerImageDescriptor
                        .createFromMarker((IMarker) element);
                return localResourceManager.createImage(imageDescriptor);
            }
            Object input = getInput();
            if (input instanceof ILabelResource) {
                if (element instanceof String)
                    return localResourceManager.createImage(MindMapUI
                            .getImages().get(IMindMapImages.LABEL, true));
            } else if (input instanceof IAssigneeResource) {
                if (element instanceof String) {
                    URL url = FileLocator.find(
                            Platform.getBundle(MindMapUIPlugin.PLUGIN_ID),
                            new Path("$nl$/icons/assignee.gif"), null); //$NON-NLS-1$
                    return localResourceManager
                            .createImage(ImageDescriptor.createFromURL(url));
                }
            }
            return super.getImage(element);
        }

    }

    private class OutlineViewerSorter extends ViewerComparator {

        public int category(Object element) {
            if (element instanceof IWorkbookRef || element instanceof ISheet)
                return 1;
            else if (element instanceof IMarkerGroup)
                return 2;
            else if (element instanceof IMarker)
                return 4;
            else if (element instanceof String)
                return 8;
            else if (element instanceof ITopic)
                return 16;
            return super.category(element);
        }

        public int compare(Viewer viewer, Object e1, Object e2) {

            Object input = getInput();
            if (input instanceof ITaskDateResource) {
                ITaskDateResource resource = (ITaskDateResource) input;
                if (e1 instanceof ITopic && e2 instanceof ITopic) {
                    String pTaskDate = resource.getTaskDate((ITopic) e1);
                    String qTaskDate = resource.getTaskDate((ITopic) e2);
                    int compareDate = super.compare(viewer, pTaskDate,
                            qTaskDate);
                    return compareDate == 0 ? super.compare(viewer, e1, e2)
                            : compareDate;

                }
            }

            if (input instanceof IAZResource) {
                if (e1 instanceof ITopic && e2 instanceof ITopic) {
                    if (((IAZResource) input).isPositiveSequence())
                        return super.compare(viewer, e1, e2);
                    else
                        return super.compare(viewer, e2, e1);
                }
            }

            if (e1 instanceof IMarkerGroup && e2 instanceof IMarkerGroup) {
                IMarkerSheet pSheet = ((IMarkerGroup) e1).getOwnedSheet();
                IMarkerSheet qSheet = ((IMarkerGroup) e2).getOwnedSheet();

                if (pSheet == qSheet) {
                    List<IMarkerGroup> markerGroups = pSheet.getMarkerGroups();
                    return markerGroups.indexOf(e1) - markerGroups.indexOf(e2);
                } else {
                    if (MindMapUI.getResourceManager()
                            .getSystemMarkerSheet() == pSheet)
                        return -10000;
                    else
                        return 10000;
                }

            } else if (e1 instanceof IMarker && e2 instanceof IMarker) {
                IMarkerGroup pMarkerGroup = ((IMarker) e1).getParent();
                IMarkerGroup qMarkerGroup = ((IMarker) e2).getParent();

                if (pMarkerGroup == qMarkerGroup && pMarkerGroup != null) {
                    List<IMarker> markers = pMarkerGroup.getMarkers();
                    return markers.indexOf(e1) - markers.indexOf(e2);
                }
            }

            return super.compare(viewer, e1, e2);
        }
    }

    private String viewerType;

    private LocalResourceManager localResourceManager;

    public OutlineViewer(Composite parent, String viewerType) {
        super(parent,
                SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        this.viewerType = viewerType;
        localResourceManager = new LocalResourceManager(
                JFaceResources.getResources(), parent);
        initViewer(viewerType);
    }

    private void initViewer(String viewerType) {
        setContentProvider(new OutlineContentProvider());
        setLabelProvider(new OutlineLabelProvider());
        setComparator(new OutlineViewerSorter());

        if (VIEWERTYPE_MULITCOLUMN.equals(viewerType)) {
            getTree().setHeaderVisible(true);

            TreeViewerColumn taskColumn = new TreeViewerColumn(this, SWT.LEFT);
            taskColumn.getColumn()
                    .setText(MindMapMessages.OutlineViewer_Task_col);
            taskColumn.getColumn().setWidth(200);
            taskColumn.setLabelProvider(new ColumnLabelProvider() {

                public String getText(Object element) {
                    Object input = getInput();
                    if (input instanceof ITaskDateResource) {
                        if (element instanceof ITopic) {
                            return ((ITopic) element).getTitleText();
                        }
                    }
                    return null;
                }
            });

            TreeViewerColumn dateColumn = new TreeViewerColumn(this, SWT.LEFT);
            dateColumn.getColumn().setWidth(200);
            dateColumn.setLabelProvider(new ColumnLabelProvider() {

                @Override
                public String getText(Object element) {
                    Object input = getInput();
                    if (input instanceof ITaskDateResource) {
                        if (element instanceof ITopic) {
                            return ((ITaskDateResource) input)
                                    .getTaskDate((ITopic) element);
                        }
                    }
                    return null;
                }
            });
        }
    }

    public String getViewerType() {
        return viewerType;
    }

    public void setFocus() {
        Control control = getControl();
        if (control != null && !control.isDisposed())
            control.setFocus();
    }

}
