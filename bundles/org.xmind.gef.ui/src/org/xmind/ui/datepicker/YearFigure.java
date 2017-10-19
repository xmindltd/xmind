/**
 * 
 */
package org.xmind.ui.datepicker;

public class YearFigure extends BaseFigure {

    private int year = -1;

    public YearFigure() {
        super();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
        setText(year < 0 ? null : "" + year); //$NON-NLS-1$
        repaint();
    }

}