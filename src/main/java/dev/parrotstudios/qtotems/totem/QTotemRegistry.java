package dev.parrotstudios.qtotems.totem;

import dev.parrotstudios.qtotems.QTotems;
import dev.parrotstudios.qtotems.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Central registry that manages all registered custom totems
 * and keeps track of active totem equip/pop updates for players.
 */
public class QTotemRegistry {
    private static final List<QTotem> qTotems = new ArrayList<>();

    private static final HashMap<UUID,QTotem> activePlayerEquips = new HashMap<>();

    /**
     * Gets a copy list of all registered custom totems.
     *
     * @return List of QTotems.
     */
    public static List<QTotem> getQTotems(){
        return new ArrayList<>(qTotems);
    }

    /**
     * Adds and registers a new custom totem to the system cache.
     *
     * @param qTotem The QTotem instance.
     */
    public static void add(QTotem qTotem){
        qTotems.add(qTotem);
    }

    /**
     * Retrieves the names of all registered totems.
     *
     * @return List of registered totem names.
     */
    public static List<String> getTotemNames(){
        return qTotems.stream().map(QTotem::getName).toList();
    }

    public static Map<UUID,QTotem> getActivePlayerEquips(){
        return Map.copyOf(activePlayerEquips);
    }

    /**
     * Checks if the given item stack is a registered custom totem.
     *
     * @param stack The item stack to check.
     * @return true if it is a custom totem, false otherwise.
     */
    public static boolean isQTotem(ItemStack stack){
        if (stack == null || !stack.hasItemMeta()) return false;
        PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
        return qTotems.stream().map(QTotem::getKey).anyMatch(pdc::has);
    }

    /**
     * Safely clears previous passive equip effects from the player.
     *
     * @param player Target player.
     */
    public static void clearPastEffects(Player player){
        QTotem active = activePlayerEquips.get(player.getUniqueId());
        activePlayerEquips.remove(player.getUniqueId());
        if (active != null) {
            try{
                active.removeEquipEffects(player);
            }
            catch(Exception e){
                player.clearActivePotionEffects();
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles equipping a custom totem.
     * Removes old equip effects, validates the item, and applies the new totem's passive effects.
     *
     * @param player Target player.
     * @param stack The item stack equipped.
     */
    public static void handleEquip(Player player, ItemStack stack){
        clearPastEffects(player);
        if(!isQTotem(stack)) {
            return;
        }
        Optional<QTotem> qTotem = qTotems.stream().filter(qTotem1 -> {
            PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
            return pdc.has(qTotem1.getKey(), PersistentDataType.BOOLEAN);
        }).findFirst();
        if(qTotem.isEmpty()) return;
        qTotem.get().provideEquipEffects(player);
        activePlayerEquips.put(player.getUniqueId(), qTotem.get());

    }

    /**
     * Handles popping/consuming a custom totem.
     * Triggers the totem's pop effects and updates the player's active equip tracking.
     *
     * @param player Target player.
     * @param stack The totem item popped.
     */
    public static void handlePop(Player player, ItemStack stack){
        if(!isQTotem(stack)) {
            return;
        }
        Optional<QTotem> qTotem = qTotems.stream().filter(qTotem1 -> {
            PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
            return pdc.has(qTotem1.getKey(), PersistentDataType.BOOLEAN);
        }).findFirst();
        if(qTotem.isEmpty()) return;
        QTotems.getInstance().getServer().getScheduler().runTaskLater(QTotems.getInstance(),()->
                qTotem.get().providePopEffects(player),1L);
        activePlayerEquips.remove(player.getUniqueId(),qTotem.get());
    }

    /**
     * Cleans up player totem status and removes active effects when they disconnect.
     *
     * @param player Target player.
     */
    public static void handleLeave(Player player){
        clearPastEffects(player);
        activePlayerEquips.remove(player.getUniqueId());
    }

    /**
     * Re-evaluates and applies totem effects when a player joins the server.
     *
     * @param player Target player.
     */
    public static void handleJoin(Player player){
        clearPastEffects(player);
        ItemStack stack = player.getInventory().getItemInOffHand();
        if(!isQTotem(stack)) {
            return;
        }
        Optional<QTotem> qTotem = qTotems.stream().filter(qTotem1 -> {
            PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
            return pdc.has(qTotem1.getKey(), PersistentDataType.BOOLEAN);
        }).findFirst();
        if(qTotem.isEmpty()) return;
        qTotem.get().provideEquipEffects(player);
        activePlayerEquips.put(player.getUniqueId(), qTotem.get());

    }

    /**
     * Forces re-application of passive equip effects when active effects are changed.
     * Useful when other plugins or actions clear player potion effects.
     *
     * @param player Target player.
     */
    public static void handleEffectChange(Player player){
        if(!activePlayerEquips.containsKey(player.getUniqueId())) return;
        activePlayerEquips.remove(player.getUniqueId());
        ItemStack stack = player.getInventory().getItemInOffHand();
        if(!isQTotem(stack)) {
            return;
        }
        Optional<QTotem> qTotem = qTotems.stream().filter(qTotem1 -> {
            PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
            return pdc.has(qTotem1.getKey(), PersistentDataType.BOOLEAN);
        }).findFirst();
        if(qTotem.isEmpty()) return;
        QTotems.getInstance().getServer().getScheduler().runTaskLater(QTotems.getInstance(),()->
                qTotem.get().provideEquipEffects(player),1L);
        activePlayerEquips.put(player.getUniqueId(), qTotem.get());
    }


    /**
     * Resolves a registered custom totem by its name ID.
     *
     * @param totemName Unique key name of the totem.
     * @return The registered QTotem, or null if not found.
     */
    public static QTotem getTotem(String totemName){
        return getQTotems().stream().filter(qTotem ->  qTotem.getName().equals(totemName)).findFirst().orElse(null);
    }

    /**
     * Reads all active totem configurations from config.yml, parses effects,
     * builds QTotem items, and registers them.
     */
    public static void populate(){
        ConfigManager.getSection("totems").getKeys(false).forEach(qTotem -> {
            try{
                if(!ConfigManager.getBoolean("totems."+qTotem+".enabled",true)) {
                    return;
                }
                QTotem totem = QTotem.create(qTotem)
                        .displayName(ConfigManager.getString("totems."+qTotem+".name"))
                        .lore(ConfigManager.getStringList("totems."+qTotem+".lore"));
                List<String> popEffects =  ConfigManager.getStringList("totems."+qTotem+".popEffects");
                List<String> equipEffects =  ConfigManager.getStringList("totems."+qTotem+".equipEffects");
                popEffects.forEach(effect -> {
                    String[] split = effect.split(";");
                    totem.addPopEffect(split[0].toLowerCase(), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                });
                equipEffects.forEach(effect -> {
                    String[] split = effect.split(";");
                    totem.addEquipEffect(split[0].toLowerCase(), Integer.parseInt(split[1]));
                });
                totem.register();
                QTotems.getInstance().getLogger().info("Registered Qtotem: "+ qTotem);
            } catch (Exception e) {
                QTotems.getInstance().getLogger().warning("Invalid configuration for Qtotem: "+ qTotem);
                e.printStackTrace();
            }

        });
    }

    /**
     * Resets the registry cache, clears active players, and re-populates configurations.
     */
    public static void reload(){
        qTotems.clear();
        populate();
        getActivePlayerEquips().forEach((uuid, _) -> {
            Player player = QTotems.getInstance().getServer().getPlayer(uuid);
            if(player != null){
                handleEquip(player,player.getInventory().getItemInOffHand());
            }
        });

    }
}
