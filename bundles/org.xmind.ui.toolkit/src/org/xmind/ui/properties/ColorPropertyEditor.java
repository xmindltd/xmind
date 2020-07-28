/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package org.xmind.ui.properties;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.ColorPickerConfigurer;
import org.xmind.ui.color.ColorSelection;
import org.xmind.ui.resources.ColorUtils;

/**
 * @author Frank Shaka
 */
public class ColorPropertyEditor extends PropertyEditor {

    private static final int DEFAULT_TEXT_STYLE = SWT.SINGLE | SWT.BORDER;

    private static final String COLOR_NONE_VALUE = "none"; //$NON-NLS-1$

    private int textStyle;

    private ColorPickerConfigurer configurer;

    private Text text;

    private ToolBarManager toolBar;

    private ColorPicker picker;

    private boolean modifying = false;

    /**
     * 
     */
    public ColorPropertyEditor() {
        this(DEFAULT_TEXT_STYLE, new ColorPickerConfigurer());
    }

    public ColorPropertyEditor(ColorPickerConfigurer colorPickerConfigurer) {
        this(DEFAULT_TEXT_STYLE, colorPickerConfigurer);
    }

    /**
     * @param parent
     * @param style
     */
    public ColorPropertyEditor(int textStyle,
            ColorPickerConfigurer colorPickerConfigurer) {
        Assert.isNotNull(colorPickerConfigurer);
        this.textStyle = textStyle;
        this.configurer = colorPickerConfigurer;
    }

    @Override
    protected Control createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 3;
        composite.setLayout(layout);

        Control textControl = createText(composite);
        GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        textData.widthHint = SWT.DEFAULT;
        textData.heightHint = SWT.DEFAULT;
        textControl.setLayoutData(textData);

        Control toolBarControl = createToolBar(composite);
        GridData toolBarData = new GridData(SWT.FILL, SWT.CENTER, false, true);
        toolBarData.widthHint = SWT.DEFAULT;
        toolBarData.heightHint = SWT.DEFAULT;
        toolBarControl.setLayoutData(toolBarData);

        return composite;
    }

    private Control createText(Composite parent) {
        text = new Text(parent, textStyle);
        text.setTextLimit(7);
        text.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        if (text == null || text.isDisposed())
                            return;

                        if (isAncestorShell(text.getShell(), Display
                                .getCurrent().getActiveShell()))
                            return;
                        textEditingFinished();
                    }
                });
            }

            public void focusGained(FocusEvent e) {
            }
        });
        text.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE) {
                    fireCancelEditing();
                    e.doit = false;
                } else if (e.detail == SWT.TRAVERSE_RETURN) {
                    textEditingFinished();
                    e.doit = false;
                }
            }
        });
        text.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                if (e.keyCode == 27 && e.stateMask == 0) {
                    fireCancelEditing();
                    e.doit = false;
                } else if (e.keyCode == 13) {
                    textEditingFinished();
                    e.doit = false;
                } else {
                    if (COLOR_NONE_VALUE.equals(e.text)) {
                        return;
                    }
                    char[] charArray = e.text.toCharArray();
                    for (char character : charArray) {
                        boolean isValid = character == '#'
                                || (character >= 'A' && character <= 'F')
                                || (character >= 'a' && character <= 'f')
                                || (character >= '0' && character <= '9');
                        if (!isValid) {
                            e.doit = false;
                            return;
                        }
                    }
                }
            }
        });
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (modifying)
                    return;
                String value = text.getText();
                if ((value == null || value.lastIndexOf('#') != 0)
                        && !COLOR_NONE_VALUE.equals(value)) {
                    value = ""; //$NON-NLS-1$
                }
                changeValue("".equals(value) ? null : value); //$NON-NLS-1$
            }
        });
        text.addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (e.character == '\r') {
                    textEditingFinished();
                    e.doit = false;
                }
            }
        });
        return text;
    }

    private boolean isAncestorShell(Shell ancestor, Shell shell) {
        if (ancestor == null || shell == null || shell == ancestor)
            return false;
        Composite parent = shell.getParent();
        if (parent == ancestor)
            return true;
        if (parent instanceof Shell)
            return isAncestorShell(ancestor, (Shell) parent);
        return false;
    }

    private void textEditingFinished() {
        String value = text.getText();
        if ((value == null || value.length() != 7)
                && !COLOR_NONE_VALUE.equals(value))
            text.setText(""); //$NON-NLS-1$
        setValueToPicker(value);
        fireApplyEditorValue();
    }

    private Control createToolBar(Composite parent) {
        toolBar = new ToolBarManager();

        picker = new ColorPicker(configurer.getPopupStyle(),
                configurer.getPalette());
        configureColorPicker(picker);

        toolBar.add(picker);
        toolBar.createControl(parent);
        return toolBar.getControl();
    }

    /**
     * @param picker
     */
    private void configureColorPicker(ColorPicker picker) {
        picker.setAutoColor(configurer.getAutoColor());
        picker.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                ColorSelection selection = (ColorSelection) event
                        .getSelection();
                Object value;
                if (selection.isNone()) {
                    value = configurer.getNoneValue();
                } else if (selection.isAutomatic()) {
                    value = configurer.getAutoValue();
                } else {
                    RGB color = selection.getColor();
                    value = color == null ? null : ColorUtils.toString(color);
                }
                setValueToText(value);
                changeValue(value);
                fireApplyEditorValue();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.properties.PropertyEditor#setBackground(org.eclipse.swt.
     * graphics.Color)
     */
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        if (getControl() != null && !getControl().isDisposed())
            getControl().setBackground(color);
        if (toolBar != null && toolBar.getControl() != null
                && !toolBar.getControl().isDisposed())
            toolBar.getControl().setBackground(color);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.properties.PropertyEditor#setForeground(org.eclipse.swt.
     * graphics.Color)
     */
    @Override
    public void setForeground(Color color) {
        super.setForeground(color);
        if (getControl() != null && !getControl().isDisposed()) {
            getControl().setForeground(color);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.properties.PropertyEditor#setFont(org.eclipse.swt.graphics
     * .Font)
     */
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (getControl() != null && !getControl().isDisposed()) {
            getControl().setFont(font);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.properties.PropertyEditor#setFocus()
     */
    @Override
    public void setFocus() {
        if (text != null && !text.isDisposed()) {
            text.setFocus();
        } else {
            super.setFocus();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.properties.PropertyEditor#activateWidget()
     */
    @Override
    protected void activateWidget() {
        super.activateWidget();
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                if (toolBar == null || toolBar.getControl() == null
                        || toolBar.getControl().isDisposed())
                    return;
                picker.open();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.properties.PropertyEditor#setValueToWidget(java.lang.Object)
     */
    @Override
    protected void setValueToWidget(Object value) {
        setValueToText(value);
        setValueToPicker(value);
    }

    private void setValueToText(Object value) {
        if (value instanceof String) {
            text.setText((String) value);
        } else if (value instanceof RGB) {
            text.setText(ColorUtils.toString((RGB) value));
        } else if (value == null) {
            text.setText(""); //$NON-NLS-1$
        } else {
            text.setText(value.toString());
        }
    }

    private void setValueToPicker(Object value) {
        ISelection selection;
        if (configurer.isNoneValueSet()
                && (configurer.getNoneValue() == value || (configurer
                        .getNoneValue() != null && configurer.getNoneValue()
                        .equals(value)))) {
            selection = new ColorSelection(ColorSelection.NONE);
        } else if (configurer.isAutoValueSet()
                && (configurer.getAutoValue() == value || (configurer
                        .getAutoValue() != null && configurer.getAutoValue()
                        .equals(value)))) {
            selection = new ColorSelection(ColorSelection.AUTO);
        } else if (value instanceof String) {
            RGB color = ColorUtils.toRGB((String) value);
            selection = color == null ? ColorSelection.EMPTY
                    : new ColorSelection(color);
        } else if (value instanceof RGB) {
            selection = new ColorSelection((RGB) value);
        } else {
            selection = ColorSelection.EMPTY;
        }
        picker.setSelection(selection);
    }

}
