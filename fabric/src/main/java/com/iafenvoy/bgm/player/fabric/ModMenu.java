package com.iafenvoy.bgm.player.fabric;

import com.iafenvoy.bgm.player.screen.BgmScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return BgmScreen::new;
    }
}
