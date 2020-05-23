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

final class BackpackContainerVisitor extends ClassVisitor {
    private static final Type ITEM_STACK = Type.getType("net/minecraft/item/ItemStack");

    BackpackContainerVisitor(final ClassWriter writer) {
        super(Opcodes.ASM5, writer);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (access == Opcodes.ACC_PUBLIC && "<init>".equals(name) && "(Lnet/minecraft/entity/player/EntityPlayer;)V".equals(desc)) {
            return new GeneratorAdapter(Opcodes.ASM5, mv, access, name, desc) {
                @Override
                public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
                    if (BackpackClassTransformer.isItemStackGetItem(opcode, owner, name, desc)) {
                        this.loadArg(0); // EntityPlayer
                        this.invokeStatic(BackpackClassTransformer.BACKPACK_BAUBLES, BackpackClassTransformer.GET_BACKPACK_STACK);
                        this.storeLocal(5, ITEM_STACK);
                        this.loadLocal(5, ITEM_STACK);
                    }
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            };
        }
        return mv;
    }
}
