package net.dungeonz.util;

import java.util.List;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class InventoryHelper {

    public static boolean hasRequiredItemStacks(PlayerInventory playerInventory, List<ItemStack> requiredItemStacks) {
        for (int i = 0; i < requiredItemStacks.size(); i++) {
            int requiredCount = requiredItemStacks.get(i).getCount();
            for (int u = 0; u < playerInventory.main.size(); u++) {
                if (ItemStack.areItemsEqual(playerInventory.main.get(u), requiredItemStacks.get(i))) {
                    requiredCount -= playerInventory.main.get(u).getCount();
                    if (requiredCount <= 0) {
                        break;
                    }
                }
            }
            if (requiredCount > 0) {
                return false;
            }
        }
        return true;
    }

    public static void decrementRequiredItemStacks(PlayerInventory playerInventory, List<ItemStack> requiredItemStacks) {
        if (!requiredItemStacks.isEmpty()) {
            for (int i = 0; i < requiredItemStacks.size(); i++) {
                int requiredCount = requiredItemStacks.get(i).getCount();
                for (int u = 0; u < playerInventory.main.size(); u++) {
                    if (ItemStack.areItemsEqual(playerInventory.main.get(u), requiredItemStacks.get(i))) {
                        requiredCount -= playerInventory.main.get(u).getCount();
                        playerInventory.main.get(u).decrement(requiredCount);
                        if (requiredCount <= 0) {
                            break;
                        }
                    }
                }
            }
        }
    }

}
