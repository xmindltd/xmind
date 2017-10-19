package org.xmind.ui.datepicker;

public class MinutesFigure extends BaseFigure {

    private static final String[] MINUTES = new String[] { "00", "01", "02", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "03", "04", "05", "06", "07", "08", "09" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    private int minute = -1;

    public MinutesFigure() {
        super();
    }

    public int getMinutes() {
        return minute;
    }

    public void setMinutes(int minute) {
        this.minute = minute;
        String hour_tx = MINUTES[0];
        if (minute >= 0 && minute < 60)
            hour_tx = (minute >= 0 && minute < MINUTES.length) ? MINUTES[minute]
                    : minute + ""; //$NON-NLS-1$
        setText(hour_tx);
        repaint();
    }
}
