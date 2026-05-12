package dev.parrotstudios.qTotems.listener;

import dev.parrotstudios.qTotems.totems.QTotemRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {
    @EventHandler
    public void onPop(EntityResurrectEvent event){
        if(!(event.getEntity() instanceof Player player)) return;
        if (event.getHand() == null) return;
        QTotemRegistry.handlePop(player, player.getInventory().getItem(event.getHand()));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getRawSlot() != 40) return;
        ItemStack itemPlaced = event.getCursor();
        if (itemPlaced.getType() == Material.AIR) return;
        QTotemRegistry.handleEquip((Player) event.getWhoClicked(), itemPlaced);

    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        ItemStack itemToOffhand = event.getOffHandItem();
        if (itemToOffhand.getType() != Material.AIR) {
            QTotemRegistry.handleEquip(event.getPlayer(), itemToOffhand);
        }
    }

}
