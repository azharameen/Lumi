package com.example

import com.example.data.remote.FirebaseAiCloudEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LumiFirebaseAiCloudTest {

    @Test
    fun testFirebaseAiCloudEngineSingleton() {
        val instance1 = FirebaseAiCloudEngine.getInstance()
        val instance2 = FirebaseAiCloudEngine.getInstance()

        assertNotNull(instance1)
        assertEquals(instance1, instance2)
    }

    @Test
    fun testDefaultModelConfiguration() {
        assertEquals("gemini-2.5-flash", FirebaseAiCloudEngine.DEFAULT_MODEL)
    }
}
