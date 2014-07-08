package com.bkahlert.nebula.gallery.demoSuits.instruction.explanatory.normal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.explanation.ExplanationComposite;
import com.bkahlert.nebula.widgets.explanation.explanatory.ExplanatoryComposite;

@Demo
public class ExplanatoryCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		final ExplanatoryComposite explanatoryComposite = new ExplanatoryComposite(
				parent, SWT.NONE);

		/*
		 * explanation
		 */
		final ExplanationComposite explanationComposite = new ExplanationComposite(
				explanatoryComposite, SWT.NONE, SWT.ICON_INFORMATION);
		explanationComposite.setLayout(new GridLayout(1, false));

		Composite explanationContent = new Composite(explanationComposite,
				SWT.NONE);
		explanationContent.setLayout(new GridLayout(1, false));
		explanationContent.setLayoutData(new GridData(SWT.BEGINNING,
				SWT.CENTER, true, true));

		Label explanationLabel = new Label(explanationContent, SWT.NONE);
		explanationLabel.setLayoutData(new GridData(SWT.BEGINNING,
				SWT.BEGINNING, false, false));
		explanationLabel.setText("I'm the explanation.");

		Button explanationButton = new Button(explanationContent, SWT.PUSH);
		explanationButton.setLayoutData(new GridData(SWT.BEGINNING,
				SWT.BEGINNING, false, false));
		explanationButton.setText("Hide the explanation...");
		explanationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				explanatoryComposite.hideExplanation();
			}
		});

		/*
		 * demoAreaContent
		 */
		Button contentControl = new Button(explanatoryComposite, SWT.PUSH);
		explanatoryComposite.setContentControl(contentControl);
		contentControl.setText("Show the explanation...");
		contentControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				explanatoryComposite.showExplanation(explanationComposite);
			}
		});
	}
}
