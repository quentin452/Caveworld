package caveworld.util;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.Locale;

public class CaveLog
{
	public static final CaveLog log = new CaveLog();

	private Logger myLog;

	private static boolean configured;

	private static void configureLogging()
	{
		log.myLog = LogManager.getLogger("Caveworld");
		ThreadContext.put("side", FMLLaunchHandler.side().name().toLowerCase(Locale.ENGLISH));

		configured = true;
	}

	public static void log(String targetLog, Level level, String format, Object... data)
	{
		LogManager.getLogger(targetLog).log(level, String.format(format, data));
	}

	public static void log(Level level, String format, Object... data)
	{
		if (!configured)
		{
			configureLogging();
		}

		log.myLog.log(level, String.format(format, data));
	}

	public static void log(String targetLog, Level level, Throwable ex, String format, Object... data)
	{
		LogManager.getLogger(targetLog).log(level, String.format(format, data), ex);
	}

	public static void log(Level level, Throwable ex, String format, Object... data)
	{
		if (!configured)
		{
			configureLogging();
		}

		log.myLog.log(level, String.format(format, data), ex);
	}

	public static void severe(String format, Object... data)
	{
		log(Level.ERROR, format, data);
	}

	public static void warning(String format, Object... data)
	{
		log(Level.WARN, format, data);
	}

	public static void info(String format, Object... data)
	{
		log(Level.INFO, format, data);
	}

	public static void fine(String format, Object... data)
	{
		log(Level.DEBUG, format, data);
	}

	public static void finer(String format, Object... data)
	{
		log(Level.TRACE, format, data);
	}

	public Logger getLogger()
	{
		return myLog;
	}
}
