/**
 * 
 */
package org.xmind.ui.datepicker;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;

import java.util.Calendar;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

class NextMonthAnimation extends CalendarAnimation {

	private int newRows = 0;

	public NextMonthAnimation(IAnimationAdvisor advisor) {
		super(advisor);
	}

	@Override
	protected void oldDayInNewMonth(DayFigure dayFigure, Calendar date,
			int month, int index) {
		newDays.add(dayFigure);
		if (date.get(DATE) == 1) {
			newRows = index / 7;
		}
	}

	@Override
	protected void createNewDays() {
		List oldDayFigures = advisor.getPanel().getChildren();
		Calendar date = ((DayFigure) oldDayFigures
				.get(oldDayFigures.size() - 1)).getDate();
		for (int r = 0; r < newRows; r++) {
			for (int i = 0; i < 7; i++) {
				date = (Calendar) date.clone();
				date.add(DATE, 1);
				DayFigure dayFigure = new DayFigure();
				dayFigure.setDate(date);
				advisor.initNewDay(dayFigure);
				newPanel.add(dayFigure);
				if (date.get(MONTH) == advisor.getNewMonth()) {
					newDays.add(dayFigure);
				}
			}
		}
	}

	@Override
	protected Rectangle createOldInitConstraint() {
		int oldRows = 6;
		Rectangle constraint = new Rectangle();
		constraint.width = oldSize.width;
		constraint.height = oldSize.height * (oldRows + newRows) / oldRows;
		return constraint;
	}

	@Override
	protected Rectangle createOldFinalConstraint() {
		return new Rectangle(0, oldSize.height - oldInitConstraint.height,
				oldInitConstraint.width, oldInitConstraint.height);
	}

	@Override
	protected void removeOldDays() {
		IFigure oldPanel = advisor.getPanel();
		while (oldPanel.getChildren().size() > DatePicker.TOTAL_DAYS) {
			oldPanel.remove((IFigure) oldPanel.getChildren().get(0));
		}
	}

	@Override
	protected void updateJobs() {
		advisor.addMonthsToRoll(-1);
	}

}