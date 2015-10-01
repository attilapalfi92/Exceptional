package com.attilapalfi.exceptional.ui.main.main_page

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.persistence.ExceptionInstanceStore
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Math.pow
import java.lang.Math.sqrt
import java.util.*
import javax.inject.Inject

public class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    @Inject
    lateinit val exceptionInstanceStore: ExceptionInstanceStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        Injector.INSTANCE.applicationComponent.inject(this)
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this);
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let {
            val addedExceptions = ArrayList<Exception>()
            exceptionInstanceStore.exceptionList.forEach({
                e ->
                if ( !containsCloseOne(addedExceptions, e) ) {
                    addedExceptions.add(e)
                    it.addMarker(MarkerOptions().position(LatLng(e.latitude, e.longitude)))
                }
            })
        }
    }

    private fun containsCloseOne(addedExceptions: ArrayList<Exception>, e: Exception): Boolean {
        addedExceptions.forEach {
            val dist = sqrt(pow(it.longitude - e.longitude, 2.0) + pow(it.latitude - e.latitude, 2.0))
            if ( dist < 0.0010 ) {
                return true
            }
        }
        return false
    }


}
