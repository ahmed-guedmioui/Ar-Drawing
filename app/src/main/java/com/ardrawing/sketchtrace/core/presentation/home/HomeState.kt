package com.ardrawing.sketchtrace.core.presentation.home


/**
 * @author Ahmed Guedmioui
 */
data class HomeState(
     var doubleBackToExitPressedOnce: Boolean = false,
     val showHelperDialog: Boolean = false,
)