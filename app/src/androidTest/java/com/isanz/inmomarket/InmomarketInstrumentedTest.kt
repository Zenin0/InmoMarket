package com.isanz.inmomarket

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.isanz.inmomarket.ui.add.AddViewModel
import com.isanz.inmomarket.ui.chat.ChatViewModel
import com.isanz.inmomarket.ui.home.HomeViewModel
import com.isanz.inmomarket.ui.profile.ProfileViewModel
import com.isanz.inmomarket.ui.search.SearchViewModel
import com.isanz.inmomarket.ui.settings.SettingsViewModel
import com.isanz.inmomarket.utils.entities.Message
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Method

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InmomarketInstrumentedTest {
    @Test
    fun viewModelsNotNull() {
        val chatViewModel = ChatViewModel()
        assertNotNull(chatViewModel)
        val homeViewModel = HomeViewModel()
        assertNotNull(homeViewModel)
        val addViewModel = AddViewModel()
        assertNotNull(addViewModel)
        val searchViewModel = SearchViewModel()
        assertNotNull(searchViewModel)
        val profileViewModel = ProfileViewModel()
        assertNotNull(profileViewModel)
        val settingsViewModel = SettingsViewModel()
        assertNotNull(settingsViewModel)
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.isanz.inmomarket", appContext.packageName)
    }

    @Test
    fun chatMessageHasRightFormat() {
        val chatViewModel = ChatViewModel()
        val createMesssageMetoth: Method = ChatViewModel::class.java.getDeclaredMethod("createMessage", String::class.java, String::class.java)
        createMesssageMetoth.isAccessible = true
        val message : Message = createMesssageMetoth.invoke(chatViewModel, "Hello", "123") as Message
        assertEquals("Hello", message.message )
        assertEquals("123", message.senderId)
    }
}
