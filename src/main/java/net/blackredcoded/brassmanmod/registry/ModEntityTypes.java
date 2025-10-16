package net.blackredcoded.brassmanmod.registry;

import net.blackredcoded.brassmanmod.BrassManMod;
import net.blackredcoded.brassmanmod.entity.FlyingArmorPieceEntity;
import net.blackredcoded.brassmanmod.entity.FlyingSuitEntity;
import net.blackredcoded.brassmanmod.entity.SentryArmorEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, BrassManMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<FlyingSuitEntity>> FLYING_SUIT =
            ENTITY_TYPES.register("flying_suit", () -> EntityType.Builder.<FlyingSuitEntity>of(
                            FlyingSuitEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("flying_suit"));

    public static final DeferredHolder<EntityType<?>, EntityType<FlyingArmorPieceEntity>> FLYING_ARMOR_PIECE =
            ENTITY_TYPES.register("flying_armor_piece", () -> EntityType.Builder.<FlyingArmorPieceEntity>of(
                            FlyingArmorPieceEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("flying_armor_piece"));

    public static final DeferredHolder<EntityType<?>, EntityType<SentryArmorEntity>> SENTRY_ARMOR =
            ENTITY_TYPES.register("sentry_armor", () -> EntityType.Builder.<SentryArmorEntity>of(
                            SentryArmorEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(64)
                    .updateInterval(20)
                    .build("sentry_armor"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
