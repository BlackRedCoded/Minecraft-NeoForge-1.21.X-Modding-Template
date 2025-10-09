package net.blackredcoded.brassmanmod.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.blackredcoded.brassmanmod.registry.ModBlockEntities;

import java.util.HashMap;
import java.util.Map;

public class CustomizationStationBlockEntity extends BlockEntity {
    private final Map<String, Integer> previewColors = new HashMap<>();
    private String selectedPiece = null;

    public CustomizationStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CUSTOMIZATION_STATION.get(), pos, state);
    }

    public void setPreviewColor(String piece, int color) {
        previewColors.put(piece, color);
        setChanged();
    }

    public Integer getPreviewColor(String piece) {
        return previewColors.get(piece);
    }

    public void clearPreviewColor(String piece) {
        previewColors.remove(piece);
        setChanged();
    }

    public void setSelectedPiece(String piece) {
        this.selectedPiece = piece;
        setChanged();
    }

    public String getSelectedPiece() {
        return selectedPiece;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider regs) {
        super.saveAdditional(tag, regs);
        CompoundTag ct = new CompoundTag();
        previewColors.forEach(ct::putInt);
        tag.put("Preview", ct);
        if (selectedPiece != null) tag.putString("Selected", selectedPiece);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider regs) {
        super.loadAdditional(tag, regs);
        previewColors.clear();
        if (tag.contains("Preview")) {
            CompoundTag ct = tag.getCompound("Preview");
            ct.getAllKeys().forEach(k -> previewColors.put(k, ct.getInt(k)));
        }
        if (tag.contains("Selected")) {
            selectedPiece = tag.getString("Selected");
        }
    }
}
