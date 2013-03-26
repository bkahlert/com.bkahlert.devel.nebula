package com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.list;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


import com.bkahlert.devel.nebula.widgets.explanation.ListExplanationComposite;
import com.bkahlert.devel.nebula.widgets.explanation.ListExplanationComposite.ListExplanation;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class ListExplanationCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		ListExplanationComposite listExplanationComposite = new ListExplanationComposite(
				parent, SWT.NONE);
		ListExplanation listItemExplanation = new ListExplanation(
				SWT.ICON_INFORMATION, "I'm the introductory text...",
				"List item 1", "List item 2", "List item 3", "List item 4",
				"List item 5", "List item 6", "List item 7", "List item 8",
				"List item 9", "List item 10", "List item 11", "List item 12",
				"List item 13", "List item 14", "List item 15");
		listExplanationComposite.setExplanation(listItemExplanation);
	}
}
