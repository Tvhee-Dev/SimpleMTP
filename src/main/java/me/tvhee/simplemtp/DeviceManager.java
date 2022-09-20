package me.tvhee.simplemtp;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import jmtp.PortableDevice;
import jmtp.PortableDeviceManager;

public final class DeviceManager
{
	static
	{
		String path = "SimpleMTP";
		loadLib(path, "jmtp32", System.getProperty("os.arch").contains("32"));
		loadLib(path, "jmtp64", System.getProperty("os.arch").contains("64"));
	}

	public static List<ExternalDevice> getDevices()
	{
		PortableDeviceManager manager = new PortableDeviceManager();
		manager.refreshDeviceList();

		List<ExternalDevice> devices = new ArrayList<>();

		for(PortableDevice device : manager)
			devices.add(new ExternalDevice(device));

		return devices;
	}

	private static void loadLib(String path, String name, boolean loadToClassPath)
	{
		name = name + ".dll";

		try
		{
			InputStream in = DeviceManager.class.getClassLoader().getResourceAsStream(name);
			File directory = new File(System.getProperty("java.io.tmpdir") + "/" + path + "/lib-bin/");
			directory.mkdirs();

			File output = new File(directory, name);
			output.createNewFile();

			Files.copy(in, output.toPath(), StandardCopyOption.REPLACE_EXISTING);

			if(loadToClassPath)
				System.load(output.toString());

			in.close();
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Failed to load required DLL", e);
		}
	}
}
