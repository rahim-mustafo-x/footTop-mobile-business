package uz.coder.foottopbusiness.presentation.main.home

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.lifecycle.viewModelScope
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.domain.usecase.admin.DashboardUseCase
import uz.coder.foottopbusiness.domain.usecase.match.GetMatchesUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumByIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumsUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.GetTournamentsUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetUserUseCase
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.domain.usecase.booking.CreateBookingUseCase
import uz.coder.foottopbusiness.data.network.dto.booking.BookingRequestDto
import uz.coder.foottopbusiness.core.platform.checkNotificationPermissionStatus
import uz.coder.foottopbusiness.core.platform.PermissionStatus
import uz.coder.foottopbusiness.core.platform.openAppSettings
import uz.coder.foottopbusiness.core.UserSession
import uz.coder.foottopbusiness.core.minutesBetween
import uz.coder.foottopbusiness.core.plusMinutes
import uz.coder.foottopbusiness.core.isOverlap
import uz.coder.foottopbusiness.core.toLocalDateTimeSafe
import uz.coder.foottopbusiness.domain.usecase.booking.GetBookingsByStadiumIdUseCase
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.presentation.main.home.HomeContract.Effect.*
import kotlinx.coroutines.launch

private fun durationMinutesKey(key: String): Int = when(key) {
    "SIXTY" -> 60
    "NINETY" -> 90
    "ONE_HUNDRED_TWENTY" -> 120
    else -> 60
}

/**
 * Telefon raqamini backend kutadigan 9 xonali "901234567" ko'rinishiga keltiradi.
 *
 * Maydonning o'zi allaqachon faqat raqam saqlaydi, lekin bu yerda "+998 (90)
 * 123-45-67" yoki 998 prefiksli qiymat kelib qolsa ham to'g'ri format ketadi.
 */
private fun normalizeBookingPhone(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    return if (digits.length == 12 && digits.startsWith("998")) digits.substring(3) else digits
}

private typealias Slot = Triple<LocalDateTime, LocalDateTime, Boolean>

/**
 * Slotlar panjarasining qadami (daqiqada) - ketma-ket ikki slot boshlanishidan
 * olinadi. Ro'yxat bitta slotdan iborat bo'lsa 30 daqiqa deb qabul qilinadi.
 */
private fun slotStepMinutes(slots: List<Slot>): Int {
    val step = if (slots.size >= 2) minutesBetween(slots[0].first, slots[1].first) else 0
    return if (step > 0) step else 30
}

/**
 * [startIndex] dan boshlab tanlangan davomiylikni to'liq qoplaydigan slotlar.
 *
 * Ro'yxat uzilib qolsa (stadion yopiq oraliq yoki kun oxiri) null qaytaradi -
 * ya'ni bunday oraliqni band qilib bo'lmaydi. Ilgari bu yerda indeks bo'yicha
 * hisob ishlatilardi va davomiylikdan keyingi "chegara" sloti ham ro'yxatda
 * bo'lishi talab qilinardi: shu sababli kunning oxirgi bo'sh soatini (masalan
 * 22:00, agar ro'yxat 22:30 da tugasa) umuman band qilib bo'lmasdi.
 */
/**
 * Haqiqatda bron bilan band bo'lgan slotlar boshlanish vaqti.
 *
 * Backend slotlarni panjara qadami bilan, lekin tanlangan DAVOMIYLIKDAGI oyna
 * sifatida qaytaradi va bron bilan kesishgan har bir oynani band deb belgilaydi.
 * Shuning uchun 16:00-17:00 broni 15:30 slotini ham (oynasi 15:30-16:30) band
 * qilib ko'rsatadi, garchi 15:30-16:00 aslida bo'sh bo'lsa ham.
 *
 * Katakchaning o'z vaqti (start .. start+qadam) haqiqatda band bo'lishi uchun uni
 * qoplaydigan BARCHA oynalar band bo'lishi kerak: bittasi bo'sh bo'lsa, demak
 * katakchaning o'zi ham bo'sh - u shunchaki davomiylik uchun qisqalik qiladi.
 */
private fun occupiedSlotStarts(slots: List<Slot>, duration: String): Set<LocalDateTime> {
    if (slots.isEmpty()) return emptySet()
    val step = slotStepMinutes(slots)
    val windowCells = ((durationMinutesKey(duration) + step - 1) / step).coerceAtLeast(1)

    return slots.indices
        .filter { i ->
            if (slots[i].third) return@filter false
            // i-katakchani qoplaydigan oynalar: [i - windowCells + 1 .. i].
            // Ro'yxatda uzilish bo'lsa oyna i gacha yetib bormaydi - bunday
            // oynalar hisobga olinmaydi.
            (maxOf(0, i - windowCells + 1)..i).none { j ->
                slots[j].third && slots[j].first.plusMinutes((i - j) * step) == slots[i].first
            }
        }
        .mapTo(mutableSetOf()) { slots[it].first }
}

private fun coveringSlots(slots: List<Slot>, startIndex: Int, duration: String): List<Slot>? {
    val step = slotStepMinutes(slots)
    val minutes = durationMinutesKey(duration)
    val needed = (minutes + step - 1) / step
    if (needed <= 0 || startIndex + needed > slots.size) return null

    var expected = slots[startIndex].first
    for (i in startIndex until startIndex + needed) {
        if (slots[i].first != expected) return null
        expected = expected.plusMinutes(step)
    }
    return slots.subList(startIndex, startIndex + needed)
}

class HomeViewModel(
    private val getStadiumsUseCase: GetStadiumsUseCase,
    private val getStadiumByIdUseCase: GetStadiumByIdUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val preferencesManager: PreferencesManager,
    private val dashboardUseCase: DashboardUseCase,
    private val getMatchesUseCase: GetMatchesUseCase,
    private val getTournamentsUseCase: GetTournamentsUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val getBookingsByStadiumIdUseCase: GetBookingsByStadiumIdUseCase,
    private val userSession: UserSession
) : BaseViewModel<HomeContract.State, HomeContract.Effect, HomeContract.Event>(
    // Rol va foydalanuvchi sessiyada allaqachon bor (MainScreen shusiz bu
    // ekranni ochmaydi) - shuning uchun boshlang'ich holatga darrov qo'yamiz.
    // Ilgari ular faqat quyidagi collector'lar orqali kelardi, ya'ni birinchi
    // kadr UNKNOWN rol bilan chizilardi: sarlavhada "ROL ANIQLANMAGAN"
    // nishonchasi paydo bo'lib, keyin haqiqiy rolga almashardi va shu payt
    // header balandligi sakrab ketardi.
    initialState = HomeContract.State(
        user = userSession.user.value,
        userRole = userSession.role.value,
        isAdmin = userSession.role.value == UserRole.SUPER_ADMIN || userSession.role.value == UserRole.DISTRICT_ADMIN,
        isOwner = userSession.role.value == UserRole.OWNER,
        selectedDate = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    )
) {
    init {
        viewModelScope.launch {
            userSession.user.collect { currentUser ->
                if (currentUser != null) {
                    val role = userSession.role.value
                    updateState {
                        copy(
                            user = currentUser,
                            isAdmin = role == UserRole.SUPER_ADMIN || role == UserRole.DISTRICT_ADMIN,
                            isOwner = role == UserRole.OWNER,
                            userRole = role,
                            isLoadingUser = false
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            userSession.role.collect { role ->
                if (role != UserRole.UNKNOWN) {
                    updateState {
                        copy(
                            userRole = role,
                            isAdmin = role == UserRole.SUPER_ADMIN || role == UserRole.DISTRICT_ADMIN,
                            isOwner = role == UserRole.OWNER,
                            isLoadingUser = false
                        )
                    }
                }
            }
        }
        handleEvent(HomeContract.Event.Load)
    }

    override fun handleEvent(event: HomeContract.Event) {
        when (event) {
            HomeContract.Event.Load, HomeContract.Event.Refresh -> {
                loadUser()
                loadStadiumCount()
                loadMatches()
                loadTournaments()
            }

            is HomeContract.Event.SelectTournament -> updateState { copy(selectedTournament = event.t) }
            HomeContract.Event.ClearTournament -> updateState { copy(selectedTournament = null) }

            is HomeContract.Event.SelectStadiumForSlots -> {
                updateState { copy(selectedStadiumForTime = event.stadium) }
                loadSlots(event.stadium.id ?: return, state.value.selectedDate, state.value.selectedDuration)
            }

            is HomeContract.Event.ChangeDate -> {
                updateState { copy(selectedDate = event.date) }
                state.value.selectedStadiumForTime?.id?.let { loadSlots(it, event.date, state.value.selectedDuration) }
            }

            is HomeContract.Event.ChangeDuration -> {
                updateState { copy(selectedDuration = event.duration) }
                state.value.selectedStadiumForTime?.id?.let { loadSlots(it, state.value.selectedDate, event.duration) }
            }

            is HomeContract.Event.SelectSlot -> {
                val availableSlots = state.value.stadiumSlots
                val slotIndex = availableSlots.indexOf(event.slot)
                if (slotIndex == -1) return

                // Oraliqni qoplaydigan slotlarning hammasi bo'sh bo'lishi kerak
                val covering = coveringSlots(availableSlots, slotIndex, state.value.selectedDuration)
                val canBook = covering != null && covering.all { it.third }

                if (canBook) {
                    updateState { copy(selectedSlot = event.slot, isBookingSlot = true) }
                } else {
                    sendEffect(ShowToast("Tanlangan vaqt oralig'ida bo'sh joy yetarli emas"))
                }
            }

            is HomeContract.Event.CreateBooking -> {
                val s = state.value
                val slot = s.selectedSlot ?: return
                val stadium = s.selectedStadiumForTime ?: return

                updateState { copy(isBookingSlot = false, selectedSlot = null) }

                executeAsync(
                    block = {
                        val userId = preferencesManager.userId.first().toLong()

                        // Tugash vaqti to'g'ridan-to'g'ri davomiylikdan hisoblanadi.
                        // Ilgari u ro'yxatdagi keyingi slotdan olinardi va o'sha
                        // slot bo'lmasa (kun oxiri) slot.second ga - API qaytargan,
                        // davomiylikka bog'liq qiymatga - qaytib ketardi.
                        val endTime = slot.first.plusMinutes(durationMinutesKey(s.selectedDuration))

                        val totalPrice = (stadium.pricePerHour ?: 0.0) * (durationMinutesKey(s.selectedDuration) / 60.0)

                        val request = BookingRequestDto(
                            userId = userId,
                            stadiumId = stadium.id?.toLong(),
                            startTime = slot.first.toString(),
                            endTime = endTime.toString(),
                            totalPrice = totalPrice,
                            status = "PENDING",
                            paymentMethod = "CASH",
                            // Mijoz ma'lumoti: dialogda so'raladi, lekin ilgari
                            // so'rovga qo'shilmasdan yo'qolib ketardi
                            name = event.name.trim(),
                            phone = normalizeBookingPhone(event.phone)
                        )
                        createBookingUseCase(request).first()
                    },
                    onSuccess = {
                        sendEffect(ShowToast("Muvaffaqiyatli band qilindi: ${event.name}"))
                        sendEffect(NavigateBack)
                        loadSlots(stadium.id ?: return@executeAsync, s.selectedDate, s.selectedDuration)
                        // Band qilish muvaffaqiyatli o'tgach bildirishnoma ruxsatini so'raymiz
                        handleEvent(HomeContract.Event.CheckNotificationPermission)
                    },
                    onError = {
                        sendEffect(ShowToast("Xatolik: ${it.message}"))
                    }
                )
            }

            HomeContract.Event.DismissBookingDialog -> updateState { copy(isBookingSlot = false) }

            HomeContract.Event.ClearStadiumForSlots -> updateState {
                copy(
                    selectedStadiumForTime = null,
                    stadiumSlots = emptyList(),
                    occupiedSlotStarts = emptySet(),
                    selectedSlot = null,
                    isBookingSlot = false
                )
            }

            HomeContract.Event.ShowExitToast -> sendEffect(ShowToast("Chiqish uchun yana bir marta bosing"))

            is HomeContract.Event.SetShowNotificationPermissionDialog -> updateState { copy(showNotificationPermissionDialog = event.show) }
            HomeContract.Event.RequestNotificationPermission -> {
                updateState { copy(showNotificationPermissionDialog = false, triggerNotificationRequest = true) }
            }
            is HomeContract.Event.OnNotificationPermissionResult -> {
                updateState { copy(triggerNotificationRequest = false) }
                handlePermissionResult(event.status)
            }
            HomeContract.Event.CheckNotificationPermission -> {
                checkPermission()
            }
            HomeContract.Event.DismissPermanentlyDeniedDialog -> {
                updateState { copy(showPermanentlyDeniedDialog = false) }
            }
            HomeContract.Event.OpenSettings -> {
                updateState { copy(showPermanentlyDeniedDialog = false) }
                openAppSettings()
            }
        }
    }

    private fun handlePermissionResult(status: PermissionStatus) {
        viewModelScope.launch {
            val currentCount = preferencesManager.notificationRequestCount.first()
            preferencesManager.setNotificationRequestCount(currentCount + 1)

            if (status == PermissionStatus.GRANTED) {
                preferencesManager.setNotificationPermission(true)
            } else if (status == PermissionStatus.DENIED) {
                if (currentCount >= 2) {
                    updateState { copy(showPermanentlyDeniedDialog = true) }
                }
            }
        }
    }

    private fun checkPermission() {
        executeAsync {
            val grantedInPrefs = preferencesManager.notificationPermission.first()
            if (!grantedInPrefs) {
                val status = checkNotificationPermissionStatus()
                if (status != PermissionStatus.GRANTED) {
                    val requestCount = preferencesManager.notificationRequestCount.first()
                    if (requestCount <= 2) {
                        updateState { copy(showNotificationPermissionDialog = true) }
                    } else {
                        // 3 martadan ko'p rad etilgan - sozlamalarga yo'naltiramiz
                        updateState { copy(showPermanentlyDeniedDialog = true) }
                    }
                } else {
                    preferencesManager.setNotificationPermission(true)
                }
            }
        }
    }

    private fun loadUser() {
        val currentUser = userSession.user.value
        if (currentUser != null) {
            val role = userSession.role.value
            updateState {
                copy(
                    user = currentUser,
                    isAdmin = role == UserRole.SUPER_ADMIN || role == UserRole.DISTRICT_ADMIN,
                    isOwner = role == UserRole.OWNER,
                    userRole = role,
                    isLoadingUser = false
                )
            }
            loadDashboardStats()
            return
        }

        updateState { copy(isLoadingUser = true) }
        executeAsync {
            val userId = preferencesManager.userId.filter { it != 0 }.first()
            getUserUseCase(userId.toLong()).collect { result ->
                userSession.setUser(result)
                val userRole = userSession.role.value

                updateState {
                    copy(
                        user = result,
                        isAdmin = userRole == UserRole.SUPER_ADMIN || userRole == UserRole.DISTRICT_ADMIN,
                        isOwner = userRole == UserRole.OWNER,
                        userRole = userRole,
                        isLoadingUser = false
                    )
                }
                loadDashboardStats() // Rol aniqlangach statistikani yuklaymiz
            }
        }
    }

    private fun loadSlots(id: Int, date: String, duration: String) {
        updateState { copy(isLoadingSlots = true, selectedSlot = null) }
        executeAsync {
            getStadiumByIdUseCase(id, date, duration).collect { responseList ->
                val stadium = responseList.firstOrNull()
                val triples = stadium?.slots?.map {
                    Triple(
                        LocalDateTime.parse(it.start ?: ""),
                        LocalDateTime.parse(it.end ?: ""),
                        it.status == "AVAILABLE"
                    )
                } ?: emptyList()
                updateState {
                    copy(
                        stadiumSlots = triples,
                        occupiedSlotStarts = occupiedSlotStarts(triples, duration),
                        isLoadingSlots = false
                    )
                }
                loadOccupiedFromBookings(id, date, triples)
            }
        }
    }

    /**
     * Haqiqiy bandlikni bronlar ro'yxatidan aniqlaydi.
     *
     * [occupiedSlotStarts] slot oynalaridan xulosa chiqaradi, lekin ikki bron
     * orasida qolgan yarim soatni ajrata olmaydi: 18:30-19:30 va 20:00-21:00
     * bronlarida 19:30 katakchasini qoplaydigan ikkala oyna ham band ko'rinadi,
     * holbuki 19:30-20:00 aslida bo'sh turadi. Bronlarning o'zi bilan solishtirish
     * bunday bo'sh oraliqlarni to'g'ri ko'rsatadi - gazon bekor turgan vaqt
     * hisobda "band" bo'lib qolmaydi.
     *
     * So'rov muvaffaqiyatsiz bo'lsa oynalardan chiqarilgan taxmin joyida qoladi.
     */
    private fun loadOccupiedFromBookings(id: Int, date: String, slots: List<Slot>) {
        if (slots.isEmpty()) return
        executeAsync(
            block = { getBookingsByStadiumIdUseCase(id.toLong(), date).first() },
            onSuccess = { bookings ->
                val step = slotStepMinutes(slots)
                val intervals = bookings.mapNotNull { booking ->
                    // Bekor qilingan bron gazonni band qilmaydi
                    if (booking.status == "CANCELLED") return@mapNotNull null
                    val start = booking.startTime.toLocalDateTimeSafe() ?: return@mapNotNull null
                    val end = booking.endTime.toLocalDateTimeSafe() ?: return@mapNotNull null
                    start to end
                }

                val occupied = slots
                    .filter { slot ->
                        val cellEnd = slot.first.plusMinutes(step)
                        intervals.any { (bookingStart, bookingEnd) ->
                            isOverlap(slot.first, cellEnd, bookingStart, bookingEnd)
                        }
                    }
                    .mapTo(mutableSetOf()) { it.first }

                // Shu orada sana yoki stadion almashgan bo'lsa natija eskirgan
                if (state.value.stadiumSlots == slots) {
                    updateState { copy(occupiedSlotStarts = occupied) }
                }
            }
        )
    }

    private fun loadDashboardStats() {
        if (!state.value.isAdmin && !state.value.isOwner) {
            // Admin bo'lmagan rollar uchun admin API'lari chaqirilmaydi,
            // statistika boshqa so'rovlardan hisoblanadi
            updateLocalStats()
            return
        }

        executeAsync {
            dashboardUseCase().collect { dashboard ->
                updateState {
                    copy(
                        activeStadiums = dashboard.activeStadiumsCount,
                        totalTournaments = dashboard.tournamentsCount,
                        totalUsers = dashboard.usersCount,
                        totalEarnings = dashboard.stadiumRevenues.sumOf { it.totalRevenue }
                    )
                }
            }
        }
    }

    private fun updateLocalStats() {
        val s = state.value
        if (s.isAdmin || s.isOwner) return // Admin uchun dashboard API'si ishlatiladi

        updateState {
            copy(
                totalEarnings = s.matches.sumOf { (it.currentPlayers ?: 0) * (it.pricePerPlayer ?: 0.0) },
                totalTournaments = s.tournaments.size
                // activeStadiums loadStadiumCount() ichida yangilanadi
            )
        }
    }

    /**
     * Stadionlar soni va birinchi sahifadagi ro'yxat.
     *
     * Son [activeStadiums] uchun kerak (admin/owner'da dashboard'dan ham
     * keladi, bu esa zaxira). Ro'yxatning o'zi esa "Bron qilish" amalida
     * stadion tanlash uchun ishlatiladi - shuning uchun uni tashlab
     * yubormaymiz, qo'shimcha so'rov ham kerak bo'lmaydi.
     */
    private fun loadStadiumCount() {
        executeAsync(
            block = { getStadiumsUseCase(name = null, isActive = null, page = 0).first() },
            onSuccess = { pageData ->
                updateState {
                    copy(
                        stadiums = pageData.content ?: emptyList(),
                        activeStadiums = pageData.totalElements?.toInt() ?: activeStadiums
                    )
                }
            }
        )
    }

    private fun loadMatches() {
        updateState { copy(isLoadingMatches = true) }
        executeAsync {
            getMatchesUseCase().collect { matches ->
                updateState { copy(matches = matches, isLoadingMatches = false) }
                updateLocalStats()
            }
        }
    }

    private fun loadTournaments() {
        updateState { copy(isLoadingTournaments = true) }
        executeAsync {
            getTournamentsUseCase().collect { pageData ->
                updateState { copy(tournaments = pageData.content ?: emptyList(), isLoadingTournaments = false) }
                updateLocalStats()
            }
        }
    }
}
