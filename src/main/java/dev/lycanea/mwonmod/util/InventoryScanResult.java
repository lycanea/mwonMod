package dev.lycanea.mwonmod.util;

import net.minecraft.item.Item;

import java.util.Map;

public record InventoryScanResult(Map<Item, Integer> itemCounts, Map<Item, Integer> itemSlots, int emptySlots) {
}
