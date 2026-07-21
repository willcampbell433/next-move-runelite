package com.nextmove;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.runelite.client.callback.ClientThread;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class NextMovePluginThreadingTest
{
	@Test
	public void enablingWhileLoggedInDefersInitialQuestCaptureToClientThread()
		throws Exception
	{
		NextMovePlugin plugin = new NextMovePlugin();
		RecordingClientThread clientThread = new RecordingClientThread();
		Field clientThreadField = NextMovePlugin.class.getDeclaredField("clientThread");
		clientThreadField.setAccessible(true);
		clientThreadField.set(plugin, clientThread);

		Method schedule;
		try
		{
			schedule = NextMovePlugin.class.getDeclaredMethod(
				"scheduleInitialLocalPlayerLoad");
		}
		catch (NoSuchMethodException exception)
		{
			fail("Logged-in startup must schedule quest capture on ClientThread");
			return;
		}
		schedule.setAccessible(true);
		schedule.invoke(plugin);

		assertNotNull("Quest capture was not deferred", clientThread.scheduled);
	}

	private static final class RecordingClientThread extends ClientThread
	{
		private Runnable scheduled;

		@Override
		public void invokeLater(Runnable runnable)
		{
			scheduled = runnable;
		}
	}
}
