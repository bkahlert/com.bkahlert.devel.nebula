package com.bkahlert.nebula.gallery.demoSuits.instruction.explanatory.list;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.explanation.ListExplanationComposite.ListExplanation;
import com.bkahlert.nebula.widgets.explanation.explanatory.ListExplanatoryComposite;

@Demo
public class IntroductoryTextOnlyListExplanatoryCompositeDemo extends
		AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		final ListExplanatoryComposite explanatoryComposite = new ListExplanatoryComposite(
				parent, SWT.NONE);

		Button contentControl = new Button(explanatoryComposite, SWT.NONE);
		explanatoryComposite.setContentControl(contentControl);
		contentControl.setText("Show the list explanation...");
		contentControl.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int icon = SWT.NONE;
				String text = "I tell you how to use this composite.\n"
						+ "This message closes in 5 seconds.";
				ListExplanation expl = new ListExplanation(icon, text);
				explanatoryComposite.showExplanation(expl);

				Display.getCurrent().timerExec(5000, new Runnable() {

					@Override
					public void run() {
						explanatoryComposite.hideExplanation();
					}

				});
			}

		});
	}
}
