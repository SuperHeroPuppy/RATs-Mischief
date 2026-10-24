# Custom Fabric 1.20.1 port

This worktree updates the custom Fabric 1.19.2 branch to Minecraft 1.20.1. The 1.19.2 checkout remains separate. The port uses Fabric Loader, Fabric API, GeckoLib 4, Trinkets, and Cardinal Components. It has no Quilt or Requiem runtime dependency; the rat-specific possession implementation remains in this project.

Build and launch with Java 17:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\gradlew.bat build
.\gradlew.bat runClient
```

The output is `build/libs/ratsmischief-2.0.1-custom-fabric-1.20.1.jar`. Install it with Fabric API 0.92.2+1.20.1, GeckoLib 4.4.9, Trinkets 3.7.2, and Cardinal Components base and entity 5.2.3. Fabric Loader 0.16.14 or newer is required.

Rat Master smithing recipes now use a Rat Master Smithing Template. Craft one from paper and a clothed ingot, then combine the template, base item, and another clothed ingot in the smithing table. The ocarina cycles actions on left click through a client to server packet.

The client starts and enters an integrated world. The Fabric possession replacement controls a rat for the Curse of the Rat and an owned spy rat for the Rat Master Mirror; press **V** to return. As in the 1.19.2 Fabric port, controlled players use spectator mode, and Requiem's general soul and shell systems and save data conversion are outside this mod. Rat control, equipment, crafting, and multiplayer gameplay still need a manual playtest before release.
