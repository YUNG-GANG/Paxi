package com.yungnickyoung.minecraft.paxi;

import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Custom ResourcePackSource for Paxi-loaded packs.
 * As a result, Paxi-loaded packs will be printed with a unique light purple color.
 */
public interface PaxiResourcePackSource extends IPackNameDecorator {
    IPackNameDecorator PACK_SOURCE_PAXI = paxiText();

    static IPackNameDecorator paxiText() {
        ITextComponent text = new StringTextComponent("paxi");
        return (text2) -> (new TranslationTextComponent("pack.nameAndSource", text2, text)).mergeStyle(TextFormatting.LIGHT_PURPLE);
    }
}
