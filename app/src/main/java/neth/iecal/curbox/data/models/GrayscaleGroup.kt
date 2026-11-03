package neth.iecal.curbox.data.models

import java.util.UUID

data class GrayscaleGroup(
val groupId: String = UUID.randomUUID().toString(),
val groupName: String = "",
val packages: HashSet<String> = hashSetOf(),
var timeConfig: AppTimeConfig = AppTimeConfig(),
var isActive: Boolean = true,
// When true, [packages] keep their color and every other app is grayscale
val isWhitelistMode: Boolean = false
) {
    override fun toString(): String {
        return if (isWhitelistMode) "$groupName (all apps except ${packages.size})"
            else "$groupName (${packages.size} apps)"
    }

    /** True when this group turns [packageName] grayscale. Does not check the schedule. */
    fun graysOut(packageName: String): Boolean =
        if (isWhitelistMode) !packages.contains(packageName) else packages.contains(packageName)
}