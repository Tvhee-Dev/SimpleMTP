package me.tvhee.simplemtp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import jmtp.PortableDevice;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;
import jmtp.PortableDeviceType;
import jmtp.PowerSource;

public final class ExternalDevice
{
	private final PortableDevice original;
	private boolean opened;

	public ExternalDevice(PortableDevice original)
	{
		this.original = original;
	}

	public PortableDevice getOriginal()
	{
		return original;
	}

	public String getName()
	{
		return original.getFriendlyName();
	}

	public String getManufacturer()
	{
		return original.getManufacturer();
	}

	public String getDescription()
	{
		return original.getDescription();
	}

	public String getSerialNumber()
	{
		return original.getSerialNumber();
	}

	public String getFirmwareVersion()
	{
		return original.getFirmwareVersion();
	}

	public String getModel()
	{
		return original.getModel();
	}

	public String getProtocol()
	{
		return original.getProtocol();
	}

	public String getSyncPartner()
	{
		return original.getSyncPartner();
	}

	public int getPowerLevel()
	{
		return original.getPowerLevel();
	}

	public PortableDeviceType getType()
	{
		return original.getType();
	}

	public PowerSource getPowerSource()
	{
		return original.getPowerSource();
	}

	public boolean isNonConsumableSupported()
	{
		return original.isNonConsumableSupported();
	}

	public void openDevice()
	{
		if(!opened)
		{
			original.open();
			opened = true;
		}
	}

	public void closeDevice()
	{
		if(opened)
		{
			original.close();
			opened = false;
		}
	}

	public DeviceFile addFile(File file, String mtpDirectory) throws IOException
	{
		deleteFile(mtpDirectory, file.getName());

		String lastpartofpath = mtpDirectory.substring(mtpDirectory.lastIndexOf("\\") + 1);
		PortableDeviceStorageObject storage = getStorage();
		PortableDeviceFolderObject folder = createFolder(mtpDirectory, storage, null, lastpartofpath);

		return new DeviceFile(folder.addAudioObject(file, "--", "--", new BigInteger("0")), this);
	}

	public DeviceFile getFile(String mtpDirectory, String fileName) throws FileNotFoundException
	{
		PortableDeviceObject file = findFile(mtpDirectory, fileName);

		if(file == null)
			throw new FileNotFoundException(mtpDirectory + "/" + fileName);

		return new DeviceFile(file, this);
	}

	public List<DeviceFile> getFiles(String... extensions)
	{
		List<DeviceFile> files = new ArrayList<>();
		List<String> extensionList = Arrays.asList(extensions);

		PortableDeviceStorageObject root = getStorage();

		if(root == null)
			return files;

		for(PortableDeviceObject file : getChildFiles(root.getChildObjects()))
		{
			String extension = file.getOriginalFileName().contains(".") ?
					file.getOriginalFileName().substring(file.getOriginalFileName().lastIndexOf('.') + 1) : "";

			if(!extensionList.contains(extension))
				continue;

			files.add(new DeviceFile(file, this));
		}

		return files;
	}

	public void deleteFile(String mtpDirectory, String fileName)
	{
		PortableDeviceObject fileObject = findFile(mtpDirectory, fileName);

		if(fileObject != null && fileObject.canDelete())
			fileObject.delete();
	}

	public void deleteDirectory(String mtpDirectory)
	{
		String lastpartofpath = mtpDirectory.substring(mtpDirectory.lastIndexOf("\\") + 1);
		PortableDeviceStorageObject storage = getStorage();
		PortableDeviceFolderObject folder = createFolder(mtpDirectory, storage, null, lastpartofpath);

		for(PortableDeviceObject fileObject : folder.getChildObjects())
		{
			if(fileObject != null && fileObject.canDelete())
				fileObject.delete();
		}
	}

	public List<DeviceFile> getDirectory(String mtpDirectory)
	{
		String lastpartofpath = mtpDirectory.substring(mtpDirectory.lastIndexOf("\\") + 1);
		PortableDeviceStorageObject storage = getStorage();
		PortableDeviceFolderObject folder = createFolder(mtpDirectory, storage, null, lastpartofpath);

		List<DeviceFile> files = new ArrayList<>();

		for(PortableDeviceObject file : folder.getChildObjects())
			files.add(new DeviceFile(file, this));

		return files;
	}

	public List<DeviceFile> getFilesAfter(Date lastChecked, String mtpPath)
	{
		String lastpartofpath = mtpPath.substring(mtpPath.lastIndexOf("\\") + 1);
		PortableDeviceStorageObject storage = getStorage();
		PortableDeviceFolderObject folder = createFolder(mtpPath, storage, null, lastpartofpath);

		List<DeviceFile> newFiles = new ArrayList<>();

		for(PortableDeviceObject object : folder.getChildObjects())
		{
			if(object.getDateModified() != null && object.getDateModified().after(lastChecked))
				newFiles.add(new DeviceFile(object, this));
		}

		return newFiles;
	}

	public List<DeviceFile> getFilesBefore(Date lastChecked, String mtpPath)
	{
		String lastpartofpath = mtpPath.substring(mtpPath.lastIndexOf("\\") + 1);
		PortableDeviceStorageObject storage = getStorage();
		PortableDeviceFolderObject folder = createFolder(mtpPath, storage, null, lastpartofpath);

		List<DeviceFile> newFiles = new ArrayList<>();

		for(PortableDeviceObject object : folder.getChildObjects())
		{
			if(object.getDateModified() != null && object.getDateModified().before(lastChecked))
				newFiles.add(new DeviceFile(object, this));
		}

		return newFiles;
	}

	private PortableDeviceObject findFile(String mtpDirectory, String fileName)
	{
		PortableDeviceStorageObject storage = getStorage();
		PortableDeviceFolderObject folder;

		if(storage == null)
			return null;

		String lastpartofpath = mtpDirectory.substring(mtpDirectory.lastIndexOf("\\") + 1);

		if(!lastpartofpath.equals(""))
		{
			folder = createFolder(mtpDirectory, storage, null, lastpartofpath);

			for(PortableDeviceObject object : folder.getChildObjects())
			{
				if(object.getOriginalFileName().equals(fileName))
					return object;
			}
		}
		else
		{
			for(PortableDeviceObject object : storage.getChildObjects())
			{
				if(object.getOriginalFileName().equals(fileName))
					return object;
			}
		}

		return null;
	}

	private List<PortableDeviceObject> getChildFiles(PortableDeviceObject[] files)
	{
		List<PortableDeviceObject> childFileList = new ArrayList<>();

		for(PortableDeviceObject file : files)
		{
			if(file instanceof PortableDeviceStorageObject)
				childFileList.addAll(getChildFiles(((PortableDeviceStorageObject) file).getChildObjects()));
			else if(file instanceof PortableDeviceFolderObject)
				childFileList.addAll(getChildFiles(((PortableDeviceFolderObject) file).getChildObjects()));
			else
				childFileList.add(file);
		}

		return childFileList;
	}

	private PortableDeviceFolderObject createFolder(String mtpPath, PortableDeviceStorageObject storage, PortableDeviceFolderObject folder, String lastDir)
	{
		mtpPath = mtpPath.substring((mtpPath.indexOf("\\") + 1));
		PortableDeviceFolderObject folderNew;

		if(mtpPath.contains("\\"))
		{
			String root = mtpPath.substring(0, mtpPath.indexOf("\\"));

			if(folder == null)
			{
				folderNew = (PortableDeviceFolderObject) getChildByName(storage, root);

				if(folderNew == null)
					folderNew = storage.createFolderObject(root);
			}
			else
			{
				folderNew = getChildByName(folder, root);

				if(folderNew == null)
					folderNew = folder.createFolderObject(root);
			}

			return createFolder(mtpPath, storage, folderNew, lastDir);
		}
		else
		{
			if(folder == null)
			{
				folderNew = (PortableDeviceFolderObject) getChildByName(storage, lastDir);
			}
			else
			{
				folderNew = getChildByName(folder, lastDir);

				if(folderNew == null)
					folderNew = folder.createFolderObject(lastDir);

			}

			return folderNew;
		}
	}

	private PortableDeviceStorageObject getStorage()
	{
		if(original.getRootObjects() != null)
		{
			for(PortableDeviceObject object : original.getRootObjects())
			{
				if(object instanceof PortableDeviceStorageObject)
					return (PortableDeviceStorageObject) object;
			}
		}

		return null;
	}

	private PortableDeviceFolderObject getChildByName(PortableDeviceFolderObject folder, String name)
	{
		for(PortableDeviceObject object : folder.getChildObjects())
		{
			if(object.getOriginalFileName().equals(name))
				return (PortableDeviceFolderObject) object;
		}

		return null;
	}

	private PortableDeviceObject getChildByName(PortableDeviceStorageObject storage, String name)
	{
		for(PortableDeviceObject object : storage.getChildObjects())
		{
			if(object.getOriginalFileName().equals(name))
				return object;
		}

		return null;
	}

	/*public void addFile(MachineFile machineFile)
	{
		original.open();

		File cache = new File(System.getProperty("java.io.tmpdir") + "/SimpleMTP/cache/");
		File input = new File(cache, machineFile.getName());

		try
		{
			Files.copy(input.toPath(), machineFile.getOutputStream());
			input.deleteOnExit();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return;
		}

		String[] directories = machineFile.getName().split(File.separator);
		PortableDeviceObject root = getRoot();

		if(root == null)
			return;

		for(String directoryLookingFor : directories)
		{
			PortableDeviceObject[] childObjects = new PortableDeviceObject[0];

			if(root instanceof PortableDeviceStorageObject)
				childObjects = ((PortableDeviceStorageObject) root).getChildObjects();
			else if(root instanceof PortableDeviceFolderObject)
				childObjects = ((PortableDeviceFolderObject) root).getChildObjects();

			boolean fileFound = false;

			for(PortableDeviceObject file : childObjects)
			{
				if(file.getName().equals(directoryLookingFor))
				{
					root = file;
					fileFound = true;
					break;
				}
			}

			if(!fileFound && (root instanceof PortableDeviceFolderObject || root instanceof PortableDeviceStorageObject))
			{


				continue;
			}

			throw new IllegalArgumentException("Invalid path " + machineFile.getName());
		}
	}

	public List<DeviceFile> getDirectory(String directoryName) throws FileNotFoundException
	{
		original.open();

		List<DeviceFile> filesFound = new ArrayList<>();
		String[] directories = directoryName.split(File.separator);
		PortableDeviceToHostImpl32 copy = new PortableDeviceToHostImpl32();
		PortableDeviceObject root = getRoot();

		if(root == null)
			throw new FileNotFoundException();

		for(String directoryLookingFor : directories)
		{
			PortableDeviceObject[] childObjects = new PortableDeviceObject[0];

			if(root instanceof PortableDeviceStorageObject)
				childObjects = ((PortableDeviceStorageObject) root).getChildObjects();
			else if(root instanceof PortableDeviceFolderObject)
				childObjects = ((PortableDeviceFolderObject) root).getChildObjects();

			boolean fileFound = false;

			for(PortableDeviceObject file : childObjects)
			{
				if(file.getName().equals(directoryLookingFor))
				{
					root = file;
					fileFound = true;
					break;
				}
			}

			if(!fileFound)
				throw new FileNotFoundException(directoryName);
		}

		File cache = new File(System.getProperty("java.io.tmpdir") + "/SimpleMTP/cache/");
		File outputDirectory = new File(cache, root.getOriginalFileName());

		for(PortableDeviceObject file : getChildObjects(root))
		{
			File output = new File(outputDirectory, file.getOriginalFileName());

			try
			{
				copy.copyFromPortableDeviceToHost(root.getID(), output.getPath(), original);
				outputDirectory.deleteOnExit();
				filesFound.add(new DeviceFile(root.getOriginalFileName(), Files.newInputStream(outputDirectory.toPath())));
			}
			catch(COMException | IOException e)
			{
				e.printStackTrace();
			}
		}

		original.close();
		return filesFound;
	}

	public DeviceFile copyFileToMachine(String fileName) throws FileNotFoundException
	{
		original.open();

		String[] directories = fileName.split(File.separator);
		PortableDeviceToHostImpl32 copy = new PortableDeviceToHostImpl32();
		PortableDeviceObject root = getRoot();

		if(root == null)
			throw new FileNotFoundException();

		for(String directoryLookingFor : directories)
		{
			boolean fileFound = false;

			for(PortableDeviceObject file : getChildObjects(root))
			{
				if(file.getName().equals(directoryLookingFor))
				{
					root = file;
					fileFound = true;
					break;
				}
			}

			if(!fileFound)
				throw new FileNotFoundException(fileName);
		}

		File cache = new File(System.getProperty("java.io.tmpdir") + "/SimpleMTP/cache/");
		File output = new File(cache, root.getOriginalFileName());

		original.close();

		try
		{
			copy.copyFromPortableDeviceToHost(root.getID(), output.getPath(), original);
			output.deleteOnExit();
			return new DeviceFile(root.getOriginalFileName(), Files.newInputStream(output.toPath()));
		}
		catch(COMException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private PortableDeviceObject[] getChildObjects(PortableDeviceObject root)
	{
		PortableDeviceObject[] childObjects = new PortableDeviceObject[0];

		if(root instanceof PortableDeviceStorageObject)
			childObjects = ((PortableDeviceStorageObject) root).getChildObjects();
		else if(root instanceof PortableDeviceFolderObject)
			childObjects = ((PortableDeviceFolderObject) root).getChildObjects();

		return childObjects;
	}

	private PortableDeviceObject getRoot()
	{
		for(PortableDeviceObject file : original.getRootObjects())
		{
			if(file instanceof PortableDeviceStorageObject)
				return file;
		}

		return null;
	}*/
}
