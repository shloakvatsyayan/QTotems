package dev.parrotstudios.qTotems.totems;

import dev.parrotstudios.qTotems.QTotems;
import dev.parrotstudios.qTotems.config.ConfigManager;
import org.bukkit.Keyed;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class QTotemRegistry {
    private static final List<QTotem> qTotems = new ArrayList<>();

    private static final HashMap<UUID,QTotem> activePlayerEquips = new HashMap<>();

    public static List<QTotem> getQTotems(){
        return new ArrayList<>(qTotems);
    }

    public static void add(QTotem qTotem){
        qTotems.add(qTotem);
    }

    public static List<String> getTotemNames(){
        return qTotems.stream().map(QTotem::getName).toList();
    }

    public static boolean isQTotem(ItemStack stack){
        if (stack == null || !stack.hasItemMeta()) return false;
        PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
        return qTotems.stream().map(QTotem::getKey).anyMatch(pdc::has);
    }

    public static void handleEquip(Player player, ItemStack stack){
        if(!isQTotem(stack)) {
            activePlayerEquips.remove(player);
            return;
        }
        Optional<QTotem> qTotem = qTotems.stream().filter(qTotem1 -> {
            PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
            return pdc.has(qTotem1.getKey(), PersistentDataType.BOOLEAN);
        }).findFirst();
        if(qTotem.isEmpty()) return;
        activePlayerEquips.put(player.getUniqueId(), qTotem.get());
    }
    public static void handlePop(Player player, ItemStack stack){
        if(!isQTotem(stack)) {
            return;
        }
        Optional<QTotem> qTotem = qTotems.stream().filter(qTotem1 -> {
            PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
            return pdc.has(qTotem1.getKey(), PersistentDataType.BOOLEAN);
        }).findFirst();
        if(qTotem.isEmpty()) return;
        qTotem.get().providePopEffects(player);
        activePlayerEquips.remove(player.getUniqueId(),qTotem.get());
    }

    public static QTotem getTotem(String totemName){
        return getQTotems().stream().filter(qTotem ->  qTotem.getName().equals(totemName)).findFirst().orElse(null);
    }

    public static void populate(){
        ConfigManager.getSection("totems").getKeys(false).forEach(qtotem -> {
            QTotem totem = QTotem.create(qtotem)
                    .displayName(ConfigManager.getString("totems."+qtotem+".name"))
                    .lore(ConfigManager.getStringList("totems."+qtotem+".lore"));
            List<String> popEffects =  ConfigManager.getStringList("totems."+qtotem+".popEffects");
            List<String> equipEffects =  ConfigManager.getStringList("totems."+qtotem+".equipEffects");
            popEffects.forEach(effect -> {
                String[] split = effect.split(";");
                totem.addPopEffect(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            });
            equipEffects.forEach(effect -> {
                String[] split = effect.split(";");
                totem.addEquipEffect(split[0], Integer.parseInt(split[1]));
            });
            totem.register();
        });
    }

    public static void reload(){
        qTotems.clear();
        activePlayerEquips.clear();
        populate();
    }


}
