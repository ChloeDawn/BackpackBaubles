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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import static dev.sapphic.backpackbaubles.asm.BackpackClassTransformer.BACKPACK_BAUBLES;
import static dev.sapphic.backpackbaubles.asm.BackpackClassTransformer.GET_BACKPACK_STACK;

final class BackpackFeatureVisitor extends ClassVisitor {
    private static final Type LIVING_ENTITY = Type.getType("net/minecraft/entity/EntityLiving");
    private static final Type ITEM_STACK = Type.getType("net/minecraft/item/ItemStack");

    BackpackFeatureVisitor(final ClassWriter writer) {
        super(Opcodes.ASM5, writer);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (access == (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC) && "isEntityWearingBackpack".equals(name)) {
            switch (desc) {
                case "(Lnet/minecraft/entity/Entity;)Z":
                    return new GeneratorAdapter(Opcodes.ASM5, mv, access, name, desc) {
                        @Override
                        public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
                            if (BackpackClassTransformer.isItemStackGetItem(opcode, owner, name, desc)) {
                                this.loadLocal(1, LIVING_ENTITY);
                                this.invokeStatic(BACKPACK_BAUBLES, GET_BACKPACK_STACK);
                                this.storeLocal(2, ITEM_STACK);
                                this.loadLocal(2, ITEM_STACK);
                            }
                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                        }
                    };
                case "(Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Z":
                    return new GeneratorAdapter(Opcodes.ASM5, mv, access, name, desc) {
                        @Override
                        public void visitVarInsn(final int opcode, final int var) {
                            if (opcode == Opcodes.ALOAD && var == 1) {
                                this.loadLocal(2, LIVING_ENTITY);
                                this.invokeStatic(BACKPACK_BAUBLES, GET_BACKPACK_STACK);
                                this.storeLocal(3, ITEM_STACK);
                                this.loadLocal(3, ITEM_STACK);
                            }
                            super.visitVarInsn(opcode, var);
                        }
                    };
            }
        }
        return mv;
    }
}
