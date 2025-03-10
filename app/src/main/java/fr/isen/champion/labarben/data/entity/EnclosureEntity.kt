package fr.isen.champion.labarben.data.entity

data class EnclosureEntity(
    val id: String = "",
    val id_biomes: String = "",
    val maintenance: Boolean = false,
    val meal: String = "",
    val animals: List<AnimalEntity> = emptyList(),
    val ratings: Map<String, Int> = emptyMap()
)