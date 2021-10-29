package ink.ptms.chemdah.module.synchronous

import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.util.increaseAny
import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.EconomyResponse
import net.milkbowl.vault.economy.EconomyResponse.ResponseType
import org.bukkit.Bukkit
import taboolib.common5.Coerce

/**
 * @author sky
 * @since 2021/7/4 7:46 下午
 */
class SynchronizedVault : AbstractEconomy() {

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getName(): String {
        return "chemdah"
    }

    override fun hasBankSupport(): Boolean {
        return false
    }

    override fun fractionalDigits(): Int {
        return -1
    }

    override fun format(p0: Double): String {
        return Coerce.format(p0).toString()
    }

    override fun currencyNamePlural(): String {
        return ""
    }

    override fun currencyNameSingular(): String {
        return ""
    }

    override fun hasAccount(name: String): Boolean {
        return true
    }

    override fun hasAccount(name: String, p1: String?): Boolean {
        return true
    }

    override fun getBalance(name: String): Double {
        val player = Bukkit.getPlayerExact(name) ?: error("player offline")
        return Coerce.toDouble(player.chemdahProfile.persistentDataContainer[Synchronous.playerDataToVault!!]?.data ?: 0)
    }

    override fun getBalance(name: String, p1: String?): Double {
        return getBalance(name)
    }

    override fun has(name: String, p1: Double): Boolean {
        return getBalance(name) >= p1
    }

    override fun has(name: String, p1: String?, p2: Double): Boolean {
        return getBalance(name) >= p2
    }

    override fun withdrawPlayer(name: String, p1: Double): EconomyResponse {
        return depositPlayer(name, -p1)
    }

    override fun withdrawPlayer(name: String, p1: String?, p2: Double): EconomyResponse {
        return depositPlayer(name, -p2)
    }

    override fun depositPlayer(name: String, p1: Double): EconomyResponse {
        val player = Bukkit.getPlayerExact(name) ?: error("player offline")
        val dataContainer = player.chemdahProfile.persistentDataContainer
        dataContainer[Synchronous.playerDataToVault!!] = dataContainer[Synchronous.playerDataToVault!!].increaseAny(p1)
        return EconomyResponse(p1, 0.0, ResponseType.SUCCESS, "")
    }

    override fun depositPlayer(name: String, p1: String?, p2: Double): EconomyResponse {
        return depositPlayer(name, p2)
    }

    override fun createBank(name: String, p1: String?) = null

    override fun deleteBank(name: String) = null

    override fun bankBalance(name: String) = null

    override fun bankHas(name: String, p1: Double) = null

    override fun bankWithdraw(name: String, p1: Double) = null

    override fun bankDeposit(name: String, p1: Double) = null

    override fun isBankOwner(name: String, p1: String?) = null

    override fun isBankMember(name: String, p1: String?) = null

    override fun getBanks() = emptyList<String>()

    override fun createPlayerAccount(name: String) = true

    override fun createPlayerAccount(name: String, p1: String?) = true
}