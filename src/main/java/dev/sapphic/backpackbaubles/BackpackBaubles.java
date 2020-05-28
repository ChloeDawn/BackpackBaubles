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

package dev.sapphic.backpackbaubles;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import dev.sapphic.backpackbaubles.client.BackpackLayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DependencyParser;
import net.minecraftforge.fml.common.versioning.DependencyParser.DependencyInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import vazkii.quark.oddities.item.ItemBackpack;

import java.io.File;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BackpackBaubles extends DummyModContainer {
    public static final String ID = "backpackbaubles";
    public static final String NAME = "Backpack Baubles";

    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation(ID, "capability");
    private static final int BAUBLE_BODY_SLOT = 5;

    private static final IBauble BACKPACK_BAUBLE = new IBauble() {
        @Override
        public BaubleType getBaubleType(final ItemStack stack) {
            return BaubleType.BODY;
        }

        @Override
        public boolean canEquip(final ItemStack stack, final EntityLivingBase entity) {
            // Only allow equip if the entity has no backpack in their chestplate slot
            return !getChestplateBackpack(entity).isEmpty();
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

    private static final ICapabilityProvider CAPABILITY_PROVIDER = new ICapabilityProvider() {
        @Override
        public boolean hasCapability(final Capability<?> capability, final @Nullable EnumFacing side) {
            return CapabilityHolder.bauble == capability;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> @Nullable T getCapability(final Capability<T> capability, final @Nullable EnumFacing side) {
            return CapabilityHolder.bauble == capability ? (T) BACKPACK_BAUBLE : null;
        }
    };

    public BackpackBaubles() {
        super(new ModMetadata());
        final ModMetadata metadata = this.getMetadata();
        metadata.modId = ID;
        metadata.name = NAME;
        metadata.version = "$version";
        metadata.description = "Allows for Quark's backpack to be equipped in Baubles' body slot";
        metadata.url = "https://github.com/ChloeDawn/BackpackBaubles";
        metadata.authorList = ImmutableList.of("Chloe Dawn");
        final DependencyInfo info = new DependencyParser(ID, FMLCommonHandler.instance().getSide())
            .parseDependencies("required-after:quark@[r1.6-179,);required-after:baubles@[1.5.2,)");
        metadata.requiredMods = info.requirements;
        metadata.dependencies = info.dependencies;
        metadata.dependants = info.dependants;
    }

    public static boolean hasNoBaubleBackpack(final Entity entity) {
        return !(entity instanceof EntityPlayer && !getBaubleBackpack((EntityPlayer) entity).isEmpty());
    }

    @SideOnly(Side.CLIENT)
    public static void setupRenderLayers(final Map<String, RenderPlayer> renderers) {
        for (final RenderPlayer renderer : renderers.values()) {
            renderer.addLayer(new BackpackLayer(renderer));
        }
    }

    public static ItemStack getBackpackStack(final ItemStack chestplate, final EntityLivingBase entity) {
        if (!(chestplate.getItem() instanceof ItemBackpack) && entity instanceof EntityPlayer) {
            final ItemStack bauble = getBaubleBackpack((EntityPlayer) entity);
            if (!bauble.isEmpty()) {
                return bauble;
            }
        }
        return chestplate;
    }

    public static ItemStack getBaubleBackpack(final EntityPlayer player) {
        final ItemStack bauble = getBaubleHandler(player).getStackInSlot(BAUBLE_BODY_SLOT);
        return bauble.getItem() instanceof ItemBackpack ? bauble : ItemStack.EMPTY;
    }

    private static ItemStack getChestplateBackpack(final EntityLivingBase entity) {
        final ItemStack chestplate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        return chestplate.getItem() instanceof ItemBackpack ? chestplate : ItemStack.EMPTY;
    }

    private static IItemHandler getItemHandler(final ItemStack stack) {
        final @Nullable IItemHandler handler = stack.getCapability(CapabilityHolder.itemHandler, null);
        return handler != null ? handler : EmptyHandler.INSTANCE;
    }

    private static IItemHandler getBaubleHandler(final EntityPlayer player) {
        final @Nullable IBaublesItemHandler handler = player.getCapability(CapabilityHolder.baubleHandler, null);
        if (handler != null) {
            // Azanor whyyyyyyyyyyyyy
            handler.setPlayer(player);
            return handler;
        }
        return EmptyHandler.INSTANCE;
    }

    @Subscribe
    public void subscribeToEventBus(final FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void attachCapabilities(final AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof ItemBackpack) {
            Preconditions.checkState(CapabilityHolder.bauble != null, "Missing bauble capability");
            event.addCapability(CAPABILITY_ID, CAPABILITY_PROVIDER);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void equipBackpackAsBauble(final PlayerInteractEvent.RightClickItem event) {
        final ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof ItemBackpack) {
            final EntityPlayer player = event.getEntityPlayer();
            EnumActionResult result = EnumActionResult.FAIL;
            if (!getChestplateBackpack(player).isEmpty()) {
                final IItemHandler handler = getBaubleHandler(player);
                final ItemStack remainder = handler.insertItem(BAUBLE_BODY_SLOT, stack.copy(), true);
                if (remainder.getCount() < stack.getCount()) {
                    player.playSound(((ItemArmor) stack.getItem()).getArmorMaterial().getSoundEvent(), 1.0F, 1.0F);
                    handler.insertItem(BAUBLE_BODY_SLOT, stack.copy(), false);
                    stack.setCount(remainder.getCount());
                    result = EnumActionResult.SUCCESS;
                }
            }
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    @Override
    public List<ArtifactVersion> getDependants() {
        return this.getMetadata().dependants;
    }

    @Override
    public List<ArtifactVersion> getDependencies() {
        return this.getMetadata().dependencies;
    }

    @Override
    public Set<ArtifactVersion> getRequirements() {
        return this.getMetadata().requiredMods;
    }

    @Override
    public File getSource() {
        return LoadingPlugin.getSource();
    }

    @Override
    public boolean registerBus(final EventBus bus, final LoadController controller) {
        bus.register(this);
        return true;
    }

    @Override
    public @Nullable Certificate getSigningCertificate() {
        final @Nullable CodeSource source = BackpackBaubles.class.getProtectionDomain().getCodeSource();
        final Certificate @Nullable [] certificates = source != null ? source.getCertificates() : null;
        return certificates != null && certificates.length > 0 ? certificates[0] : null;
    }

    @Override
    public List<String> getOwnedPackages() {
        return ImmutableList.of(
            "dev.sapphic.backpackbaubles",
            "dev.sapphic.backpackbaubles.asm",
            "dev.sapphic.backpackbaubles.client"
        );
    }

    private static final class CapabilityHolder {
        @CapabilityInject(IItemHandler.class)
        private static @MonotonicNonNull Capability<IItemHandler> itemHandler;

        @CapabilityInject(IBaublesItemHandler.class)
        private static @MonotonicNonNull Capability<IBaublesItemHandler> baubleHandler;

        @CapabilityInject(IBauble.class)
        private static @MonotonicNonNull Capability<IBauble> bauble;
    }
}
