package caveworld.util;

import caveworld.core.Caveworld;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import cpw.mods.fml.common.ModContainer;
import net.minecraftforge.classloading.FMLForgePlugin;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class Version
{
	private static Optional<String> CURRENT = Optional.absent();
	private static Optional<String> LATEST = Optional.absent();

	public static boolean DEV_DEBUG = false;

	private static Optional<Status> status = Optional.fromNullable(Status.PENDING);

	public static enum Status
	{
		PENDING,
		FAILED,
		UP_TO_DATE,
		OUTDATED,
		AHEAD
	}

	private static void initialize()
	{
		CURRENT = Optional.of(Strings.nullToEmpty(Caveworld.metadata.version));
		LATEST = Optional.fromNullable(CURRENT.orNull());

		ModContainer mod = CaveUtils.getModContainer();
		File file = mod == null ? null : mod.getSource();

		if (file != null && file.exists())
		{
			if (file.isFile())
			{
				String name = FilenameUtils.getBaseName(file.getName());

				if (StringUtils.endsWithIgnoreCase(name, "dev"))
				{
					DEV_DEBUG = true;
				}
			}
			else if (file.isDirectory())
			{
				DEV_DEBUG = true;
			}
		}
		else if (!FMLForgePlugin.RUNTIME_DEOBF)
		{
			DEV_DEBUG = true;
		}

		if (Caveworld.metadata.version.endsWith("dev"))
		{
			DEV_DEBUG = true;
		}
		else if (DEV_DEBUG)
		{
			Caveworld.metadata.version += "-dev";
		}
	}

	public static void versionCheck()
	{
		if (!CURRENT.isPresent() || !LATEST.isPresent())
		{
			initialize();
		}
	}

	public static String getCurrent()
	{
		return CURRENT.orNull();
	}

	public static String getLatest()
	{
		return LATEST.or(getCurrent());
	}

	public static Status getStatus()
	{
		return status.orNull();
	}

	public static boolean isOutdated()
	{
		return getStatus() == Status.OUTDATED;
	}

	public static boolean isBeta()
	{
		return StringUtils.containsIgnoreCase(getCurrent(), "beta");
	}

	public static boolean isAlpha()
	{
		return StringUtils.containsIgnoreCase(getCurrent(), "alpha");
	}
}
