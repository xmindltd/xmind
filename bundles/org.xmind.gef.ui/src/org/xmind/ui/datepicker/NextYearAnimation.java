/**
 * 
 */
package org.xmind.ui.datepicker;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;

import java.util.Calendar;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Rectangle;

class NextYearAnimation extends CalendarAnimation {

	private int distance;

	public NextYearAnimation(IAnimationAdvisor advisor) {
		super(advisor);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected IFigure createNewPanel() {
		IFigure newPanel = new Layer();
		newPanel.setLayoutManager(new DatePanelLayout());
		return newPanel;
	}

	@Override
	protected void createNewDays() {
		Calendar date = DatePicker.getCalendarStart(oldDays.get(0).getDate(), advisor
				.getNewYear(), advisor.getNewMonth());
		for (int i = 0; i < DatePicker.TOTAL_DAYS; i++) {
			if (i > 0) {
				date = (Calendar) date.clone();
				date.add(DATE, 1);
			}
			DayFigure dayFigure = new DayFigure();
			dayFigure.setDate(date);
			advisor.initNewDay(dayFigure);
			newPanel.add(dayFigure);
			if (date.get(MONTH) == advisor.getNewMonth()) {
				newDays.add(dayFigure);
			}
		}
	}

	@Override
	protected Rectangle createOldInitConstraint() {
		distance = oldSize.width + 10;
		return new Rectangle(0, 0, oldSize.width, oldSize.height);
	}

	@Override
	protected Rectangle createOldFinalConstraint() {
		return new Rectangle(-distance, 0, oldSize.width, oldSize.height);
	}

	@Override
	protected Rectangle createNewInitConstraint() {
		return new Rectangle(distance, 0, oldSize.width, oldSize.height);
	}

	@Override
	protected Rectangle createNewFinalConstraint() {
		return new Rectangle(0, 0, oldSize.width, oldSize.height);
	}

	@Override
	protected void removeOldDays() {
		advisor.getLayer().remove(advisor.getPanel());
	}

	@Override
	protected void updateJobs() {
		advisor.addYearsToRoll(-1);
	}

}