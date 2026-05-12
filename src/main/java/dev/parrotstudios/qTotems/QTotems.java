package dev.parrotstudios.qTotems;

import dev.parrotstudios.qTotems.command.QTotemsCommand;
import dev.parrotstudios.qTotems.config.ConfigManager;
import dev.parrotstudios.qTotems.listener.EventListener;
import dev.parrotstudios.qTotems.totems.QTotemRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class QTotems extends JavaPlugin {

    public static QTotems getInstance(){
        return JavaPlugin.getPlugin(QTotems.class);
    }

    @Override
    public void onEnable() {
        ConfigManager.init(this);
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        Objects.requireNonNull(getCommand("totems")).setExecutor(new QTotemsCommand());
        QTotemRegistry.populate();
        getLogger().info("Plugins is enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugins is disabled.");
    }
}
