package com.soultware.scrollbill.domain.usage

data class UsageExclusionPolicy(
    val excludedPackages: Set<String>,
) {
    fun excludes(packageName: String): Boolean = packageName in excludedPackages

    companion object {
        fun forApplication(
            ownPackageName: String,
            homePackageName: String?,
        ): UsageExclusionPolicy = UsageExclusionPolicy(
            excludedPackages = buildSet {
                add(ownPackageName)
                add(ANDROID_PACKAGE)
                add(SYSTEM_UI_PACKAGE)
                homePackageName?.takeIf { it.isNotBlank() }?.let(::add)
            },
        )

        private const val ANDROID_PACKAGE = "android"
        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"
    }
}
