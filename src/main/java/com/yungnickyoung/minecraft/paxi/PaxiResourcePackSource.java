package com.yungnickyoung.minecraft.paxi;

import net.minecraft.resource.ResourcePackSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

/**
 * Custom ResourcePackSource for Paxi-loaded packs.
 * As a result, Paxi-loaded packs will be printed with a unique light purple color.
 */
public interface PaxiResourcePackSource extends ResourcePackSource {
    ResourcePackSource PACK_SOURCE_PAXI = paxiText();

    static ResourcePackSource paxiText() {
        Text text = new LiteralText("paxi");
        return (text2) -> (new TranslatableText("pack.nameAndSource", text2, text)).formatted(Formatting.LIGHT_PURPLE);
    }
}
