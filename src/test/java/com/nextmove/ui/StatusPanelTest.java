package com.nextmove.ui;

import java.awt.Component;
import javax.swing.JLabel;
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

	@Test
	public void wrapsLongStatusMessagesAtSidebarWidth()
	{
		StatusPanel panel = new StatusPanel("That player was not found on the public Hiscores.");
		JLabel label = (JLabel) panel.getComponent(0);

		org.junit.Assert.assertTrue(label.getText().startsWith("<html>"));
		org.junit.Assert.assertTrue(label.getText().contains("width: 188px"));
	}
}
