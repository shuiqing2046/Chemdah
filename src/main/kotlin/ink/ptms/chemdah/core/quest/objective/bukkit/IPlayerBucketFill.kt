package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import org.bukkit.event.player.PlayerBucketFillEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerBucketFill
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerBucketFill : APlayerBucket<PlayerBucketFillEvent>() {

    override val name = "bucket fill"
    override val event = PlayerBucketFillEvent::class.java
}