# Fabric 1.19.2 port

This worktree is a Fabric copy of the `custom-2.0.1` branch. It keeps Minecraft at 1.19.2. The original `custom` checkout is unchanged.

Build with Java 17 using `./gradlew build` (or `gradlew.bat build` on Windows). Install the resulting `ratsmischief-2.0.1-custom-fabric.jar` alongside Fabric API 0.76.1, GeckoLib 3.1.40 for Fabric, Trinkets 3.4.1, and Cardinal Components base and entity 5.0.2. Mialee Misc and the step height attribute mod are nested in the jar.

The 1.19.2 release of Requiem requires Quilt, so this port replaces its RAT's Mischief interactions with Fabric code. The Curse of the Rat creates and controls a rat until the effect expires. The Rat Master Mirror controls a nearby owned spy rat; press **V** to return to the player's body. Movement, jumping, flying ascent, and basic attacks are supported while controlling a rat.

This replacement does not include Requiem's general soul, shell, or possession systems. Players in rat control use spectator mode, so normal block and inventory interaction while controlling a rat is unavailable. Requiem remnant state in existing Quilt saves is not converted. Test saves should be backed up before opening them with this port.

The Fabric client reaches Minecraft startup with the mod loaded. Rat control gameplay should be checked in a client and server session before release.
