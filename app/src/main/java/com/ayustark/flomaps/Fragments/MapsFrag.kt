package com.ayustark.flomaps.Fragments

import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.ayustark.flomaps.R
import com.ayustark.flomaps.databinding.FragmentMapsBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException


class MapsFrag : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var location: Location? = null
    private lateinit var mSocket: Socket
    private var binding: FragmentMapsBinding? = null
    private val bind get() = binding!!
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context?.applicationContext as Context)
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.e("GRANTED", isGranted.toString())
        if (isGranted) {
            checkGPS()
        } else {
            showToast("No access to User Location")
        }
    }
    private val GPSRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                getUserLocation()
            } else {
                showToast("Turn on GPS to get location")
            }
        }

    private var cancellationTokenSource = CancellationTokenSource()

    init {
        try {
            mSocket = IO.socket("https://flo-app-api.herokuapp.com/")
        } catch (err: URISyntaxException) {
            Log.e("Socket Error", "${err.reason} ${err.message}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        mSocket.connect()
        Log.e("SOCKET1", mSocket.connected().toString())
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        checkPermission()
    }

    private fun checkGPS() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(LocationRequest.create().setPriority(PRIORITY_HIGH_ACCURACY))
            .addLocationRequest(
                LocationRequest.create().setPriority(PRIORITY_BALANCED_POWER_ACCURACY)
            )
        val client = LocationServices.getSettingsClient(context as Context)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            getUserLocation()

        }
        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(it.resolution).build()
                    GPSRequestLauncher.launch(intentSenderRequest)
                } catch (sendEx: SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                context as Context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        checkGPS()
    }

    private fun getUserLocation() {
        showToast("User Location access on")
        val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
            PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        )
        currentLocationTask.addOnCompleteListener { task: Task<Location> ->
            val result = if (task.isSuccessful) {
                val result: Location = task.result
                location = result
                val latLng = LatLng(result.latitude, result.longitude)
                val payload = JSONObject()
                payload.put("latitude", result.latitude)
                payload.put("longitude", result.longitude)
                Log.e("SOCKET", mSocket.connected().toString())
                mSocket.emit("receive", payload, object : Ack {
                    override fun call(vararg args: Any?) {
                        Log.e("Receive", args.size.toString())
                    }
                })
                mMap.addMarker(
                    MarkerOptions().position(latLng)
                        .title("User").snippet("Current Location").icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.ic_person)
                        )
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                mMap.setOnMarkerClickListener {
                    showToast("Latitude:${it.position.latitude}, Longitude:${it.position.longitude}")
                    return@setOnMarkerClickListener false
                }
                mSocket.emit("send", object : Ack {
                    override fun call(vararg args: Any?) {
                        Log.e("Receive", args.size.toString())
                        val latLngFinal = LatLng(args[0] as Double, args[1] as Double)
                        Log.e("FINAL", latLngFinal.toString())
                        activity?.runOnUiThread {
                            mMap.addMarker(
                                MarkerOptions().position(latLngFinal)
                                    .title("Destination")
                                    .snippet("Latitude:${latLngFinal.latitude}, Longitude:${latLngFinal.longitude}")
                                    .icon(
                                        BitmapDescriptorFactory.fromResource(R.drawable.ic_person2)
                                    )
                            )
                            mMap.addPolyline(
                                PolylineOptions().add(latLng, latLngFinal)
                                    .color(Color.BLUE)
                                    .width(5F)
                            )
                        }
                    }
                })
                "Location (success): ${result.latitude}, ${result.longitude}"
            } else {
                val exception = task.exception
                "Location (failure): ${exception?.message}"
            }

            Log.d("Get Location", "getCurrentLocation() result: $result")
        }

    }

    private fun showToast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        mSocket.disconnect()
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
    }

}