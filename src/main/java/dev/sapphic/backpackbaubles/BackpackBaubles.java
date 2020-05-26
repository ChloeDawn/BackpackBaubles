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
import baubles.api.BaublesApi;
import baubles.api.IBauble;
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

    public static final int BAUBLE_BODY_SLOT = 5;

    private static final IBauble BACKPACK_BAUBLE = new IBauble() {
        @Override
        public BaubleType getBaubleType(final ItemStack stack) {
            return BaubleType.BODY;
        }

        @Override
        public boolean canEquip(final ItemStack stack, final EntityLivingBase entity) {
            // Only allow the backpack to be equipped in the bauble slot if there isn't one in the chest slot
            return !(entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemBackpack);
        }

        @Override
        public boolean canUnequip(final ItemStack stack, final EntityLivingBase entity) {
            // Prevent the backpack being unequipped if it has items; follows semantics
            // of the chest slot, although Quark uses a binding curse in that situation
            return !ItemBackpack.doesBackpackHaveItems(stack);
        }
    };

    @CapabilityInject(IBauble.class)
    private static @MonotonicNonNull Capability<IBauble> baubleCapability;

    private static final ICapabilityProvider CAPABILITY_PROVIDER = new ICapabilityProvider() {
        @Override
        public boolean hasCapability(final Capability<?> capability, final @Nullable EnumFacing side) {
            return baubleCapability == capability;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> @Nullable T getCapability(final Capability<T> capability, final @Nullable EnumFacing side) {
            return baubleCapability == capability ? (T) BACKPACK_BAUBLE : null;
        }
    };

    public BackpackBaubles() {
        super(createModMetadata());
    }

    public static ItemStack getBackpackStack(final ItemStack chestStack, final EntityLivingBase entity) {
        if (!(chestStack.getItem() instanceof ItemBackpack) && entity instanceof EntityPlayer) {
            final IItemHandler handler = BaublesApi.getBaublesHandler((EntityPlayer) entity);
            final ItemStack baubleStack = handler.getStackInSlot(BAUBLE_BODY_SLOT);
            if (baubleStack.getItem() instanceof ItemBackpack) {
                return baubleStack;
            }
        }
        return chestStack;
    }

    public static boolean isBaubleSlotEmpty(final Entity entity) {
        if (entity instanceof EntityPlayer) {
            final IItemHandler handler = BaublesApi.getBaublesHandler((EntityPlayer) entity);
            return !(handler.getStackInSlot(BAUBLE_BODY_SLOT).getItem() instanceof ItemBackpack);
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    public static void setupRenderLayers(final Map<String, RenderPlayer> renderers) {
        for (final RenderPlayer renderer : renderers.values()) {
            renderer.addLayer(new BackpackLayer(renderer));
        }
    }

    private static ModMetadata createModMetadata() {
        final ModMetadata metdata = new ModMetadata();
        metdata.modId = ID;
        metdata.name = "Backpack Baubles";
        metdata.version = "$version";
        metdata.description = "Allows for Quark's backpack to be equipped in Baubles' body slot";
        metdata.url = "https://github.com/ChloeDawn/BackpackBaubles";
        metdata.authorList = ImmutableList.of("Chloe Dawn");
        final DependencyInfo info = new DependencyParser(ID, FMLCommonHandler.instance().getSide())
            .parseDependencies("required-after:quark@[r1.6-179,);required-after:baubles@[1.5.2,)");
        metdata.requiredMods = info.requirements;
        metdata.dependencies = info.dependencies;
        metdata.dependants = info.dependants;
        return metdata;
    }

    @Subscribe
    public void construct(final FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void attachCapabilities(final AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof ItemBackpack) {
            Preconditions.checkState(baubleCapability != null, "Missing bauble capability");
            event.addCapability(new ResourceLocation(ID, "capability"), CAPABILITY_PROVIDER);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void equipBackpackAsBauble(final PlayerInteractEvent.RightClickItem event) {
        final ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ItemBackpack)) {
            return;
        }
        EnumActionResult result = EnumActionResult.FAIL;
        final EntityPlayer player = event.getEntityPlayer();
        if (!(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemBackpack)) {
            final IItemHandler handler = BaublesApi.getBaublesHandler(player);
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
}
