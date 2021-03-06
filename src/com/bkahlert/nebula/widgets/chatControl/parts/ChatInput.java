package com.bkahlert.nebula.widgets.chatControl.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * This control displays an input field for text input
 * 
 * @author bkahlert
 */
public class ChatInput extends Composite {
	/**
	 * When no message is entered, display this hint
	 */
	public static final String HINT_TEXT = "Enter message here...";

	/**
	 * When no message is entered, use this color to display the hint
	 */
	public static final Color HINT_COLOR = Display.getDefault().getSystemColor(
			SWT.COLOR_TITLE_INACTIVE_FOREGROUND);

	/**
	 * Color no of entered message
	 */
	public static final Color TEXT_COLOR = Display.getDefault().getSystemColor(
			SWT.COLOR_WIDGET_FOREGROUND);

	protected StyledText text;

	public ChatInput(Composite parent, int style) {
		super(parent, SWT.NONE);
		this.setLayout(new FillLayout());

		this.text = new StyledText(this, style);

		/*
		 * Show/hide the hint appropriately
		 */
		this.text.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				String message = ChatInput.this.text.getText();
				if (message.equals("")) { //$NON-NLS-1$
					ChatInput.this.showHint();
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				String message = ChatInput.this.text.getText();
				ChatInput.this.hideHint();

				/*
				 * Hide clears the text; we need to recover the text
				 */
				if (!message.equals(HINT_TEXT)) {
					ChatInput.this.setText(message);
				}
			}
		});

		this.showHint();
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		this.text.setBackground(color);
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		this.text.addKeyListener(listener);
	}

	protected void showHint() {
		this.text.setForeground(HINT_COLOR);
		this.text.setText(HINT_TEXT);
	}

	protected void hideHint() {
		this.text.setForeground(TEXT_COLOR);
		this.text.setText(""); //$NON-NLS-1$
	}

	/**
	 * Sets the widgets text
	 * 
	 * @param string
	 *            the new text
	 */
	public void setText(String string) {
		this.text.setText(string);
	}

	/**
	 * Return entered text in the widget
	 * 
	 * @return the entered text
	 */
	public String getText() {
		return this.text.getText();
	}

	@Override
	public boolean setFocus() {
		return this.text.setFocus();
	}

}
