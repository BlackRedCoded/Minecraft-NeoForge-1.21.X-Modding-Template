package net.blackredcoded.brassmanmod.items.upgrades;

import net.blackredcoded.brassmanmod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Set;

public class AirTankUpgradeItem extends UpgradeModuleItem {

    public AirTankUpgradeItem(Properties properties) {
        super(properties, "Compressed Air Tank", ChatFormatting.AQUA);
    }

    @Override
    protected void addUpgradeDescription(List<Component> tooltip) {
        tooltip.add(Component.literal("+3000 Maximum Air").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("Max 4 per chestplate").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public String getUpgradeType() {
        return "air_tank";
    }

    @Override
    public int getMaxStacksPerArmor() {
        return 5; // +15000 air total
    }

    @Override
    public Set<Item> getApplicableItems() {
        return Set.of(ModItems.BRASS_MAN_CHESTPLATE.get());
    }
}
