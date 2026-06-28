package com.tonihacks.qalam.ui.words

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tonihacks.qalam.ui.theme.QalamLapis
import com.tonihacks.qalam.ui.theme.QalamLapisC
import com.tonihacks.qalam.ui.theme.Typography

@Composable
fun DictionaryRow(name: String, url: String) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = QalamLapisC,
        modifier = Modifier
            .padding(horizontal = 22.dp, vertical = 4.dp)
            .clickable(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Outlined.VolumeUp,
                contentDescription = null,
                tint = QalamLapis,
                modifier = Modifier.size(18.dp),
            )
            Text(
                name,
                style = Typography.labelLarge,
                color = QalamLapis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint = QalamLapis,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
