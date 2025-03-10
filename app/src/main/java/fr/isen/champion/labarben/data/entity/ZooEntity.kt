package fr.isen.champion.labarben.data.entity

data class ZooEntity(
    val id: String = "",
    val color: String = "",
    val name: String = "",
    val enclosures: List<EnclosureEntity> = emptyList()
)