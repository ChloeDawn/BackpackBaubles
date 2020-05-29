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

package dev.sapphic.backpackbaubles.client;

import dev.sapphic.backpackbaubles.BackpackBaubles;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;
import vazkii.quark.oddities.client.model.ModelBackpack;

public final class BackpackLayer implements LayerRenderer<AbstractClientPlayer> {
    private static final ResourceLocation WORN_TEXTURE = new ResourceLocation("quark", "textures/misc/backpack_worn.png");
    private static final ResourceLocation WORN_OVERLAY_TEXTURE = new ResourceLocation("quark", "textures/misc/backpack_worn_overlay.png");

    private final ModelBackpack model = new ModelBackpack();
    private final RenderPlayer renderer;

    public BackpackLayer(final RenderPlayer renderer) {
        this.renderer = renderer;
        this.model.setVisible(false);
        this.model.bipedBody.showModel = true;
    }

    @Override
    public void doRenderLayer(final AbstractClientPlayer player, final float limbSwing, final float limbSwingAmount, final float delta, final float age, final float yaw, final float pitch, final float scale) {
        final ItemStack stack = BackpackBaubles.getBaubleBackpack(player);
        if (!stack.isEmpty()) {
            this.renderBackpack(stack, player, limbSwing, limbSwingAmount, delta, age, yaw, pitch, scale);
        }
    }

    @Override
    @Contract(value = " -> false", pure = true)
    public boolean shouldCombineTextures() {
        return false;
    }

    private void renderBackpack(final ItemStack stack, final AbstractClientPlayer player, final float limbSwing, final float limbSwingAmount, final float delta, final float age, final float yaw, final float pitch, final float scale) {
        this.model.setModelAttributes(this.renderer.getMainModel());
        this.model.setLivingAnimations(player, limbSwing, limbSwingAmount, delta);

        final int color = ((ItemArmor) stack.getItem()).getColor(stack);
        final float red = (color >> 16 & 255) / 255.0F;
        final float green = (color >> 8 & 255) / 255.0F;
        final float blue = (color & 255) / 255.0F;

        this.renderer.bindTexture(WORN_TEXTURE);
        GlStateManager.color(1.0F * red, 1.0F * green, 1.0F * blue, 1.0F);
        this.model.render(player, limbSwing, limbSwingAmount, age, yaw, pitch, scale);

        this.renderer.bindTexture(WORN_OVERLAY_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.model.render(player, limbSwing, limbSwingAmount, age, yaw, pitch, scale);
    }
}
