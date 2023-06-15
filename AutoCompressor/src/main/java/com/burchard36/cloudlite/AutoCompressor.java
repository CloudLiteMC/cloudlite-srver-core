package com.burchard36.cloudlite;

import com.burchard36.cloudlite.config.AutoCompressorConfig;
import com.burchard36.cloudlite.config.AutoCompressorMaterial;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Objects;

public class AutoCompressor {
    protected final Permission permission;
    protected final AutoCompressorConfig compressorConfig;
    protected final AutoCompressorModule moduleInstance;

    public AutoCompressor(final AutoCompressorModule moduleInstance) {
        this.moduleInstance = moduleInstance;
        this.compressorConfig = this.moduleInstance.getAutoCompressorConfig();
        this.permission = this.moduleInstance.getPluginInstance().getVaultPermissions();
    }

    /**
     * Simple boolean method to prevent duplicate code for checking is an item is able to be
     * compressed
     * @param toProtect ItemStack to check
     * @return true if the ItemStack isn't null and is 64
     */
    public boolean isNotSafe(ItemStack toProtect) {
        if (toProtect == null) return true;
        if (toProtect.getItemMeta() == null) return true;
        return toProtect.getAmount() != 64;
    }

    public boolean isVanilla(ItemStack itemStack) {
        return Objects.requireNonNull(itemStack.getItemMeta()).getPersistentDataContainer().isEmpty();
    }

    public boolean canCompressMaterial(Player player, Material material, boolean ignoreEnabled) {
        final CompressorPlayer compressorPlayer = new CompressorPlayer(player);
        final AutoCompressorMaterial materialData = this.compressorConfig.getCompressMaterialData(material);
        assert materialData != null;
        return materialData.canCompress(compressorPlayer);
    }
    public boolean canSuperCompress(Player player, Material material, boolean ignoreEnabled) {
        final CompressorPlayer compressorPlayer = new CompressorPlayer(player);
        final AutoCompressorMaterial materialData = this.compressorConfig.getCompressMaterialData(material);
        assert materialData != null;
        return materialData.canSuperCompress(compressorPlayer);
    }

    public boolean canMegaCompress(Player player, Material material, boolean ignoreEnabled) {
        final CompressorPlayer compressorPlayer = new CompressorPlayer(player);
        final AutoCompressorMaterial materialData = this.compressorConfig.getCompressMaterialData(material);
        assert materialData != null;
        return materialData.canMegaCompress(compressorPlayer);
    }

    public final Material getCompressedOriginMaterial(ItemStack compressedStack) {
        return this.compressorConfig.fromItem(compressedStack).getType();
    }

    public void compressPlayer(final Player player, boolean ignoreEnabled) {
        final PlayerInventory theInventory = player.getInventory();
        final CompressorPlayer compressorPlayer = new CompressorPlayer(player);

        for (ItemStack anItem : theInventory.getContents()) {
            if (this.isNotSafe(anItem)) continue;
            AutoCompressorMaterial compressorMaterial = this.compressorConfig.getCompressMaterialData(anItem.getType());

            if (this.isVanilla(anItem)
                    && compressorMaterial != null
                    && (compressorMaterial.hasCompressorEnabled(compressorPlayer) || ignoreEnabled)) { // ItemStack is vanilla
                this.removeAndAdd(theInventory,anItem, compressorMaterial.getCompressedItem());
            }

            compressorMaterial = this.compressorConfig.getCompressMaterialData(anItem);
            if (compressorMaterial == null) continue;
            if (compressorMaterial.isCompressed(anItem)
                    && (compressorMaterial.hasSuperCompressorEnabled(compressorPlayer) || ignoreEnabled)) {
                this.removeAndAdd(theInventory, anItem, compressorMaterial.getSuperCompressedItem());
            }

            if (compressorMaterial.isSuperCompressed(anItem)
                    && (compressorMaterial.hasMegaCompressorEnabled(compressorPlayer) || ignoreEnabled)) {
                this.removeAndAdd(theInventory, anItem, compressorMaterial.getMegaCompressedItem());
            }
        }
    }

    protected void removeAndAdd(final PlayerInventory theInventory, final ItemStack toRemove, final ItemStack toAdd) {
        theInventory.remove(toRemove);
        theInventory.addItem(toAdd);
    }

}
