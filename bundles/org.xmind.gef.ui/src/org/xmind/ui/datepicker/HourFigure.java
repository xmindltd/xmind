package org.xmind.ui.datepicker;

public class HourFigure extends BaseFigure {

    private static final String[] HOURS_12 = new String[] { "00", "01", "02", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
    private static final String[] HOURS_24 = new String[] { "00", "01", "02", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$
            "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$

    private int hour = -1;

    public HourFigure() {
        super();
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour, int type) {
        this.hour = hour;
        String hour_tx = HOURS_12[0];
        if (type == DatePicker.TIME_12) {
            if (hour > 12)
                hour = hour % 12;
            hour_tx = (hour > 0 && hour < HOURS_12.length) ? HOURS_12[hour]
                    : HOURS_12[12];
        } else if (type == DatePicker.TIME_24) {
            hour_tx = hour >= 0 && hour < HOURS_24.length ? HOURS_24[hour]
                    : HOURS_24[0];
        }

        setText(hour_tx);
        repaint();
    }
}
