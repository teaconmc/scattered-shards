package cn.zbx1425.scatteredshards;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;

public interface RegistriesWrapper {

    void registerBlock(String id, Lazy<Block> block);

    void registerItem(String id, Lazy<Item> item);

    void registerBlockAndItem(String id, Lazy<Block> block, RegistryKey<ItemGroup> tab);

    void registerBlockEntityType(String id, Lazy<? extends BlockEntityType<? extends BlockEntity>> blockEntityType);

    void registerEntityType(String id, Lazy<? extends EntityType<? extends Entity>> entityType);

    void registerSoundEvent(String id, SoundEvent soundEvent);

    <T> void registerDataComponentType(String id, Lazy<ComponentType<T>> componentType);

}
