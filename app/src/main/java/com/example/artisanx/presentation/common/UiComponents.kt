package com.example.artisanx.presentation.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Roofing
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

// ---------------------------------------------------------------------------
// Shimmer
// ---------------------------------------------------------------------------

@Composable
fun ShimmerBox(modifier: Modifier = Modifier, height: Dp = 16.dp, width: Dp = Dp.Unspecified) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "shimmerAlpha"
    )
    val base = modifier
        .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier.fillMaxWidth())
        .height(height)
        .clip(RoundedCornerShape(6.dp))
        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.15f))
    Box(base)
}

@Composable
fun JobCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerBox(height = 18.dp, width = 200.dp)
            ShimmerBox(height = 14.dp, width = 120.dp)
            ShimmerBox(height = 14.dp, width = 160.dp)
        }
    }
}

@Composable
fun BookingCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ShimmerBox(height = 18.dp, width = 180.dp)
                ShimmerBox(height = 18.dp, width = 80.dp)
            }
            ShimmerBox(height = 40.dp)  // stepper placeholder
            ShimmerBox(height = 40.dp)  // button placeholder
        }
    }
}

@Composable
fun ChatListSkeleton() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ShimmerBox(height = 40.dp, width = 40.dp)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShimmerBox(height = 14.dp, width = 140.dp)
                ShimmerBox(height = 12.dp, width = 200.dp)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Empty states
// ---------------------------------------------------------------------------

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Offline banner
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Rating stars with fractional fill
// ---------------------------------------------------------------------------

@Composable
fun RatingStars(
    rating: Double,
    starSize: Dp = 20.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val fraction = (rating / 5.0).toFloat().coerceIn(0f, 1f)
    Box {
        Row {
            repeat(5) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = tint.copy(alpha = 0.35f),
                    modifier = Modifier.size(starSize)
                )
            }
        }
        Row(
            modifier = Modifier.drawWithContent {
                clipRect(right = size.width * fraction) {
                    this@drawWithContent.drawContent()
                }
            }
        ) {
            repeat(5) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Fullscreen image viewer (no save, just view)
// ---------------------------------------------------------------------------

@Composable
fun FullscreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable(onClick = onDismiss)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Category icon helper
// ---------------------------------------------------------------------------

fun iconForCategory(category: String): ImageVector = when (category.trim().lowercase()) {
    "plumbing" -> Icons.Default.Plumbing
    "electrical" -> Icons.Default.ElectricalServices
    "painting" -> Icons.Default.Brush
    "carpentry" -> Icons.Default.Construction
    "tiling" -> Icons.Default.Build
    "roofing" -> Icons.Default.Roofing
    "cleaning" -> Icons.Default.CleaningServices
    "general" -> Icons.Default.Home
    else -> Icons.Default.Handyman
}

// ---------------------------------------------------------------------------
// Lifecycle-aware refresh trigger — fires the callback every time the
// hosting screen is resumed (initial load + every navigate-back / app-resume)
// ---------------------------------------------------------------------------

@Composable
fun OnLifecycleResume(onResume: () -> Unit) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

// ---------------------------------------------------------------------------
// Offline banner
// ---------------------------------------------------------------------------

@Composable
fun OfflineBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No internet connection",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
