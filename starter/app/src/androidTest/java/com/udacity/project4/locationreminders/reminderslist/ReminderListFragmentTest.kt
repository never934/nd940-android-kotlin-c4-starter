package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.SwipeRefreshLayoutMatchers.isRefreshing
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito
import java.util.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest :
    AutoCloseKoinTest(){

    private lateinit var repository: ReminderDataSource
    private lateinit var app: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun init() {
        stopKoin()
        app = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    app,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(app) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun cleanUp() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun remindersEmpty_noData() {
        // Given
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        // Then
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun remindersNotEmpty_dataOnScreen() {
        runBlocking {
            // Given
            val data = ReminderDTO(
                title = "test",
                description = "test desc",
                location = "test location",
                latitude = 0.0,
                longitude = 0.0,
                id = UUID.randomUUID().toString()
            )
            repository.saveReminder(data)

            // When
            val scenario =
                launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            dataBindingIdlingResource.monitorFragment(scenario)

            // Then
            onView(withText(data.title)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun fabClicked_navigatedToAdd() {
        // Given
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // When
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Then
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun swipeRefreshed_turnedOffAfterUpdating() {
        // Given
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        // When
        onView(withId(R.id.refreshLayout)).perform(swipeDown())

        // Then
        onView(withId(R.id.refreshLayout)).check(matches(not(isRefreshing())))
    }

    @Test
    fun updateDataFromSwipeRefresh_dataUpdated() {
        runBlocking {
            // Given
            val scenario =
                launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            dataBindingIdlingResource.monitorFragment(scenario)
            val data = ReminderDTO(
                title = "test",
                description = "test desc",
                location = "test location",
                latitude = 0.0,
                longitude = 0.0,
                id = UUID.randomUUID().toString()
            )

            // When
            repository.saveReminder(data)
            onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
            onView(withId(R.id.refreshLayout)).perform(swipeDown())

            // Then
            onView(withId(R.id.refreshLayout)).check(matches(not(isRefreshing())))
            onView(withText(data.title)).check(matches(isDisplayed()))
        }
    }
}