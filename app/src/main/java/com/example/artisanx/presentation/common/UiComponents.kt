package com.example.artisanx.presentation.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
