package com.vergilprime.angelinventories.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class InventorySerializer {

    public static byte[] itemsToBytes(ItemStack... items) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        GZIPOutputStream compression = new GZIPOutputStream(bytes);
        BukkitObjectOutputStream out = new BukkitObjectOutputStream(compression);
        out.writeInt(items.length);
        for (ItemStack item : items) {
            out.writeObject(item);
        }
        out.close();
        return bytes.toByteArray();
    }

    public static ItemStack[] bytesToItems(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bytes = new ByteArrayInputStream(data);
        GZIPInputStream decompression = new GZIPInputStream(bytes);
        BukkitObjectInputStream in = new BukkitObjectInputStream(decompression);
        ItemStack[] items = new ItemStack[in.readInt()];
        for (int i = 0; i < items.length; i++) {
            items[i] = (ItemStack) in.readObject();
        }
        return items;
    }

}