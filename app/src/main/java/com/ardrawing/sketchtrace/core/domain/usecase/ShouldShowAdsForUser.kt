package com.ardrawing.sketchtrace.core.domain.usecase

import android.app.Application
import android.util.Log
import com.ardrawing.sketchtrace.App
import com.ardrawing.sketchtrace.util.CountryChecker

/**
 * @author Ahmed Guedmioui
 */
class ShouldShowAdsForUser(
    private val application: Application
) {

    operator fun invoke() {

        if (App.appData.isSubscribed) {
            App.appData.showAdsForThisUser = false
            Log.d("REVENUE_CUT", "ShouldShowAdsForUser: isSubscribed")
            return
        }

        if (!App.appData.areAdsForOnlyWhiteListCountries) {
            App.appData.showAdsForThisUser = true
            Log.d("REVENUE_CUT", "ShouldShowAdsForUser: not AdsForOnlyWhiteListCountries")
            return
        }

        val countryChecker = CountryChecker(application, CountryChecker.CheckerType.SpeedServer)
        countryChecker.setOnCheckerListener(object : CountryChecker.OnCheckerListener {
            override fun onCheckerCountry(country: String?, userFromGG: Boolean) {
                App.appData.countriesWhiteList.forEach { countryInWhiteList ->
                    if (countryInWhiteList == country) {
                        Log.d("REVENUE_CUT", "ShouldShowAdsForUser: countryInWhiteList")
                        App.appData.showAdsForThisUser = true
                    }
                }
            }

            override fun onCheckerError(error: String?) {
                if (!App.appData.areAdsForOnlyWhiteListCountries) {
                    Log.d("REVENUE_CUT", "ShouldShowAdsForUser: onChecker Country Error")
                    App.appData.showAdsForThisUser = true
                }
            }
        })
    }
}





