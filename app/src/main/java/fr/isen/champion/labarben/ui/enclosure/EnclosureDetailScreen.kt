package fr.isen.champion.labarben.ui.enclosure

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import fr.isen.champion.labarben.R
import fr.isen.champion.labarben.data.entity.EnclosureDetailsResultEntity
import fr.isen.champion.labarben.data.entity.EnclosureEntity
import fr.isen.champion.labarben.data.entity.UserReviewEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("DefaultLocale")
@Composable
fun EnclosureDetailScreen(
    enclosure: EnclosureEntity?,
    onBack: () -> Unit
) {
    if (enclosure == null) return

    BackHandler {
        onBack()
    }

    var rating by remember { mutableIntStateOf(0) }
    var review by remember { mutableStateOf("") }
    var allReviews by remember { mutableStateOf<List<UserReviewEntity>>(emptyList()) }
    var averageRating by remember { mutableDoubleStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var userRole by remember { mutableStateOf("") }
    var feedingDates by remember { mutableStateOf<List<LocalDate>>(emptyList()) }

    val zooId = enclosure.id_biomes
    val enclosureId = enclosure.id

    LaunchedEffect(enclosure.id) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            isLoading = false
            return@LaunchedEffect
        }

        val userSnapshot = FirebaseDatabase.getInstance()
            .getReference("users/$uid")
            .get().await()

        userRole = userSnapshot.child("role").value?.toString().orEmpty()

        val details = fetchEnclosureDetails(zooId, enclosureId)

        allReviews = details.userReviews
        averageRating = details.averageRating
        feedingDates = details.feedingDates

        val userReview = details.userReviews.firstOrNull { it.userId == uid }
        rating = userReview?.rating ?: 0
        review = userReview?.review.orEmpty()

        isLoading = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.enclosuredetailscreen_label_title) + enclosure.id,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )

                if (userRole != "admin") {
                    UserInterface(enclosure, rating, review, zooId, enclosureId, feedingDates)
                } else {
                    AdminInterface(
                        allReviews = allReviews,
                        averageRating = averageRating,
                        feedingDates = feedingDates,
                        zooId = zooId,
                        enclosureId = enclosureId,
                        onFeedingDatesUpdated = { updatedList -> feedingDates = updatedList }
                    )                }

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { onBack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.enclosuredetailscreen_label_back))
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun UserInterface(
    enclosure: EnclosureEntity,
    initialRating: Int,
    initialReview: String,
    zooId: String,
    enclosureId: String,
    feedingDates: List<LocalDate>,
) {
    var localRating by remember { mutableIntStateOf(initialRating) }
    var localReview by remember { mutableStateOf(initialReview) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {

            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    stringResource(R.string.enclosuredetailscreen_label_animal_title),
                    style = MaterialTheme.typography.titleMedium
                )

                enclosure.animals.forEach { animal ->
                    Text("- ${animal.name}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FeedingCalendar(
                    feedingDates = feedingDates,
                    onDateSelected = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.enclosuredetailscreen_label_rating),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    (1..5).forEach { i ->
                        val starColor =
                            if (i <= localRating) MaterialTheme.colorScheme.secondary else Color.Gray
                        val icon = if (i <= localRating) Icons.Filled.Star else Icons.Outlined.Star

                        Icon(
                            imageVector = icon,
                            contentDescription = stringResource(R.string.enclosuredetailscreen_label_star) + i,
                            tint = starColor,
                            modifier = Modifier
                                .scale(1.2f)
                                .clickable {
                                    localRating = i
                                }
                                .padding(end = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.enclosuredetailscreen_label_review),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = localReview,
                    onValueChange = { newText ->
                        localReview = newText
                    },
                    label = { Text(stringResource(R.string.enclosuredetailscreen_label_review_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        updateUserEnclosureData("ratings", zooId, enclosureId, localRating)
                        updateUserEnclosureData("reviews", zooId, enclosureId, localReview)
                    }
                ) {
                    Text(text = stringResource(R.string.enclosuredetailscreen_label_submit_review))
                }
            }
        }
    }
}



@SuppressLint("DefaultLocale")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AdminInterface(
    allReviews: List<UserReviewEntity>,
    averageRating: Double,
    feedingDates: List<LocalDate>,
    zooId: String,
    enclosureId: String,
    onFeedingDatesUpdated: (List<LocalDate>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (allReviews.isEmpty()) {
                    Text(stringResource(R.string.enclosuredetailscreen_label_no_reviews_yet),
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Text(
                        stringResource(R.string.enclosuredetailscreen_label_average_rating) + String.format(
                            "%.1f",
                            averageRating
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    allReviews.forEach { item ->
                        Text("${item.firstName} ${item.lastName} - ${item.rating} ★")
                        if (item.review.isNotEmpty()) {
                            Text("\"${item.review}\"", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                FeedingCalendar(
                    feedingDates = feedingDates,
                    onDateSelected = { date ->
                        toggleFeedingDateInFirebase(
                            zooId,
                            enclosureId,
                            date,
                            feedingDates
                        ) { updatedList ->
                            onFeedingDatesUpdated(updatedList)
                        }
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun FeedingCalendar(
    feedingDates: List<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentMonth = YearMonth.now()
    val startMonth = currentMonth.minusYears(5)
    val endMonth = currentMonth.plusYears(5)
    val firstDayOfWeek = firstDayOfWeekFromLocale()
    var isCalendarScrolled by remember { mutableStateOf(false) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!isCalendarScrolled) {
            val earliest = feedingDates.minOrNull()
            if (earliest != null) {
                val target = YearMonth.from(earliest).coerceIn(startMonth, endMonth)
                scope.launch {
                    delay(200)
                    state.scrollToMonth(target)
                }
            }
            isCalendarScrolled = true
        }
    }

    HorizontalCalendar(
        modifier = Modifier.fillMaxWidth(),
        state = state,
        monthHeader = { month ->
            val monthYearText = month.yearMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)
            ).replaceFirstChar { it.uppercaseChar() }
            Text(
                stringResource(R.string.enclosuredetailscreen_label_calendar_tile) + monthYearText,
                style = MaterialTheme.typography.titleMedium
            )
        },
        dayContent = { day ->
            if (day.position == DayPosition.MonthDate) {
                val dayDate = day.date
                val isFeedingDay = feedingDates.contains(dayDate)
                Box(
                    Modifier
                        .size(48.dp)
                        .border(1.dp, Color.LightGray)
                        .background(if (isFeedingDay) Color.Red else Color.Transparent)
                        .clickable { onDateSelected(dayDate) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        dayDate.dayOfMonth.toString(),
                        color = if (isFeedingDay) Color.White else Color.Black
                    )
                }
            } else {
                Spacer(Modifier.size(40.dp))
            }
        }
    )
    Spacer(Modifier.height(16.dp))
}

@RequiresApi(Build.VERSION_CODES.O)
private fun toggleFeedingDateInFirebase(
    zooId: String,
    enclosureId: String,
    date: LocalDate,
    currentList: List<LocalDate>,
    onSuccess: (List<LocalDate>) -> Unit
) {
    val dbRef = FirebaseDatabase.getInstance().getReference("zoo")
    dbRef.get().addOnSuccessListener { zooSnapshot ->
        zooSnapshot.children
            .firstOrNull { it.child("id").value?.toString() == zooId }
            ?.child("enclosures")
            ?.children
            ?.firstOrNull { it.child("id").value?.toString() == enclosureId }
            ?.ref
            ?.child("feeding")
            ?.setValue((if (currentList.contains(date)) currentList - date else currentList + date).map { it.toString() })
            ?.addOnSuccessListener {
                onSuccess(if (currentList.contains(date)) currentList - date else currentList + date)
            }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private suspend fun fetchEnclosureDetails(
    zooId: String,
    enclosureId: String
): EnclosureDetailsResultEntity {
    val dbRef = FirebaseDatabase.getInstance().getReference("zoo")
    val userRef = FirebaseDatabase.getInstance().getReference("users")

    val snapshot = dbRef.get().await()

    val enclosureSnapshot = snapshot.children
        .firstOrNull { it.child("id").value?.toString() == zooId }
        ?.child("enclosures")?.children
        ?.firstOrNull { it.child("id").value?.toString() == enclosureId }
        ?: return EnclosureDetailsResultEntity(emptyList(), 0.0, emptyList())

    val ratingsSnapshot = enclosureSnapshot.child("ratings")
    val reviewsSnapshot = enclosureSnapshot.child("reviews")
    val feedingSnapshot = enclosureSnapshot.child("feeding")

    val userIds = (ratingsSnapshot.children.mapNotNull { it.key } +
            reviewsSnapshot.children.mapNotNull { it.key }).toSet()

    val userReviews = userIds.map { uid ->
        val rating = ratingsSnapshot.child(uid).getValue(Int::class.java) ?: 0
        val reviewText = reviewsSnapshot.child(uid).value?.toString().orEmpty()

        val userSnapshot = userRef.child(uid).get().await()
        val firstName = userSnapshot.child("firstName").value?.toString().orEmpty()
        val lastName = userSnapshot.child("lastName").value?.toString().orEmpty()

        UserReviewEntity(uid, firstName, lastName, rating, reviewText)
    }

    val averageRating = userReviews.map { it.rating }.filter { it > 0 }.average().takeIf { it.isFinite() } ?: 0.0

    val feedingDates = feedingSnapshot.children.mapNotNull {
        runCatching { LocalDate.parse(it.value.toString()) }.getOrNull()
    }

    return EnclosureDetailsResultEntity(
        userReviews = userReviews,
        averageRating = averageRating,
        feedingDates = feedingDates
    )
}

private inline fun <reified T> updateUserEnclosureData(
    dataType: String,
    zooId: String,
    enclosureId: String,
    value: T
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    FirebaseDatabase.getInstance().getReference("zoo").get().addOnSuccessListener { snapshot ->
        snapshot.children.firstOrNull { it.child("id").value?.toString() == zooId }
            ?.child("enclosures")?.children
            ?.firstOrNull { it.child("id").value?.toString() == enclosureId }
            ?.ref
            ?.child(dataType)
            ?.child(uid)
            ?.setValue(value)
    }
}