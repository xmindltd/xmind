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
package org.xmind.ui.font;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.xmind.ui.color.ColorSelection;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.color.PaletteContents;
import org.xmind.ui.color.PaletteItem;
import org.xmind.ui.color.PaletteViewer;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.viewers.ISliderContentProvider;
import org.xmind.ui.viewers.SliderViewer;

/**
 * @author briansun
 */
public class FontDialog extends Dialog implements IFontChooser {

    private static class TreeArrayContentProvider extends ArrayContentProvider
            implements ITreeContentProvider {

        /**
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parentElement) {
            return new Object[0];
        }

        /**
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element) {
            return null;
        }

        /**
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object element) {
            return false;
        }

    }

    private class SizeControlListener
            implements ModifyListener, ISelectionChangedListener {

        private boolean synchronizingSelection = false;

        public void selectionChanged(SelectionChangedEvent event) {
            if (synchronizingSelection)
                return;

            IStructuredSelection ss = (IStructuredSelection) event
                    .getSelection();
            Object o = ss.getFirstElement();
            if (o instanceof Integer) {
                int v = ((Integer) o).intValue();
                synchronizingSelection = true;
                setFontHeight(v);
                synchronizingSelection = false;
                fireFontChanged();
            }
        }

        public void modifyText(ModifyEvent e) {
            if (synchronizingSelection)
                return;

            int height;
            try {
                height = Integer.parseInt(sizeText.getText());
            } catch (Exception e1) {
                return;
            }

            synchronizingSelection = true;
            sizeChangedByText = true;
            setFontHeight(height);
            sizeChangedByText = false;
            synchronizingSelection = false;
            fireFontChanged();
        }

    }

    private static final int DEFAULT_GROUP_WIDTH = SWT.DEFAULT;
    private static final int DEFAULT_GROUP_HEIGHT = Util.isMac() ? 200 : 185;

    private static final int MIN_FONT_HEIGHT = 5;
    private static final int MAX_FONT_HEIGHT = 72;

    private static String DEFAULT_FONT_NAME = null;

    private static int DEFAULT_FONT_HEIGHT = SWT.DEFAULT;

    private static PaletteContents DEFAULT_PALLETE = null;

    private String fontName = null;

    private int fontHeight = SWT.DEFAULT;

    private RGB color = null;

    private boolean bold = false;

    private boolean italic = false;

    private boolean underline = false;

    private boolean strikeout = false;

    private FilteredTree fontTree = null;

    private Text sizeText = null;

    private ListViewer sizeList = null;

    private SliderViewer sizeScale = null;

    private Button boldButton = null;

    private Button italicButton = null;

    private Button underlineButton = null;

    private Button strikeoutButton = null;

    private PaletteViewer paletteViewer = null;

    private List<IFontChooserListener> fontDialogListeners = null;

    private boolean sizeChangedByText = false;

    /**
     * 
     */
    public FontDialog(Shell parent) {
        super(parent);
        setShellStyle(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite p = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
                .margins(5, 5).applyTo(p);

        Group fontGroup = new Group(p, SWT.NONE);
        fontGroup.setText(Messages.FamilyGroup_text);
        GridLayoutFactory.swtDefaults().applyTo(fontGroup);
        GridDataFactory.fillDefaults()
                .hint(DEFAULT_GROUP_WIDTH, DEFAULT_GROUP_HEIGHT)
                .applyTo(fontGroup);
        createFontControl(fontGroup);

        Group sizeGroup = new Group(p, SWT.NONE);
        sizeGroup.setText(Messages.HeightGroup_text);
//        GridLayoutFactory.swtDefaults().applyTo( sizeGroup );
        GridDataFactory.fillDefaults()
                .hint(DEFAULT_GROUP_WIDTH, DEFAULT_GROUP_HEIGHT)
                .applyTo(sizeGroup);
        createSizeControl(sizeGroup);

        Composite typeComposite = new Composite(p, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(typeComposite);
        GridDataFactory.fillDefaults()
                .hint(DEFAULT_GROUP_WIDTH, DEFAULT_GROUP_HEIGHT)
                .applyTo(typeComposite);

        Group typeGroup = new Group(typeComposite, SWT.NONE);
        typeGroup.setText(Messages.TypeGroup_text);
        GridLayoutFactory.swtDefaults().applyTo(typeGroup);
        GridDataFactory.fillDefaults().applyTo(typeGroup);
        createTypeControl(typeGroup);

        Group colorGroup = new Group(typeComposite, SWT.NONE);
        colorGroup.setText(Messages.ColorGroup_text);
        GridLayoutFactory.swtDefaults().applyTo(colorGroup);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(colorGroup);
        createColorControl(colorGroup);

        return p;
    }

    /**
     * @param parent
     */
    private void createColorControl(Group parent) {
        paletteViewer = new PaletteViewer();
        paletteViewer.setShowStandardLabel(false);
        paletteViewer.setHasVerticalIndent(false);
        paletteViewer.createControl(parent);
        paletteViewer.setInput(getPaletteContents());
        if (getColor() != null) {
            paletteViewer.setSelection(new ColorSelection(getColor()));
        }
        paletteViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        ISelection selection = event.getSelection();
                        if (selection instanceof IColorSelection) {
                            setColor(((IColorSelection) selection).getColor());
                        } else {
                            setColor(null);
                        }
                        fireFontChanged();
                    }
                });
        paletteViewer.getControl().setBackground(parent.getBackground());
    }

    protected PaletteContents getPaletteContents() {
        return getDefaultPaletteContents();
    }

    private static PaletteContents getDefaultPaletteContents() {
        if (DEFAULT_PALLETE == null) {
            PaletteContents paletteContent = new PaletteContents(9, 3, 3);

            paletteContent.addItem(PaletteItem.Red);
            paletteContent.addItem(PaletteItem.Orange);
            paletteContent.addItem(PaletteItem.Green);
            paletteContent.addItem(PaletteItem.Blue);
            paletteContent.addItem(PaletteItem.Yellow);
            paletteContent.addItem(PaletteItem.Indigo);
            paletteContent.addItem(PaletteItem.Purple);
            paletteContent.addItem(PaletteItem.Black);
            paletteContent.addItem(PaletteItem.White);
            DEFAULT_PALLETE = paletteContent;
        }
        return DEFAULT_PALLETE;
    }

    /**
     * @param parent
     */
    private void createTypeControl(Group parent) {
        boldButton = new Button(parent, SWT.CHECK);
        boldButton.setText(Messages.BoldButton_text);

        boldButton.setFont(JFaceResources.getFontRegistry()
                .getBold(JFaceResources.DEFAULT_FONT));
        boldButton.setSelection(getBold());

        italicButton = new Button(parent, SWT.CHECK);
        italicButton.setText(Messages.ItalicButton_text);
        italicButton.setFont(JFaceResources.getFontRegistry()
                .getItalic(JFaceResources.DEFAULT_FONT));
        italicButton.setSelection(getItalic());

        underlineButton = new Button(parent, SWT.CHECK);
        underlineButton.setText(Messages.UnderlineButton_text);
        underlineButton.addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent e) {
                e.gc.drawLine(e.x + 20, e.y + e.height - 4, e.x + e.width,
                        e.y + e.height - 4);
            }

        });
        underlineButton.setSelection(getUnderline());

        strikeoutButton = new Button(parent, SWT.CHECK);
        strikeoutButton.setText(Messages.StrikeoutButton_text);
        strikeoutButton.addPaintListener(new PaintListener() {

            public void paintControl(PaintEvent e) {
                e.gc.drawLine(e.x + 20, e.y + e.height / 2, e.x + e.width,
                        e.y + e.height / 2);
            }

        });
        strikeoutButton.setSelection(getStrikeout());

        Listener listener = new Listener() {

            public void handleEvent(Event event) {
                setBold(boldButton.getSelection());
                setItalic(italicButton.getSelection());
                setUnderline(underlineButton.getSelection());
                setStrikeout(strikeoutButton.getSelection());
                fireFontChanged();
            }

        };
        boldButton.addListener(SWT.Selection, listener);
        italicButton.addListener(SWT.Selection, listener);
        underlineButton.addListener(SWT.Selection, listener);
        strikeoutButton.addListener(SWT.Selection, listener);
    }

    /**
     * @param parent
     */
    private void createSizeControl(Group parent) {
        GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false)
                .applyTo(parent);

        SizeControlListener listener = new SizeControlListener();
        sizeText = new Text(parent, SWT.BORDER | SWT.CENTER);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(sizeText);

        sizeText.addModifyListener(listener);

        sizeList = new ListViewer(parent,
                SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        sizeList.setContentProvider(new ArrayContentProvider());
        sizeList.setLabelProvider(new LabelProvider());
        sizeList.setInput(Arrays.asList(8, 9, 10, 11, 12, 14, 16, 18, 20, 22,
                24, 36, 48, 56, 64, 72));

        int listWidthHint = Util.isMac() ? SWT.DEFAULT : 40;
        GridDataFactory.fillDefaults().grab(false, true)
                .hint(listWidthHint, SWT.DEFAULT)
                .applyTo(sizeList.getControl());

        sizeScale = new SliderViewer(parent, SWT.VERTICAL);
        int scaleWidthHint = Util.isMac() ? SWT.DEFAULT : 20;
        GridDataFactory.fillDefaults().grab(false, true)
                .hint(scaleWidthHint, SWT.DEFAULT)
                .applyTo(sizeScale.getControl());

        sizeScale.setContentProvider(new ISliderContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }

            public void dispose() {
            }

//            public Object[] getValues(Object input) {
//                return new Integer[] { Integer.valueOf(MIN_FONT_HEIGHT),
//                        Integer.valueOf(MAX_FONT_HEIGHT) };
//            }

            public Object getValue(Object input, double ratio) {
                int value = (int) Math
                        .round((MAX_FONT_HEIGHT - MIN_FONT_HEIGHT) * ratio
                                + MIN_FONT_HEIGHT);
                return Integer.valueOf(value);
            }

            public double getRatio(Object input, Object value) {
                int v = ((Integer) value).intValue();
                return (v - MIN_FONT_HEIGHT) * 1.0d
                        / (MAX_FONT_HEIGHT - MIN_FONT_HEIGHT);
            }

        });

        sizeList.addSelectionChangedListener(listener);
        sizeScale.addSelectionChangedListener(listener);

        setFontHeight(getFontHeight());
    }

    /**
     * @param parent
     */
    private void createFontControl(Group parent) {
//        fontTree = new FilteredTree(parent, SWT.FULL_SELECTION,
//                new PatternFilter());
        fontTree = new FilteredTree(parent, SWT.FULL_SELECTION,
                new PatternFilter(), true);
        fontTree.getViewer().setContentProvider(new TreeArrayContentProvider());
        fontTree.getViewer().setInput(FontUtils.getAvailableFontNames());
        setFontName(getFontName());
        fontTree.getViewer()
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        IStructuredSelection sel = (IStructuredSelection) event
                                .getSelection();
                        if (sel.isEmpty()) {
                            setFontName(null);
                        } else {
                            setFontName((String) sel.getFirstElement());
                        }
                        fireFontChanged();
                    }

                });
//        fontTree.setEnabled(false);
//        FontUtils.fetchAvailableFontNames(parent.getDisplay(),
//                new IFontNameListCallback() {
//                    public void setAvailableFontNames(List<String> fontNames) {
//                        if (fontTree.isDisposed())
//                            return;
//                        fontTree.setEnabled(true);
//                        fontTree.getViewer().setInput(fontNames);
//                        setFontName(getFontName());
//                        fontTree.getViewer().addSelectionChangedListener(
//                                new ISelectionChangedListener() {
//                                    public void selectionChanged(
//                                            SelectionChangedEvent event) {
//                                        IStructuredSelection sel = (IStructuredSelection) event
//                                                .getSelection();
//                                        if (sel.isEmpty()) {
//                                            setFontName(null);
//                                        } else {
//                                            setFontName((String) sel
//                                                    .getFirstElement());
//                                        }
//                                        fireFontChanged();
//                                    }
//
//                                });
//                    }
//
//                });
        GridDataFactory.fillDefaults().grab(true, true).hint(150, SWT.DEFAULT)
                .applyTo(fontTree);
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.FontDialog_text);
//        newShell.setSize( 400, 200 );
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#getInitialLocation(org.eclipse.swt.graphics.Point)
     */
    @Override
    protected Point getInitialLocation(Point initialSize) {
        Rectangle r = getParentShell().getBounds();
        return new Point(r.x + r.width - initialSize.x,
                r.y + r.height - initialSize.y);
    }

    /**
     * @param listener
     */
    public void addFontChooserListener(IFontChooserListener listener) {
        if (fontDialogListeners == null)
            fontDialogListeners = new ArrayList<IFontChooserListener>();
        fontDialogListeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeFontChooserListener(IFontChooserListener listener) {
        if (fontDialogListeners == null)
            return;
        fontDialogListeners.remove(listener);
    }

    /**
     * 
     */
    protected void fireFontChanged() {
        if (fontDialogListeners == null)
            return;
        for (final Object l : fontDialogListeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IFontChooserListener) l).fontChanged(FontDialog.this);
                }
            });
        }
    }

    /**
     * @return the color
     */
    public RGB getColor() {
        return color;
    }

    /**
     * @param color
     *            the color to set
     */
    public void setColor(RGB color) {
        this.color = color;
        if (paletteViewer != null && !paletteViewer.getControl().isDisposed()) {
            if (color == null)
                paletteViewer.setSelection(ColorSelection.EMPTY);
            else
                paletteViewer.setSelection(new ColorSelection(color));
        }
    }

    /**
     * @return the bold
     */
    public boolean getBold() {
        return bold;
    }

    /**
     * @param bold
     *            the bold to set
     */
    public void setBold(boolean bold) {
        this.bold = bold;
        if (boldButton != null && !boldButton.isDisposed()) {
            boldButton.setSelection(bold);
        }
    }

    /**
     * @return the italic
     */
    public boolean getItalic() {
        return italic;
    }

    /**
     * @param italic
     *            the italic to set
     */
    public void setItalic(boolean italic) {
        this.italic = italic;
        if (italicButton != null && !italicButton.isDisposed()) {
            italicButton.setSelection(italic);
        }
    }

    /**
     * @return the underline
     */
    public boolean getUnderline() {
        return underline;
    }

    /**
     * @param underline
     *            the underline to set
     */
    public void setUnderline(boolean underline) {
        this.underline = underline;
        if (underlineButton != null && !underlineButton.isDisposed()) {
            underlineButton.setSelection(underline);
        }
    }

    /**
     * @return the strikeout
     */
    public boolean getStrikeout() {
        return strikeout;
    }

    /**
     * @param strikeout
     *            the strikeout to set
     */
    public void setStrikeout(boolean strikeout) {
        this.strikeout = strikeout;
        if (strikeoutButton != null && !strikeoutButton.isDisposed()) {
            strikeoutButton.setSelection(strikeout);
        }
    }

    /**
     * @return the fontName
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * @param fontName
     *            the fontName to set
     */
    public void setFontName(String fontName) {
        if (fontName == null)
            fontName = getDefaultFontName();
        this.fontName = fontName;
        if (fontTree != null && !fontTree.isDisposed()
                && fontTree.isEnabled()) {
            fontTree.setInitialText(fontName);
        }
    }

    /**
     * @return the fontHeight
     */
    public int getFontHeight() {
        return fontHeight;
    }

    /**
     * @param fontHeight
     *            the fontHeight to set
     */
    public void setFontHeight(int fontHeight) {
        if (fontHeight < 0)
            fontHeight = getDefaultFontHeight();
        this.fontHeight = fontHeight;
        if (sizeText != null && !sizeText.isDisposed() && !sizeChangedByText) {
            int position = -1;
            if (sizeText.isFocusControl()) {
                position = sizeText.getSelection().x;
            }
            sizeText.setText(Integer.toString(fontHeight));
            if (position >= 0) {
                sizeText.setSelection(
                        Math.min(position, sizeText.getText().length()));
            }
        }
        if (sizeList != null && !sizeList.getControl().isDisposed()) {
            sizeList.setSelection(
                    new StructuredSelection(Integer.valueOf(fontHeight)), true);
        }
        if (sizeScale != null && !sizeScale.getControl().isDisposed()) {
            sizeScale.setSelection(
                    new StructuredSelection(Integer.valueOf(fontHeight)), true);
        }
    }

    protected static String getDefaultFontName() {
        if (DEFAULT_FONT_NAME == null)
            DEFAULT_FONT_NAME = JFaceResources.getDefaultFont().getFontData()[0]
                    .getName();
        return DEFAULT_FONT_NAME;
    }

    protected static int getDefaultFontHeight() {
        if (DEFAULT_FONT_HEIGHT < 0)
            DEFAULT_FONT_HEIGHT = JFaceResources.getDefaultFont()
                    .getFontData()[0].getHeight();
        return DEFAULT_FONT_HEIGHT;
    }

    /**
     * @param fd
     */
    public void setInitialFont(FontData fd) {
        setFontName(fd.getName());
        setFontHeight(fd.getHeight());
        setBold((fd.getStyle() & SWT.BOLD) != 0);
        setItalic((fd.getStyle() & SWT.ITALIC) != 0);
    }

}