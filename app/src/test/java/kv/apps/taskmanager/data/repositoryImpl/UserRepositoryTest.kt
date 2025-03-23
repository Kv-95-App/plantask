package kv.apps.taskmanager.data.repositoryImpl

import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.UserRepository
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        // Mock the UserRepository instance
        userRepository = mockk()
    }

    @Test
    fun `test getUserDetails success`() = runBlocking {
        // Arrange: Create a mock user
        val mockUser = User("1", "John Doe", "john@example.com")
        coEvery { userRepository.getUserDetails() } returns Result.success(mockUser)

        // Act: Call getUserDetails
        val result = userRepository.getUserDetails()

        // Assert: Validate the result
        assertTrue(result.isSuccess)
        assertEquals(mockUser, result.getOrNull())
    }

    @Test
    fun `test getUserDetails failure`() = runBlocking {
        // Arrange: Simulate an error
        coEvery { userRepository.getUserDetails() } returns Result.failure(Exception("User not found"))

        // Act: Call getUserDetails
        val result = userRepository.getUserDetails()

        // Assert: Validate the failure case
        assertTrue(result.isFailure)
        assertEquals("User not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test saveUserDetails success`() = runBlocking {
        // Arrange: Create a mock user
        val user = User("1", "John Doe", "john@example.com")
        coEvery { userRepository.saveUserDetails(user) } returns Result.success(Unit)

        // Act: Call saveUserDetails
        val result = userRepository.saveUserDetails(user)

        // Assert: Validate the success case
        assertTrue(result.isSuccess)
    }

    @Test
    fun `test saveUserDetails failure`() = runBlocking {
        // Arrange: Simulate a failure when saving user details
        val user = User("1", "John Doe", "john@example.com")
        coEvery { userRepository.saveUserDetails(user) } returns Result.failure(Exception("Failed to save user"))

        // Act: Call saveUserDetails
        val result = userRepository.saveUserDetails(user)

        // Assert: Validate the failure case
        assertTrue(result.isFailure)
        assertEquals("Failed to save user", result.exceptionOrNull()?.message)
    }
}
