package ink.ptms.chemdah.database

import ink.ptms.chemdah.Chemdah

/**
 * Chemdah
 * ink.ptms.chemdah.database.Type
 *
 * @author sky
 * @since 2021/3/5 3:51 下午
 */
enum class Type {

    LOCAL, SQL, MONGODB;

    companion object {

        val INSTANCE: Type by lazy {
            try {
                valueOf(Chemdah.conf.getString("database.use", "")!!.toUpperCase())
            } catch (ignored: Throwable) {
                LOCAL
            }
        }
    }
}