package dev.parrotstudios.qTotems.totems;

import dev.parrotstudios.qTotems.QTotems;
import dev.parrotstudios.qTotems.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Registry;
import java.util.HashMap;
import java.util.List;

public class QTotem {
    private final String name;
    private final ItemStack totemItem;
    private final ItemMeta totemMeta;
    private final NamespacedKey key;
    private final HashMap<PotionEffectType, Integer> equipEffects = new HashMap<>();
    public record PopEffect(PotionEffectType type, int level, int duration) {}
    private final List<PopEffect> popEffects = new java.util.ArrayList<>();
    public static QTotem create(String name){
        return new QTotem(name);
    }

    private QTotem(String name){
        this.name = name;
        totemItem = new ItemStack(Material.TOTEM_OF_UNDYING);
        totemMeta = totemItem.getItemMeta();
        key = new NamespacedKey(QTotems.getInstance(), name);
        totemMeta.setEnchantmentGlintOverride(true);
        totemMeta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
    }


    public String getName() {
        return name;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public ItemStack getTotemItem(){
        return totemItem.clone();
    }

    public HashMap<PotionEffectType, Integer> getEquipEffects() {
        return new HashMap<>(equipEffects);
    }

    public List<PopEffect> getPopEffects() {
        return List.copyOf(popEffects);
    }

    public QTotem displayName(String name){
        totemMeta.displayName(Utils.text(name));
        return this;
    }

    public QTotem lore(List<String> lore){
        List<Component> loreFormat = lore.stream().map(Utils::text).toList();
        totemMeta.lore(loreFormat);
        return this;
    }

    public QTotem addEquipEffect(String potionEffectName, int level){
        PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(potionEffectName));
        if(type == null){
            QTotems.getInstance().getLogger().warning("Invalid pop effect name: " + potionEffectName + " for totem: " + this.getName());
            return this;
        }
        equipEffects.put(type, level);
        return this;
    }

    public QTotem addPopEffect(String potionEffectName, int level, int duration){
        PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(potionEffectName));
        if(type == null){
            QTotems.getInstance().getLogger().warning("Invalid pop effect name: " + potionEffectName + " for totem: " + this.getName());
            return this;
        }
        popEffects.add(new PopEffect(type, level, duration));
        return this;
    }

    public void provideEquipEffects(Player player){
        this.getEquipEffects().forEach((type, level) -> player.addPotionEffect(new PotionEffect(type, 40, level)));
    }

    public void providePopEffects(Player player){
        this.getPopEffects().forEach(popEffect ->
                player.addPotionEffect(new PotionEffect(popEffect.type, popEffect.duration, popEffect.level)));
    }

    public void register(){
        totemItem.setItemMeta(totemMeta);
        QTotemRegistry.add(this);
    }

}
