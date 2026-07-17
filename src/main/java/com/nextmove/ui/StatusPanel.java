package com.nextmove.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class StatusPanel extends JPanel
{
	StatusPanel(String text)
	{
		super(new BorderLayout());
		setOpaque(false);
		setAlignmentX(Component.LEFT_ALIGNMENT);
		add(new JLabel(text), BorderLayout.WEST);
	}
}
