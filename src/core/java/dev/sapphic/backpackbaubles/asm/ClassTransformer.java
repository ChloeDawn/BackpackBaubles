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

package dev.sapphic.backpackbaubles.asm;

import dev.sapphic.backpackbaubles.asm.visitor.BackpackContainerVisitor;
import dev.sapphic.backpackbaubles.asm.visitor.BackpackFeatureVisitor;
import dev.sapphic.backpackbaubles.asm.visitor.BackpackItemVisitor;
import dev.sapphic.backpackbaubles.asm.visitor.PlayerRendererVisitor;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.util.function.Function;

public final class ClassTransformer implements IClassTransformer {
    public static final String BACKPACK_BAUBLES = "dev/sapphic/backpackbaubles/server/BackpackBaubles";
    public static final String BACKPACK_LAYER = "dev/sapphic/backpackbaubles/client/BackpackLayer";
    public static final String GET_BACKPACK_STACK = "net.minecraft.item.ItemStack getBackpackStack " +
        "(net.minecraft.item.ItemStack, net.minecraft.entity.EntityLivingBase)";

    private static byte[] transform(final byte[] bytes, final Function<ClassWriter, ClassVisitor> visitor) {
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        new ClassReader(bytes).accept(visitor.apply(writer), ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    @Override
    public byte[] transform(final String name, final String mappedName, final byte[] bytes) {
        switch (mappedName) {
            case "vazkii.quark.oddities.feature.Backpacks":
                return transform(bytes, BackpackFeatureVisitor::new);
            case "vazkii.quark.oddities.inventory.ContainerBackpack":
                return transform(bytes, BackpackContainerVisitor::new);
            case "vazkii.quark.oddities.item.ItemBackpack":
                return transform(bytes, BackpackItemVisitor::new);
            case "net.minecraft.client.renderer.entity.RenderPlayer":
                return transform(bytes, PlayerRendererVisitor::new);
            default:
                return bytes;
        }
    }
}
