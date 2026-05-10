package ph.rentconnect.app

import android.app.Application
import com.mapbox.common.MapboxOptions

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MapboxOptions.accessToken = BuildConfig.MAPBOX_PUBLIC_TOKEN
    }
}
