package com.example

import com.example.data.firebase.LumiAppCheckManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LumiAppCheckTest {

    @Test
    fun testAppCheckManagerSingletonInstance() {
        val instance1 = LumiAppCheckManager.getInstance()
        val instance2 = LumiAppCheckManager.getInstance()
        
        assertNotNull(instance1)
        assertEquals(instance1, instance2)
    }

    @Test
    fun testInitialState() {
        val manager = LumiAppCheckManager.getInstance()
        assertNotNull(manager.isInitialized)
        assertNotNull(manager.providerName)
        assertNotNull(manager.latestToken)
        assertNotNull(manager.statusMessage)
    }
}
