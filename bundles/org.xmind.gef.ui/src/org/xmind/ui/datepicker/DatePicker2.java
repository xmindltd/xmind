package org.xmind.ui.datepicker;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.YEAR;
import static org.eclipse.jface.resource.JFaceResources.DEFAULT_FONT;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.ui.dialogs.PopupDialog;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.viewers.SWTUtils;

/**
 * A viewer to pick a date on the calendar.
 */
public class DatePicker2 extends Viewer {

    private static final String[] MONTHS = new String[] { Messages.January,
            Messages.Feburary, Messages.March, Messages.April, Messages.May,
            Messages.June, Messages.July, Messages.August, Messages.September,
            Messages.October, Messages.November, Messages.December };

    private static final String[] WEEK_SYMBOLS = new String[] { Messages.Sunday,
            Messages.Monday, Messages.Tuesday, Messages.Wednesday,
            Messages.Thursday, Messages.Friday, Messages.Saturday };

    private static final int FUTURE_YEARS = 7;

    private static final int PASSED_YEARS = 3;

    private static final String COLOR_TEXT = "#000000"; //$NON-NLS-1$

    private static final String COLOR_WEEKEND = "#EE0000"; //$NON-NLS-1$

    private static final String COLOR_SEPARATOR = "#C0C0C0"; //$NON-NLS-1$

    private static final String COLOR_TODAY = "#ff9900"; //$NON-NLS-1$

    private static final String COLOR_WEEK_SYMBOL = "#808080"; //$NON-NLS-1$

    private static final String COLOR_ARROW_BORDER = "#808080"; //$NON-NLS-1$

    private static final String COLOR_ARROW_FILL = "#C0C0C0"; //$NON-NLS-1$

    private static final String COLOR_CANCEL = "#D80000"; //$NON-NLS-1$

    private static final int NORMAL_ALPHA = 0xff;

    private static final int SIBLING_MONTH_ALPHA = 0x20;

    private static final int DURATION = 200;

    private static final int TOTAL_DAYS = 42;

    private static Color validColor = ColorUtils.getColor("#ffffff"); //$NON-NLS-1$

    private static Color invalidColor = ColorUtils.getColor("#f0f0f0"); //$NON-NLS-1$

    private class EventHandler
            implements MouseListener, MouseMotionListener, Listener {

        private boolean dayPressed = false;

        private BaseFigure target = null;

        public void attach(IFigure figure) {
            figure.addMouseListener(this);
            figure.addMouseMotionListener(this);
        }

        public void detach(IFigure figure) {
            figure.removeMouseListener(this);
            figure.removeMouseMotionListener(this);
        }

        public void install(Control control) {
            control.addListener(SWT.MouseUp, this);
            control.addListener(SWT.MouseWheel, this);
            control.addListener(SWT.KeyDown, this);
        }

        public void uninstall(Control control) {
            control.removeListener(SWT.MouseUp, this);
            control.removeListener(SWT.MouseWheel, this);
            control.removeListener(SWT.KeyDown, this);
        }

        public void handleEvent(Event event) {
            if (event.type == SWT.MouseUp) {
                dayPressed = false;
                if (target != null) {
                    final BaseFigure eventTarget = target;
                    target.setPressed(false);
                    target = null;
                    selected(eventTarget);
                }
            } else if (event.type == SWT.MouseWheel) {
                if (event.count == 0)
                    return;
                // wheel upwards, count > 0, month should decrease
                // wheel downwards, count < 0, month should increase
                rollMonth(event.count > 0 ? -1 : 1);
            } else if (event.type == SWT.KeyDown) {
                handleKeyPress(event.keyCode, event.stateMask);
            }
        }

        private void handleKeyPress(int key, int mask) {
            if (SWTUtils.matchKey(mask, key, 0, SWT.ARROW_UP)) {
                lastMonthSelected(true);
            } else if (SWTUtils.matchKey(mask, key, 0, SWT.ARROW_DOWN)) {
                nextMonthSelected(true);
            } else if (SWTUtils.matchKey(mask, key, 0, SWT.ARROW_LEFT)) {
                lastYearSelected(true);
            } else if (SWTUtils.matchKey(mask, key, 0, SWT.ARROW_RIGHT)) {
                nextYearSelected(true);
            }
        }

        public void mouseDoubleClicked(MouseEvent me) {
            // do nothing
        }

        public void mouseDragged(MouseEvent me) {
            // do nothing
        }

        public void mouseEntered(MouseEvent me) {
            if (target == null) {
                BaseFigure source = (BaseFigure) me.getSource();
                if (source instanceof DayFigure && dayPressed) {
                    source.setPressed(true);
                    source.setPreselected(false);
                } else {
                    if (source == monthFigure || source == lastMonth
                            || source == nextMonth) {
                        monthFigure.setPreselected(true);
                        lastMonth.getContent().setVisible(true);
                        nextMonth.getContent().setVisible(true);
                    } else if (source == yearFigure || source == lastYear
                            || source == nextYear) {
                        yearFigure.setPreselected(true);
                        lastYear.getContent().setVisible(true);
                        nextYear.getContent().setVisible(true);
                    }
                    source.setPreselected(true);
                }
            }
        }

        public void mouseExited(MouseEvent me) {
            if (target == null) {
                BaseFigure source = (BaseFigure) me.getSource();
                if (source instanceof DayFigure) {
                    source.setPreselected(false);
                    if (dayPressed) {
                        source.setPressed(false);
                    }
                } else {
                    if (source == monthFigure || source == lastMonth
                            || source == nextMonth) {
                        monthFigure.setPreselected(false);
                        lastMonth.getContent().setVisible(false);
                        nextMonth.getContent().setVisible(false);
                    } else if (source == yearFigure || source == lastYear
                            || source == nextYear) {
                        yearFigure.setPreselected(false);
                        lastYear.getContent().setVisible(false);
                        nextYear.getContent().setVisible(false);
                    }
                    source.setPreselected(false);
                }
            }
        }

        public void mouseHover(MouseEvent me) {
            // do nothing
        }

        public void mouseMoved(MouseEvent me) {
            // do nothing
        }

        public void mousePressed(MouseEvent me) {
            BaseFigure source = (BaseFigure) me.getSource();
            source.setPressed(true);
            source.setPreselected(false);
            if (source instanceof DayFigure) {
                dayPressed = true;
            } else {
                target = source;
            }
        }

        public void mouseReleased(MouseEvent me) {
            BaseFigure source = (BaseFigure) me.getSource();
            source.setPressed(false);
            if (source instanceof DayFigure) {
                if (dayPressed) {
                    daySelected((DayFigure) me.getSource());
                }
                source.setPreselected(true);
            } else {
                if (!source.isSelected()) {
                    source.setPreselected(true);
                }
            }
        }

    }

    private class DropdownDatePicker extends PopupDialog {

        public DropdownDatePicker(Shell parent) {
            super(parent, SWT.NO_TRIM, true, false, false, false, false, null,
                    null);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            createDatePicker(composite);
            datePicker.setLayoutData(new org.eclipse.swt.layout.GridData(
                    SWT.FILL, SWT.FILL, true, true));
            initDatePicker();
            update();
            return composite;
        }

        @Override
        public boolean close() {
            boolean closed = super.close();
            if (closed) {
                datePicker = null;
            }
            return closed;
        }

        @Override
        protected Point getInitialLocation(Point initialSize) {
            Control c = DatePicker2.this.getControl();
            org.eclipse.swt.graphics.Rectangle r = c.getBounds();
            return c.toDisplay(-2, r.height - 1);
        }

    }

    private class MonthAction extends Action {

        private int month;

        public MonthAction(int month) {
            super(MONTHS[month]);
            this.month = month;
        }

        public void run() {
            monthSelected(month);
        }
    }

    private class YearAction extends Action {

        private int year;

        public void setYear(int year) {
            this.year = year;
            setText("" + year); //$NON-NLS-1$
        }

        public int getYear() {
            return year;
        }

        public void run() {
            yearSelected(year);
        }
    }

    private class AnimationAdvisor implements IAnimationAdvisor {

        private int monthsToRoll = 0;

        private int yearsToRoll = 0;

        private int duration = -1;

        private int oldYear;

        private int oldMonth;

        private int newYear;

        private int newMonth;

        public void addMonthsToRoll(int count) {
            monthsToRoll += count;
            duration = -1;
        }

        public void addYearsToRoll(int count) {
            yearsToRoll += count;
            duration = -1;
        }

        public int getDuration() {
            if (duration < 0) {
                int steps = Math.abs(monthsToRoll) + Math.abs(yearsToRoll);
                duration = steps == 0 ? 0 : DURATION / steps;
            }
            return duration;
        }

        public IFigure getLayer() {
            return DatePicker2.this.dateLayer;
        }

        public int getMonthsToRoll() {
            return monthsToRoll;
        }

        public int getNewMonth() {
            return newMonth;
        }

        public int getNewYear() {
            return newYear;
        }

        public int getOldMonth() {
            return oldMonth;
        }

        public int getOldYear() {
            return oldYear;
        }

        public IFigure getPanel() {
            return DatePicker2.this.datePanel;
        }

        public int getYearsToRoll() {
            return yearsToRoll;
        }

        public void initNewDay(DayFigure figure) {
            updateDayFigure(figure, oldYear, oldMonth);
        }

        public void initOldDay(DayFigure figure) {
            figure.setPreselected(false);
            figure.setSelected(isSameDay(figure.getDate(), getSelectedDate()));
            eventHandler.detach(figure);
        }

        public boolean isDone() {
            return monthsToRoll == 0 && yearsToRoll == 0;
        }

        public void setEndMonth(int newYear, int newMonth) {
            this.newYear = newYear;
            this.newMonth = newMonth;
        }

        public void setStartMonth(int oldYear, int oldMonth) {
            this.oldYear = oldYear;
            this.oldMonth = oldMonth;
        }

        public void updateNewDay(DayFigure figure) {
            figure.setPreselected(false);
            figure.setSelected(isSameDay(figure.getDate(), getSelectedDate()));
            eventHandler.attach(figure);
            updateDayFigure(figure, newYear, newMonth);
        }

    }

    private int style;

    private Control control;

    private FigureCanvas datePicker;

    private Composite placeholder;

    private Text yearText;

    private Text monthText;

    private Text dayText;

    private Button showDatePickerButton;

    private Label slash1;

    private Label slash2;

    private ILabelProvider dateLabelProvider;

    private DropdownDatePicker dropdownDatePicker;

    private Calendar today;

    private int currentMonth;

    private int currentYear;

    private Calendar selection;

    private IFigure dateLayer;

    private IFigure datePanel;

    private MonthFigure monthFigure;

    private YearFigure yearFigure;

    private BaseFigure todayFigure;

    private BaseFigure cancelFigure;

    private EventHandler eventHandler;

    private MenuManager monthMenu;

    private MonthAction[] monthActions;

    private MenuManager yearMenu;

    private YearAction[] yearActions;

    private ArrowFigure lastYear;

    private ArrowFigure nextYear;

    private ArrowFigure lastMonth;

    private ArrowFigure nextMonth;

    private boolean firingSelectionChange = false;

    private boolean animating = false;

    private IAnimationAdvisor animationAdvisor = new AnimationAdvisor();

    private String oldValue = ""; //$NON-NLS-1$

    private Calendar minTime;

    private Calendar maxTime;

    /**
     * Constructs a new instance of this class given its parent and a style
     * value describing its behavior and appearance.
     * 
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param style
     *            the style of control to construct
     * @see SWT#SIMPLE
     * @see SWT#DROP_DOWN
     * @see SWT#CANCEL
     */
    public DatePicker2(Composite parent, int style) {
        this(parent, style, Calendar.getInstance());
    }

    public DatePicker2(Composite parent, int style, Calendar today) {
        this.today = today;
        this.currentMonth = today.get(MONTH);
        this.currentYear = today.get(YEAR);
        this.style = style;
        if ((style & SWT.DROP_DOWN) != 0) {
            createPlaceholder(parent);
            this.control = placeholder;
            initPlaceholder();
        } else {
            createDatePicker(parent);
            this.control = datePicker;
            initDatePicker();
        }
        update();
    }

    private void createPlaceholder(Composite parent) {
        Composite composite = new Composite(parent, SWT.BORDER);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(
                6, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);
        composite.setBackground(validColor);

        dayText = new Text(composite, SWT.SINGLE | SWT.NONE);
        org.eclipse.swt.layout.GridData dateData = new org.eclipse.swt.layout.GridData(
                SWT.RIGHT, SWT.CENTER, false, false);
//        dateData.widthHint = 14;
        dayText.setLayoutData(dateData);
        dayText.setTextLimit(2);
        dayText.setBackground(dayText.getParent().getBackground());

        slash1 = new Label(composite, SWT.NONE);
        slash1.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.CENTER,
                SWT.CENTER, false, false));
        slash1.setBackground(slash1.getParent().getBackground());
        slash1.setText("/"); //$NON-NLS-1$

        monthText = new Text(composite, SWT.SINGLE | SWT.NONE);
        org.eclipse.swt.layout.GridData monthData = new org.eclipse.swt.layout.GridData(
                SWT.CENTER, SWT.CENTER, false, false);
//        monthData.widthHint = 14;
        monthText.setLayoutData(monthData);
        monthText.setTextLimit(2);
        monthText.setBackground(monthText.getParent().getBackground());

        slash2 = new Label(composite, SWT.NONE);
        slash2.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.CENTER,
                SWT.CENTER, false, false));
        slash2.setBackground(slash2.getParent().getBackground());
        slash2.setText("/"); //$NON-NLS-1$

        yearText = new Text(composite, SWT.SINGLE | SWT.NONE);
        org.eclipse.swt.layout.GridData yearData = new org.eclipse.swt.layout.GridData(
                SWT.LEFT, SWT.CENTER, false, false);
//        yearData.widthHint = 28;
        yearText.setLayoutData(yearData);
        yearText.setTextLimit(4);
        yearText.setBackground(yearText.getParent().getBackground());

        showDatePickerButton = new Button(composite, SWT.ARROW | SWT.DOWN);
        showDatePickerButton.setLayoutData(new org.eclipse.swt.layout.GridData(
                SWT.LEFT, SWT.CENTER, false, false));

        filterInput();

        this.placeholder = composite;
    }

    private void filterInput() {
        checkInput(dayText);
        checkInput(monthText);
        checkInput(yearText);
    }

    private void checkInput(final Text field) {
        KeyListener keyListener = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.character) {
                case SWT.ESC:
                    field.setText(oldValue);
                    field.getParent().forceFocus();
                    break;

                case SWT.CR:
                    field.getParent().forceFocus();
                    break;
                }
            }
        };

        FocusListener focusListener = new FocusListener() {

            public void focusLost(FocusEvent e) {
                editField(field, oldValue);
                oldValue = ""; //$NON-NLS-1$
            }

            public void focusGained(FocusEvent e) {
                oldValue = field.getText();
                field.selectAll();
            }
        };

        field.addKeyListener(keyListener);
        field.addFocusListener(focusListener);
    }

    private void editField(Text field, String oldValue) {
        String text = field.getText();
        if (text.equals(oldValue)) {
            return;
        }
        if ("".equals(text)) { //$NON-NLS-1$
            field.setText(oldValue);
            return;
        }

        int value = 0;
        text = text.trim();
        Pattern re = Pattern.compile("^\\d+$"); //$NON-NLS-1$
        Matcher m = re.matcher(text);
        if (!m.matches()) {
            field.setText(oldValue);
            return;
        }

        value = parseInt(m.group());
        int calendarField = (field == yearText ? Calendar.YEAR
                : (field == monthText ? Calendar.MONTH
                        : Calendar.DAY_OF_MONTH));
        if (calendarField == Calendar.MONTH) {
            value -= 1;
        }

        Calendar newTime = Calendar.getInstance();
        newTime.setTimeInMillis(getSelectedDate().getTimeInMillis());
        newTime.set(calendarField, value);
        newTime = checkTime(newTime);
        changeDate(newTime);

        refresh();
    }

    private int parseInt(String s) {
        if (s != null && !"".equals(s)) {//$NON-NLS-1$
            try {
                return Integer.parseInt(s, 10);
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }

    private Calendar checkTime(Calendar newTime) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(newTime.getTimeInMillis());
        if (calendar.before(minTime)) {
            MessageDialog.openInformation(Display.getDefault().getActiveShell(),
                    Messages.TimeCheckInvalid_label,
                    Messages.TimeCheckInvalidSmall_message);
            calendar.setTimeInMillis(minTime.getTimeInMillis());
        } else if (calendar.after(maxTime)) {
            MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
                    Messages.TimeCheckInvalid_label,
                    Messages.TimeCheckInvalidBig_message);
            calendar.setTimeInMillis(maxTime.getTimeInMillis());
        }

        return calendar;
    }

    private void createDatePicker(Composite parent) {
        this.datePicker = new FigureCanvas(parent);
    }

    public void setLabelProvider(ILabelProvider labelProvider) {
        this.dateLabelProvider = labelProvider;
        update();
    }

    public ILabelProvider getLabelProvider() {
        if (this.dateLabelProvider == null) {
            this.dateLabelProvider = new DateLabelProvider();
        }
        return this.dateLabelProvider;
    }

    public Control getControl() {
        return control;
    }

    public FigureCanvas getDatePicker() {
        return datePicker;
    }

    public void setBackground(Color color) {
        if (control != null && !control.isDisposed()) {
            control.setBackground(color);
        }
    }

    public void setEnabled(boolean enabled) {
        if (placeholder != null) {
            placeholder.setEnabled(enabled);
            placeholder.setBackground(enabled ? validColor : invalidColor);
            yearText.setEnabled(enabled);
            yearText.setBackground(enabled ? validColor : invalidColor);
            monthText.setEnabled(enabled);
            monthText.setBackground(enabled ? validColor : invalidColor);
            dayText.setEnabled(enabled);
            dayText.setBackground(enabled ? validColor : invalidColor);
            slash1.setEnabled(enabled);
            slash1.setBackground(enabled ? validColor : invalidColor);
            slash2.setEnabled(enabled);
            slash2.setBackground(enabled ? validColor : invalidColor);
            showDatePickerButton.setEnabled(enabled);
        }
    }

    public Composite getPlaceholder() {
        return placeholder;
    }

    public ISelection getSelection() {
        return new DateSelection(selection);
    }

    @Override
    public void setSelection(ISelection selection) {
        setSelection(selection, true);
    }

    @Override
    public void setSelection(ISelection selection, boolean reveal) {
        if (selection instanceof DateSelection) {
            setDateSelection(((DateSelection) selection).getDate(), reveal);
        } else if (selection instanceof IStructuredSelection) {
            Object sel = ((IStructuredSelection) selection).getFirstElement();
            if (sel instanceof Calendar) {
                setDateSelection((Calendar) sel, reveal);
            }
        }
    }

    public void setDateSelection(Calendar date, boolean reveal) {
        changeDate(date);
        if (reveal && date != null) {
            changeCalendar(date.get(YEAR), date.get(MONTH));
        }
        update();
    }

    private void initPlaceholder() {
        showDatePickerButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (dropdownDatePicker == null
                        || dropdownDatePicker.getShell() == null
                        || dropdownDatePicker.getShell().isDisposed()
                        || !dropdownDatePicker.isClosing())
                    showDropdown();
            }

        });
    }

    private void showDropdown() {
        placeholder.forceFocus();
        createDropdownDatePicker();
        dropdownDatePicker.open();
        Shell shell = dropdownDatePicker.getShell();
        if (shell != null && !shell.isDisposed()) {
            shell.addListener(SWT.Dispose, new Listener() {
                public void handleEvent(Event event) {
                    if (placeholder != null && !placeholder.isDisposed()) {
                        placeholder.forceFocus();
                    }
                }
            });
        }
    }

    /**
     * Shows the drop-down menu if this date picker is created with
     * <code>SWT.DROP_DOWN</code> style bit.
     */
    public void open() {
        showDropdown();
    }

    private void createDropdownDatePicker() {
        if (dropdownDatePicker != null)
            return;
        dropdownDatePicker = new DropdownDatePicker(control.getShell());
    }

    private void initDatePicker() {
        datePicker.setScrollBarVisibility(FigureCanvas.NEVER);

        eventHandler = new EventHandler();
        eventHandler.install(datePicker);
        datePicker.addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event event) {
                eventHandler.uninstall(datePicker);
            }
        });

        Viewport viewport = new Viewport(true);
        viewport.setContentsTracksHeight(true);
        viewport.setContentsTracksWidth(true);
        datePicker.setViewport(viewport);

        IFigure container = new Layer();
        datePicker.setContents(container);

        GridLayout containerLayout = new GridLayout(1, true);
        containerLayout.horizontalSpacing = 3;
        containerLayout.verticalSpacing = 3;
        containerLayout.marginHeight = 3;
        containerLayout.marginWidth = 3;
        container.setLayoutManager(containerLayout);
        createTopPanel(container);
        createSeparator(container);
        createWeekPanel(container);
        createDaysPanel(container);
        createSeparator(container);
        createBottomPanel(container);
    }

    private void createTopPanel(IFigure parent) {
        IFigure panel = new Layer();
        GridData panelConstraint = new GridData(SWT.FILL, SWT.FILL, true,
                false);
        parent.add(panel, panelConstraint);

        GridLayout panelLayout = new GridLayout(12, true);
        panelLayout.horizontalSpacing = 0;
        panelLayout.verticalSpacing = 0;
        panelLayout.marginHeight = 0;
        panelLayout.marginWidth = 0;
        panel.setLayoutManager(panelLayout);

        lastMonth = createArrowFigure(panel, ArrowFigure.UP);
        monthFigure = createMonthFigure(panel);
        nextMonth = createArrowFigure(panel, ArrowFigure.DOWN);
        lastYear = createArrowFigure(panel, ArrowFigure.LEFT);
        yearFigure = createYearFigure(panel);
        nextYear = createArrowFigure(panel, ArrowFigure.RIGHT);
    }

    private MonthFigure createMonthFigure(IFigure parent) {
        MonthFigure figure = new MonthFigure();
        figure.setTextCandidates(MONTHS);
        GridData constraint = new GridData(SWT.FILL, SWT.FILL, true, true);
        constraint.horizontalSpan = 5;
        parent.add(figure, constraint);
        eventHandler.attach(figure);
        return figure;
    }

    private YearFigure createYearFigure(IFigure parent) {
        YearFigure figure = new YearFigure();
        figure.setTextCandidates(new String[] { "0000" }); //$NON-NLS-1$
        GridData constraint = new GridData(SWT.FILL, SWT.FILL, true, true);
        constraint.horizontalSpan = 3;
        parent.add(figure, constraint);
        eventHandler.attach(figure);
        return figure;
    }

    private ArrowFigure createArrowFigure(IFigure parent, int orientation) {
        ArrowFigure arrow = new ArrowFigure();
        arrow.setOrientation(orientation);
        arrow.setForegroundColor(ColorUtils.getColor(COLOR_ARROW_BORDER));
        arrow.setBackgroundColor(ColorUtils.getColor(COLOR_ARROW_FILL));
        arrow.getContent().setVisible(false);
        GridData constraint = new GridData(SWT.FILL, SWT.FILL, true, true);
        parent.add(arrow, constraint);
        eventHandler.attach(arrow);
        return arrow;
    }

    private void createSeparator(IFigure parent) {
        HorizontalLine line = new HorizontalLine();
        line.setMargin(3);
        line.setForegroundColor(ColorUtils.getColor(COLOR_SEPARATOR));
        GridData constraint = new GridData(SWT.FILL, SWT.FILL, true, false);
        constraint.heightHint = 3;
        parent.add(line, constraint);
    }

    private void createWeekPanel(IFigure parent) {
        IFigure panel = new Layer();
        GridData panelConstraint = new GridData(SWT.FILL, SWT.FILL, true,
                false);
        parent.add(panel, panelConstraint);
        GridLayout panelLayout = new GridLayout(7, true);
        panelLayout.horizontalSpacing = 0;
        panelLayout.verticalSpacing = 0;
        panelLayout.marginHeight = 0;
        panelLayout.marginWidth = 0;
        panel.setLayoutManager(panelLayout);
        Font symbolFont = FontUtils.getRelativeHeight(DEFAULT_FONT, -2);
        for (int i = 0; i < 7; i++) {
            TextLayer symbol = new TextLayer();
            symbol.setFont(symbolFont);
            symbol.setText(WEEK_SYMBOLS[i]);
            if (i == 0 || i == 6) {
                symbol.setForegroundColor(ColorUtils.getColor(COLOR_WEEKEND));
            } else {
                symbol.setForegroundColor(
                        ColorUtils.getColor(COLOR_WEEK_SYMBOL));
            }
            GridData symbolConstraint = new GridData(SWT.FILL, SWT.FILL, true,
                    true);
            panel.add(symbol, symbolConstraint);
        }
    }

    private void createDaysPanel(IFigure parent) {
        dateLayer = new Layer();
        GridData layerConstraint = new GridData(SWT.FILL, SWT.FILL, true, true);
        parent.add(dateLayer, layerConstraint);
        dateLayer.setLayoutManager(new ConstraintStackLayout());

        datePanel = new Layer();
        dateLayer.add(datePanel, null);
        datePanel.setLayoutManager(new DatePanelLayout());
        for (int i = 0; i < TOTAL_DAYS; i++) {
            DayFigure dayFigure = new DayFigure();
            eventHandler.attach(dayFigure);
            datePanel.add(dayFigure);
        }
    }

    private void createBottomPanel(IFigure parent) {
        boolean hasCancel = (style & SWT.CANCEL) != 0;
        IFigure panel = new Layer();
        GridData panelConstraint = new GridData(SWT.FILL, SWT.FILL, true,
                false);
        parent.add(panel, panelConstraint);
        GridLayout panelLayout = new GridLayout(hasCancel ? 2 : 1, false);
        panelLayout.horizontalSpacing = 0;
        panelLayout.verticalSpacing = 0;
        panelLayout.marginHeight = 0;
        panelLayout.marginWidth = 0;
        panel.setLayoutManager(panelLayout);

        todayFigure = createTodayFigure(panel);
        if (hasCancel) {
            cancelFigure = createCancelFigure(panel);
        }
    }

    private BaseFigure createTodayFigure(IFigure parent) {
        BaseFigure figure = new BaseFigure();
        figure.setFont(FontUtils.getRelativeHeight(DEFAULT_FONT, -2));
        figure.setForegroundColor(ColorUtils.getColor(COLOR_TODAY));
        GridData constraint = new GridData(SWT.FILL, SWT.FILL, true, true);
        parent.add(figure, constraint);
        eventHandler.attach(figure);
        return figure;
    }

    private BaseFigure createCancelFigure(IFigure parent) {
        BaseFigure figure = new BaseFigure();
        figure.setText(" X "); //$NON-NLS-1$
        figure.setFont(FontUtils.getRelativeHeight(DEFAULT_FONT, -2));
        figure.setForegroundColor(ColorUtils.getColor(COLOR_CANCEL));
        GridData constraint = new GridData(SWT.FILL, SWT.FILL, false, true);
        parent.add(figure, constraint);
        eventHandler.attach(figure);
        return figure;
    }

    private Calendar getSelectedDate() {
        return selection;
    }

    private void changeCalendar(int newYear, int newMonth) {
        changeCalendar(newYear, newMonth, false);
    }

    private void changeCalendar(int newYear, int newMonth, boolean smooth) {
        boolean calendarChanged = newMonth != currentMonth
                || newYear != currentYear;
        if (!calendarChanged)
            return;
        if (smooth) {
            if (datePicker != null) {
                int months = (newYear - currentYear) * 12 + newMonth
                        - currentMonth;
                animationAdvisor.addYearsToRoll(months / 12);
                animationAdvisor.addMonthsToRoll(months % 12);
                performRollCalendarAnimation(currentYear, currentMonth);
            }
            currentYear = newYear;
            currentMonth = newMonth;
        } else {
            currentYear = newYear;
            currentMonth = newMonth;
            if (datePicker != null) {
                updateCalendar();
            }
            updateSelection();
        }
    }

    private void updateCalendar() {
        today = Calendar.getInstance();
        updateDayFigures(datePanel.getChildren(), currentYear, currentMonth);
        monthFigure.setMonth(currentMonth);
        yearFigure.setYear(currentYear);
        todayFigure.setText(Messages.TodayPattern);
//        todayFigure.setText(NLS.bind(Messages.TodayPattern,
//                String.format("%1$tb %1$te, %1$tY", today))); //$NON-NLS-1$
    }

    private void updateDayFigures(List dayFigures, int year, int month) {
        Calendar date = getCalendarStart(today, year, month);
        for (int i = 0; i < dayFigures.size(); i++) {
            DayFigure dayFigure = (DayFigure) dayFigures.get(i);
            date = (Calendar) date.clone();
            if (i > 0)
                date.add(DATE, 1);
            dayFigure.setDate(date);
            updateDayFigure(dayFigure, year, month);
        }
    }

    private void updateDayFigure(DayFigure figure, int year, int month) {
        figure.setFont(FontUtils.getBold(DEFAULT_FONT));
        Calendar date = figure.getDate();
        if (isSameDay(date, today)) {
            figure.setForegroundColor(ColorUtils.getColor(COLOR_TODAY));
        } else if (isWeekend(date)) {
            figure.setForegroundColor(ColorUtils.getColor(COLOR_WEEKEND));
        } else {
            figure.setForegroundColor(ColorUtils.getColor(COLOR_TEXT));
        }
        if (date.get(MONTH) == month && date.get(YEAR) == year) {
            figure.setTextAlpha(NORMAL_ALPHA);
        } else {
            figure.setTextAlpha(SIBLING_MONTH_ALPHA);
        }
    }

    private void updateSelection() {
        if (datePicker != null) {
            for (Object figure : datePanel.getChildren()) {
                DayFigure dayFigure = (DayFigure) figure;
                dayFigure.setSelected(
                        isSameDay(dayFigure.getDate(), getSelectedDate()));
            }
        }
        if (placeholder != null && selection != null) {
            String day = String.format("%1$td", selection); //$NON-NLS-1$
            String month = String.format("%1$tm", selection); //$NON-NLS-1$
            String year = String.format("%1$tY", selection); //$NON-NLS-1$

            dayText.setText(day);
            monthText.setText(month);
            yearText.setText(year);
        }

        placeholder.layout();
    }

    private void update() {
        if (datePicker != null) {
            updateCalendar();
        }
        updateSelection();
    }

    private void performRollCalendarAnimation(int oldYear, int oldMonth) {
        if (animating)
            return;

        animating = true;

        final int newYear, newMonth;
        final CalendarAnimation animation;
        if (animationAdvisor.getYearsToRoll() != 0) {
            if (animationAdvisor.getYearsToRoll() < 0) {
                newYear = oldYear - 1;
                newMonth = oldMonth;
                animation = new LastYearAnimation(animationAdvisor);
            } else {
                newYear = oldYear + 1;
                newMonth = oldMonth;
                animation = new NextYearAnimation(animationAdvisor);
            }
        } else if (animationAdvisor.getMonthsToRoll() != 0) {
            if (animationAdvisor.getMonthsToRoll() < 0) {
                newYear = oldMonth <= 0 ? oldYear - 1 : oldYear;
                newMonth = oldMonth <= 0 ? 11 : oldMonth - 1;
                animation = new LastMonthAnimation(animationAdvisor);
            } else {
                newYear = oldMonth >= 11 ? oldYear + 1 : oldYear;
                newMonth = oldMonth >= 11 ? 0 : oldMonth + 1;
                animation = new NextMonthAnimation(animationAdvisor);
            }
        } else {
            newYear = oldYear;
            newMonth = oldMonth;
            animation = null;
        }
        monthFigure.setMonth(newMonth);
        yearFigure.setYear(newYear);
        if (animation != null) {
            animationAdvisor.setStartMonth(oldYear, oldMonth);
            animationAdvisor.setEndMonth(newYear, newMonth);
            animation.callback(new Runnable() {
                public void run() {
                    datePanel = animation.getNewPanel();
                    if (animationAdvisor.isDone()) {
                        animating = false;
                    } else {
                        Display.getCurrent().asyncExec(new Runnable() {
                            public void run() {
                                animating = false;
                                performRollCalendarAnimation(newYear, newMonth);
                            }
                        });
                    }

                }
            }).start();
        }
    }

    private void selected(final BaseFigure target) {
        if (target instanceof MonthFigure) {
            target.setSelected(true);
            showMonthPopup();
        } else if (target instanceof YearFigure) {
            target.setSelected(true);
            showYearPopup();
        } else if (target == todayFigure) {
            todaySelected();
        } else if (target == lastMonth) {
            lastMonthSelected(true);
        } else if (target == nextMonth) {
            nextMonthSelected(true);
        } else if (target == lastYear) {
            lastYearSelected(true);
        } else if (target == nextYear) {
            nextYearSelected(true);
        } else if (target == cancelFigure) {
            cancelSelected();
        }
    }

    private void showMonthPopup() {
        createMonthMenu();
        for (int month = 0; month < monthActions.length; month++) {
            MonthAction action = monthActions[month];
            action.setChecked(month == currentMonth);
        }
        Rectangle b = monthFigure.getBounds();
        Point loc = control.toDisplay(b.x, b.y + b.height);
        final Menu menu = monthMenu.createContextMenu(control);
        menu.setLocation(loc.x + 10, loc.y + 1);
        menu.setVisible(true);
    }

    private void createMonthMenu() {
        if (monthMenu != null)
            return;
        monthMenu = new MenuManager();
        monthMenu.addMenuListener(new IMenuListener2() {
            public void menuAboutToShow(IMenuManager manager) {
                // do nothing
            }

            public void menuAboutToHide(IMenuManager manager) {
                monthFigure.setSelected(false);
            }
        });
        monthActions = new MonthAction[12];
        for (int month = 0; month < 12; month++) {
            MonthAction action = new MonthAction(month);
            monthMenu.add(action);
            monthActions[month] = action;
        }
    }

    private void showYearPopup() {
        createYearMenu();
        int start = currentYear - PASSED_YEARS;
        for (int year = 0; year < yearActions.length; year++) {
            YearAction action = yearActions[year];
            action.setYear(start + year);
            action.setChecked(action.getYear() == currentYear);
        }
        Rectangle b = yearFigure.getBounds();
        Point loc = control.toDisplay(b.x, b.y + b.height);
        Menu menu = yearMenu.createContextMenu(control);
        menu.setLocation(loc.x, loc.y + 1);
        menu.setVisible(true);
    }

    private void createYearMenu() {
        if (yearMenu != null)
            return;
        yearMenu = new MenuManager();
        yearMenu.addMenuListener(new IMenuListener2() {
            public void menuAboutToShow(IMenuManager manager) {
                // do nothing
            }

            public void menuAboutToHide(IMenuManager manager) {
                yearFigure.setSelected(false);
            }
        });
        yearActions = new YearAction[FUTURE_YEARS + PASSED_YEARS + 1];
        for (int year = 0; year < yearActions.length; year++) {
            YearAction action = new YearAction();
            yearMenu.add(action);
            yearActions[year] = action;
        }
    }

    private void monthSelected(int month) {
        changeCalendar(currentYear, month);
    }

    private void yearSelected(int year) {
        changeCalendar(year, currentMonth);
    }

    private void daySelected(DayFigure day) {
        Calendar date = day.getDate();
        changeDate(date);
        if (date != null && date.get(MONTH) != currentMonth) {
            changeCalendar(date.get(YEAR), date.get(MONTH), true);
        }
    }

    private void todaySelected() {
        changeDate(today);
        changeCalendar(today.get(YEAR), today.get(MONTH));
    }

    private void lastMonthSelected(boolean smooth) {
        if (currentMonth <= 0) {
            changeCalendar(currentYear - 1, 11, smooth);
        } else {
            changeCalendar(currentYear, currentMonth - 1, smooth);
        }
    }

    private void nextMonthSelected(boolean smooth) {
        if (currentMonth >= 11) {
            changeCalendar(currentYear + 1, 0, smooth);
        } else {
            changeCalendar(currentYear, currentMonth + 1, smooth);
        }
    }

    private void lastYearSelected(boolean smooth) {
        changeCalendar(currentYear - 1, currentMonth, smooth);
    }

    private void nextYearSelected(boolean smooth) {
        changeCalendar(currentYear + 1, currentMonth, smooth);
    }

    private void cancelSelected() {
        changeDate(null);
    }

    private void rollMonth(int count) {
        Calendar temp = (Calendar) today.clone();
        temp.set(YEAR, currentYear);
        temp.set(MONTH, currentMonth);
        temp.add(MONTH, count);
        changeCalendar(temp.get(YEAR), temp.get(MONTH), true);
    }

    private void changeDate(Calendar date) {
        if (date != null) {
            date.set(Calendar.MILLISECOND, 0);
        }
        this.selection = date;
        updateSelection();

        if (firingSelectionChange)
            return;
        firingSelectionChange = true;
        if (dropdownDatePicker != null) {
            control.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    dropdownDatePicker.close();
                }
            });
        }
        fireSelectionChanged(
                new SelectionChangedEvent(DatePicker2.this, getSelection()));
        firingSelectionChange = false;
    }

    @Override
    public Object getInput() {
        return today;
    }

    @Override
    public void refresh() {
        update();
    }

    @Override
    public void setInput(Object input) {
        if (input instanceof Calendar) {
            Calendar oldInput = this.today;
            this.today = (Calendar) input;
            inputChanged(input, oldInput);
        }
    }

    @Override
    protected void inputChanged(Object input, Object oldInput) {
        update();
    }

    public void setMinTime(Calendar minTime) {
        if (minTime != null) {
            minTime.set(Calendar.MILLISECOND, 0);
            this.minTime = minTime;
        }
    }

    public Calendar getMinTime() {
        return minTime;
    }

    public void setMaxTime(Calendar maxTime) {
        if (maxTime != null) {
            maxTime.set(Calendar.MILLISECOND, 0);
            this.maxTime = maxTime;
        }
    }

    public Calendar getMaxTime() {
        return maxTime;
    }

    private static boolean isWeekend(Calendar date) {
        int dow = date.get(DAY_OF_WEEK);
        return dow == SUNDAY || dow == SATURDAY;
    }

    private static Calendar getCalendarStart(Calendar date, int year,
            int month) {
        date = (Calendar) date.clone();
        date.set(year, month, 1);
        while (date.get(DAY_OF_WEEK) != SUNDAY) {
            date.add(DATE, -1);
        }
        return date;
    }

    private static boolean isSameDay(Calendar date1, Calendar date2) {
        if (date1 == null)
            return date2 == null;
        if (date2 == null)
            return false;
        return date1.get(DATE) == date2.get(DATE)
                && date1.get(MONTH) == date2.get(MONTH)
                && date1.get(YEAR) == date2.get(YEAR);
    }

}
