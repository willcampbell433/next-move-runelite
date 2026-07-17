package com.nextmove.ui;

import java.awt.Component;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StatusPanelTest
{
	@Test
	public void participatesInTheSameLeftAxisAsSidebarContent()
	{
		StatusPanel panel = new StatusPanel("WikiSync ready");

		assertEquals(Component.LEFT_ALIGNMENT, panel.getAlignmentX(), 0.0f);
	}
}
