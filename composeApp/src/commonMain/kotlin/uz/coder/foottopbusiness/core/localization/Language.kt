package uz.coder.foottopbusiness.core.localization

interface Language {
    val welcome: String
    val loginDescription: String
    val username: String
    val password: String
    val login: String
    val noAccount: String
    val contactAdmin: String
    val loading: String
    val error: String
    val success: String
    val settings: String
    val profile: String
    val logout: String
    val back: String
    val save: String
    val cancel: String
    val delete: String
    val edit: String
    val add: String
    val search: String
    val phoneNumber: String
    val fullName: String
    val location: String
    val stadium: String
    val address: String
    val capacity: String
    val price: String
    val openTime: String
    val closeTime: String
    val description: String
    val status: String
    val active: String
    val inactive: String
    val chooseRegion: String
    val chooseDistrict: String
    val selectRole: String
    val createAccount: String
    val changePassword: String
    val helpAndContact: String
    val rateApp: String
    val aboutApp: String
    val dangerZone: String
    
    // Home/Dashboard
    val management: String
    val quickActions: String
    val todaySchedule: String
    val systemManagement: String
    val myStadium: String
    val stadiums: String
    val activeFields: String
    val users: String
    val totalMembers: String
    val tournaments: String
    val totalEvents: String
    val revenue: String
    val addStadium: String
    val addEmployee: String
    val createTournament: String
    val accessRestricted: String
    val switchAccount: String
    val refresh: String
    val noBookingsToday: String
    val latestMatches: String
    
    // Notifications
    val notifications: String
    val notifyUsers: String
    val msgType: String
    val booking: String
    val match: String
    val tournament: String
    val system: String
    val title: String
    val msgBody: String
    val sendToAll: String
    val pushHint: String
    val titleHint: String
    val bodyHint: String
    val notificationRationaleTitle: String
    val notificationRationaleDesc: String
    val notificationBenefit1: String
    val notificationBenefit2: String
    val notificationBenefit3: String
    val notificationBenefit4: String
    val enableNotifications: String
    val maybeLater: String
    val notificationsDeniedTitle: String
    val notificationsDeniedDesc: String
    val openSettings: String

    // Settings & Profile
    val profileAndSettings: String
    val personal: String
    val editProfile: String
    val editProfileSubtitle: String
    val changePasswordSubtitle: String
    val app: String
    val helpAndContactSubtitle: String
    val rateAppSubtitle: String
    val aboutAppSubtitle: String
    val danger: String
    val deleteAccountSubtitle: String
    val logoutConfirmTitle: String
    val logoutConfirmMessage: String
    val yesLogout: String
    val deleteConfirmText: String
    val oldPassword: String
    val newPassword: String
    val confirmPassword: String
    val oldPasswordPlaceholder: String
    val newPasswordPlaceholder: String
    val confirmPasswordPlaceholder: String
    val saveSuccessfully: String
    val enterName: String
    val chooseUsername: String
    val enterLocation: String
    val appDescription: String
    val version: String
    val user: String
    val deleteAccountTitle: String
    
    // About
    val fullControl: String
    val fullControlDesc: String
    val tournamentSystem: String
    val tournamentSystemDesc: String
    val easyBooking: String
    val easyBookingDesc: String
    val allRightsReserved: String

    val upcoming: String
    val ongoing: String
    val finished: String

    // Add Stadium / Pitch
    val newStadium: String
    val locationInfo: String
    val preciseAddress: String
    val addressPlaceholder: String
    val assignOwner: String
    val technicalInfo: String
    val fieldCapacity: String
    val hourlyPrice: String
    val workingHoursAndImages: String
    val workingHours: String
    val addPhoto: String
    val sportType: String
    val football: String
    val tennis: String

    // Bottom Nav
    val tabPanel: String
    val tabHome: String
    val tabStadium: String
    val tabSchedule: String
    val tabRevenue: String
    val tabRoles: String
    val tabCoaches: String

    // Add Coach / User
    val addCoach: String
    val addPlayer: String
    val addDistrictAdmin: String
    val specialty: String
    val experience: String
    val availability: String
    val loginEmail: String
    val createUser: String
    val userTypeQuestion: String
    val newEmployee: String
    val newEmployeeSubtitle: String
    val coachProfile: String
    val coachProfileSubtitle: String
    val loginEmailPlaceholder: String
    val randomPassword: String
    val passwordPlaceholder: String
    val chooseArea: String
    val smsHint: String

    // Tournaments
    val tournamentDetails: String
    val tournamentName: String
    val tournamentDate: String
    val tournamentTime: String
    val prizeFund: String
    val entryFee: String
    val participants: String
    val register: String
    val organizer: String
    val noTournaments: String

    // Stadium List
    val confirmDelete: String
    val deleteConfirmMsg: String
    val schedule: String
    val unknown: String
    val fieldCount: String
    val uzsPerHour: String
    val underRepair: String
    val closed: String

    // Reports
    val financialReport: String
    val incomeMonitoring: String
    val incomeOverview: String
    val growth: String
    val customers: String
    val weeklyAnalysis: String
    val revenueDynamics: String
    val dailyDetails: String
    val filter: String
    val noDataYet: String
    val totalRevenue: String
    val activeStadiums: String
    val totalTournaments: String
    val totalMatches: String

    // Stadium Details
    val stadiumInfo: String
    val selectDay: String
    val duration: String
    val freeSlots: String
    val nearestSlot: String
    val noSlotsToday: String
    val infoNotAvailable: String
    val editStadium: String
    val understand: String
    val free: String
    val booked: String
    val past: String
    val selected: String
    val bookNow: String
    val pitchName: String
    val field: String
    val showBooked: String
    val showNames: String
    val statusBookedWord: String
    val statusBookedSentence: String
    val statusAvailableWord: String
    val statusAvailableSentence: String
    val statusSelectedWord: String
    val statusSelectedSentence: String
    val statusPastWord: String
    val statusPastSentence: String

    // Errors
    val districtScopeViolation: String
    val dataIntegrityViolation: String
    val bookingTimeAlreadyTaken: String

    val coachInfo: String
    val experienceYears: String
    val reviews: String
    val featureComingSoon: String
}

class EnLanguage : Language {
    override val welcome = "Welcome!"
    override val loginDescription = "Enter your details to log in to the system"
    override val username = "Username"
    override val password = "Password"
    override val login = "Login"
    override val noAccount = "Don't have an account? "
    override val contactAdmin = "Contact admin"
    override val loading = "Loading..."
    override val error = "Error"
    override val success = "Success"
    override val settings = "Settings"
    override val profile = "Profile"
    override val logout = "Logout"
    override val back = "Back"
    override val save = "Save"
    override val cancel = "Cancel"
    override val delete = "Delete"
    override val edit = "Edit"
    override val add = "Add"
    override val search = "Search"
    override val phoneNumber = "Phone Number"
    override val fullName = "Full Name"
    override val location = "Location"
    override val stadium = "Stadium"
    override val address = "Address"
    override val capacity = "Capacity"
    override val price = "Price"
    override val openTime = "Open Time"
    override val closeTime = "Close Time"
    override val description = "Description"
    override val status = "Status"
    override val active = "Active"
    override val inactive = "Inactive"
    override val chooseRegion = "Choose Region"
    override val chooseDistrict = "Choose District"
    override val selectRole = "Select Role"
    override val createAccount = "Create Account"
    override val changePassword = "Change Password"
    override val helpAndContact = "Help & Contact"
    override val rateApp = "Rate App"
    override val aboutApp = "About App"
    override val dangerZone = "Danger Zone"
    
    override val management = "Management"
    override val quickActions = "Quick Actions"
    override val todaySchedule = "Today's Schedule"
    override val systemManagement = "System Management"
    override val myStadium = "My Stadium"
    override val stadiums = "STADIUMS"
    override val activeFields = "Active fields"
    override val users = "USERS"
    override val totalMembers = "Total members"
    override val tournaments = "TOURNAMENTS"
    override val totalEvents = "Total events"
    override val revenue = "REVENUE"
    override val addStadium = "Add Stadium"
    override val addEmployee = "Add Employee"
    override val createTournament = "Create Tournament"
    override val accessRestricted = "Access Restricted"
    override val switchAccount = "Switch account"
    override val refresh = "Refresh"
    override val noBookingsToday = "No bookings for today"
    override val latestMatches = "Upcoming Matches"
    
    override val notifications = "Notifications"
    override val notifyUsers = "Notify users"
    override val msgType = "MESSAGE TYPE"
    override val booking = "Booking"
    override val match = "Match"
    override val tournament = "Tournament"
    override val system = "System"
    override val title = "TITLE"
    override val msgBody = "MESSAGE BODY"
    override val sendToAll = "Send to All"
    override val pushHint = "All users will receive a push notification."
    override val titleHint = "e.g.: New tournament announcement"
    override val bodyHint = "Write your message in detail..."
    override val notificationRationaleTitle = "Stay Updated"
    override val notificationRationaleDesc = "We use notifications to remind you about important updates like:"
    override val notificationBenefit1 = "Match reminders"
    override val notificationBenefit2 = "Booking status changes"
    override val notificationBenefit3 = "Tournament updates"
    override val notificationBenefit4 = "Important announcements"
    override val enableNotifications = "Enable Notifications"
    override val maybeLater = "Maybe Later"
    override val notificationsDeniedTitle = "Notifications Disabled"
    override val notificationsDeniedDesc = "You've disabled notifications. To stay updated on tournaments and bookings, please enable them in settings."
    override val openSettings = "Open Settings"

    override val tabPanel = "Panel"
    override val tabHome = "Home"
    override val tabStadium = "Stadium"
    override val tabSchedule = "Schedule"
    override val tabRevenue = "Revenue"
    override val tabRoles = "Roles"
    override val tabCoaches = "Coaches"

    override val profileAndSettings = "Profile and Settings"
    override val personal = "PERSONAL"
    override val editProfile = "Edit Profile"
    override val editProfileSubtitle = "Name, employees and information"
    override val changePasswordSubtitle = "To ensure security"
    override val app = "APP"
    override val helpAndContactSubtitle = "Contact us"
    override val rateAppSubtitle = "Your opinion is important to us"
    override val aboutAppSubtitle = "Version"
    override val danger = "DANGER"
    override val deleteAccountSubtitle = "Data cannot be recovered"
    override val logoutConfirmTitle = "Logout"
    override val logoutConfirmMessage = "Are you sure you want to log out?"
    override val yesLogout = "Yes, logout"
    override val deleteConfirmText = "To confirm deletion of your account, enter your username below:"
    override val oldPassword = "Old Password"
    override val newPassword = "New Password"
    override val confirmPassword = "Confirm Password"
    override val oldPasswordPlaceholder = "Enter old password"
    override val newPasswordPlaceholder = "Enter new password"
    override val confirmPasswordPlaceholder = "Re-enter new password"
    override val saveSuccessfully = "Profile updated successfully"
    override val enterName = "Enter your name"
    override val chooseUsername = "Choose a username"
    override val enterLocation = "Enter your location"
    override val appDescription = "FootTop Business - Management system for stadium owners and administrators."
    override val version = "Version"
    override val user = "User"
    override val deleteAccountTitle = "Delete Account"
    
    override val fullControl = "Full Control"
    override val fullControlDesc = "Track all bookings and financial reports at your stadium in one place."
    override val tournamentSystem = "Tournament System"
    override val tournamentSystemDesc = "Organize your own tournaments, manage teams and grow the football community."
    override val easyBooking = "Easy Booking"
    override val easyBookingDesc = "Provide customers with real-time view and booking of available slots."
    override val allRightsReserved = "All rights reserved."

    override val upcoming = "Upcoming"
    override val ongoing = "Ongoing"
    override val finished = "Finished"

    override val newStadium = "New Stadium"
    override val locationInfo = "Location Information"
    override val preciseAddress = "PRECISE ADDRESS"
    override val addressPlaceholder = "Street, building number"
    override val assignOwner = "Assign Owner"
    override val technicalInfo = "Technical Information"
    override val fieldCapacity = "FIELDS"
    override val hourlyPrice = "HOURLY PRICE"
    override val workingHoursAndImages = "Working Hours & Images"
    override val workingHours = "WORKING HOURS"
    override val addPhoto = "Add Photo"
    override val sportType = "SPORT TYPE"
    override val football = "Football"
    override val tennis = "Tennis"

    override val addCoach = "Add Coach"
    override val addPlayer = "Add Player"
    override val addDistrictAdmin = "Add District Admin"
    override val specialty = "Specialty"
    override val experience = "Experience"
    override val availability = "Availability"
    override val loginEmail = "LOGIN (EMAIL)"
    override val createUser = "Create User"
    override val userTypeQuestion = "What type of user do you want to create?"
    override val newEmployee = "New Employee"
    override val newEmployeeSubtitle = "Create a login account for Admin or Owner"
    override val coachProfile = "Coach Profile"
    override val coachProfileSubtitle = "Assign coaching authority and info to existing user"
    override val loginEmailPlaceholder = "coach@malaeb.uz"
    override val randomPassword = "Random password"
    override val passwordPlaceholder = "Enter password"
    override val chooseArea = "CHOOSE AREA"
    override val smsHint = "Login and password will be sent via SMS"

    override val tournamentDetails = "Tournament Details"
    override val tournamentName = "Name"
    override val tournamentDate = "Date"
    override val tournamentTime = "Time"
    override val prizeFund = "Prize Fund"
    override val entryFee = "Entry Fee"
    override val participants = "Participants"
    override val register = "Register"
    override val organizer = "Organizer"
    override val noTournaments = "No tournaments found"

    override val confirmDelete = "Confirm Delete"
    override val deleteConfirmMsg = "Are you sure you want to delete this stadium?"
    override val schedule = "Schedule"
    override val unknown = "Unknown"
    override val fieldCount = "fields"
    override val uzsPerHour = "UZS/hr"
    override val underRepair = "Under Repair"
    override val closed = "Closed"

    override val financialReport = "Financial Report"
    override val incomeMonitoring = "Income and analytics monitoring"
    override val incomeOverview = "INCOME OVERVIEW"
    override val growth = "GROWTH"
    override val customers = "CUSTOMERS"
    override val weeklyAnalysis = "Weekly analysis"
    override val revenueDynamics = "Revenue dynamics"
    override val dailyDetails = "Daily details"
    override val filter = "Filter"
    override val noDataYet = "No data yet"
    override val totalRevenue = "Total revenue"
    override val activeStadiums = "Active stadiums"
    override val totalTournaments = "Total tournaments"
    override val totalMatches = "Total matches"

    override val stadiumInfo = "Stadium Information"
    override val selectDay = "Select Day"
    override val duration = "Duration"
    override val freeSlots = "Free Slots"
    override val nearestSlot = "Nearest available slot"
    override val noSlotsToday = "No slots for this day"
    override val infoNotAvailable = "Information not available"
    override val editStadium = "Edit Stadium"
    override val understand = "I understand"
    override val free = "Free"
    override val booked = "Booked"
    override val past = "Past"
    override val selected = "Selected"
    override val bookNow = "Book Now"
    override val pitchName = "Pitch Name"
    override val field = "Field"
    override val showBooked = "Show already booked slots"
    override val showNames = "Show names"
    override val statusBookedWord = "Booked"
    override val statusBookedSentence = "This slot is already booked."
    override val statusAvailableWord = "Available"
    override val statusAvailableSentence = "This slot is available for booking."
    override val statusSelectedWord = "Selected"
    override val statusSelectedSentence = "You have selected this slot."
    override val statusPastWord = "Past"
    override val statusPastSentence = "This time slot has already passed."

    override val districtScopeViolation = "You can only create stadiums in your own assigned district."
    override val dataIntegrityViolation = "Data integrity error. This information may be used by other records."
    override val bookingTimeAlreadyTaken = "This time slot is already booked. Please choose another time."

    override val coachInfo = "Coach Information"
    override val experienceYears = "years"
    override val reviews = "Reviews"
    override val featureComingSoon = "This feature will be added in future versions"
}

class RuLanguage : Language {
    override val welcome = "Добро пожаловать!"
    override val loginDescription = "Введите свои данные, чтобы войти в систему"
    override val username = "Имя пользователя"
    override val password = "Пароль"
    override val login = "Войти"
    override val noAccount = "Нет аккаунта? "
    override val contactAdmin = "Свяжитесь с админом"
    override val loading = "Загрузка..."
    override val error = "Ошибка"
    override val success = "Успех"
    override val settings = "Настройки"
    override val profile = "Профиль"
    override val logout = "Выйти"
    override val back = "Назад"
    override val save = "Сохранить"
    override val cancel = "Отмена"
    override val delete = "Удалить"
    override val edit = "Редактировать"
    override val add = "Добавить"
    override val search = "Поиск"
    override val phoneNumber = "Номер телефона"
    override val fullName = "Полное имя"
    override val location = "Местоположение"
    override val stadium = "Стадион"
    override val address = "Адрес"
    override val capacity = "Вместимость"
    override val price = "Цена"
    override val openTime = "Время открытия"
    override val closeTime = "Время закрытия"
    override val description = "Описание"
    override val status = "Статус"
    override val active = "Активен"
    override val inactive = "Неактивен"
    override val chooseRegion = "Выберите регион"
    override val chooseDistrict = "Выберите район"
    override val selectRole = "Выберите роль"
    override val createAccount = "Создать аккаунт"
    override val changePassword = "Изменить пароль"
    override val helpAndContact = "Помощь и контакты"
    override val rateApp = "Оценить приложение"
    override val aboutApp = "О приложении"
    override val dangerZone = "Опасная зона"
    
    override val management = "Управление"
    override val quickActions = "Быстрые действия"
    override val todaySchedule = "Сегодняшнее расписание"
    override val systemManagement = "Управление системой"
    override val myStadium = "Мой стадион"
    override val stadiums = "СТАДИОНЫ"
    override val activeFields = "Активные поля"
    override val users = "ПОЛЬЗОВАТЕЛИ"
    override val totalMembers = "Всего участников"
    override val tournaments = "ТУРНИРЫ"
    override val totalEvents = "Всего событий"
    override val revenue = "ДОХОД"
    override val addStadium = "Добавить стадион"
    override val addEmployee = "Добавить сотрудника"
    override val createTournament = "Создать турнир"
    override val accessRestricted = "Доступ ограничен"
    override val switchAccount = "Сменить аккаунт"
    override val refresh = "Обновить"
    override val noBookingsToday = "На сегодня бронирований нет"
    override val latestMatches = "Ближайшие матчи"
    
    override val notifications = "Уведомления"
    override val notifyUsers = "Уведомить пользователей"
    override val msgType = "ТИП СООБЩЕНИЯ"
    override val booking = "Бронирование"
    override val match = "Матч"
    override val tournament = "Турнир"
    override val system = "Система"
    override val title = "ЗАГОЛОВОК"
    override val msgBody = "ТЕКСТ СООБЩЕНИЯ"
    override val sendToAll = "Отправить всем"
    override val pushHint = "Все пользователи получат пуш-уведомление."
    override val titleHint = "Напр.: Объявление о новом турнире"
    override val bodyHint = "Напишите сообщение подробно..."
    override val notificationRationaleTitle = "Будьте в курсе"
    override val notificationRationaleDesc = "Мы используем уведомления, чтобы напоминать вам о важных обновлениях, таких как:"
    override val notificationBenefit1 = "Напоминания о матчах"
    override val notificationBenefit2 = "Изменения статуса бронирования"
    override val notificationBenefit3 = "Обновления турниров"
    override val notificationBenefit4 = "Важные объявления"
    override val enableNotifications = "Включить уведомления"
    override val maybeLater = "Может позже"
    override val notificationsDeniedTitle = "Уведомления отключены"
    override val notificationsDeniedDesc = "Вы отключили уведомления. Чтобы быть в курсе турниров и бронирований, пожалуйста, включите их в настройках."
    override val openSettings = "Открыть настройки"

    override val tabPanel = "Панель"
    override val tabHome = "Главная"
    override val tabStadium = "Стадион"
    override val tabSchedule = "Расписание"
    override val tabRevenue = "Доход"
    override val tabRoles = "Роли"
    override val tabCoaches = "Тренеры"

    override val profileAndSettings = "Профиль и Настройки"
    override val personal = "ЛИЧНОЕ"
    override val editProfile = "Редактировать профиль"
    override val editProfileSubtitle = "Имя, сотрудники и информация"
    override val changePasswordSubtitle = "Для обеспечения безопасности"
    override val app = "ПРИЛОЖЕНИЕ"
    override val helpAndContactSubtitle = "Свяжитесь с нами"
    override val rateAppSubtitle = "Ваше мнение важно для нас"
    override val aboutAppSubtitle = "Версия"
    override val danger = "ОПАСНО"
    override val deleteAccountSubtitle = "Данные не могут быть восстановлены"
    override val logoutConfirmTitle = "Выход"
    override val logoutConfirmMessage = "Вы уверены, что хотите выйти из системы?"
    override val yesLogout = "Да, выйти"
    override val deleteConfirmText = "Для подтверждения удаления аккаунта введите свое имя пользователя ниже:"
    override val oldPassword = "Старый пароль"
    override val newPassword = "Новый пароль"
    override val confirmPassword = "Подтвердите пароль"
    override val oldPasswordPlaceholder = "Введите старый пароль"
    override val newPasswordPlaceholder = "Введите новый пароль"
    override val confirmPasswordPlaceholder = "Введите новый пароль еще раз"
    override val saveSuccessfully = "Профиль успешно обновлен"
    override val enterName = "Введите ваше имя"
    override val chooseUsername = "Выберите имя пользователя"
    override val enterLocation = "Введите ваше местоположение"
    override val appDescription = "FootTop Business - система управления для владельцев стадионов и администраторов."
    override val version = "Версия"
    override val user = "Пользователь"
    override val deleteAccountTitle = "Удалить аккаунт"
    
    override val fullControl = "Полный контроль"
    override val fullControlDesc = "Отслеживайте все бронирования и финансовые отчеты вашего стадиона в одном месте."
    override val tournamentSystem = "Система турниров"
    override val tournamentSystemDesc = "Организуйте свои турниры, управляйте командами и развивайте футбольное сообщество."
    override val easyBooking = "Легкое бронирование"
    override val easyBookingDesc = "Предоставьте клиентам возможность просмотра и бронирования свободных слотов в реальном времени."
    override val allRightsReserved = "Все права защищены."

    override val upcoming = "Ожидается"
    override val ongoing = "Идет"
    override val finished = "Завершено"

    override val newStadium = "Новый стадион"
    override val locationInfo = "Информация о местоположении"
    override val preciseAddress = "ТОЧНЫЙ АДРЕС"
    override val addressPlaceholder = "Улица, номер дома"
    override val assignOwner = "Назначить владельца"
    override val technicalInfo = "Техническая информация"
    override val fieldCapacity = "ПОЛЯ"
    override val hourlyPrice = "ПОЧАСОВАЯ ЦЕНА"
    override val workingHoursAndImages = "Рабочее время и изображения"
    override val workingHours = "РАБОЧЕЕ ВРЕМЯ"
    override val addPhoto = "Добавить фото"
    override val sportType = "ТИП СПОРТА"
    override val football = "Футбол"
    override val tennis = "Теннис"

    override val addCoach = "Добавить тренера"
    override val addPlayer = "Добавить игрока"
    override val addDistrictAdmin = "Добавить админа района"
    override val specialty = "Специализация"
    override val experience = "Опыт"
    override val availability = "Доступность"
    override val loginEmail = "ЛОГИН (EMAIL)"
    override val createUser = "Создать пользователя"
    override val userTypeQuestion = "Какой тип пользователя вы хотите создать?"
    override val newEmployee = "Новый сотрудник"
    override val newEmployeeSubtitle = "Создать учетную запись для админа или владельца"
    override val coachProfile = "Профиль тренера"
    override val coachProfileSubtitle = "Назначить полномочия и инфо тренера существующему пользователю"
    override val loginEmailPlaceholder = "coach@malaeb.uz"
    override val randomPassword = "Случайный пароль"
    override val passwordPlaceholder = "Введите пароль"
    override val chooseArea = "ВЫБЕРИТЕ ОБЛАСТЬ"
    override val smsHint = "Логин и пароль будут отправлены по SMS"

    override val tournamentDetails = "Детали турнира"
    override val tournamentName = "Название"
    override val tournamentDate = "Дата"
    override val tournamentTime = "Время"
    override val prizeFund = "Призовой фонд"
    override val entryFee = "Взнос за участие"
    override val participants = "Участники"
    override val register = "Регистрация"
    override val organizer = "Организатор"
    override val noTournaments = "Турниры не найдены"

    override val confirmDelete = "Подтверждение удаления"
    override val deleteConfirmMsg = "Вы уверены, что хотите удалить этот стадион?"
    override val schedule = "Расписание"
    override val unknown = "Неизвестно"
    override val fieldCount = "поля"
    override val uzsPerHour = "сумм/ч"
    override val underRepair = "На ремонте"
    override val closed = "Закрыто"

    override val financialReport = "Финансовый отчет"
    override val incomeMonitoring = "Мониторинг доходов и аналитики"
    override val incomeOverview = "ОБЩИЙ СЧЕТ"
    override val growth = "РОСТ"
    override val customers = "КЛИЕНТЫ"
    override val weeklyAnalysis = "Еженедельный анализ"
    override val revenueDynamics = "Динамика доходов"
    override val dailyDetails = "Дневные детали"
    override val filter = "Фильтр"
    override val noDataYet = "Пока нет данных"
    override val totalRevenue = "Общий доход"
    override val activeStadiums = "Активные стадионы"
    override val totalTournaments = "Всего турниров"
    override val totalMatches = "Всего матчей"

    override val stadiumInfo = "Информация о стадионе"
    override val selectDay = "Выберите день"
    override val duration = "Продолжительность"
    override val freeSlots = "Свободные слоты"
    override val nearestSlot = "Ближайшее свободное время"
    override val noSlotsToday = "На этот день слотов нет"
    override val infoNotAvailable = "Информация недоступна"
    override val editStadium = "Редактировать стадион"
    override val understand = "Понятно"
    override val free = "Свободно"
    override val booked = "Занято"
    override val past = "Прошло"
    override val selected = "Выбрано"
    override val bookNow = "Забронировать"
    override val pitchName = "Название поля"
    override val field = "Поле"
    override val showBooked = "Показать уже забронированные слоты"
    override val showNames = "Показать имена"
    override val statusBookedWord = "Занято"
    override val statusBookedSentence = "Это время уже забронировано."
    override val statusAvailableWord = "Свободно"
    override val statusAvailableSentence = "Этот слот доступен для бронирования."
    override val statusSelectedWord = "Выбрано"
    override val statusSelectedSentence = "Вы выбрали этот слот."
    override val statusPastWord = "Прошло"
    override val statusPastSentence = "Это время уже прошло."

    override val districtScopeViolation = "Вы можете создавать стадионы только в своем закрепленном районе."
    override val dataIntegrityViolation = "Ошибка целостности данных. Эта информация может использоваться другими записями."
    override val bookingTimeAlreadyTaken = "Это время уже занято. Пожалуйста, выберите другое время."

    override val coachInfo = "Информация о тренере"
    override val experienceYears = "лет"
    override val reviews = "Отзывы"
    override val featureComingSoon = "Эта функция будет добавлена в будущих версиях"
}

class UzLanguage : Language {
    override val welcome = "Xush kelibsiz!"
    override val loginDescription = "Tizimga kirish uchun ma'lumotlaringizni kiriting"
    override val username = "Foydalanuvchi nomi"
    override val password = "Parol"
    override val login = "Kirish"
    override val noAccount = "Hisobingiz yo'qmi? "
    override val contactAdmin = "Admin bilan bog'laning"
    override val loading = "Yuklanmoqda..."
    override val error = "Xatolik"
    override val success = "Muvaffaqiyatli"
    override val settings = "Sozlamalar"
    override val profile = "Profil"
    override val logout = "Chiqish"
    override val back = "Orqaga"
    override val save = "Saqlash"
    override val cancel = "Bekor qilish"
    override val delete = "O'chirish"
    override val edit = "Tahrirlash"
    override val add = "Qo'shish"
    override val search = "Qidirish"
    override val phoneNumber = "Telefon raqami"
    override val fullName = "To'liq ism"
    override val location = "Manzil"
    override val stadium = "Stadion"
    override val address = "Aniq manzil"
    override val capacity = "Sig'imi"
    override val price = "Narxi"
    override val openTime = "Ochilish vaqti"
    override val closeTime = "Yopilish vaqti"
    override val description = "Tavsif"
    override val status = "Holat"
    override val active = "Aktiv"
    override val inactive = "Nofaol"
    override val chooseRegion = "Viloyatni tanlang"
    override val chooseDistrict = "Tumanni tanlang"
    override val selectRole = "Rol tanlang"
    override val createAccount = "Hisob yaratish"
    override val changePassword = "Parolni o'zgartirish"
    override val helpAndContact = "Yordam va Aloqa"
    override val rateApp = "Ilovani baholang"
    override val aboutApp = "Ilova haqida"
    override val dangerZone = "Xavfli zona"
    
    override val management = "Boshqaruv"
    override val quickActions = "Tezkor amallar"
    override val todaySchedule = "Bugungi jadval"
    override val systemManagement = "Tizim Boshqaruvi"
    override val myStadium = "Mening Stadionim"
    override val stadiums = "STADIONLAR"
    override val activeFields = "Aktiv maydonlar"
    override val users = "FOYDALANUVCHILAR"
    override val totalMembers = "Jami a'zolar"
    override val tournaments = "TURNIRLAR"
    override val totalEvents = "Jami tadbirlar"
    override val revenue = "DAROMAD"
    override val addStadium = "Stadion qo'sh"
    override val addEmployee = "Xodim qo'shish"
    override val createTournament = "Turnir yarat"
    override val accessRestricted = "Kirish cheklangan"
    override val switchAccount = "Boshqa hisobga o'tish"
    override val refresh = "Yangilash"
    override val noBookingsToday = "Bugun uchun bandlar yo'q"
    override val latestMatches = "Yaqin oradagi o'yinlar"
    
    override val notifications = "Bildirishnomalar"
    override val notifyUsers = "Foydalanuvchilarni xabardor qiling"
    override val msgType = "XABAR TURI"
    override val booking = "Band qilish"
    override val match = "O'yin"
    override val tournament = "Turnir"
    override val system = "Tizim"
    override val title = "SARLAVHA"
    override val msgBody = "XABAR MATNI"
    override val sendToAll = "Barchaga yuborish"
    override val pushHint = "Xabar yuborilganda barcha foydalanuvchilarga push-xabarnoma boradi."
    override val titleHint = "Masalan: Yangi turnir e'loni"
    override val bodyHint = "Xabaringizni batafsil yozing..."
    override val notificationRationaleTitle = "Xabardor bo'ling"
    override val notificationRationaleDesc = "Muhim yangilanishlar haqida eslatib turish uchun bildirishnomalardan foydalanamiz:"
    override val notificationBenefit1 = "O'yin eslatmalari"
    override val notificationBenefit2 = "Bron holati o'zgarishi"
    override val notificationBenefit3 = "Turnir yangiliklari"
    override val notificationBenefit4 = "Muhim e'lonlar"
    override val enableNotifications = "Bildirishnomalarni yoqish"
    override val maybeLater = "Keyinroq"
    override val notificationsDeniedTitle = "Bildirishnomalar o'chirilgan"
    override val notificationsDeniedDesc = "Siz bildirishnomalarni taqiqlab qo'ygansiz. Turnir va bronlar haqida xabardor bo'lish uchun sozlamalardan ruxsat berishingiz kerak."
    override val openSettings = "Sozlamalarni ochish"

    override val tabPanel = "Panel"
    override val tabHome = "Bosh"
    override val tabStadium = "Stadion"
    override val tabSchedule = "Jadval"
    override val tabRevenue = "Daromad"
    override val tabRoles = "Rollar"
    override val tabCoaches = "Coachlar"

    override val profileAndSettings = "Profil va Sozlamalar"
    override val personal = "SHAXSIY"
    override val editProfile = "Profilni tahrirlash"
    override val editProfileSubtitle = "Ism, xodimlar va ma'lumotlar"
    override val changePasswordSubtitle = "Xavfsizlikni ta'minlash uchun"
    override val app = "ILOVA"
    override val helpAndContactSubtitle = "Biz bilan bog'laning"
    override val rateAppSubtitle = "Fikringiz biz uchun muhim"
    override val aboutAppSubtitle = "Versiya"
    override val danger = "XAVFLI"
    override val deleteAccountSubtitle = "Ma'lumotlar qaytarilmaydi"
    override val logoutConfirmTitle = "Chiqish"
    override val logoutConfirmMessage = "Hisobingizdan chiqmoqchimisiz?"
    override val yesLogout = "Ha, chiqish"
    override val deleteConfirmText = "Hisobingizni o'chirishni tasdiqlash uchun foydalanuvchi nomingizni pastga kiriting:"
    override val oldPassword = "Eski parol"
    override val newPassword = "Yangi parol"
    override val confirmPassword = "Parolni tasdiqlang"
    override val oldPasswordPlaceholder = "Eski parolni kiriting"
    override val newPasswordPlaceholder = "Yangi parolni kiriting"
    override val confirmPasswordPlaceholder = "Yangi parolni qayta kiriting"
    override val saveSuccessfully = "Profil muvaffaqiyatli yangilandi"
    override val enterName = "Ismingizni kiriting"
    override val chooseUsername = "Username tanlang"
    override val enterLocation = "Manzilingizni kiriting"
    override val appDescription = "FootTop Business - Stadion egalari va administratorlari uchun boshqaruv tizimi."
    override val version = "Versiya"
    override val user = "Foydalanuvchi"
    override val deleteAccountTitle = "Hisobni o'chirish"
    
    override val fullControl = "To'liq Boshqaruv"
    override val fullControlDesc = "Stadioningizdaki barcha band qilingan vaqtlar va moliyaviy hisobotlarni bir joyda kuzatib boring."
    override val tournamentSystem = "Turnirlar Tizimi"
    override val tournamentSystemDesc = "O'z turnirlaringizni tashkil qiling, jamoalarni boshqaring va futbol hamjamiyatini rivojlantiring."
    override val easyBooking = "Oson Band Qilish"
    override val easyBookingDesc = "Mijozlar uchun real vaqt rejimida bo'sh vaqtlarni ko'rish va band qilish imkoniyatini taqdim eting."
    override val allRightsReserved = "Barcha huquqlar himoyalangan."

    override val upcoming = "Kutilmoqda"
    override val ongoing = "Davom etmoqda"
    override val finished = "Tugagan"

    override val newStadium = "Yangi stadion"
    override val locationInfo = "Manzil ma'lumotlari"
    override val preciseAddress = "ANIQ MANZIL"
    override val addressPlaceholder = "Ko'cha, uy raqami"
    override val assignOwner = "Owner biriktirish"
    override val technicalInfo = "Texnik ma'lumotlar"
    override val fieldCapacity = "MAYDONLAR"
    override val hourlyPrice = "SOATLIK NARX (SO'M)"
    override val workingHoursAndImages = "Ish vaqti va Rasmlar"
    override val workingHours = "ISH VAQTI"
    override val addPhoto = "Rasm qo'shish"
    override val sportType = "SPORT TURI"
    override val football = "Futbol"
    override val tennis = "Tennis"

    override val addCoach = "Coach qo'shish"
    override val addPlayer = "O'yinchi qo'shish"
    override val addDistrictAdmin = "Tuman admini qo'shish"
    override val specialty = "Mutaxassislik"
    override val experience = "Tajriba"
    override val availability = "Mavjudlik"
    override val loginEmail = "LOGIN (EMAIL)"
    override val createUser = "Foydalanuvchi yaratish"
    override val userTypeQuestion = "Qaysi turdagi foydalanuvchini yaratmoqchisiz?"
    override val newEmployee = "Yangi xodim"
    override val newEmployeeSubtitle = "Admin yoki Owner uchun tizimga kirish hisobini yaratish"
    override val coachProfile = "Coach profili"
    override val coachProfileSubtitle = "Mavjud foydalanuvchiga coachlik vakolatlarini va ma'lumotlarini biriktirish"
    override val loginEmailPlaceholder = "murabbiy@malaeb.uz"
    override val randomPassword = "Tasodifiy parol"
    override val passwordPlaceholder = "Parol kiriting"
    override val chooseArea = "HUDUDNI TANLANG"
    override val smsHint = "Login va parol SMS orqali yuboriladi"

    override val tournamentDetails = "Turnir Tafsilotlari"
    override val tournamentName = "Nomi"
    override val tournamentDate = "Sana"
    override val tournamentTime = "Vaqt"
    override val prizeFund = "Mukofot jamg'armasi"
    override val entryFee = "Kirish to'lovi"
    override val participants = "Ishtirokchilar"
    override val register = "Ro'yxatdan o'tish"
    override val organizer = "Tashkilotchi"
    override val noTournaments = "Turnirlar topilmadi"

    override val confirmDelete = "O'chirishni tasdiqlang"
    override val deleteConfirmMsg = "stadionini rostdan ham o'chirmoqchimisiz?"
    override val schedule = "Jadval"
    override val unknown = "Noma'lum"
    override val fieldCount = "maydon"
    override val uzsPerHour = "so'm/s"
    override val underRepair = "Ta'mirda"
    override val closed = "Yopiq"

    override val financialReport = "Moliyaviy Hisobot"
    override val incomeMonitoring = "Daromad va tahlillar monitoringi"
    override val incomeOverview = "UMUMIY HISOB"
    override val growth = "O'SISH"
    override val customers = "MIJOZLAR"
    override val weeklyAnalysis = "Haftalik tahlil"
    override val revenueDynamics = "Daromad dinamikasi"
    override val dailyDetails = "Kunlik tafsilotlar"
    override val filter = "Filtrlash"
    override val noDataYet = "Hozircha ma'lumotlar yo'q"
    override val totalRevenue = "Jami daromad"
    override val activeStadiums = "Aktiv stadionlar"
    override val totalTournaments = "Jami turnirlar"
    override val totalMatches = "Jami o'yinlar"

    override val stadiumInfo = "Stadion ma'lumotlari"
    override val selectDay = "Kunni tanlang"
    override val duration = "Davomiylik"
    override val freeSlots = "Bo'sh vaqtlar"
    override val nearestSlot = "Eng yaqin bo'sh vaqt"
    override val noSlotsToday = "Bu kunda slot yo'q"
    override val infoNotAvailable = "Ma'lumot mavjud emas"
    override val editStadium = "Stadionni tahrirlash"
    override val understand = "Tushundim"
    override val free = "Bo'sh"
    override val booked = "Band"
    override val past = "O'tgan"
    override val selected = "Tanlangan"
    override val bookNow = "Bron qilish"
    override val pitchName = "Pitch nomi"
    override val field = "Maydon"
    override val showBooked = "Band qilingan vaqtlarni ko'rsatish"
    override val showNames = "Ismlarni ko'rsatish"
    override val statusBookedWord = "Band"
    override val statusBookedSentence = "Ushbu vaqt allaqachon band qilingan."
    override val statusAvailableWord = "Bo'sh"
    override val statusAvailableSentence = "Ushbu vaqt bron qilish uchun mavjud."
    override val statusSelectedWord = "Tanlangan"
    override val statusSelectedSentence = "Siz ushbu vaqtni tanladingiz."
    override val statusPastWord = "O'tgan"
    override val statusPastSentence = "Ushbu vaqt o'tib ketgan."

    override val districtScopeViolation = "Siz faqat o'zingizga biriktirilgan hududda stadion yarata olasiz."
    override val dataIntegrityViolation = "Ma'lumotlar yaxlitligi xatosi. Ushbu ma'lumot boshqa yozuvlar tomonidan foydalanilayotgan bo'lishi mumkin."
    override val bookingTimeAlreadyTaken = "Ushbu vaqt allaqachon band qilingan. Iltimos, boshqa vaqtni tanlang."

    override val coachInfo = "Murabbiy ma'lumotlari"
    override val experienceYears = "yil"
    override val reviews = "Sharhlar"
    override val featureComingSoon = "Bu funksiya keyingi versiyalarda qo'shiladi"
}
