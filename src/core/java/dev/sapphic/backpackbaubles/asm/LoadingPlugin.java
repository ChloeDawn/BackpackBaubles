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

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.DependsOn;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.Map;

@Name("BackpackBaublesLoadingPlugin")
@SortingIndex(1001)
@DependsOn({ "quark", "baubles" })
@MCVersion("1.12.2")
@TransformerExclusions("dev.sapphic.backpackbaubles")
public final class LoadingPlugin implements IFMLLoadingPlugin {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    @Contract(value = " -> new", pure = true)
    public String[] getASMTransformerClass() {
        return new String[] { "dev.sapphic.backpackbaubles.asm.ClassTransformer" };
    }

    @Override
    @Contract(value = " -> null", pure = true)
    public @Nullable String getModContainerClass() {
        return null;
    }

    @Override
    @Contract(value = " -> null", pure = true)
    public @Nullable String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(final Map<String, Object> data) {
    }

    @Override
    @Contract(value = " -> null", pure = true)
    public @Nullable String getAccessTransformerClass() {
        return null;
    }
}
