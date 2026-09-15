package com.soultware.scrollbill.data.usage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PackageMetadataResolutionTest {
    @Test
    fun `launcher index metadata is preferred over later metadata sources`() {
        val selected = mergeMetadataCandidates(
            "com.example.video",
            MetadataCandidate(label = "Indexed video", icon = "indexed-icon"),
            MetadataCandidate(label = "Launcher video", icon = "launcher-icon"),
            MetadataCandidate(label = "Package video", icon = "package-icon"),
        )

        assertEquals("Indexed video", selected.label)
        assertEquals("indexed-icon", selected.icon)
    }

    @Test
    fun `package manager fills an unavailable launcher field`() {
        val selected = mergeMetadataCandidates(
            "com.example.mail",
            MetadataCandidate(label = "Mail", icon = null),
            MetadataCandidate(label = "Package mail", icon = "package-icon"),
        )

        assertEquals("Mail", selected.label)
        assertEquals("package-icon", selected.icon)
    }

    @Test
    fun `package name and neutral icon state are used when metadata is unavailable`() {
        val selected = mergeMetadataCandidates<String>(
            "com.example.unknown",
            null,
            null,
        )

        assertEquals("com.example.unknown", selected.label)
        assertNull(selected.icon)
    }

    @Test
    fun `multiple launcher activities select one deterministically without duplicates`() {
        val selected = selectLauncherMetadata(
            listOf(
                LauncherMetadataEntry(
                    packageName = "com.example.video",
                    activityClassName = "com.example.SecondActivity",
                    metadata = MetadataCandidate("Second", "second"),
                ),
                LauncherMetadataEntry(
                    packageName = "com.example.video",
                    activityClassName = "com.example.FirstActivity",
                    metadata = MetadataCandidate("First", "first"),
                ),
            ),
        )

        assertEquals("First", selected?.label)
        assertEquals("first", selected?.icon)
    }

    @Test
    fun `launcher metadata index has one entry per package`() {
        val index = buildLauncherMetadataIndex(
            listOf(
                LauncherMetadataEntry(
                    packageName = "com.example.video",
                    activityClassName = "com.example.video.SecondActivity",
                    metadata = MetadataCandidate("Second", "second-icon"),
                ),
                LauncherMetadataEntry(
                    packageName = "com.example.mail",
                    activityClassName = "com.example.mail.MainActivity",
                    metadata = MetadataCandidate("Mail", "mail-icon"),
                ),
                LauncherMetadataEntry(
                    packageName = "com.example.video",
                    activityClassName = "com.example.video.FirstActivity",
                    metadata = MetadataCandidate("Video", "video-icon"),
                ),
            ),
        )

        assertEquals(setOf("com.example.video", "com.example.mail"), index.keys)
        assertEquals("Video", index["com.example.video"]?.label)
        assertEquals("video-icon", index["com.example.video"]?.icon)
        assertEquals("Mail", index["com.example.mail"]?.label)
    }
}
