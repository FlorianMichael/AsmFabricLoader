/*
 * This file is part of AsmFabricLoader - https://github.com/FlorianMichael/AsmFabricLoader
 * Copyright (C) 2023-2024 FlorianMichael/EnZaXD <florian.michael07@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.florianmichael.asmfabricloader.hook.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.florianmichael.asmfabricloader.AsmFabricLoader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {

    public MixinTitleScreen(Text title) {
        super(title);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"))
    public int drawAFLIndicator(DrawContext instance, TextRenderer textRenderer, String text, int x, int y, int color, Operation<Integer> original) {
        final int modsSize = AsmFabricLoader.getLoader().getAflMods().size();
        instance.drawTextWithShadow(textRenderer, "AsmFabricLoader: " + modsSize + " mod" + (modsSize != 1 ? "s" : "") + " loaded", x, y - 10, color);

        return original.call(instance, textRenderer, text, x, y, color);
    }

}
