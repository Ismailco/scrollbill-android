package com.soultware.scrollbill.data.usage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PackageMetadataResolutionTest {
    @Test
    fun `launcher metadata is preferred over package manager metadata`() {
        val selected = mergeMetadataCandidates(
            packageName = "com.example.video",
            launcher = MetadataCandidate(label = "Video", icon = "launcher-icon"),
            packageManager = MetadataCandidate(label = "Package video", icon = "package-icon"),
        )

        assertEquals("Video", selected.label)
        assertEquals("launcher-icon", selected.icon)
    }

    @Test
    fun `package manager fills an unavailable launcher field`() {
        val selected = mergeMetadataCandidates(
            packageName = "com.example.mail",
            launcher = MetadataCandidate(label = "Mail", icon = null),
            packageManager = MetadataCandidate(label = "Package mail", icon = "package-icon"),
        )

        assertEquals("Mail", selected.label)
        assertEquals("package-icon", selected.icon)
    }

    @Test
    fun `package name and neutral icon state are used when metadata is unavailable`() {
        val selected = mergeMetadataCandidates<String>(
            packageName = "com.example.unknown",
            launcher = null,
            packageManager = null,
        )

        assertEquals("com.example.unknown", selected.label)
        assertNull(selected.icon)
    }

    @Test
    fun `multiple launcher activities select one deterministically without duplicates`() {
        val selected = selectLauncherMetadata(
            listOf(
                LauncherMetadataEntry("com.example.SecondActivity", MetadataCandidate("Second", "second")),
                LauncherMetadataEntry("com.example.FirstActivity", MetadataCandidate("First", "first")),
            ),
        )

        assertEquals("First", selected?.label)
        assertEquals("first", selected?.icon)
    }
}
