package com.elink.aigallery

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GalleryPermissionTest {

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun permissionEmptyStateAndRequestFlow() {
        TestPermissions.revokeMediaPermissions(device)
        clearScanPrefs(InstrumentationRegistry.getInstrumentation().targetContext)

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            onView(withId(R.id.empty_state)).check(matches(isDisplayed()))
            onView(withId(R.id.request_permission_button)).perform(click())
            SystemDialogHelper.waitForPermissionDialog(device)
            device.pressBack()
            waitForAppForeground(device, InstrumentationRegistry.getInstrumentation().targetContext.packageName)

            onView(withId(R.id.request_permission_button))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.empty_message))
                .check(matches(withText(R.string.gallery_permission_hint)))
        } finally {
            scenario.close()
        }
    }
}
