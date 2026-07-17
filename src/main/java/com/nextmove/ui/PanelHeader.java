package com.nextmove.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.FontManager;

final class PanelHeader extends JPanel
{
	PanelHeader(Runnable refresh, Runnable settings)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);

		JLabel title = new JLabel("NEXT MOVE");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(title);
		add(Box.createRigidArea(new Dimension(0, 6)));

		JPanel actions = new JPanel(new GridLayout(1, 2, 6, 0));
		actions.setOpaque(false);
		actions.setAlignmentX(Component.LEFT_ALIGNMENT);
		actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(event -> refresh.run());
		JButton settingsButton = new JButton("Settings");
		settingsButton.addActionListener(event -> settings.run());
		actions.add(refreshButton);
		actions.add(settingsButton);
		add(actions);
	}
}
