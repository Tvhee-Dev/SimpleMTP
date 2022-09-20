package me.tvhee.simplemtp;

import be.derycke.pieter.com.COMException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceToHostImpl32;

public final class DeviceFile
{
	private final PortableDeviceObject original;
	private final ExternalDevice device;

	public DeviceFile(PortableDeviceObject original, ExternalDevice device)
	{
		this.original = original;
		this.device = device;
	}

	public String getID()
	{
		return original.getID();
	}

	public String getName()
	{
		return original.getOriginalFileName();
	}

	public boolean canDelete()
	{
		return original.canDelete();
	}

	public boolean isHidden()
	{
		return original.isHidden();
	}

	public void saveToHost(File destination)
	{
		try
		{
			String desiredFilename = getName();

			if(!destination.isDirectory())
			{
				desiredFilename = destination.getName();
				destination = destination.getParentFile();
			}

			destination.mkdirs();
			new PortableDeviceToHostImpl32().copyFromPortableDeviceToHost(original.getID(), destination.getPath(), device.getOriginal());

			if(!desiredFilename.equals(getName()))
			{
				File original = new File(destination, getName());
				File desired = new File(destination, desiredFilename);

				Files.copy(original.toPath(), desired.toPath());
				original.delete();
			}
		}
		catch(COMException | IOException e)
		{
			e.printStackTrace();
		}
	}
}
