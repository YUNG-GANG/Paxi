package com.yungnickyoung.minecraft.paxi;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.PackSource;

/**
 * Custom ResourcePackSource for Paxi-loaded packs.
 * As a result, Paxi-loaded packs will be printed with a unique light purple color.
 */
public interface PaxiResourcePackSource extends PackSource {
    PackSource PACK_SOURCE_PAXI = paxiText();

    static PackSource paxiText() {
        Component text = new TextComponent("paxi");
        return (text2) -> (new TranslatableComponent("pack.nameAndSource", text2, text)).withStyle(ChatFormatting.LIGHT_PURPLE);
    }
}
