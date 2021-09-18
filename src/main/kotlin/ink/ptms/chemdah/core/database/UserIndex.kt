package ink.ptms.chemdah.core.database

import ink.ptms.chemdah.Chemdah

/**
 * Chemdah
 * ink.ptms.chemdah.core.database.UserIndex
 *
 * @author sky
 * @since 2021/9/18 10:56 下午
 */
enum class UserIndex {

    NAME, UUID;

    companion object {

        val INSTANCE: UserIndex by lazy {
            try {
                valueOf(Chemdah.conf.getString("database.user-index", "")!!.uppercase())
            } catch (ignored: Throwable) {
                UUID
            }
        }
    }
}