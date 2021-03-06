package com.bkahlert.nebula.gallery.demoSuits.viewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.viewer.ProjectDisplayComposite;

@Demo
public class ProjectDisplayCompositeDemo extends AbstractDemo {
	protected ProjectDisplayComposite projectDisplayComposite;

	@Override
	public void createDemo(Composite parent) {
		parent.setLayout(new FillLayout());

		this.projectDisplayComposite = new ProjectDisplayComposite(parent,
				SWT.BORDER);
	}
}
