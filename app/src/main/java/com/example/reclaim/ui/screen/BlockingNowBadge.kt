package com.example.reclaim.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.ui.theme.ReclaimRed
import com.example.reclaim.ui.theme.ReclaimRedBg

@Composable
fun BlockingNowBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ReclaimRedBg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = "Blocking now",
            color = ReclaimRed,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
