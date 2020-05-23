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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import static dev.sapphic.backpackbaubles.asm.BackpackClassTransformer.BACKPACK_BAUBLES;
import static dev.sapphic.backpackbaubles.asm.BackpackClassTransformer.IS_BAUBLE_SLOT_EMPTY;

final class BackpackItemVisitor extends ClassVisitor {
    private static final Type SUPER_CLASS = Type.getType("vazkii/arl/item/ItemModArmor");

    private static final Method IS_VALID_ARMOR = Method.getMethod(
        "boolean isValidArmor (" +
            "net.minecraft.item.ItemStack," +
            "net.minecraft.inventory.EntityEquipmentSlot," +
            "net.minecraft.entity.Entity" +
            ")");

    BackpackItemVisitor(final ClassWriter writer) {
        super(Opcodes.ASM5, writer);
    }

    @Override
    public void visitEnd() {
        final GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, IS_VALID_ARMOR, null, null, this);

        final Label ifeq = mg.newLabel();
        final Label exit = mg.newLabel();

        mg.loadArg(2); // Entity
        mg.invokeStatic(BackpackClassTransformer.BACKPACK_BAUBLES, BackpackClassTransformer.IS_BAUBLE_SLOT_EMPTY);
        mg.ifZCmp(Opcodes.IFEQ, ifeq);
        mg.loadThis();
        mg.loadArgs(); // ItemStack, EntityEquipmentSlot, Entity
        // essentially invokeSpecial, GeneratorAdaptor lacks in this department
        mg.invokeConstructor(SUPER_CLASS, IS_VALID_ARMOR);
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
