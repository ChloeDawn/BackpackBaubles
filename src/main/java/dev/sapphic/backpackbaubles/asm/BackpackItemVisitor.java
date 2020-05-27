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

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

final class BackpackItemVisitor extends ClassVisitor {
    BackpackItemVisitor(final ClassWriter writer) {
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

        mg.loadArg(2); // Entity
        mg.invokeStatic(Type.getObjectType("dev/sapphic/backpackbaubles/BackpackBaubles"),
            Method.getMethod("boolean isBaubleSlotEmpty (net.minecraft.entity.Entity)")
        );
        mg.ifZCmp(Opcodes.IFEQ, ifeq);
        mg.loadThis();
        mg.loadArgs(); // ItemStack, EntityEquipmentSlot, Entity
        // essentially invokeSpecial, GeneratorAdaptor lacks in this department
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
