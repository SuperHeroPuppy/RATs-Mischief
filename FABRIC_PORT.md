# Clean Fabric 1.20.1 port

This worktree is a separate copy of the custom Fabric 1.20.1 port. It keeps the rat gameplay and possession fixes, the four original pouches, and Rat Master equipment. The crossed-out features from the custom creative tab have been removed: the Rabbit Hide Pouch, Rat Master blocks, extra ingots and cloth, Loyalty, and Bellicist equipment and materials.

Clothed Ingots and Rat Master Smithing Templates appear in Ancient City chests. Duplicate a template with the template above a Clothed Ingot and Eyes of Ender in the other seven crafting slots; the recipe yields two templates. Rat Master equipment uses a template and another Clothed Ingot in a smithing table.

Build with Java 17:

```powershell
.\gradlew.bat build
```

The output is `build/libs/ratsmischief-2.0.1-1.20.1-fabric-clean.jar`. Install it with Fabric Loader 0.16.14 or newer, Fabric API 0.92.2+1.20.1, GeckoLib 4.4.9, and Trinkets 3.7.2. The port has no Quilt or Requiem runtime dependency; rat possession is implemented here. The clean variant has passed a build and resource checks, but has not had a separate gameplay test.
