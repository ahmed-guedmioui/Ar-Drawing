package com.ardrawing.sketchtrace.core.domain.usecase

import android.app.Application
import android.util.Log
import com.ardrawing.sketchtrace.core.data.DataManager
import com.ardrawing.sketchtrace.util.CountryChecker

/**
 * @author Ahmed Guedmioui
 */
class ShouldShowAdsForUser(
    private val application: Application
) {

    operator fun invoke() {

        if (DataManager.appData.isSubscribed) {
            DataManager.appData.showAdsForThisUser = false
            Log.d("REVENUE_CUT", "ShouldShowAdsForUser: isSubscribed")
            return
        }

        if (!DataManager.appData.areAdsForOnlyWhiteListCountries) {
            DataManager.appData.showAdsForThisUser = true
            Log.d("REVENUE_CUT", "ShouldShowAdsForUser: not AdsForOnlyWhiteListCountries")
            return
        }

        val countryChecker = CountryChecker(application, CountryChecker.CheckerType.SpeedServer)
        countryChecker.setOnCheckerListener(object : CountryChecker.OnCheckerListener {
            override fun onCheckerCountry(country: String?, userFromGG: Boolean) {
                DataManager.appData.countriesWhiteList.forEach { countryInWhiteList ->
                    if (countryInWhiteList == country) {
                        Log.d("REVENUE_CUT", "ShouldShowAdsForUser: countryInWhiteList")
                        DataManager.appData.showAdsForThisUser = true
                    }
                }
            }

            override fun onCheckerError(error: String?) {
                if (!DataManager.appData.areAdsForOnlyWhiteListCountries) {
                    Log.d("REVENUE_CUT", "ShouldShowAdsForUser: onChecker Country Error")
                    DataManager.appData.showAdsForThisUser = true
                }
            }
        })
    }
}





