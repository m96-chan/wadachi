package io.github.m96chan.wadachi

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {
    @Test
    fun platformHasName() {
        assertTrue(getPlatform().name.isNotBlank())
    }
}
