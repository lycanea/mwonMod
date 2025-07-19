package dev.lycanea.mwonmod.util;

import java.util.Map;

public record InventoryScanResult(Map<String, Integer> itemCounts, Map<String, Integer> itemSlots, int emptySlots) {
}
