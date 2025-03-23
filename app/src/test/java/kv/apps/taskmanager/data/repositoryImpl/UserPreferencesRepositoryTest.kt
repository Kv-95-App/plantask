package kv.apps.taskmanager.data.repositoryImpl

import io.mockk.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kv.apps.taskmanager.domain.repository.UserPreferencesRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserPreferencesRepositoryTest {

    private lateinit var userPreferencesRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        userPreferencesRepository = mockk()
    }

    @Test
    fun `test save and retrieve user session`() = runBlocking {
        // Arrange
        val userId = "user_123"
        coEvery { userPreferencesRepository.saveUserSession(userId) } just Runs
        every { userPreferencesRepository.getUserSession() } returns flowOf(userId)

        // Act
        userPreferencesRepository.saveUserSession(userId)
        val result = userPreferencesRepository.getUserSession().first()

        // Assert
        assertEquals(userId, result)
        coVerify(exactly = 1) { userPreferencesRepository.saveUserSession(userId) }
    }

    @Test
    fun `test clear user session`() = runBlocking {
        // Arrange
        coEvery { userPreferencesRepository.clearUserSession() } just Runs
        every { userPreferencesRepository.getUserSession() } returns flowOf(null)

        // Act
        userPreferencesRepository.clearUserSession()
        val result = userPreferencesRepository.getUserSession().first()

        // Assert
        assertNull(result)
        coVerify(exactly = 1) { userPreferencesRepository.clearUserSession() }
    }

    @Test
    fun `test save and retrieve keep logged in preference`() = runBlocking {
        // Arrange
        coEvery { userPreferencesRepository.saveKeepLoggedIn(true) } just Runs
        every { userPreferencesRepository.isKeepLoggedIn() } returns flowOf(true)

        // Act
        userPreferencesRepository.saveKeepLoggedIn(true)
        val result = userPreferencesRepository.isKeepLoggedIn().first()

        // Assert
        assertTrue(result)
        coVerify(exactly = 1) { userPreferencesRepository.saveKeepLoggedIn(true) }
    }

    @Test
    fun `test default keep logged in value is false`() = runBlocking {
        // Arrange
        every { userPreferencesRepository.isKeepLoggedIn() } returns flowOf(false)

        // Act
        val result = userPreferencesRepository.isKeepLoggedIn().first()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `test session retrieval when no session exists`() = runBlocking {
        // Arrange
        every { userPreferencesRepository.getUserSession() } returns flowOf(null)

        // Act
        val result = userPreferencesRepository.getUserSession().first()

        // Assert
        assertNull(result)
    }

    @Test
    fun `test failure when saving user session`() = runBlocking {
        // Arrange
        val userId = "user_123"
        coEvery { userPreferencesRepository.saveUserSession(userId) } throws Exception("Storage error")

        // Act & Assert
        try {
            userPreferencesRepository.saveUserSession(userId)
            fail("Exception was expected")
        } catch (e: Exception) {
            assertEquals("Storage error", e.message)
        }
    }

    @Test
    fun `test failure when retrieving user session`() = runBlocking {
        // Arrange
        every { userPreferencesRepository.getUserSession() } throws Exception("Failed to fetch session")

        // Act & Assert
        try {
            userPreferencesRepository.getUserSession().first()
            fail("Exception was expected")
        } catch (e: Exception) {
            assertEquals("Failed to fetch session", e.message)
        }
    }
}
