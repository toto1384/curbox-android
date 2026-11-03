package neth.iecal.curbox.data.models

import java.util.UUID

data class GrayscaleGroup(
val groupId: String = UUID.randomUUID().toString(),
val groupName: String = "",
val packages: HashSet<String> = hashSetOf(),
var timeConfig: AppTimeConfig = AppTimeConfig(),
var isActive: Boolean = true,
// When true, the group applies to every app and [packages] is ignored
val allApps: Boolean = false
) {
    override fun toString(): String {
        return if (allApps) "$groupName (all apps)" else "$groupName (${packages.size} apps)"
    }

    fun appliesTo(packageName: String): Boolean = allApps || packages.contains(packageName)
}