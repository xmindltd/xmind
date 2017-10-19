package org.xmind.ui.datepicker;

import java.util.Calendar;

import org.eclipse.jface.viewers.LabelProvider;

public class DateLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        if (element == null)
            return Messages.None;
        if (element instanceof Long) {
            element = Calendar.getInstance();
            ((Calendar) element).setTimeInMillis(((Long) element).longValue());
        }
        if (element instanceof Calendar) {
            return getDateText((Calendar) element);
        }
        return Messages.Illegal;
    }

    protected String getDateText(Calendar date) {
//        return String.format("%1$tF %1$tT ", date); //$NON-NLS-1$   %1$tb %1$te, %1$tY"
        return String.format("%1$tb %1$te, %1$tY", date); //$NON-NLS-1$
    }
}
