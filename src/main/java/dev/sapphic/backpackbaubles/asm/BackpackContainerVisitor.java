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
import org.objectweb.asm.commons.Method;

final class BackpackContainerVisitor extends ClassVisitor {
    BackpackContainerVisitor(final ClassWriter writer) {
        super(Opcodes.ASM5, writer);
    }

    private static boolean isConstructor(final int access, final String name, final String desc) {
        return access == Opcodes.ACC_PUBLIC && "<init>".equals(name)
            && "(Lnet/minecraft/entity/player/EntityPlayer;)V".equals(desc);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return isConstructor(access, name, desc) ? new GeneratorAdapter(Opcodes.ASM5, mv, access, name, desc) {
            @Override
            public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
                if (ClassTransformer.isItemStackGetItem(opcode, owner, name, desc)) {
                    this.loadArg(0); // EntityPlayer
                    this.invokeStatic(Type.getObjectType("dev/sapphic/backpackbaubles/BackpackBaubles"),
                        Method.getMethod("net.minecraft.item.ItemStack getBackpackStack " +
                            "(net.minecraft.item.ItemStack, net.minecraft.entity.EntityLivingBase)")
                    );
                    this.storeLocal(5, Type.getObjectType("net/minecraft/item/ItemStack"));
                    this.loadLocal(5, Type.getObjectType("net/minecraft/item/ItemStack"));
                }
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        } : mv;
    }
}
