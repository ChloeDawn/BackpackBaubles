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

package dev.sapphic.backpackbaubles.asm.visitor;

import dev.sapphic.backpackbaubles.asm.ClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public final class BackpackItemVisitor extends ClassVisitor {
    public BackpackItemVisitor(final ClassWriter writer) {
        super(Opcodes.ASM5, writer);
    }

    @Override
    public void visitEnd() {
        final GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC,
            Method.getMethod("boolean isValidArmor (" +
                "net.minecraft.item.ItemStack, " +
                "net.minecraft.inventory.EntityEquipmentSlot, " +
                "net.minecraft.entity.Entity" +
                ")"), null, null, this
        );

        final Label ifeq = mg.newLabel();
        final Label exit = mg.newLabel();

        mg.loadArg(2); // entity
        mg.invokeStatic(Type.getObjectType(ClassTransformer.BACKPACK_BAUBLES),
            Method.getMethod("boolean hasNoBaubleBackpack (net.minecraft.entity.Entity)")
        );
        mg.ifZCmp(Opcodes.IFEQ, ifeq);
        mg.loadThis();
        mg.loadArgs(); // stack, slot, entity
        // invokeSpecial
        mg.invokeConstructor(Type.getObjectType("vazkii/arl/item/ItemModArmor"),
            Method.getMethod("boolean isValidArmor (" +
                "net.minecraft.item.ItemStack, " +
                "net.minecraft.inventory.EntityEquipmentSlot, " +
                "net.minecraft.entity.Entity" +
                ")")
        );
        mg.ifZCmp(Opcodes.IFEQ, ifeq);
        mg.push(true);
        mg.goTo(exit);

        mg.mark(ifeq);
        mg.push(false);

        mg.mark(exit);
        mg.returnValue();

        mg.endMethod();
        super.visitEnd();
    }
}
