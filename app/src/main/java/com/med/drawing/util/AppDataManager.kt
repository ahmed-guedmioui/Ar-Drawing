package com.med.drawing.util

import com.med.drawing.core.domain.model.AppData

/**
 * @author Android Devs Academy (Ahmed Guedmioui)
 */
object AppDataManager {

    lateinit var appData: AppData


    class AdType {
        companion object {
            const val facebook = "facebook"
            const val admob = "admob"
        }
    }


}