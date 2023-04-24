package de.westnordost.streetcomplete.data.osm.edits.create

import de.westnordost.streetcomplete.data.osm.edits.ElementIdProvider
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.changesApplied
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataChanges
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.way
import de.westnordost.streetcomplete.util.math.translate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class CreateNodeFromVertexActionTest {
    private lateinit var repos: MapDataRepository
    private lateinit var provider: ElementIdProvider

    @Before
    fun setUp() {
        repos = mock()
        provider = mock()
    }

    @Test(expected = ConflictException::class)
    fun `conflict when node position changed`() {
        val n = node()
        val n2 = n.copy(position = n.position.translate(1.0, 0.0)) // moved by 1 meter
        on(repos.getNode(n.id)).thenReturn(n2)
        on(repos.getWaysForNode(n.id)).thenReturn(listOf())

        CreateNodeFromVertexAction(n, StringMapChanges(listOf()), listOf())
            .createUpdates(repos, provider)
    }

    @Test(expected = ConflictException::class)
    fun `conflict when node is not part of exactly the same ways as before`() {
        val n = node()
        on(repos.getNode(n.id)).thenReturn(n)
        on(repos.getWaysForNode(n.id)).thenReturn(listOf(way(1), way(2)))

        CreateNodeFromVertexAction(n, StringMapChanges(listOf()), listOf(1L))
            .createUpdates(repos, provider)
    }

    @Test
    fun `create updates`() {
        val n = node()
        on(repos.getNode(n.id)).thenReturn(n)
        on(repos.getWaysForNode(n.id)).thenReturn(listOf(way(1), way(2)))

        val changes = StringMapChanges(listOf(StringMapEntryAdd("a", "b")))

        val data = CreateNodeFromVertexAction(n, changes, listOf(1L, 2L)).createUpdates(repos, provider)

        val n2 = n.changesApplied(changes)

        assertEquals(MapDataChanges(modifications = listOf(n2)), data)
    }
}
