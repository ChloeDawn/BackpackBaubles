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
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public final class PlayerRendererVisitor extends ClassVisitor {
    public PlayerRendererVisitor(final ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (access == Opcodes.ACC_PUBLIC && "<init>".equals(name) && "(Lnet/minecraft/client/renderer/entity/RenderManager;Z)V".equals(desc)) {
            return new GeneratorAdapter(Opcodes.ASM5, mv, access, name, desc) {
                @Override
                public void visitInsn(final int opcode) {
                    if (opcode == Opcodes.RETURN) {
                        this.loadThis();
                        this.newInstance(Type.getObjectType(ClassTransformer.BACKPACK_LAYER));
                        this.dup();
                        this.loadThis();
                        this.invokeConstructor(Type.getObjectType(ClassTransformer.BACKPACK_LAYER),
                            Method.getMethod("void <init> (net.minecraft.client.renderer.entity.RenderPlayer)")
                        );
                        this.invokeVirtual(Type.getObjectType("net/minecraft/client/renderer/entity/RenderPlayer"),
                            Method.getMethod("boolean func_177094_a (net.minecraft.client.renderer.entity.layers.LayerRenderer)")
                        );
                        this.pop();
                    }
                    super.visitInsn(opcode);
                }

                @Override
                public void invokeVirtual(final Type owner, final Method method) {
                    final FMLDeobfuscatingRemapper rm = FMLDeobfuscatingRemapper.INSTANCE;
                    final String desc = method.getDescriptor();
                    final String unmappedOwner = rm.unmap(owner.getInternalName());
                    final String mappedName = rm.mapMethodName(unmappedOwner, method.getName(), desc);
                    super.invokeVirtual(owner, new Method(mappedName, desc));
                }
            };
        }
        return mv;
    }
}
