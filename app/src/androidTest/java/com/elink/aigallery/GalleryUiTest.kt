package com.elink.aigallery

import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import android.view.View
import androidx.appcompat.widget.SearchView
import com.elink.aigallery.data.db.AppDatabase
import com.elink.aigallery.data.db.FaceEmbedding
import com.elink.aigallery.data.db.ImageTag
import com.elink.aigallery.data.db.MediaItem
import com.elink.aigallery.data.db.PersonEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.allOf

@RunWith(AndroidJUnit4::class)
class GalleryUiTest {

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        TestPermissions.grantMediaPermissions(device)
    }

    @Test
    fun tabsAndMainNavigationToPhoto() {
        val (scenario, images) = launchWithImages("ElinkFolderMain", 2)
        try {
            onView(withId(R.id.view_pager)).check(matches(isDisplayed()))
            onView(withId(R.id.btn_tab_smart)).perform(click())
            onView(withText(R.string.category_people)).check(matches(isDisplayed()))
            onView(withId(R.id.btn_tab_gallery)).perform(click())
            onView(withText(images.first().folderName)).check(matches(isDisplayed()))

            onView(allOf(withId(R.id.content_list), isDisplayed())).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(images.first().folderName)),
                    click()
                )
            )
            onView(withId(R.id.grid_list)).check(matches(isDisplayed()))
            onView(withId(R.id.grid_list)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )
            onView(withId(R.id.photo_pager)).check(matches(isDisplayed()))
        } finally {
            scenario.close()
            cleanupImages(images)
        }
    }

    @Test
    fun searchOverlayShowsResultsAndEmptyState() {
        val (scenario, images) = launchWithImages("ElinkSearchFolder", 1, waitForFolderText = false)
        try {
            insertTagFor(images.first(), "Food")
            inputSearch("美食")
            waitForRecyclerViewItemCount(scenario, R.id.search_list, 1, requireVisible = false)
            onView(withId(R.id.view_pager)).check(matches(withEffectiveVisibility(Visibility.GONE)))

            inputSearch("no_such_query_123")
            waitForSearchEmptyState(scenario)

            inputSearch("")
            waitForViewVisibility(scenario, R.id.view_pager, android.view.View.VISIBLE)
        } finally {
            scenario.close()
            cleanupImages(images)
        }
    }

    @Test
    fun personNavigationChain() {
        val (scenario, images) = launchWithImages("ElinkPersonFolder", 1)
        try {
            insertPersonFor(images.first(), "Test Person")
            onView(withId(R.id.btn_tab_smart)).perform(click())
            onView(withText(R.string.category_people)).perform(click())
            waitForText(device, "Test Person")

            onView(withId(R.id.person_list)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Test Person")),
                    click()
                )
            )
            onView(withId(R.id.grid_list)).check(matches(isDisplayed()))
            onView(withId(R.id.grid_list)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )
            onView(withId(R.id.photo_pager)).check(matches(isDisplayed()))
        } finally {
            scenario.close()
            cleanupImages(images)
        }
    }

    @Test
    fun folderLongPressTriggersSystemDelete() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        val (scenario, images) = launchWithImages("ElinkDeleteFolder", 1)
        try {
            onView(allOf(withId(R.id.content_list), isDisplayed())).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(images.first().folderName)),
                    longClick()
                )
            )
            onView(withId(android.R.id.button1)).perform(click())
            SystemDialogHelper.clickDelete(device, context.packageName)
            runBlocking { delay(800) }
            waitForTextGone(device, images.first().folderName)
        } finally {
            scenario.close()
            cleanupImages(images)
        }
    }

    @Test
    fun categoryLongPressTriggersSystemDelete() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        val (scenario, images) = launchWithImages("ElinkDeleteCategory", 1)
        try {
            insertTagFor(images.first(), "Food")
            onView(withId(R.id.btn_tab_smart)).perform(click())
            onView(withText(R.string.category_food)).perform(longClick())
            onView(withId(android.R.id.button1)).perform(click())
            SystemDialogHelper.clickDelete(device, context.packageName)
            runBlocking { delay(800) }
        } finally {
            scenario.close()
            cleanupImages(images)
        }
    }

    @Test
    fun mediaGridLongPressTriggersSystemDelete() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        val (scenario, images) = launchWithImages("ElinkDeleteGrid", 1)
        try {
            onView(allOf(withId(R.id.content_list), isDisplayed())).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(images.first().folderName)),
                    click()
                )
            )
            onView(withId(R.id.grid_list)).check(matches(isDisplayed()))
            onView(withId(R.id.grid_list)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, longClick())
            )
            onView(withId(android.R.id.button1)).perform(click())
            SystemDialogHelper.clickDelete(device, context.packageName)
            runBlocking { delay(800) }
            runBlocking { delay(500) }
            waitForMediaItemDeleted(images.first().dataPath)
        } finally {
            scenario.close()
            cleanupImages(images)
        }
    }

    @Test
    fun photoDeleteTriggersSystemDeleteAndBack() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        val (scenario, images) = launchWithImages("ElinkDeletePhoto", 1)
        try {
            onView(allOf(withId(R.id.content_list), isDisplayed())).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(images.first().folderName)),
                    click()
                )
            )
            onView(withId(R.id.grid_list)).check(matches(isDisplayed()))
            onView(withId(R.id.grid_list)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )
            onView(withId(R.id.btn_delete)).perform(click())
            SystemDialogHelper.clickDelete(device, context.packageName)
            runBlocking { delay(800) }
            waitForMediaItemDeleted(images.first().dataPath)
        } finally {
            scenario.close()
            cleanupImages(images)
        }
    }

    private fun launchWithImages(
        folderName: String,
        count: Int,
        waitForFolderText: Boolean = true
    ): Pair<ActivityScenario<MainActivity>, List<TestImage>> {
        clearScanPrefs(context)
        val uniqueFolderName = "${folderName}_${System.currentTimeMillis()}"
        val relativePath = "Pictures/ElinkAIGalleryTest/$uniqueFolderName/"
        val images = (0 until count).map { index ->
            val name = "elink_${folderName}_$index.jpg"
            TestMediaStore.insertImageFromAsset(context, "coffee.jpg", name, relativePath)
        }
        seedMediaItems(images)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForAppForeground(device, context.packageName)
        if (waitForFolderText) {
            waitForText(device, uniqueFolderName)
        } else {
            waitForViewVisibility(scenario, R.id.view_pager, android.view.View.VISIBLE)
        }
        return scenario to images
    }

    private fun inputSearch(query: String) {
        val submit = query.isNotBlank()
        onView(withId(R.id.search_view)).perform(setSearchQuery(query, submit))
    }

    private fun insertTagFor(image: TestImage, label: String) {
        val mediaItem = waitForMediaItem(image.dataPath)
        val tag = ImageTag(mediaId = mediaItem.id, label = label, confidence = 1.0f)
        runBlocking {
            AppDatabase.getInstance(context).mediaDao().insertImageTags(listOf(tag))
            delay(500)
        }
    }

    private fun insertPersonFor(image: TestImage, name: String) {
        val mediaItem = waitForMediaItem(image.dataPath)
        val now = System.currentTimeMillis()
        runBlocking {
            val personDao = AppDatabase.getInstance(context).personDao()
            val personId = personDao.insertPerson(
                PersonEntity(
                    name = name,
                    embedding = ByteArray(4),
                    embeddingDim = 4,
                    sampleCount = 1,
                    createdAt = now
                )
            )
            val embedding = FaceEmbedding(
                mediaId = mediaItem.id,
                personId = personId,
                embedding = ByteArray(4),
                embeddingDim = 4,
                leftPos = 0,
                topPos = 0,
                rightPos = 1,
                bottomPos = 1,
                createdAt = now
            )
            personDao.insertFaceEmbeddings(listOf(embedding))
            delay(500)
        }
    }

    private fun waitForMediaItem(path: String): MediaItem {
        return runBlocking {
            val dao = AppDatabase.getInstance(context).mediaDao()
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < 10_000L) {
                dao.getMediaItemByPath(path)?.let { return@runBlocking it }
                delay(300)
            }
            throw AssertionError("Media item not found for path: $path")
        }
    }

    private fun cleanupImages(images: List<TestImage>) {
        images.forEach { TestMediaStore.deleteIfExists(context, it.uri) }
    }

    private fun seedMediaItems(images: List<TestImage>) {
        val now = System.currentTimeMillis()
        val items = images.map { image ->
            MediaItem(
                mediaStoreId = image.mediaStoreId,
                path = image.dataPath,
                dateTaken = now,
                folderName = image.folderName,
                width = 1,
                height = 1
            )
        }
        runBlocking {
            AppDatabase.getInstance(context).mediaDao().insertMediaItems(items)
            delay(300)
        }
    }

    private fun waitForMediaItemDeleted(path: String) {
        runBlocking {
            val dao = AppDatabase.getInstance(context).mediaDao()
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < 10_000L) {
                if (dao.getMediaItemByPath(path) == null) return@runBlocking
                delay(300)
            }
            throw AssertionError("Media item not deleted: $path")
        }
    }

    private fun waitForRecyclerViewItemCount(
        scenario: ActivityScenario<MainActivity>,
        viewId: Int,
        minCount: Int,
        timeoutMs: Long = 10_000L,
        requireVisible: Boolean = false
    ) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            var count = 0
            var visible = false
            scenario.onActivity { activity ->
                val recyclerView = activity.findViewById<RecyclerView>(viewId)
                count = recyclerView?.adapter?.itemCount ?: 0
                visible = recyclerView?.visibility == android.view.View.VISIBLE && recyclerView.isShown
            }
            if (count >= minCount && (!requireVisible || visible)) return
            runBlocking { delay(200) }
        }
        throw AssertionError("RecyclerView item count < $minCount for viewId=$viewId")
    }

    private fun waitForViewVisibility(
        scenario: ActivityScenario<MainActivity>,
        viewId: Int,
        expectedVisibility: Int,
        timeoutMs: Long = 10_000L
    ) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            var matches = false
            scenario.onActivity { activity ->
                val view = activity.findViewById<android.view.View>(viewId)
                matches = view?.visibility == expectedVisibility
            }
            if (matches) return
            runBlocking { delay(200) }
        }
        throw AssertionError("View visibility not matched for viewId=$viewId")
    }

    private fun waitForSearchEmptyState(
        scenario: ActivityScenario<MainActivity>,
        timeoutMs: Long = 10_000L
    ) {
        val start = System.currentTimeMillis()
        val expected = context.getString(R.string.gallery_search_empty_hint)
        while (System.currentTimeMillis() - start < timeoutMs) {
            var matches = false
            scenario.onActivity { activity ->
                val emptyMessage = activity.findViewById<android.widget.TextView>(R.id.empty_message)
                val searchList = activity.findViewById<RecyclerView>(R.id.search_list)
                val count = searchList?.adapter?.itemCount ?: 0
                matches = emptyMessage?.text?.toString() == expected && count == 0
            }
            if (matches) return
            runBlocking { delay(200) }
        }
        throw AssertionError("Search empty state not reached")
    }

    private fun setSearchQuery(query: String, submit: Boolean): ViewAction {
        return object : ViewAction {
            override fun getConstraints() = isAssignableFrom(SearchView::class.java)

            override fun getDescription() = "Set search query"

            override fun perform(uiController: UiController, view: View) {
                val searchView = view as SearchView
                searchView.setQuery(query, submit)
                searchView.clearFocus()
                uiController.loopMainThreadUntilIdle()
            }
        }
    }
}

private class RecyclerViewItemCountAssertion(
    private val expectedCount: Int
) : androidx.test.espresso.ViewAssertion {
    override fun check(
        view: android.view.View?,
        noViewFoundException: androidx.test.espresso.NoMatchingViewException?
    ) {
        if (noViewFoundException != null) throw noViewFoundException
        val recyclerView = view as? RecyclerView
            ?: throw AssertionError("View is not RecyclerView")
        val actual = recyclerView.adapter?.itemCount ?: 0
        if (actual != expectedCount) {
            throw AssertionError("Expected $expectedCount items but was $actual")
        }
    }
}
