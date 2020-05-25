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

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

final class EntityRendererVisitor extends ClassVisitor {
    EntityRendererVisitor(final ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    private static boolean isConstructor(final int access, final String name, final String desc) {
        return access == Opcodes.ACC_PUBLIC && "<init>".equals(name) && ("(" +
            "Lnet/minecraft/client/renderer/texture/TextureManager;" +
            "Lnet/minecraft/client/renderer/RenderItem;" +
            ")V").equals(desc);
    }

    private static boolean isForgeHook(final int opcode, final String owner, final String name, final String desc) {
        return opcode == Opcodes.INVOKESTATIC
            && "net/minecraftforge/fml/client/registry/RenderingRegistry".equals(owner)
            && "loadEntityRenderers".equals(name)
            && "(Lnet/minecraft/client/renderer/entity/RenderManager;Ljava/util/Map;)V".equals(desc);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return isConstructor(access, name, desc) ? new GeneratorAdapter(Opcodes.ASM5, mv, access, name, desc) {
            @Override
            public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, final boolean itf) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                if (isForgeHook(opcode, owner, name, desc)) {
                    this.loadThis();
                    this.getField(Type.getObjectType("net/minecraft/client/renderer/entity/RenderManager"), "field_178636_l", Type.getObjectType("java/util/Map"));
                    this.invokeStatic(Type.getObjectType("dev/sapphic/backpackbaubles/BackpackBaubles"), Method.getMethod("void setupRenderLayers (java.util.Map)"));
                }
            }

            @Override
            public void getField(final Type owner, String name, final Type type) {
                final FMLDeobfuscatingRemapper rm = FMLDeobfuscatingRemapper.INSTANCE;
                name = rm.mapFieldName(rm.unmap(owner.getInternalName()), name, type.getDescriptor());
                super.getField(owner, name, type);
            }
        } : mv;
    }
}
