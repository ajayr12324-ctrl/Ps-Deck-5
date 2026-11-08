package com.example

import com.example.model.AppItem
import com.example.model.LauncherTab
import com.example.model.TrophyProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun appItem_modelTest() {
    val trophies = TrophyProgress(
      platinum = 1,
      gold = 5,
      silver = 10,
      bronze = 20,
      totalEarned = 36,
      completionPercent = 80
    )
    val app = AppItem(
      id = "com.test.game",
      name = "Test Game",
      packageName = "com.test.game",
      tab = LauncherTab.GAMES,
      trophyProgress = trophies,
      isCustomGame = true
    )

    assertEquals("Test Game", app.name)
    assertEquals(LauncherTab.GAMES, app.tab)
    assertEquals(80, app.trophyProgress.completionPercent)
    assertEquals(1, app.trophyProgress.platinum)
    assertTrue(app.isCustomGame)
  }

  @Test
  fun tabEnum_verification() {
    assertEquals(LauncherTab.GAMES, LauncherTab.valueOf("GAMES"))
    assertEquals(LauncherTab.MEDIA, LauncherTab.valueOf("MEDIA"))
  }
}
