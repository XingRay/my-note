# File size exceeds configured limit (2560000), code insight features not available

In IntelliJ 2016 and newer you can change this setting from the Help menu, Edit Custom Properties (as commented by @eggplantbr).

On older versions, there's no GUI to do it. But you can change it if you [edit the IntelliJ IDEA Platform Properties](https://intellij-support.jetbrains.com/hc/en-us/articles/206544869-Configuring-JVM-options-and-platform-properties) file:

```
#---------------------------------------------------------------------
# Maximum file size (kilobytes) IDE should provide code assistance for.
# The larger file is the slower its editor works and higher overall system memory requirements are
# if code assistance is enabled. Remove this property or set to very large number if you need
# code assistance for any files available regardless their size.
#---------------------------------------------------------------------
idea.max.intellisense.filesize=2500
```



Go to Help > Edit Custom Properties

Add:

```
idea.max.intellisense.filesize=999999
```

Restart the IDE.





Edit config file for IDEA: `IDEA_HOME/bin/idea.properties`

```
# Maximum file size (kilobytes) IDE should provide code assistance for.
idea.max.intellisense.filesize=60000

# Maximum file size (kilobytes) IDE is able to open.
idea.max.content.load.filesize=60000
```

Save and restart IDEA



Go to IDE:

- STEP 1: Open the menu item: click on **Help** then click on **Edit Custom Properties**
- STEP 2: Set the parameter:`idea.max.intellisense.filesize= 999999999`
- STEP 3: restart IDE





PhpStorm 2020

1. Help -> Edit Custom Properties.
2. File will open in Editor. Paste the section below to the file.

```
 #---------------------------------------------------------------------
 # Maximum file size (kilobytes) IDE should provide code assistance for.
 # The larger file is the slower its editor works and higher overall system memory 
 requirements are
 # if code assistance is enabled. Remove this property or set to very large number 
 if you need
 # code assistance for any files available regardless their size.
 #---------------------------------------------------------------------
 idea.max.intellisense.filesize=2500
```

1. File -> Invalidate / Restart -> Restart

This might not update sometimes and you might need to edit the root idea.properties file.

To edit this file for any version of Idea

1. Go to application location i.e linux => /opt/PhpStorm[version]/bin
2. Find file called idea.properties Make backup of file i.e idea.properties.old
3. Open original with any txt editor.
4. Find `idea.max.intellisense.filesize=`
5. Replace with `idea.max.intellisense.filesize=your_required_size` i.e `idea.max.intellisense.filesize=10480` `NB by default this size is in kb`
6. Save and restart the IDE.





## For the 64-bit 2020 version

I tried in vain to find the current location of this file for version 2020. When navigating to `Help > Edit Custom Properties` a new (empty) file is created at `appData/Roaming/JetBrains/IntelliJIdea2020.1/idea.properties`. There is no /bin directory as some tutorials suggest. However, neither adding the block from the [accepted answer](https://stackoverflow.com/a/23058324/1264804) to this file nor adding the same file to /bin resulted in an update to my configuration.

I finally discovered that a reference to the same property exists in `appData/Roaming/JetBrains/IntelliJIdea2020.1/idea64.exe.vmoptions`. It looks like this:

```
-Didea.max.intellisense.filesize=3470
```

I changed it to this, which should be adequate for my needs:

```
-Didea.max.intellisense.filesize=9999
```

This strikes me as a bug in this version where the behavior of the menu item doesn't reflect what's needed, but it may also be the case that my particular setup is somehow different than stock. I do have PyCharm and the JDK installed.





To clarify [Alvaro's](https://stackoverflow.com/a/23058324/892040) answer, you need to add the -D option to the list of command lines. I'm using PyCharm, but the concept is the same:

```
pycharm{64,.exe,64.exe}.vmoptions:
<code>
    -server
    -Xms128m
    ...
    -Didea.max.intellisense.filesize=999999 # <--- new line
</code>
```





For those who don't know where to find the file they're talking about. On my machine (OSX) it is in:

- PyCharm CE: `/Applications/PyCharm CE.app/Contents/bin/idea.properties`
- WebStorm: `/Applications/WebStorm.app/Contents/bin/idea.properties`



**Windows default install location for \*Webstorm\***:

> C:\Program Files\JetBrains\WebStorm 2019.1.3\bin\idea.properties

I went `x4 default for intellisense` and `x5 for file size`

(my business workstation is a beast though: ***8th gen i7, 32Gb RAM, NVMe PCIE3.0x4 SDD, gloat, etc, gloat, etc\***)

```
#---------------------------------------------------------------------
# Maximum file size (kilobytes) IDE should provide code assistance for.
# The larger file is the slower its editor works and higher overall system memory requirements are
# if code assistance is enabled. Remove this property or set to very large number if you need
# code assistance for any files available regardless their size.
#--------------------------------------------------------------------- 
idea.max.intellisense.filesize=10000

    #---------------------------------------------------------------------
    # Maximum file size (kilobytes) IDE is able to open.
    #--------------------------------------------------------------------- 

idea.max.content.load.filesize=100000
```



Changing the above options form Help menu didn't work for me. You have edit idea.properties file and change to some large no.

```
MAC: /Applications/<Android studio>.app/Contents/bin[Open App contents] 

Idea.max.intellisense.filesize=999999 

WINDOWS: IDE_HOME\bin\idea.properties
```





Go to ` Help | Edit Custom Properties... ` and add the following lines:



```csharp
#---------------------------------------------------------------------
# Maximum file size (kilobytes) IDE should provide code assistance for.
# The larger file is the slower its editor works and higher overall system memory requirements are
# if code assistance is enabled. Remove this property or set to very large number if you need
# code assistance for any files available regardless their size.
#---------------------------------------------------------------------
idea.max.intellisense.filesize=4000

#---------------------------------------------------------------------
# Maximum file size (kilobytes) IDE is able to open.
#---------------------------------------------------------------------
idea.max.content.load.filesize=20000
```



C# detected

You can change the values to whatever suits your needs.

You can also read more about the ` idea.properties ` file here: https://intellij-support.jetbrains.com/hc/articles/206544869

Subsystem: Inspections → IDE

State: Submitted → Wait for Reply