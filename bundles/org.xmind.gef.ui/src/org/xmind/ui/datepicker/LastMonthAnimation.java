/**
 * 
 */
package org.xmind.ui.datepicker;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;

import java.util.Calendar;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

class LastMonthAnimation extends CalendarAnimation {

	private int newRows;

	public LastMonthAnimation(IAnimationAdvisor advisor) {
		super(advisor);
	}

	@Override
	protected void oldDayInNewMonth(DayFigure dayFigure, Calendar date,
			int month, int index) {
		newDays.add(dayFigure);
	}

	@Override
	protected void createNewDays() {
		newRows = 0;
		Calendar date = ((DayFigure) advisor.getPanel().getChildren()
				.get(0)).getDate();
		boolean head = false;
		while (!head) {
			for (int i = 0; i < 7; i++) {
				date = (Calendar) date.clone();
				date.add(DATE, -1);
				DayFigure dayFigure = new DayFigure();
				dayFigure.setDate(date);
				advisor.initNewDay(dayFigure);
				newPanel.add(dayFigure, 0);
				if (date.get(MONTH) == advisor.getNewMonth())
					newDays.add(0, dayFigure);
				if (date.get(DATE) == 1) {
					head = true;
				}
			}
			newRows++;
		}
	}

	@Override
	protected Rectangle createOldInitConstraint() {
		int oldRows = 6;
		Rectangle constraint = new Rectangle();
		constraint.width = oldSize.width;
		constraint.height = oldSize.height * (oldRows + newRows) / oldRows;
		constraint.y = oldSize.height - constraint.height;
		return constraint;
	}

	@Override
	protected Rectangle createOldFinalConstraint() {
		return new Rectangle(0, 0, oldInitConstraint.width,
				oldInitConstraint.height);
	}

	@Override
	protected void removeOldDays() {
		IFigure oldPanel = advisor.getPanel();
		while (oldPanel.getChildren().size() > DatePicker.TOTAL_DAYS) {
			oldPanel.remove((IFigure) oldPanel.getChildren().get(
					oldPanel.getChildren().size() - 1));
		}
	}

	@Override
	protected void updateJobs() {
		advisor.addMonthsToRoll(1);
	}
}