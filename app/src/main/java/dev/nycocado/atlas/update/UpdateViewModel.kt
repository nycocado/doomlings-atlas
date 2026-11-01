package dev.nycocado.atlas.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// The banner's dismissal lives with the activity: it survives recreation, and a new launch shows the banner again.
@HiltViewModel
class UpdateViewModel
    @Inject
    constructor(
        check: UpdateCheck,
    ) : ViewModel() {
        private val dismissed = MutableStateFlow(false)

        val update: StateFlow<AppUpdate?> = check.update

        val banner: StateFlow<AppUpdate?> =
            combine(update, dismissed) { update, dismissed -> update.takeUnless { dismissed } }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        fun dismiss() {
            dismissed.value = true
        }
    }
