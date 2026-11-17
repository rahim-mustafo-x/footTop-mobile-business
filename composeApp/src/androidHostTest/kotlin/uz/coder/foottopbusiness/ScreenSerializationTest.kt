package uz.coder.foottopbusiness

import cafe.adriel.voyager.core.screen.Screen
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import kotlin.test.Test
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.booking.BookingResponseDto
import uz.coder.foottopbusiness.data.network.dto.stadium.ImageDto
import uz.coder.foottopbusiness.data.network.dto.stadium.LocationDto
import uz.coder.foottopbusiness.data.network.dto.stadium.SlotDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.presentation.main.booking.details.BookingDetailsScreen
import uz.coder.foottopbusiness.presentation.main.customer.CustomerScreen
import uz.coder.foottopbusiness.presentation.main.home.slots.SlotsControlVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumDetailsVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.edit.EditStadiumVoyager
import uz.coder.foottopbusiness.presentation.main.stadium.edit.MapSelectionScreen
import uz.coder.foottopbusiness.presentation.main.tournaments.edit.TournamentEditScreen

/**
 * Voyager navigator stack'ini Android Bundle'ga java-serialization orqali yozadi
 * (ilova fonga o'tganda: qo'ng'iroq, galereya, brauzer...). Ekran ichidagi biror
 * maydon Serializable bo'lmasa ilova NotSerializableException bilan yiqiladi.
 */
class ScreenSerializationTest {
    private val stadium = StadiumResponse(
        id = 1, name = "A", location = LocationDto(41.0, 69.0, "x"),
        images = listOf(ImageDto("/a.jpg")), slots = listOf(SlotDto("10:00", "11:00", "FREE", 1.0))
    )

    private fun serialize(screen: Screen) {
        val bytes = ByteArrayOutputStream().also { out -> ObjectOutputStream(out).use { it.writeObject(screen) } }.toByteArray()
        java.io.ObjectInputStream(bytes.inputStream()).use { it.readObject() }
    }

    @Test
    fun allScreensWithArgumentsAreJavaSerializable() {
        val screens = listOf(
            StadiumDetailsVoyager(stadium),
            EditStadiumVoyager(stadium),
            SlotsControlVoyager(stadium),
            BookingDetailsScreen(BookingResponseDto(id = 1, name = "x")),
            TournamentEditScreen(TournamentResponseDto(id = 1, location = LocationDto(1.0, 2.0))),
            CustomerScreen("+998901234567", "Ali"),
            MapSelectionScreen(41.0, 69.0) { _, _ -> },
        )
        val failures = screens.mapNotNull { s ->
            runCatching { serialize(s) }.exceptionOrNull()?.let { "${s::class.simpleName}: $it" }
        }
        check(failures.isEmpty()) { failures.joinToString("\n") }
    }
}
