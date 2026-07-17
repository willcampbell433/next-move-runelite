package com.nextmove.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;

final class StatusPanel extends JPanel
{
	StatusPanel(String text)
	{
		super(new BorderLayout());
		setOpaque(false);
		setAlignmentX(Component.LEFT_ALIGNMENT);
		add(SidebarUi.wrapped(text), BorderLayout.WEST);
	}
}
