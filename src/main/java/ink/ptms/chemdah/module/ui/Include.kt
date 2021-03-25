package ink.ptms.chemdah.module.ui

import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.module.ui.Include
 *
 * @author sky
 * @since 2021/3/24 8:38 上午
 */
class Include(val id: String, activeItem: ItemStack, normalItem: ItemStack) {

    val activeItem = activeItem
        get() = field.clone()

    val normalItem = normalItem
        get() = field.clone()
}