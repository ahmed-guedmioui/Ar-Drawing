package com.ardrawing.sketchtrace.splash.domain.usecase

import android.app.Application
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.util.CountryChecker

/**
 * @author Ahmed Guedmioui
 */
class ShouldShowAdsForUser(
    private val application: Application
) {

    operator fun invoke() {
        if (!DataManager.appData.areAdsForOnlyWhiteListCountries) {
            DataManager.appData.showAdsForThisUser = true
            return
        }

        val countryChecker = CountryChecker(application, CountryChecker.CheckerType.SpeedServer)
        countryChecker.setOnCheckerListener(object : CountryChecker.OnCheckerListener {
            override fun onCheckerCountry(country: String?, userFromGG: Boolean) {
                DataManager.appData.countriesWhiteList.forEach { countryInWhiteList ->
                    if (countryInWhiteList == country) {
                        DataManager.appData.showAdsForThisUser = true
                    }
                }
            }

            override fun onCheckerError(error: String?) {
                if (!DataManager.appData.areAdsForOnlyWhiteListCountries) {
                    DataManager.appData.showAdsForThisUser = true
                }
            }
        })
    }
}