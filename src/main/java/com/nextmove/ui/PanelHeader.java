package com.nextmove.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.FontManager;

final class PanelHeader extends JPanel
{
	PanelHeader(Runnable refresh, Runnable settings)
	{
		super(new BorderLayout());
		setOpaque(false);

		JLabel title = new JLabel("NEXT MOVE");
		title.setFont(FontManager.getRunescapeBoldFont());
		add(title, BorderLayout.WEST);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		actions.setOpaque(false);
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(event -> refresh.run());
		JButton settingsButton = new JButton("Settings");
		settingsButton.addActionListener(event -> settings.run());
		actions.add(refreshButton);
		actions.add(settingsButton);
		add(actions, BorderLayout.EAST);
	}
}
