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

import net.minecraft.launchwrapper.*;
import org.objectweb.asm.*;

import java.util.function.*;

public final class ClassTransformer implements IClassTransformer {
    static boolean isItemStackGetItem(final int opcode, final String owner, final String name, final String desc) {
        return opcode == Opcodes.INVOKEVIRTUAL
            && "net/minecraft/item/ItemStack".equals(owner)
            && ("func_77973_b".equals(name) || "getItem".equals(name))
            && "()Lnet/minecraft/item/Item;".equals(desc);
    }

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
            case "net.minecraft.client.renderer.entity.RenderManager":
                return transform(bytes, EntityRendererVisitor::new);
            default:
                return bytes;
        }
    }
}
