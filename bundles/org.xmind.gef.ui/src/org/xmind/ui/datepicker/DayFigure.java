/**
 * 
 */
package org.xmind.ui.datepicker;

import java.util.Calendar;

public class DayFigure extends BaseFigure {

    private Calendar date = null;

    public DayFigure() {
        super();
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
        setText("" + date.get(Calendar.DATE)); //$NON-NLS-1$
        repaint();
    }

}