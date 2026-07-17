package com.nextmove.ui;

import javax.swing.BoxLayout;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PanelHeaderTest
{
	@Test
	public void stacksTheTitleAboveActionsAtSidebarWidth()
	{
		PanelHeader header = new PanelHeader(() -> { }, () -> { });

		assertTrue(header.getLayout() instanceof BoxLayout);
	}
}
