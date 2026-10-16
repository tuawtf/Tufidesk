package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.SortOrder
import com.example.ui.StatusFilter
import com.example.ui.TimeFilter

@Composable
fun FilterBar(
    statusFilter: StatusFilter,
    onStatusChange: (StatusFilter) -> Unit,
    timeFilter: TimeFilter,
    onTimeChange: (TimeFilter) -> Unit,
    sortOrder: SortOrder,
    onSortChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Horizontally Scrollable Filter Chips (Status & Time)
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Filters
            TimeFilter.values().forEach { tf ->
                val isSelected = timeFilter == tf
                FilterChip(
                    selected = isSelected,
                    onClick = { onTimeChange(tf) },
                    label = { Text(tf.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(10.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.6f) else Color(0xFF2E2C56)
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4A2E7A),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E1D3B),
                        labelColor = Color(0xFFE2DCF0)
                    )
                )
            }

            Text(
                text = "•",
                modifier = Modifier.padding(horizontal = 2.dp),
                color = Color.White.copy(alpha = 0.3f)
            )

            // Status Filters
            StatusFilter.values().forEach { sf ->
                val isSelected = statusFilter == sf
                FilterChip(
                    selected = isSelected,
                    onClick = { onStatusChange(sf) },
                    label = { Text(sf.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(10.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.6f) else Color(0xFF2E2C56)
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4A2E7A),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E1D3B),
                        labelColor = Color(0xFFE2DCF0)
                    )
                )
            }
        }

        // Sort Button
        Box {
            IconButton(
                onClick = { sortMenuExpanded = true },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("sort_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sırala",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false }
            ) {
                SortOrder.values()
                    .filter { it != SortOrder.PRIORITY_HIGH_FIRST }
                    .forEach { order ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = order.title,
                                    fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                    color = if (sortOrder == order) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onSortChange(order)
                                sortMenuExpanded = false
                            }
                        )
                    }
            }
        }
    }
}
