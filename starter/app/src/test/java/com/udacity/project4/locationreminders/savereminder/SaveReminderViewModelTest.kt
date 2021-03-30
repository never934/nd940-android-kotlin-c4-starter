package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Use a fake repository to be injected into the view model.
    private lateinit var dataSource: FakeDataSource

    @Before
    fun setupStatisticsViewModel() {
        // stop koin
        stopKoin()
        // Initialise the repository with no tasks.
        dataSource = FakeDataSource()
        val applicationMock = Mockito.mock(Application::class.java)
        saveReminderViewModel = SaveReminderViewModel(applicationMock, dataSource)
    }

    @Test
    fun saveReminder_checkSuccess() {
        // Given
        val data = ReminderDataItem(
            title = "test",
            description = "test desc",
            location = "test location",
            latitude = 0.0,
            longitude = 0.0,
            id = UUID.randomUUID().toString()
        )

        // When
        saveReminderViewModel.saveReminder(data)

        // Then
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertEquals(saveReminderViewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }


    @Test
    fun onClear_success() = mainCoroutineRule.runBlockingTest {
        // Given
        saveReminderViewModel.reminderTitle.value = "test title"
        saveReminderViewModel.reminderDescription.value = "test description"
        saveReminderViewModel.locationSelected(LatLng(0.0,0.0))

        // When
        saveReminderViewModel.onClear()

        // Then
        assertEquals(saveReminderViewModel.reminderTitle.getOrAwaitValue(), null)
        assertEquals(saveReminderViewModel.reminderDescription.getOrAwaitValue(), null)
        assertEquals(saveReminderViewModel.latLng.getOrAwaitValue(), null)
        assertEquals(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), null)
    }

    @Test
    fun reminderValidator_success() = mainCoroutineRule.runBlockingTest {
        // Given
        val data = ReminderDataItem(
            title = "test",
            description = "test desc",
            location = "test location",
            latitude = 0.0,
            longitude = 0.0,
            id = UUID.randomUUID().toString()
        )

        // When
        val validateResult = saveReminderViewModel.validateEnteredData(data)

        // Then
        assertEquals(validateResult, true)
    }

    @Test
    fun reminderValidatorEmptyTitle_errorSnackbar() = mainCoroutineRule.runBlockingTest {
        // Given
        val data = ReminderDataItem(
            title = null,
            description = "test desc",
            location = "test location",
            latitude = 0.0,
            longitude = 0.0,
            id = UUID.randomUUID().toString()
        )

        // When
        saveReminderViewModel.validateEnteredData(data)

        // Then
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun reminderValidatorEmptyLocation_errorSnackbar() = mainCoroutineRule.runBlockingTest {
        // Given
        val data = ReminderDataItem(
            title = "test",
            description = "test desc",
            location = null,
            latitude = 0.0,
            longitude = 0.0,
            id = UUID.randomUUID().toString()
        )

        // When
        saveReminderViewModel.validateEnteredData(data)

        // Then
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

}