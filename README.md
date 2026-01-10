# Maingraph for MC

![A badge showing "ModLoader" is "NeoForge"](https://img.shields.io/badge/ModLoader-NeoForge-orange.svg)

**Maingraph for MC** is a visual blueprint engine designed for Minecraft. It allows for custom logic through intuitive node connections, requiring no (or very little) coding.

---
## Compatibility Notes

Currently known compatibility limitations with the following mods:

- **IMBlocker**: In the Blueprint Editor’s right-click search box, the input field uses a custom implementation rather than a native component. After installing IMBlocker, the input method cannot be used properly in the right-click search box.
    - **Workaround**: We recommend uninstalling IMBlocker or disabling it while editing blueprints.
    - **Estimated Fix**: Version `v0.3.*`.
- **Just Enough Characters (Pinyin Search)**: The Blueprint Editor’s search function does not currently support the Pinyin search provided by Just Enough Characters.
    - **Estimated Fix**: No plans at the moment.

## Quick Start

1. **Installation**: Place the mod in your Minecraft `mods` folder (requires NeoForge).
2. **Open Editor**: Once in-game, press `Ctrl + M` to open the Blueprint Manager.
3. **Create Blueprint**: Enter a name and click "Create".
4. **Start Authoring**: Right-click on the canvas to add nodes, connect white arrows (execution flow) and colored dots (data flow).
5. **Save & Run**: Click "Save", and your logic will start running in the background.

For detailed tutorials, please refer to [zhcn-docs.mc.maingraph.opens.ltd](http://zhcn-docs.mc.maingraph.opens.ltd/) (Note: Documentation is currently in Chinese).

Official Website: [mc.maingraph.opens.ltd](https://mc.maingraph.opens.ltd)

This project is licensed under the MIT License.
