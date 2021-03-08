package ink.ptms.chemdah.module.level

/**
 * Chemdah
 * ink.ptms.chemdah.module.level.Level
 *
 * @author sky
 * @since 2021/3/8 11:20 下午
 */
class Level(val algorithm: Algorithm, experience: Int, level: Int) {

    var experience = experience
        private set

    var level = level
        private set

    fun setExperience(value: Int) {
        experience = value
        addExperience(0)
    }

    fun addExperience(value: Int) {
        var level = this.level
        if (level >= algorithm.maxLevel) {
            return
        }
        var exp = this.experience + value
        var expNextLevel = algorithm.getExp(level)
        while (exp >= expNextLevel) {
            level += 1
            exp -= expNextLevel
            expNextLevel = algorithm.getExp(level)
        }
        if (level >= algorithm.maxLevel) {
            this.level = algorithm.maxLevel
            this.experience = expNextLevel
        } else {
            this.level = level
            this.experience = exp
        }
    }

    abstract class Algorithm {

        abstract val maxLevel: Int

        abstract fun getExp(level: Int): Int
    }
}