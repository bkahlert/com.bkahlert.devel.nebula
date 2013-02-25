package com.bkahlert.devel.nebula.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.widgets.RoundedComposite;
import com.bkahlert.devel.nebula.widgets.editor.Editor;

public class WidgetsView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.nebula.ui.views.WidgetsView";
	private int redrawInterval = 0;

	public WidgetsView() {
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(GridLayoutFactory.swtDefaults().create());

		new Thread(new Runnable() {
			@Override
			public void run() {
				Job x = new Job("Parallel Job Execution") {
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						final SubMonitor subMonitor = SubMonitor
								.convert(monitor);
						subMonitor.beginTask("Begin Task", 100);
						subMonitor.setWorkRemaining(100);
						Thread a = new Thread(new Runnable() {
							@Override
							public void run() {
								for (int i = 0; i < 50; i++) {
									try {
										Thread.sleep(100);
										System.err.println("a - " + i);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									subMonitor.worked(1);
								}
							}
						});
						Thread b = new Thread(new Runnable() {
							@Override
							public void run() {
								for (int i = 0; i < 50; i++) {
									try {
										Thread.sleep(150);
										System.err.println("b - " + i);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									subMonitor.worked(1);
								}
							}
						});
						a.start();
						b.start();
						try {
							a.join();
							System.err.println("a finished");
							b.join();
							System.err.println("b finished");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						subMonitor.done();
						return Status.OK_STATUS;
					}
				};
				x.schedule();
				try {
					x.join();
					System.err.println("x joined");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				Job y = new Job("Parallel Job Execution with Sub Monitors") {
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						final SubMonitor subMonitor = SubMonitor
								.convert(monitor);
						subMonitor.beginTask("Begin Task", 2);
						Thread a = new Thread(new Runnable() {
							@Override
							public void run() {
								SubMonitor aMonitor = subMonitor.newChild(1);
								for (int i = 0; i < 50; i++) {
									try {
										Thread.sleep(100);
										System.err.println("sub a - " + i);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									aMonitor.worked(1);
								}
								aMonitor.done();
							}
						});
						Thread b = new Thread(new Runnable() {
							@Override
							public void run() {
								SubMonitor bMonitor = subMonitor.newChild(1);
								for (int i = 0; i < 50; i++) {
									try {
										Thread.sleep(150);
										System.err.println("sub b - " + i);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									bMonitor.worked(1);
								}
								bMonitor.done();
							}
						});
						a.start();
						b.start();
						try {
							a.join();
							System.err.println("sub a finished");
							b.join();
							System.err.println("sub b finished");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						subMonitor.done();
						return Status.OK_STATUS;
					}
				};
				y.schedule();
				try {
					y.join();
					System.err.println("y joined");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}); // .start();

		Composite editorControls = new RoundedComposite(parent, SWT.BORDER);
		editorControls.setLayout(new RowLayout());
		Button editorGetSource = new Button(editorControls, SWT.PUSH);
		editorGetSource.setText("Get Source");
		Button editorSetSource = new Button(editorControls, SWT.PUSH);
		editorSetSource.setText("Set Source");
		Button editorShowSource = new Button(editorControls, SWT.PUSH);
		editorShowSource.setText("Show Source");
		Button editorHideSource = new Button(editorControls, SWT.PUSH);
		editorHideSource.setText("Hide Source");
		Button editorSelectAll = new Button(editorControls, SWT.PUSH);
		editorSelectAll.setText("Select All");
		Button editorEnable = new Button(editorControls, SWT.PUSH);
		editorEnable.setText("Enable");
		Button editorDisable = new Button(editorControls, SWT.PUSH);
		editorDisable.setText("Disable");
		Button editorLockSelection = new Button(editorControls, SWT.PUSH);
		editorLockSelection.setText("Save Selection");
		Button editorUnlockSelection = new Button(editorControls, SWT.PUSH);
		editorUnlockSelection.setText("Restore Selection");

		final Editor editor = new Editor(parent, SWT.BORDER, 2000);
		editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		editor.setSource("Hello");
		editor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				System.err.println("changed: " + e.data);
			}
		});

		editorGetSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(editor.getSource());
			}
		});
		editorSetSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.setSource("<p title='test'><b>Hallo</b><i>Welt!</i></p>");
			}
		});
		editorShowSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.showSource();
			}
		});
		editorHideSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.hideSource();
			}
		});
		editorSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.selectAll();
			}
		});
		editorEnable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.setEnabled(true);
			}
		});
		editorDisable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.setEnabled(false);
			}
		});
		editorLockSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.saveSelection();
			}
		});
		editorUnlockSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.restoreSelection();
			}
		});

		/*
		 * Button openFileListDialog = new Button(parent, SWT.PUSH);
		 * openFileListDialog.setText("Open File List Dialog");
		 * openFileListDialog.addSelectionListener(new SelectionAdapter() {
		 * 
		 * @Override public void widgetSelected(SelectionEvent e) {
		 * DirectoryListDialog dialog = new DirectoryListDialog( new Shell(),
		 * Arrays.asList(new File("abc"), new File( "/Users/bkahlert/etc.")));
		 * dialog.create(); dialog.setTitle("Data Directories");
		 * dialog.setText("Add or remove data directories."); if (dialog.open()
		 * == Window.OK) { List<File> directories = dialog.getDirectories();
		 * System.out.println(directories.size()); } } });
		 * 
		 * RoundedLabels roundedLabels = new RoundedLabels(parent, SWT.BORDER,
		 * new RGB(200, 200, 200)); roundedLabels.setTexts(new String[] { "abc",
		 * "def", "kjkdjklsdjdsdslkjlkjlk" });
		 * 
		 * RoundedLabels roundedLabels2 = new RoundedLabels(parent, SWT.NONE,
		 * new RGB(200, 200, 200)); roundedLabels2.setTexts(new String[] {
		 * "abc", "def", "kjkdjklsdjdsdslkjlkjlk" });
		 * 
		 * Composite wrapper = new Composite(parent, SWT.NONE);
		 * wrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		 * wrapper.setLayout(GridLayoutFactory.swtDefaults().margins(1, 1)
		 * .spacing(2, 0).numColumns(2).create()); Label label = new
		 * Label(wrapper, SWT.NONE);
		 * label.setLayoutData(GridDataFactory.swtDefaults()
		 * .align(SWT.BEGINNING, SWT.BEGINNING).indent(0, 4).create());
		 * label.setText("Filters:"); RoundedLabels roundedLabels3 = new
		 * RoundedLabels(wrapper, SWT.NONE, new RGB(200, 200, 200));
		 * roundedLabels3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		 * true)); roundedLabels3.setTexts(new String[] { "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
		 * "kjkdjklsdjdsdslkjlkjlk", "kjkljlj" });
		 * 
		 * ITimeline timeline = new Timeline(parent, SWT.BORDER); ((Control)
		 * timeline).setLayoutData(GridDataFactory.fillDefaults() .grab(true,
		 * false).create()); ITimelineInput input = null; timeline.show(input,
		 * 500, 500, null);
		 * 
		 * List<RGB> colors = Arrays.asList(new RGB(0, 0, 0), new RGB(0, 0,
		 * 255), new RGB(0, 255, 0), new RGB(0, 255, 255), new RGB(255, 0, 0),
		 * new RGB(255, 0, 255), new RGB(255, 255, 0), new RGB(255, 255, 255),
		 * new RGB(127, 127, 127), new RGB(127, 127, 255), new RGB(127, 255,
		 * 127), new RGB(127, 255, 255), new RGB(255, 127, 127), new RGB(255,
		 * 127, 255), new RGB(255, 255, 127), new RGB(232, 232, 232)); Composite
		 * colorComposite = new Composite(parent, SWT.BORDER);
		 * colorComposite.setLayoutData
		 * (GridDataFactory.fillDefaults().create());
		 * colorComposite.setLayout(new GridLayout(colors.size(), true)); for
		 * (RGB rgb : colors) { com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.6f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.5f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.4f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.3f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.2f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.1f).toClassicRGB()); } for (RGB rgb : colors) { new
		 * ColorPicker(colorComposite, rgb); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 1.0f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.9f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.8f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.7f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.6f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.5f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.4f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.3f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.2f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.1f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
		 * 0.0f).toClassicRGB()); }
		 * 
		 * for (RGB rgb : colors) { com.bkahlert.devel.nebula.colors.RGB rgbx =
		 * new com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 1.0f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.9f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.8f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.7f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.6f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.5f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.4f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.3f).toClassicRGB()); } for (RGB rgb : colors) {
		 * com.bkahlert.devel.nebula.colors.RGB rgbx = new
		 * com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.2f).toClassicRGB()); }
		 * 
		 * for (RGB rgb : colors) { com.bkahlert.devel.nebula.colors.RGB rgbx =
		 * new com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.1f).toClassicRGB()); }
		 * 
		 * for (RGB rgb : colors) { com.bkahlert.devel.nebula.colors.RGB rgbx =
		 * new com.bkahlert.devel.nebula.colors.RGB( rgb); new
		 * ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
		 * 0.0f).toClassicRGB()); }
		 * 
		 * if (redrawInterval > 0) { new Thread(new Runnable() {
		 * 
		 * @Override public void run() { try { Thread.sleep(redrawInterval); }
		 * catch (InterruptedException e) {
		 * 
		 * } Display.getDefault().syncExec(new Runnable() {
		 * 
		 * @Override public void run() { for (Control c : parent.getChildren())
		 * { if (c != null && !parent.isDisposed()) c.dispose(); }
		 * createPartControl(parent); parent.layout(); } }); } }).start(); } /*
		 */
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
