package com.example.presentation.screens.wardrobe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*
import com.example.domain.model.PetStatus
import com.example.presentation.pet.LumiPetView

@Composable
fun PetShowcaseCard(
    petStatus: PetStatus,
    evolutionStageTitle: String,
    onPetTouched: () -> Unit,
    onPetPetted: () -> Unit,
    onRenameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(MaterialTheme.spacing.large),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LumiPetView(
                petStatus = petStatus,
                size = 180.dp,
                onPetTouched = onPetTouched,
                onPetPetted = onPetPetted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${petStatus.name} • $evolutionStageTitle",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onRenameClick,
                    modifier = Modifier.size(MaterialTheme.spacing.large)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.desc_rename_lumi),
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = LumiGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(MaterialTheme.spacing.small)
                ) {
                    Text(
                        text = "Level ${petStatus.level}",
                        color = LumiGold,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = "${petStatus.exp} / ${petStatus.expToNextLevel} XP to Level ${petStatus.level + 1}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            val progress = remember(petStatus.exp, petStatus.expToNextLevel) {
                if (petStatus.expToNextLevel > 0) {
                    (petStatus.exp.toFloat() / petStatus.expToNextLevel).coerceIn(0f, 1f)
                } else 1f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MaterialTheme.spacing.small)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = SurfaceHighlight
            )
        }
    }
}
