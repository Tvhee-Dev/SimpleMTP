# SimpleMTP

## Usage
1. To start scanning for devices call the `DeviceManager`:
```
List<ExternalDevice> deviceList = new ArrayList<>(DeviceManager.getDevices());
```
2. You can get any device from the list by name
```
for(ExternalDevice device : deviceList)
    if(device.getName().equals(...))
        //Do something with the device
```
3. If you want to browse the files, call `openDevice();`. Then call one of the following methods:
```
addFile(File file, String mtpDirectory);

getFile(String mtpDirectory, String fileName);

List<DeviceFile> getFiles(String... extensions); //To get all files leave extensions empty

deleteFile(String mtpDirectory, String fileName);

deleteDirectory(String mtpDirectory);

getDirectory(String mtpDirectory); //Get all the files in a directory

getFilesAfter(Date lastChecked, String mtpPath);

getFilesBefore(Date lastChecked, String mtpPath);
```
4. If you are done browsing the files, call `closeDevice();`

## Important
1. This will only work on WINDOWS 10, 8 and 7.
2. You can find the original work here: https://github.com/ultrah/jMTPe and https://github.com/mheinzerling/jMTPe
