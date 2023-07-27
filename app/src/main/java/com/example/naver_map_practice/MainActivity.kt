package com.example.naver_map_practice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.naver_map_practice.databinding.ActivityMainBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapFragment
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource : FusedLocationSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentManager = supportFragmentManager
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fragmentManager.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)
        mapView = mapFragment

        binding.dialog.setOnClickListener {
            DialogBuilder(this).createDialog()
        }

        if (checkLocationService()) {
            permissionCheck()
        } else {
            Toast.makeText(this, "GPS를 켜주세요", Toast.LENGTH_LONG).show()
        }
    }

    private fun permissionCheck() {
        val preference = getPreferences(Context.MODE_PRIVATE)
        val isFirstCheck = preference.getBoolean("isFirstPermissionCheck", true)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            ) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("현재 위치를 확인하시려면 위치 권한을 허용해주세요.")
                builder.setPositiveButton("확인") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION_CODE,
                    )
                }
                builder.setNegativeButton("취소") { _, _ -> }
                builder.show()
            } else {
                if (isFirstCheck) {
                    preference.edit().putBoolean("isFirstPermissionCheck", false).apply()
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION_CODE,
                    )
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("현재 위치를 확인하시려면 설정에서 위치 권한을 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { dialog, which ->
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:$packageName"),
                        )
                        startActivity(intent)
                    }
                    builder.setNegativeButton("취소") { _, _ -> }
                    builder.show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if(locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)){
            if(!locationSource.isActivated){
                Log.d("bbotto", "1")
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            else{
                naverMap.locationTrackingMode = LocationTrackingMode.Follow
                Log.d("bbotto", "2")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == ACCESS_FINE_LOCATION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, LOCATION_PERMISSION_MESSAGE, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, LOCATION_PERMISSION_DENIED_MESSAGE, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkLocationService(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d("bbotto", "onMapReady 진입")
        this.naverMap = naverMap

        val cameraPosition = CameraPosition(
            LatLng(37.515304, 127.103078),
            16.0,
        )
        naverMap.cameraPosition = cameraPosition
        Log.d("bbotto", "센터 위치 설정 ${cameraPosition.target.latitude}, ${cameraPosition.target.longitude}")

        val marker = Marker()
        marker.position = LatLng(37.515304, 127.103078)
        Log.d("bbotto", "${marker.position}")
        marker.map = naverMap

        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = true
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        trackingMyLocation()

        Log.d("test", "etstasdas")

//        naverMap.addOnLocationChangeListener { location ->git
//            Log.d("bbotto", "${location.latitude}, ${location.longitude}")
//        }
    }

    private fun trackingMyLocation() {
        naverMap.addOnLocationChangeListener { location ->
            Toast.makeText(this, "${location.latitude}, ${location.latitude}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLocationChanged(p0: Location) {
        val coord = LatLng(p0)

        naverMap?.let {
            it.moveCamera(CameraUpdate.scrollTo(coord))
        }
    }

    override fun onLocationChanged(locations: MutableList<Location>) {
        super.onLocationChanged(locations)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        super.onStatusChanged(provider, status, extras)
    }

    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1004
        const val ACCESS_FINE_LOCATION_CODE = 1000
        private const val LOCATION_PERMISSION_MESSAGE = "위치 권한이 승인되었습니다."
        private const val LOCATION_PERMISSION_DENIED_MESSAGE = "위치 권한이 거절되었습니다."
    }
}
