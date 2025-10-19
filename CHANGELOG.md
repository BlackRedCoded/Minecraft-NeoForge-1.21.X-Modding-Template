# Changelog

All notable changes to Create: Brass Man will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/),
and this project adheres to [Semantic Versioning](https://semver.org/).

---

## [1.0.0] - 2025-10-19

### 🎉 Initial Release

#### Added - Core Systems
- **Brass Man Armor Set**
    - Chestplate with flight system and dual energy storage (air & power)
    - Helmet with HUD integration
    - Leggings with movement speed enhancement
    - Boots with fall damage negation
    - All pieces feature 15 upgrade slots

- **Energy Systems**
    - Kinetic Battery for portable energy storage
    - Air Compressor for charging batteries and armor
    - Compressed air system for flight mechanics
    - Kinetic power system for abilities
    - Battery Helper utility for energy management

- **Custom Machines**
    - Modification Station for applying upgrades
    - Customization Station with 4 armor styles
    - Brass Armor Stand for display and charging
    - Kinetic Motor powered by Kinetic Batteries
    - Air Compressor with Create integration

#### Added - Upgrade System
- **Remote Assembly System** (Chestplate-exclusive)
    - MK 7 tier (⭐) - Basic remote assembly
    - MK 42 tier (⭐⭐) - Advanced remote assembly
    - MK 50 tier (⭐⭐⭐) - Master remote assembly
    - Visual star indicators in tooltips

- **Standard Upgrade Modules**
    - Field Assembly upgrade for remote suit deployment
    - Power Cell upgrades for increased capacity
    - Quick Charge modules for faster charging
    - Repulsor Beam for offensive capabilities
    - Energy Shielding for enhanced protection
    - 15 upgrade slots per armor piece
    - Mix-and-match upgrade combinations

#### Added - JEI Integration
- Optional JEI dependency (mod works without it)
- Custom "Armor Upgrading" recipe category
- Cycling upgrade level displays (similar to anvil enchantments)
- All upgrade paths visible in recipe viewer
- Remote Assembly tier progression visualization
- Correct tooltip displays for all items
- Battery upgrade display even when uncharged

#### Added - Create Integration
- Full kinetic energy system integration
- Air Compressor uses Create's rotational force
- Kinetic Battery storage mechanics
- Kinetic Motor for portable power generation
- Compatible with all Create power sources
- Brass Armor Stand charging when placed on Air Compressor

#### Added - Gameplay Features
- Flight system with space/shift controls
- Speed boost from leggings
- Fall damage negation from boots
- HUD elements for energy monitoring
- Jarvis AI assistant for customization
- 4 armor style variants
- Remote suit assembly capability

#### Technical
- Built for Minecraft 1.21.1
- NeoForge 21.1.1+ support
- Create 0.5.1+ dependency
- Custom data components for armor/battery data
- Persistent upgrade data through charging
- Sequenced Assembly recipes for crafting
- Custom NBT data handling
- Network packet system for flight synchronization

#### Crafting
- Compact Mechanism (Sequenced Assembly)
- Smart Mechanism (Sequenced Assembly)
- Kinetic Circuit components
- All armor pieces
- Upgrade modules
- Machines and blocks

---

## [Unreleased]

### Planned Features
- Additional upgrade types
- Patchouli guidebook integration
- Config file for customization
- More armor styles
- Multiplayer optimization
- REI/EMI official support

### Known Issues
- None currently reported

---

## Version History

- **1.0.0** - Initial public release (2025-10-19)

---

[1.0.0]: https://github.com/BlackRedCoded/Create_Brass_Man/releases/tag/v1.0.0
[Unreleased]: https://github.com/BlackRedCoded/Create_Brass_Man/compare/v1.0.0...HEAD
