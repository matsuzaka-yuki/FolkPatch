package me.bmax.apatch.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun IconTextButton(
    iconRes: Int,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = true,
        backgroundColor = MiuixTheme.colorScheme.secondaryContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = iconRes),
                contentDescription = null
            )
        }
    }
}

@Composable
fun IconTextButton(
    imageVector: ImageVector,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = true,
        backgroundColor = MiuixTheme.colorScheme.secondaryContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = imageVector,
                contentDescription = null
            )
        }
    }
}

@Composable
fun ModuleStateIndicator(
    @DrawableRes icon: Int, color: Color = MiuixTheme.colorScheme.outline
) {
    Image(
        modifier = Modifier.requiredSize(150.dp),
        painter = painterResource(id = icon),
        contentDescription = null,
        alpha = 0.1f,
        colorFilter = ColorFilter.tint(color)
    )
}

@Composable
fun ModuleStateIndicator(
    imageVector: ImageVector, color: Color = MiuixTheme.colorScheme.outline
) {
    Image(
        modifier = Modifier.requiredSize(150.dp),
        imageVector = imageVector,
        contentDescription = null,
        alpha = 0.1f,
        colorFilter = ColorFilter.tint(color)
    )
}

@Composable
fun ModuleLabel(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(containerColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            fontSize = 11.sp,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}