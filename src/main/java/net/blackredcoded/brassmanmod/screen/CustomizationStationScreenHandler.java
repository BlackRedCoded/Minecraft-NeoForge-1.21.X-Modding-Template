package net.blackredcoded.brassmanmod.screen;

import net.blackredcoded.brassmanmod.network.ApplyDyePacket;
import net.blackredcoded.brassmanmod.network.ModNetworking;
import net.blackredcoded.brassmanmod.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;

public class CustomizationStationScreenHandler extends AbstractContainerMenu {
    private final Inventory playerInv;
    private final BlockPos pos;

    public CustomizationStationScreenHandler(int syncId, Inventory inv, BlockPos pos) {
        super(ModMenuTypes.CUSTOMIZATION_STATION.get(), syncId);
        this.playerInv = inv;
        this.pos = pos;
    }

    public CustomizationStationScreenHandler(int syncId, Inventory inv) {
        this(syncId, inv, BlockPos.ZERO);
    }

    @Override
    public boolean stillValid(Player p) {
        return p.distanceToSqr(pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5) <= 64;
    }

    @Override
    public ItemStack quickMoveStack(Player p, int idx) {
        return ItemStack.EMPTY;
    }

    public int getDyeCount(DyeItem dye) {
        return playerInv.items.stream()
                .filter(s -> s.getItem()==dye)
                .mapToInt(ItemStack::getCount)
                .sum();
    }
}
