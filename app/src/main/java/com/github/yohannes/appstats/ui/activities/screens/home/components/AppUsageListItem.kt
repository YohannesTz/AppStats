package com.github.yohannes.appstats.ui.activities.screens.home.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.yohannes.appstats.data.models.UsagePercentage
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun AppUsageListItem(
    appUsage: UsagePercentage,
    onClick: (String) -> Unit
) {
    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
        Card(
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    onClick(appUsage.packageName)
                },
            elevation = 4.dp,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Image(
                    painter = rememberDrawablePainter(appUsage.icon),
                    contentDescription = "App Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = appUsage.appName,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        text = appUsage.usageString,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.subtitle1,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GradientProgressbar(
                        indicatorHeight = 8.dp,
                        indicatorPadding = 8.dp,
                        progress = appUsage.usagePercentage.toFloat() / 100
                    )
                }
            }
        }
    }
}