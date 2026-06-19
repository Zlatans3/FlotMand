package dk.zlatan.flotmand.util

import dk.zlatan.flotmand.model.Group
import dk.zlatan.flotmand.model.RotationMonth
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object RotationCalculator {

    fun currentMonthId(timezone: String = "Europe/Copenhagen"): String =
        YearMonth.now(ZoneId.of(timezone)).toString()

    fun nextMonthId(timezone: String = "Europe/Copenhagen"): String =
        YearMonth.now(ZoneId.of(timezone)).plusMonths(1).toString()

    fun getDefaultHostId(group: Group, targetMonthId: String): String? {
        if (group.rotationOrder.isEmpty() || group.anchorMonth.isBlank()) return null
        val anchor = YearMonth.parse(group.anchorMonth)
        val target = YearMonth.parse(targetMonthId)
        val diff = anchor.until(target, ChronoUnit.MONTHS).toInt()
        val size = group.rotationOrder.size
        val index = ((group.anchorIndex + diff) % size + size) % size
        return group.rotationOrder.getOrNull(index)
    }

    // Returns null when the slot is vacant (no host), override host when claimed, default host otherwise.
    fun resolveHostId(group: Group, monthId: String, override: RotationMonth?): String? =
        when (override?.status) {
            RotationMonth.Status.CLAIMED -> override.overrideHostId
            RotationMonth.Status.VACANT -> null
            else -> getDefaultHostId(group, monthId)
        }
}
