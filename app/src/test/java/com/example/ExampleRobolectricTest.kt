package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("James Chat", appName)
  }

  @Test
  fun `verify james games integration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val assetStream = context.assets.open("jamesacademy.html")
    assertTrue(assetStream.available() > 0)
    assetStream.close()

    assertEquals("JAMES_GAMES", Screen.JAMES_GAMES.name)
  }
}
