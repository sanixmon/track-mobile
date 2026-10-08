package com.evrenhouse.trackscooter.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.evrenhouse.trackscooter.util.Outlets

/**
 * Global Floating Outlet Picker Button (1:1 with Web UnifiedOutletPicker.jsx).
 * Renders a compact, circular floating trigger with live unit count badge
 * positioned at the bottom-right corner, letting users switch outlet context anywhere.
 */
@Composable
fun UnifiedOutletPicker(
    viewModel: ScooterDataViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val selectedOutlet by viewModel.selectedOutlet.collectAsState()

    val getOutletCount = remember(state.scooters) {
        { outletId: String ->
            if (outletId == "all") {
                state.scooters.size
            } else {
                state.scooters.count { s ->
                    (s.currentOutlet ?: Outlets.getHomeOutletForType(s.type)) == outletId
                }
            }
        }
    }

    Box(modifier = modifier) {
        OutletDropdown(
            selectedOutletId = selectedOutlet,
            onOutletSelected = { viewModel.setSelectedOutlet(it) },
            getOutletCount = getOutletCount,
            compact = true,
        )
    }
}
