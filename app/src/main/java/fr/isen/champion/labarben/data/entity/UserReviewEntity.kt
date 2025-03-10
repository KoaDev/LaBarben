package fr.isen.champion.labarben.data.entity

data class UserReviewEntity (
    val userId: String,
    val firstName: String,
    val lastName: String,
    val rating: Int,
    val review: String
)