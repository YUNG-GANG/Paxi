package com.yungnickyoung.minecraft.paxi.services;

public class FabricModulesLoader implements IModulesLoader {
    @Override
    public void loadModules() {
        // NO-OP -- data pack injection is done via mixin, and resource pack injection is purely client-side
    }
}
