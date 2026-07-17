package com.nextmove.ui;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class SidebarUi
{
	private static final int TEXT_WIDTH = 188;

	private SidebarUi()
	{
	}

	static JLabel wrapped(String text)
	{
		JLabel label = new JLabel(
			"<html><body style='width: " + TEXT_WIDTH + "px'>"
				+ escape(text)
				+ "</body></html>");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	static JPanel buttonStack(JButton... buttons)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setOpaque(false);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		for (int index = 0; index < buttons.length; index += 1)
		{
			JButton button = buttons[index];
			button.setAlignmentX(Component.LEFT_ALIGNMENT);
			button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
			panel.add(button);
			if (index < buttons.length - 1)
			{
				panel.add(Box.createRigidArea(new Dimension(0, 4)));
			}
		}
		return panel;
	}

	private static String escape(String text)
	{
		String safe = text == null ? "" : text;
		return safe
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
			.replace("'", "&#39;");
	}
}
