package com.nextmove.ui;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ScoreBar extends JPanel
{
	public ScoreBar(String label, Integer score, int maximum)
	{
		super(new BorderLayout(0, 3));
		setOpaque(false);
		if (score == null)
		{
			add(new JLabel(label + " — Unavailable"), BorderLayout.NORTH);
			return;
		}

		add(new JLabel(label + " " + score + " / " + maximum), BorderLayout.NORTH);
		JProgressBar progress = new JProgressBar(0, maximum);
		progress.setValue(score);
		progress.setStringPainted(false);
		progress.getAccessibleContext().setAccessibleName(label + " score");
		progress.getAccessibleContext().setAccessibleDescription(
			score + " out of " + maximum);
		add(progress, BorderLayout.CENTER);
	}
}
