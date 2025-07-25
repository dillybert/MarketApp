package kz.market.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import kz.market.R
import kz.market.domain.models.SupplierSuggestion

@Composable
fun AutoCompleteTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    suggestions: List<SupplierSuggestion>,
    onSuggestionSelected: (String) -> Unit,
    label: @Composable (() -> Unit),
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }

    val filteredSuggestions = remember(value) {
        suggestions.filter {
            it.suggestion.contains(value.text.trim(), ignoreCase = true) && value.text.isNotEmpty()
        }
    }

    val density = LocalDensity.current

    Box(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = filteredSuggestions.isNotEmpty() && value.text.isNotEmpty()
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size
                },
            trailingIcon = {
                if (value.text.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onValueChange(TextFieldValue(""))
                            expanded = false
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_x_circle),
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            label = label,
            singleLine = true,
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(density) { textFieldSize.width.toDp() })
                .heightIn(max = 200.dp),
            properties = PopupProperties(
                focusable = false
            )
        ) {
            filteredSuggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            suggestion.suggestion,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onSuggestionSelected(suggestion.suggestion)
                        expanded = false
                    }
                )
            }
        }
    }
}



