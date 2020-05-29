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
import org.jetbrains.annotations.Contract;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public final class BackpackFeatureVisitor extends ClassVisitor {
    public BackpackFeatureVisitor(final ClassWriter writer) {
        super(Opcodes.ASM5, writer);
    }

    @Contract(pure = true)
    private static boolean isItemStackGetItem(final int opcode, final String owner, final String name, final String desc) {
        return opcode == Opcodes.INVOKEVIRTUAL
            && "net/minecraft/item/ItemStack".equals(owner)
            && ("func_77973_b".equals(name) || "getItem".equals(name))
            && "()Lnet/minecraft/item/Item;".equals(desc);
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
                            if (isItemStackGetItem(opcode, owner, name, desc)) {
                                this.loadLocal(1, Type.getObjectType("net/minecraft/entity/EntityLiving"));
                                this.invokeStatic(Type.getObjectType(ClassTransformer.BACKPACK_BAUBLES),
                                    Method.getMethod(ClassTransformer.GET_BACKPACK_STACK)
                                );
                                this.storeLocal(2, Type.getObjectType("net/minecraft/item/ItemStack"));
                                this.loadLocal(2, Type.getObjectType("net/minecraft/item/ItemStack"));
                            }
                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                        }
                    };
                case "(Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Z":
                    return new GeneratorAdapter(Opcodes.ASM5, mv, access, name, desc) {
                        @Override
                        public void visitVarInsn(final int opcode, final int var) {
                            if (opcode == Opcodes.ALOAD && var == 1) {
                                this.loadLocal(2, Type.getObjectType("net/minecraft/entity/EntityLiving"));
                                this.invokeStatic(Type.getObjectType(ClassTransformer.BACKPACK_BAUBLES),
                                    Method.getMethod(ClassTransformer.GET_BACKPACK_STACK)
                                );
                                this.storeLocal(3, Type.getObjectType("net/minecraft/item/ItemStack"));
                                this.loadLocal(3, Type.getObjectType("net/minecraft/item/ItemStack"));
                            }
                            super.visitVarInsn(opcode, var);
                        }
                    };
            }
        }
        return mv;
    }
}
