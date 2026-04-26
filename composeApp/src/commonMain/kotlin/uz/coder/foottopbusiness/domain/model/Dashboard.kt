package uz.coder.foottopbusiness.domain.model

data class Dashboard(
    val activeStadiumsCount: Int,
    val stadiumRevenues: List<StadiumRevenue>,
    val tournamentsCount: Int,
    val usersCount: Int
)