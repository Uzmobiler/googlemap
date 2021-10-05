package uz.mobiler.googlemap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import uz.mobiler.googlemap.databinding.ActivityMapsBinding


open class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    private val TAG = "MapsActivity"
    lateinit var binding: ActivityMapsBinding

    var INTERVAL = 1000 * 10
    var FASTEST_INTERVAL = 1000 * 5
    var mLocationRequest: LocationRequest? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var mCurrentLocation: Location? = null

    lateinit var map: GoogleMap
    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = INTERVAL.toLong()
        mLocationRequest?.fastestInterval = FASTEST_INTERVAL.toLong()
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        if (!isGooglePlayServicesAvailable()) {
            finish()
        }

        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
    }

    protected open fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClient, mLocationRequest, this
        )
        Log.d(TAG, "Location update started ..............: ")
    }


    fun isGooglePlayServicesAvailable(): Boolean {
        val status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        return if (ConnectionResult.SUCCESS == status) {
            true
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show()
            false
        }
    }

    override fun onStart() {
        super.onStart()
        if (mGoogleApiClient?.isConnected == true) {
            startLocationUpdates()
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient?.disconnect()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if (mCurrentLocation != null) {

            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        mCurrentLocation?.latitude ?: 0.0, mCurrentLocation?.longitude ?: 0.0
                    ), 4.0f
                )
            )
        }
    }

    override fun onLocationChanged(p0: Location) {
        mCurrentLocation = p0
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        Toast.makeText(this, "${p0.latitude} ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    protected open fun stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
            mGoogleApiClient, this
        )
        Log.d(TAG, "Location update stopped .......................")
    }

    override fun onResume() {
        super.onResume()
        if (mGoogleApiClient?.isConnected == true) {
            startLocationUpdates()
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    override fun onConnected(p0: Bundle?) {
        startLocationUpdates()
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

}