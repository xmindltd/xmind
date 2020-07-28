package org.xmind.ui.internal.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.Request;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.Styles;
import org.xmind.ui.viewers.ImageCachedLabelProvider;
import org.xmind.ui.viewers.MComboViewer;

public class SheetMultiBranchColorsStylePropertySectionPart
        extends StyledPropertySectionPart {

    private class MultiLineColorChangeListener
            implements ISelectionChangedListener {
        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof MultiLineColorModel)
                changeRainbowColor(((MultiLineColorModel) o).getCommandText());
        }
    }

    private class MultiLineColorModel
            implements Comparable<MultiLineColorModel> {
        private List<RGB> rgbs;
        private String name;
        private String commandText = ""; //$NON-NLS-1$

        public MultiLineColorModel(String name, String commandText) {
            this.name = name;
            this.commandText = commandText;
        }

        public List<RGB> getRgbs() {
            if (commandText != null) {
                if (rgbs == null) {
                    rgbs = new ArrayList<RGB>();
                    String[] strs = commandText.split(" +"); //$NON-NLS-1$
                    for (String str : strs) {
                        int rgbValue = Integer.parseInt(str.substring(1), 16);
                        rgbs.add(new RGB(rgbValue >> 16 & 0xff,
                                rgbValue >> 8 & 0xff, rgbValue & 0xff));
                    }
                }
                return rgbs;
            }
            return Collections.emptyList();
        }

        public String getCommandText() {
            return commandText;
        }

        public String getName() {
            return name;
        }

        public int compareTo(MultiLineColorModel model) {
            if (commandText != null)
                return commandText.compareTo(model.getCommandText());
            return -1;
        }

    }

    private class MultiLineColorLabelDescriptor
            extends ImageCachedLabelProvider {
        private static final int CELL_WIDTH = 15;
        private static final int CELL_HEIGHT = 15;
        private final RGB BACKGROUND = new RGB(182, 182, 182);

        @Override
        protected Image createImage(Object element) {
            if (element instanceof MultiLineColorModel) {
                List<RGB> rgbs = ((MultiLineColorModel) element).getRgbs();

                int imageWidth = (int) (rgbs.size() * CELL_WIDTH * 1.1);
                int imageHeight = CELL_HEIGHT;

                // image will dispose when MComboViewer dispose 
                Image image = new Image(Display.getDefault(), imageWidth,
                        imageHeight);
                GC gc = new GC(image);
                gc.setAdvanced(true);
                gc.setAntialias(SWT.ON);

                gc.setBackground(resourceManager.createColor(BACKGROUND));
                gc.fillRectangle(0, 0, imageWidth, imageHeight);

                int x = 0;
                int y = 0;
                for (int i = 0; i < rgbs.size(); i++) {
                    x = (int) (1.1 * i * CELL_WIDTH);
                    Color background = resourceManager.createColor(rgbs.get(i));
                    gc.setBackground(background);
                    gc.fillRectangle(x, y, CELL_WIDTH, CELL_HEIGHT);
                }

                gc.dispose();
                return image;
            }
            return null;
        }

        @Override
        public String getText(Object element) {

            if (element instanceof MultiLineColorModel)
                return ((MultiLineColorModel) element).getName();
            return null;
        }
    }

    private static String[] names = {
            PropertyMessages.MultiLineColors_CommandTextName1,
            PropertyMessages.MultiLineColors_CommandTextName2,
            PropertyMessages.MultiLineColors_CommandTextName3,
            PropertyMessages.MultiLineColors_CommandTextName4,
            PropertyMessages.MultiLineColors_CommandTextName5,
            PropertyMessages.MultiLineColors_CommandTextName6 };
    private static String[] commandTexts = {
            "#017c98 #00b2a1 #ffdd00 #fc8f00 #ff1500 #00b04c", //$NON-NLS-1$
            "#009978 #e2e500 #009bb6 #99b800 #d10e00 #8c4400", //$NON-NLS-1$
            "#488dfd #8d8a6b #7a6c6b #b0ad9b #7781d8 #fbc01e", //$NON-NLS-1$
            "#e90e18 #f4bfa4 #ffb800 #ff6f00 #fd62b1 #ac0708", //$NON-NLS-1$
            "#bbdefb #64b5f6 #2196f3 #0e82f4 #1976d2 #0d47a1", //$NON-NLS-1$
            "#abd69b #bbdb73 #8eac6b #45774f #003e2d #1a2b2b" }; //$NON-NLS-1$
    private ResourceManager resourceManager;
    private List<MultiLineColorModel> models;
    private Button multiLineColorsCheck;
    private MComboViewer multiLineColorsSelectionViewer;

    public SheetMultiBranchColorsStylePropertySectionPart() {

        models = new ArrayList<MultiLineColorModel>();
        for (int i = 0; i < names.length; i++) {
            models.add(new MultiLineColorModel(names[i], commandTexts[i]));
        }
    }

    @Override
    protected void createContent(Composite parent) {

        resourceManager = new LocalResourceManager(
                JFaceResources.getResources(), parent);

        Composite multiLineColors = new Composite(parent, SWT.NONE);
        multiLineColors.setLayout(createLayout(parent));

        multiLineColorsCheck = new Button(multiLineColors, SWT.CHECK);
        multiLineColorsCheck.setText(
                PropertyMessages.SheetMultiBranchColorsStylePropertySectionPart_multiBranchColor_text);
        multiLineColorsCheck.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                        UserDataConstants.CATEGORY_FORMAT,
                        UserDataConstants.TOGGLE_MULTI_COLOR);
                multiLineColorsSelectionViewer.setEnabled(
                        !multiLineColorsSelectionViewer.isEnabled());
                if (multiLineColorsSelectionViewer.isEnabled()) {
                    MultiLineColorModel model = (MultiLineColorModel) ((StructuredSelection) multiLineColorsSelectionViewer
                            .getSelection()).getFirstElement();
                    if (model == null) {
                        model = models.get(0);
                    }
                    changeRainbowColor(model.getCommandText());
                    multiLineColorsSelectionViewer
                            .setSelection(new StructuredSelection(model));
                } else {
                    changeRainbowColor(null);
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        multiLineColorsSelectionViewer = new MComboViewer(multiLineColors,
                MComboViewer.NO_TEXT);
        multiLineColorsSelectionViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, true));
        multiLineColorsSelectionViewer.getControl()
                .setToolTipText(PropertyMessages.MultiLineColors_text);

        multiLineColorsSelectionViewer
                .setContentProvider(new ArrayContentProvider());
        multiLineColorsSelectionViewer
                .setLabelProvider(new MultiLineColorLabelDescriptor());
        multiLineColorsSelectionViewer.setInput(models);
        multiLineColorsSelectionViewer.addSelectionChangedListener(
                new MultiLineColorChangeListener());

        multiLineColorsSelectionViewer.setEnabled(false);

    }

    @Override
    protected GridLayout createLayout(Composite parent) {
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 3;
        layout.marginHeight = 1;
        layout.horizontalSpacing = 1;
        layout.verticalSpacing = 1;
        return layout;
    }

    @Override
    protected void doRefresh() {
        if (multiLineColorsCheck != null
                && !multiLineColorsCheck.isDisposed()) {
            String value = getStyleValue(Styles.MultiLineColors, null);

            boolean isSelected = value != null && !Styles.NONE.equals(value);
            multiLineColorsCheck.setSelection(isSelected);
            multiLineColorsSelectionViewer.setEnabled(isSelected);
            multiLineColorsSelectionViewer.getControl().setVisible(isSelected);
            if (isSelected && value != null
                    && multiLineColorsSelectionViewer != null
                    && !multiLineColorsSelectionViewer.getControl()
                            .isDisposed()) {
                for (MultiLineColorModel model : models) {
                    if (value.equals(model.getCommandText())) {
                        multiLineColorsSelectionViewer
                                .setSelection(new StructuredSelection(model));
                        break;
                    }
                }
            }
        }

    }

    private void changeRainbowColor(String multiLineColorsCommandText) {
        Request request = createStyleRequest(
                CommandMessages.Command_ToggleMultiLineColors);
        if (multiLineColorsCommandText != null) {
            addStyle(request, Styles.MultiLineColors,
                    multiLineColorsCommandText);
        } else {
            addStyle(request, Styles.MultiLineColors, Styles.NONE);
        }
        sendRequest(request);
    }

    @Override
    public void dispose() {
        // not need to dispose resource manager, 
        //because resource manager will listen widget.dispose 
        models = null;
        multiLineColorsCheck = null;
        multiLineColorsSelectionViewer = null;
    }

    @Override
    public void setFocus() {
        multiLineColorsCheck.setFocus();
    }
}
