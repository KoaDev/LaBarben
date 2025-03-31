package fr.isen.champion.labarben.data.entity

import java.time.LocalDate

data class EnclosureDetailsResultEntity (
    val userReviews: List<UserReviewEntity>,
    val averageRating: Double,
    val feedingDates: List<LocalDate>
)