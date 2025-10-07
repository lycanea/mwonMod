package dev.lycanea.mwonmod.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lycanea.mwonmod.Mwonmod;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemUtils {
    public static String getItemID(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        NbtCompound customData = stack.getComponents().getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        String customName = stack.getComponents().getOrDefault(DataComponentTypes.CUSTOM_NAME, NbtComponent.DEFAULT).toString();
        String itemType = stack.getItem().toString();
        if (customData.getCompound("PublicBukkitValues").isEmpty()) return customName; // this is bad for stuff like gold but hey atleast its not crashing now, fix later
        Set<String> dataKeys = customData.getCompound("PublicBukkitValues").get().getKeys();
        NbtCompound bukkitValuse = customData.getCompound("PublicBukkitValues").get();

        if (dataKeys.contains("hypercube:kingbound")) {
            if (Objects.equals(itemType, "minecraft:stone_hoe")) return "royal_scythe";
        }
        if (dataKeys.contains("hypercube:autosmelter")) {
            if (bukkitValuse.getInt("hypercube:searching", -1) > 5) return "divine_hoe";
            if (bukkitValuse.getInt("hypercube:autosmelter", -1) == 1 && bukkitValuse.getInt("hypercube:reforge", -1) == 11 && bukkitValuse.getInt("hypercube:searching", -1) == 5 && bukkitValuse.getInt("hypercube:movement", -1) == 5 && bukkitValuse.getInt("hypercube:gathering", -1) == 5) {
                return "perfect_hoe";}
            return "hoe";
        }
        if (dataKeys.contains("hypercube:crystaltier")) return "enchant_crystal";

        if (Objects.equals(customName, "empty[siblings=[literal{Gold}[style={color=gold,!bold,!italic,!underlined,!strikethrough,!obfuscated}]]]")) return "gold";
        if (Objects.equals(customName, "empty[siblings=[literal{Shard}[style={color=dark_aqua,!bold,!italic,!underlined,!strikethrough,!obfuscated}]]]")) return "shard";
        if (Objects.equals(customName, "empty[siblings=[literal{Compressed Shard}[style={color=dark_aqua,!bold,!italic,!underlined,!strikethrough,!obfuscated}]]]")) return "compressed_shard";
        if (Objects.equals(customName, "empty[siblings=[literal{Grenade}[style={color=red,!bold,!italic,!underlined,!strikethrough,!obfuscated}]]]")) return "grenade";
        if (Objects.equals(customName, "empty[siblings=[literal{Flashbang}[style={color=gray,!bold,!italic,!underlined,!strikethrough,!obfuscated}]]]")) return "flashbang";
        if (Objects.equals(customName, "empty[siblings=[literal{Enchanted Melon Slice}[style={color=white,!bold,!italic,!underlined,!strikethrough,!obfuscated}]]]")) return "enchanted_melon";
        if (Objects.equals(itemType, "minecraft:melon_slice")) return "melon";
        if (Objects.equals(customName, "empty[siblings=[literal{Super Enchanted Melon}[style={color=white,!bold,!italic,!underlined,!strikethrough,!obfuscated}]]]")) return "super_enchanted_melon";
        if (Objects.equals(customName, "empty[siblings=[literal{Ultra Enchanted Melon}[style={color=green,!bold,!italic,!underlined,!strikethrough,!obfuscated}]]]")) return "ultra_enchanted_melon";
        if (Objects.equals(customName, "empty[siblings=[literal{Conquest}[style={color=red,!bold,!italic,!underlined,!strikethrough,!obfuscated}]]]")) return "conquest_start";
        if (Objects.equals(customName, "empty[siblings=[literal{Promote to Guard}[style={color=white,!bold,!italic,!underlined,!strikethrough,!obfuscated}]]]")) return "guard_promote";
        if (Objects.equals(customName, "empty[siblings=[literal{Demote to Citizen}[style={color=white,!bold,!italic,!underlined,!strikethrough,!obfuscated}]]]")) return "guard_demote";

        // gift of gold check
        Pattern pattern = Pattern.compile("empty\\[siblings=\\[literal\\{Level (\\d+)}\\[style=\\{color=gold,!bold,!italic,!underlined,!strikethrough,!obfuscated}]]]");
        Matcher matcher = pattern.matcher(customName);
        if (matcher.find()) return "gog" + matcher.group(1);

        return customName;
    }

    public static ItemStack modifyItemForRendering(ItemStack stack) {
        String itemID = getItemID(stack);
        if (itemID != null) {
            NbtCompound customData = stack.getComponents().getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
            customData.putString("mwonmod_item", itemID);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
        }
        JsonElement itemDataElement = Mwonmod.itemData.get(itemID);
        if (itemDataElement != null) {
            JsonObject itemData = itemDataElement.getAsJsonObject();
            JsonElement itemModelData = itemData.get("custom_model_data");
//            if (itemModelData != null) {
//                stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(itemModelData.getAsInt())); TEXTURE PACK FORMATTING CHANGE, 1.21.8 NEEDS FLOAT LIST FOR CUSTOM MODEL DATA, CHANGE LATER LMFAO
//            }
        }
        stack.set(DataComponentTypes.ITEM_MODEL, Identifier.of("minecraft:dirt"));
        return stack;
    }
}
