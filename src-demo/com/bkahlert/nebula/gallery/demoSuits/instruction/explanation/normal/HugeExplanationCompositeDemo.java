package com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.normal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


import com.bkahlert.devel.nebula.widgets.explanation.ExplanationComposite;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class HugeExplanationCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		ExplanationComposite expl = new ExplanationComposite(parent, SWT.NONE,
				SWT.ICON_INFORMATION);
		RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.wrap = true;
		expl.setLayout(rowLayout);

		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE)
				.setText("I'm a very long long long long long long long long long long long long label that does not wrap.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.WRAP)
				.setText("I'm a very long long long long long long long long long long long long label that does wrap.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE)
				.setText("I'm a very long long long long long long long long long long long long label that does not wrap.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.WRAP)
				.setText("I'm a very long long long long long long long long long long long long label that does wrap.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE)
				.setText("I'm a very long long long long long long long long long long long long label that does not wrap.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.WRAP)
				.setText("I'm a very long long long long long long long long long long long long label that does wrap.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
		new Button(expl, SWT.PUSH).setText("I'm a button.");
		new Label(expl, SWT.NONE).setText("I'm a label.");
	}
}
