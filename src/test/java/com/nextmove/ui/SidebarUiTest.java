package com.nextmove.ui;

import javax.swing.JLabel;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SidebarUiTest
{
	@Test
	public void normalizesUnsupportedRecommendationPunctuationAndKeepsTextInsideTheViewport()
	{
		JLabel label = SidebarUi.wrapped(
			"Sailing 15 \u2192 20. The missing capstone is Zuk\u2014not more regular Gauntlet.");

		assertTrue(label.getText().contains("width: 180px"));
		assertTrue(label.getText().contains(
			"Sailing 15 to 20. The missing capstone is Zuk-not more regular Gauntlet."));
		assertFalse(label.getText().contains("\u2192"));
		assertFalse(label.getText().contains("\u2014"));
	}
}
