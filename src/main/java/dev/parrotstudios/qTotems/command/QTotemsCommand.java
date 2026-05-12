package dev.parrotstudios.qTotems.command;

import dev.parrotstudios.qTotems.QTotems;
import dev.parrotstudios.qTotems.config.ConfigManager;
import dev.parrotstudios.qTotems.totems.QTotem;
import dev.parrotstudios.qTotems.totems.QTotemRegistry;
import dev.parrotstudios.qTotems.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QTotemsCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Utils.textWithPrefix("<red>Only players can use this command!"));
            return true;
        }
        if(args.length == 0 || args.length > 2) {
            player.sendMessage(Utils.textWithPrefix("<yellow>Usage: /qtotems <totem> {player}"));
            return true;
        }
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("reload")){
                ConfigManager.reloadConfig();
                QTotemRegistry.reload();
                player.sendMessage(Utils.textWithPrefix("<green>Reloaded config!"));
                return true;
            }
            QTotem totem = QTotemRegistry.getTotem(args[0]);
            if(totem == null) {
                player.sendMessage(Utils.textWithPrefix("<red>Invalid totem!"));
                return true;
            }
            player.getInventory().addItem(totem.getTotemItem());
            player.sendMessage(Utils.textWithPrefix("<green>Gave you a custom totem!"));
            return true;
        }
        QTotem totem = QTotemRegistry.getTotem(args[0]);
        Player target = QTotems.getInstance().getServer().getPlayer(args[1]);
        if(totem == null) {
            player.sendMessage(Utils.textWithPrefix("<red>Invalid totem!"));
            return true;
        }
        if(target == null){
            player.sendMessage(Utils.textWithPrefix("<red>Invalid target!"));
            return true;
        }

        target.getInventory().addItem(totem.getTotemItem());
        target.sendMessage(Utils.textWithPrefix("<green>Gave you a custom totem!"));
        player.sendMessage(Utils.textWithPrefix("<green>Gave %target% a custom totem!".replace("%target%", target.getName())));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length == 1){
            return QTotemRegistry.getTotemNames()
                    .stream()
                    .filter(totemName -> totemName.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if(args.length == 2){
            if(!QTotemRegistry.getTotemNames().contains(args[0].toLowerCase())) return List.of();
            return QTotems.getInstance().getServer().getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        }
        return List.of();
    }
}
