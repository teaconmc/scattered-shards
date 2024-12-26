package cn.zbx1425.scatteredshards.neoforge;

import cn.zbx1425.scatteredshards.Lazy;
import cn.zbx1425.scatteredshards.RegistriesWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.modfest.scatteredshards.ScatteredShards;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegistriesWrapperImpl implements RegistriesWrapper {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ScatteredShards.ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, ScatteredShards.ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ScatteredShards.ID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, ScatteredShards.ID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, ScatteredShards.ID);
    private static final DeferredRegister<ComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ScatteredShards.ID);

    @Override
    public void registerBlock(String id, Lazy<Block> block) {
        BLOCKS.register(id, block::get);
    }

    @Override
    public void registerBlockAndItem(String id, Lazy<Block> block, RegistryKey<ItemGroup> tab) {
        BLOCKS.register(id, block::get);
        ITEMS.register(id, () -> {
            final BlockItem blockItem = new BlockItem(block.get(), new Item.Settings());
            registerCreativeModeTab(tab, blockItem);
            return blockItem;
        });
    }

    @Override
    public void registerItem(String id, Lazy<Item> item) {
        ITEMS.register(id, item::get);
    }

    @Override
    public void registerBlockEntityType(String id, Lazy<? extends BlockEntityType<? extends BlockEntity>> blockEntityType) {
        BLOCK_ENTITY_TYPES.register(id, blockEntityType::get);
    }

    @Override
    public void registerEntityType(String id, Lazy<? extends EntityType<? extends Entity>> entityType) {
        ENTITY_TYPES.register(id, entityType::get);
    }

    @Override
    public void registerSoundEvent(String id, SoundEvent soundEvent) {
        SOUND_EVENTS.register(id, () -> soundEvent);
    }

    @Override
    public <T> void registerDataComponentType(String id, Lazy<ComponentType<T>> componentType) {
        DATA_COMPONENT_TYPES.register(id, componentType::get);
    }

    public void registerAllDeferred(IEventBus eventBus) {
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
        ENTITY_TYPES.register(eventBus);
        SOUND_EVENTS.register(eventBus);
        DATA_COMPONENT_TYPES.register(eventBus);
    }


    private static final Map<RegistryKey<ItemGroup>, ArrayList<Item>> CREATIVE_TABS = new HashMap<>();

    public static void registerCreativeModeTab(RegistryKey<ItemGroup> resourceLocation, Item item) {
        CREATIVE_TABS.computeIfAbsent(resourceLocation, ignored -> new ArrayList<>()).add(item);
    }

    public static class RegisterCreativeTabs {

        @SubscribeEvent
        public static void onRegisterCreativeModeTabsEvent(BuildCreativeModeTabContentsEvent event) {
            CREATIVE_TABS.forEach((key, items) -> {
                if (event.getTabKey().equals(key)) {
                    items.forEach(item -> event.add(new ItemStack(item), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS));
                }
            });
        }

    }
}
