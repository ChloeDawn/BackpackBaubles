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

package dev.sapphic.backpackbaubles;

import com.google.common.base.VerifyException;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.DependsOn;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Map;

@Name(BackpackBaubles.NAME)
@SortingIndex(1000)
@DependsOn({ "quark", "baubles" })
@MCVersion("1.12.2")
@TransformerExclusions({
    "dev.sapphic.backpackbaubles",
    "dev.sapphic.backpackbaubles.asm",
    "dev.sapphic.backpackbaubles.client"
})
public final class LoadingPlugin implements IFMLLoadingPlugin {
    private static final Logger LOGGER = LogManager.getLogger();

    private static @MonotonicNonNull File source;

    private static void verifyFingerprint(final boolean inDev) {
        final HashCode expectedHash = HashCode.fromString("$fingerprint");
        boolean foundCertificate = false;
        final @Nullable CodeSource source = BackpackBaubles.class.getProtectionDomain().getCodeSource();
        if (source != null) {
            final Certificate @Nullable [] certificates = source.getCertificates();
            if (certificates != null) {
                for (final Certificate certificate : certificates) {
                    final HashCode hash;
                    try {
                        hash = Hashing.sha1().hashBytes(certificate.getEncoded());
                    } catch (final CertificateEncodingException e) {
                        throw new IllegalStateException(String.valueOf(certificate), e);
                    }
                    if (expectedHash.equals(hash)) {
                        foundCertificate = true;
                        break;
                    }
                }
            }
        }
        if (!foundCertificate && !inDev) {
            throw new VerifyException(String.valueOf(source));
        }
    }

    static File getSource() {
        return source;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "dev.sapphic.backpackbaubles.asm.ClassTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return "dev.sapphic.backpackbaubles.BackpackBaubles";
    }

    @Override
    public @Nullable String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(final Map<String, Object> data) {
        verifyFingerprint(!(boolean) data.get("runtimeDeobfuscationEnabled"));
        source = (File) data.get("coremodLocation");
    }

    @Override
    public @Nullable String getAccessTransformerClass() {
        return null;
    }
}
