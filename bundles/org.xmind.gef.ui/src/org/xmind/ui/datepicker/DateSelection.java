package org.xmind.ui.datepicker;

import java.util.Calendar;

import org.eclipse.jface.viewers.ISelection;

public class DateSelection implements ISelection {

    private Calendar date;

    public DateSelection(Calendar date) {
        this.date = date;
    }

    public boolean isEmpty() {
        return date != null;
    }

    public Calendar getDate() {
        return date;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof DateSelection))
            return false;
        DateSelection that = (DateSelection) obj;
        return this.date == that.date
                || (this.date != null && this.date.equals(that.date));
    }
}
