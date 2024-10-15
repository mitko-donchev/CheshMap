package com.epicmillennium.cheshmap.presentation.ui.components


import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.presentation.ui.lending.Screen

@Composable
fun ViewExpandableFloatingButton(openScreenFromFab: (Screen) -> Unit) {
    ExpandableFloatingActionButton(
        showLabels = false,
        items = arrayListOf(
            FabItem(
                icon = Icons.Default.Add,
                label = "Add new",
                screen = Screen.ADD
            ),
            FabItem(
                icon = Icons.Default.Favorite,
                label = "Favourite",
                screen = Screen.FAVOURITE
            ),
            FabItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                screen = Screen.SETTINGS
            ),
        ),
        openScreenFromFab = openScreenFromFab
    )
}

enum class ExpandableFabState {
    COLLAPSED, EXPANDED
}

class FabItem(
    val icon: ImageVector,
    val label: String,
    val screen: Screen
)

@Composable
fun ExpandableFloatingActionButton(
    items: List<FabItem>,
    showLabels: Boolean = true,
    onStateChanged: ((state: ExpandableFabState) -> Unit)? = null,
    openScreenFromFab: (Screen) -> Unit
) {
    var currentState by remember { mutableStateOf(ExpandableFabState.COLLAPSED) }
    val stateTransition: Transition<ExpandableFabState> =
        updateTransition(targetState = currentState, label = "")

    val stateChange: () -> Unit = {
        currentState = if (stateTransition.currentState == ExpandableFabState.EXPANDED) {
            ExpandableFabState.COLLAPSED
        } else ExpandableFabState.EXPANDED
        onStateChanged?.invoke(currentState)
    }

    val isExpanded = currentState == ExpandableFabState.EXPANDED

    BackHandler(isExpanded) {
        currentState = ExpandableFabState.COLLAPSED
    }

    val customModifier = if (isExpanded) {
        Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                currentState = ExpandableFabState.COLLAPSED
            }
    } else {
        Modifier.fillMaxSize()
    }

    Box(
        modifier = customModifier.padding(14.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom,
            ) {
                items.forEachIndexed { index, fabItem ->
                    SmallFloatingActionButtonRow(
                        item = fabItem,
                        showLabel = showLabels,
                        index = index,
                        totalItems = items.size,
                        isExpanded = isExpanded,
                        onClick = {
                            openScreenFromFab.invoke(fabItem.screen)
                        }
                    )
                }

                FloatingActionButton(
                    modifier = Modifier.padding(top = 10.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = { stateChange() }
                ) {
                    AnimatedIcon(currentState)
                }
            }
        }
    }
}

@Composable
private fun AnimatedIcon(currentState: ExpandableFabState) {
    AnimatedContent(
        targetState = currentState,
        transitionSpec = {
            (fadeIn(animationSpec = tween(300)) +
                    scaleIn(animationSpec = tween(300))).togetherWith(
                fadeOut(animationSpec = tween(300)) + scaleOut(
                    animationSpec = tween(300)
                )
            )
        }, label = "Transition between icons"
    ) { targetState ->
        if (targetState == ExpandableFabState.EXPANDED) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.expanded_fab),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = stringResource(R.string.collapsed_fab),
            )
        }
    }
}

@Composable
fun SmallFloatingActionButtonRow(
    item: FabItem,
    showLabel: Boolean,
    index: Int,
    totalItems: Int,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    // Calculate delay so nearest FABs to the parent appear first
    val delayDuration = (totalItems - index) * 100 // Delay decreases as index increases
    val animationSpec = tween<Float>(durationMillis = 150, delayMillis = delayDuration)
    val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = animationSpec,
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = animationSpec,
        label = "scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(alpha)
            .scale(scale)
    ) {
        if (showLabel) {
            Text(
                text = item.label,
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer, // Match FAB color
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ) // Padding inside the background
                    .clickable(onClick = onClick),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        SmallFloatingActionButton(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            onClick = onClick,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label
            )
        }
    }
}