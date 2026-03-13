package com.presently.sharing

import com.google.common.truth.Truth.assertThat
import com.presently.sharing.data.SharingViewDesign
import com.presently.sharing.view.DesignListAdapter
import org.junit.Test

class DesignDiffUtilTest {

    @Test
    fun `GIVEN DesignDiffUtil WHEN areItemsTheSame is called AND items are not the same THEN return false`() {
        val diffUtil = DesignListAdapter.DesignDiffUtil()

        val itemOne = SharingViewDesign("id", 0, 0, 0)
        val itemTwo = SharingViewDesign("id1", 0, 0, 0)
        val actual = diffUtil.areContentsTheSame(itemOne, itemTwo)

        assertThat(actual).isFalse()
    }

    @Test
    fun `GIVEN DesignDiffUtil WHEN areItemsTheSame is called AND items are the same THEN return true`() {
        val diffUtil = DesignListAdapter.DesignDiffUtil()

        val item = SharingViewDesign("id", 0, 0, 0)
        val actual = diffUtil.areContentsTheSame(item, item)

        assertThat(actual).isTrue()
    }

    @Test
    fun `GIVEN DesignDiffUtil WHEN areContentsTheSame is called AND items are different THEN return false`() {
        val diffUtil = DesignListAdapter.DesignDiffUtil()

        val itemOne = SharingViewDesign("id", 0, 0, 0)
        val itemTwo = SharingViewDesign("id3", 0, 0, 0)
        val actual = diffUtil.areContentsTheSame(itemOne, itemTwo)

        assertThat(actual).isFalse()
    }

    @Test
    fun `GIVEN DesignDiffUtil WHEN areContentsTheSame is called AND same item has changed THEN return true`() {
        val diffUtil = DesignListAdapter.DesignDiffUtil()

        val itemOne = SharingViewDesign("id", 0, 0, 0)
        val itemTwo = SharingViewDesign("id", 1, 1, 1)
        val actual = diffUtil.areContentsTheSame(itemOne, itemTwo)

        assertThat(actual).isTrue()
    }
}