/**
 * 
 */
package org.xmind.ui.datepicker;

public class MonthFigure extends BaseFigure {

	private int month = -1;

	public MonthFigure() {
		super();
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
		setText(month >= 0 && month < DatePicker.MONTHS.length ? DatePicker.MONTHS[month]
				: null);
		repaint();
	}

}