package org.xmind.ui.preference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.xmind.ui.resources.FontUtils;

public class MultiSectionPreferencePage extends PreferencePage
        implements IWorkbenchPreferencePage, IExecutableExtension {

    private String pageId = null;

    private List<IPreferenceSection> pageSections = new ArrayList<IPreferenceSection>();
    private List<IConfigurationElement> elments = new ArrayList<IConfigurationElement>();

    @Override
    protected Control createContents(Composite parent) {

        if (pageId != null) {
            for (IExtension extension : Platform.getExtensionRegistry()
                    .getExtensionPoint(
                            "org.xmind.ui.toolkit.preferencePageSections") //$NON-NLS-1$
                    .getExtensions()) {
                for (IConfigurationElement element : extension
                        .getConfigurationElements()) {
                    String sectionPageId = element.getAttribute("pageId"); //$NON-NLS-1$
                    if (pageId.equals(sectionPageId)) {
                        elments.add(element);
//                        createSectionControl(container, element);
                    }
                }
            }
        } else {
            ///TODO no page id available
        }
        return createSectionControlAsIndex(parent);
    }

    private class IndexComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            IConfigurationElement element1 = (IConfigurationElement) o1;
            IConfigurationElement element2 = (IConfigurationElement) o2;
            String indexString1 = element1.getAttribute("index"); //$NON-NLS-1$
            String indexString2 = element2.getAttribute("index"); //$NON-NLS-1$
            int index1 = Integer.MAX_VALUE;
            int index2 = Integer.MAX_VALUE;
            if (null != indexString1 && !("".equals(indexString1))) //$NON-NLS-1$
                index1 = Integer.parseInt(indexString1);
            if (null != indexString2 && !("".equals(indexString2))) //$NON-NLS-1$
                index2 = Integer.parseInt(indexString2);
            return new Integer(index1).compareTo(new Integer(index2));
        }
    }

    @SuppressWarnings("unchecked")
    private Composite createSectionControlAsIndex(Composite parent) {

        Collections.sort(elments, new IndexComparator());
        Composite container = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(container);

        for (IConfigurationElement element : elments) {
            createSectionControl(container, element);
        }
        return container;
    }

    private void createSectionControl(Composite parent,
            IConfigurationElement element) {
        String labelName = element
                .getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);

        if (labelName != null && !("".equals(labelName))) { //$NON-NLS-1$
            Label label = new Label(parent, SWT.NONE);
            label.setText(labelName);
            configLabelFont(label);
        }

        Composite sectionContent = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false)
                .applyTo(sectionContent);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 15)
                .applyTo(sectionContent);

        Object prefPage;
        try {
            prefPage = element.createExecutableExtension(
                    IWorkbenchRegistryConstants.ATT_CLASS);

        } catch (CoreException e) {
            e.printStackTrace();
            return;
        }

        if (!(prefPage instanceof IWorkbenchPreferencePage))
            return;

        if (!(prefPage instanceof IPreferenceSection))
            return;

        pageSections.add(((IPreferenceSection) prefPage));

        ((IWorkbenchPreferencePage) prefPage).init(PlatformUI.getWorkbench());
        ((IWorkbenchPreferencePage) prefPage).createControl(sectionContent);

    }

    private void configLabelFont(Label label) {
        LocalResourceManager resource = new LocalResourceManager(
                JFaceResources.getResources(), label);
        FontData[] fontData = Display.getDefault().getSystemFont()
                .getFontData();
        label.setFont((Font) resource.get(
                FontDescriptor.createFrom(FontUtils.bold(fontData, true))));
    }

    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
        pageId = config.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
    }

    public void init(IWorkbench workbench) {
    }

    @Override
    public boolean performCancel() {
        if (null != pageSections && pageSections.size() > 0)
            for (IPreferenceSection pageSection : pageSections)
                if (!pageSection.cancel())
                    return false;
        return true;
    }

    @Override
    protected void performApply() {
        this.performOk();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        if (null != pageSections && pageSections.size() > 0)
            for (IPreferenceSection pageSection : pageSections)
                pageSection.excuteDefault();
    }

    @Override
    public boolean performOk() {
        if (null != pageSections && pageSections.size() > 0)
            for (IPreferenceSection pageSection : pageSections)
                if (!pageSection.ok())
                    return false;
        return true;
    }

}
