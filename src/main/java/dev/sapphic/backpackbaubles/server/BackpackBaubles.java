/*
 * Copyright (C) 2020 Chloe Dawn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sapphic.backpackbaubles.server;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import com.google.common.base.VerifyException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.InstanceFactory;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

import java.security.ProtectionDomain;
import java.util.Objects;

@EventBusSubscriber(modid = BackpackBaubles.ID)
@Mod(modid = BackpackBaubles.ID, useMetadata = true,
    certificateFingerprint = BackpackBaubles.FINGERPRINT)
public final class BackpackBaubles {
    public static final String ID = "backpackbaubles";
    public static final String NAME = "BackpackBaubles";
    public static final String FINGERPRINT = "$fingerprint";

    private static final int BAUBLE_BODY_SLOT = 5;

    @CapabilityInject(IBauble.class)
    private static @MonotonicNonNull Capability<IBauble> baubleCapability;

    @CapabilityInject(IBaublesItemHandler.class)
    private static @MonotonicNonNull Capability<IBaublesItemHandler> baubleHandlerCapability;

    @CapabilityInject(IItemHandler.class)
    private static @MonotonicNonNull Capability<IItemHandler> itemHandlerCapability;

    @ObjectHolder("quark:backpack")
    private static @MonotonicNonNull Item backpackItem;

    private static final ICapabilityProvider BAUBLE_PROVIDER = new ICapabilityProvider() {
        private final IBauble bauble = new IBauble() {
            @Override
            @Contract(pure = true)
            public BaubleType getBaubleType(final ItemStack stack) {
                return BaubleType.BODY;
            }

            @Override
            public boolean canEquip(final ItemStack stack, final EntityLivingBase entity) {
                // Only allow equip if the entity has no backpack in their chestplate slot
                return getChestplateBackpack(entity).isEmpty();
            }

            @Override
            public boolean canUnequip(final ItemStack stack, final EntityLivingBase entity) {
                // Only allow unequip if the backpack is empty - follows semantics of a chestplate
                // backpack where Quark enchants it silently with Curse of Binding when non-empty
                final IItemHandler handler = getItemHandler(stack);
                for (int slot = 0; slot < handler.getSlots(); ++slot) {
                    if (!handler.getStackInSlot(slot).isEmpty()) {
                        return false;
                    }
                }
                return true;
            }
        };

        @Override
        @Contract(value = "null, _ -> false", pure = true)
        public boolean hasCapability(final Capability<?> capability, final @Nullable EnumFacing side) {
            return baubleCapability != null && baubleCapability == capability;
        }

        @Override
        @SuppressWarnings("unchecked")
        @Contract(value = "null, _ -> null", pure = true)
        public <T> @Nullable T getCapability(final Capability<T> capability, final @Nullable EnumFacing side) {
            return this.hasCapability(capability, side) ? (T) this.bauble : null;
        }
    };

    private BackpackBaubles() {
    }

    @InstanceFactory
    public static Object instance() {
        return InstanceHolder.INSTANCE;
    }

    @EventHandler
    public static void fingerprintViolated(final FMLFingerprintViolationEvent event) {
        if (!(boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) {
            final ProtectionDomain domain = BackpackBaubles.class.getProtectionDomain();
            throw new VerifyException(String.valueOf(domain.getCodeSource()));
        }
    }

    @SubscribeEvent
    public static void attachCapabilities(final AttachCapabilitiesEvent<ItemStack> event) {
        if (baubleCapability != null && isBackpack(event.getObject())) {
            event.addCapability(new ResourceLocation(ID, "capability"), BAUBLE_PROVIDER);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void equipBackpackAsBauble(final PlayerInteractEvent.RightClickItem event) {
        final ItemStack stack = event.getItemStack();
        if (!isBackpack(stack)) {
            return;
        }
        final EntityPlayer player = event.getEntityPlayer();
        EnumActionResult result = EnumActionResult.FAIL;
        if (getChestplateBackpack(player).isEmpty()) {
            final IItemHandler handler = getBaubleHandler(player);
            final ItemStack remainder = handler.insertItem(BAUBLE_BODY_SLOT, stack.copy(), true);
            if (remainder.getCount() < stack.getCount()) {
                final ItemArmor armor = (ItemArmor) stack.getItem();
                final ItemArmor.ArmorMaterial material = armor.getArmorMaterial();
                player.playSound(material.getSoundEvent(), 1.0F, 1.0F);
                handler.insertItem(BAUBLE_BODY_SLOT, stack.copy(), false);
                stack.setCount(remainder.getCount());
                result = EnumActionResult.SUCCESS;
            }
        }
        event.setCancellationResult(result);
        event.setCanceled(true);
    }

    public static boolean hasNoBaubleBackpack(final Entity entity) {
        return !(entity instanceof EntityPlayer) || getBaubleBackpack((EntityPlayer) entity).isEmpty();
    }

    public static ItemStack getBackpackStack(final ItemStack chestplate, final EntityLivingBase entity) {
        if (!isBackpack(chestplate) && entity instanceof EntityPlayer) {
            final ItemStack bauble = getBaubleBackpack((EntityPlayer) entity);
            if (!bauble.isEmpty()) {
                return bauble;
            }
        }
        return chestplate;
    }

    public static ItemStack getBaubleBackpack(final EntityPlayer player) {
        final ItemStack bauble = getBaubleHandler(player).getStackInSlot(BAUBLE_BODY_SLOT);
        return isBackpack(bauble) ? bauble : ItemStack.EMPTY;
    }

    private static ItemStack getChestplateBackpack(final EntityLivingBase entity) {
        final ItemStack chestplate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        return isBackpack(chestplate) ? chestplate : ItemStack.EMPTY;
    }

    private static IItemHandler getItemHandler(final ItemStack stack) {
        Objects.requireNonNull(itemHandlerCapability, "itemHandlerCapability");
        final @Nullable IItemHandler handler = stack.getCapability(itemHandlerCapability, null);
        return handler != null ? handler : EmptyHandler.INSTANCE;
    }

    private static IItemHandler getBaubleHandler(final EntityPlayer player) {
        Objects.requireNonNull(baubleHandlerCapability, "baubleHandlerCapability");
        final @Nullable IBaublesItemHandler handler = player.getCapability(baubleHandlerCapability, null);
        if (handler == null) {
            return EmptyHandler.INSTANCE;
        }
        handler.setPlayer(player);
        return handler;
    }

    @Contract(pure = true)
    private static boolean isBackpack(final ItemStack stack) {
        return backpackItem != null && stack.getItem() == backpackItem;
    }

    @Override
    public String toString() {
        return NAME;
    }

    private static final class InstanceHolder {
        private static final BackpackBaubles INSTANCE = new BackpackBaubles();
    }
}
