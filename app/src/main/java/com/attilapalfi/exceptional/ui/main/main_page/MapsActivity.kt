package com.attilapalfi.exceptional.ui.main.main_page

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.persistence.ExceptionInstanceStore
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Math.pow
import java.lang.Math.sqrt
import java.util.*
import javax.inject.Inject

public class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var sentMarker = MarkerOptions().icon
    private var receivedMarker = MarkerOptions().icon
    @Inject
    lateinit val exceptionInstanceStore: ExceptionInstanceStore
    @Inject
    lateinit val metadataStore: MetadataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        Injector.INSTANCE.applicationComponent.inject(this)
        initMap()
    }

    private fun initMap() {
        sentMarker = BitmapDescriptorFactory.fromResource(R.drawable.outgoing_big)
        receivedMarker = BitmapDescriptorFactory.fromResource(R.drawable.incoming_big)
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this);
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let {
            it
            exceptionInstanceStore.exceptionList.forEach({
                e ->
                val markerOptions = MarkerOptions()
                        .position(LatLng(e.latitude, e.longitude))
                        .draggable(false)
                if ( e.fromWho == metadataStore.user.id ) {
                    it.addMarker(markerOptions.icon(sentMarker))
                } else {
                    it.addMarker(markerOptions.icon(receivedMarker))
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
