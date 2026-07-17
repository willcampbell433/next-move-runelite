package com.nextmove.ui;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class StatusPanel extends JPanel
{
	StatusPanel(String text)
	{
		super(new BorderLayout());
		setOpaque(false);
		add(new JLabel(text), BorderLayout.WEST);
	}
}
