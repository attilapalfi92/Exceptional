package com.attilapalfi.exceptional.ui.main.main_page

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.attilapalfi.exceptional.R
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Exception
import com.attilapalfi.exceptional.persistence.ExceptionInstanceStore
import com.attilapalfi.exceptional.persistence.FriendStore
import com.attilapalfi.exceptional.persistence.MetadataStore
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import java.util.*
import javax.inject.Inject

public class MapsActivity : AppCompatActivity(), OnMapReadyCallback, ClusterManager.OnClusterItemClickListener<Exception>,
        ClusterManager.OnClusterClickListener<Exception>, ClusterManager.OnClusterInfoWindowClickListener<Exception>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Exception> {

    private lateinit var clusterManager: ClusterManager<Exception>
    @Inject
    lateinit var exceptionInstanceStore: ExceptionInstanceStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        Injector.INSTANCE.applicationComponent.inject(this)
        initMap()
    }

    private fun initMap() {
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this);
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let {
            val exceptionList = exceptionInstanceStore.getExceptionList()
            if (exceptionList.isNotEmpty()) {
                initClusterManager(exceptionList, it)
                it.setOnCameraChangeListener(clusterManager)
                setPosition(it, LatLng(exceptionList[0].latitude, exceptionList[0].longitude))
            }
        }
    }

    private fun initClusterManager(exceptionList: ArrayList<Exception>, googleMap: GoogleMap) {
        clusterManager = ClusterManager<Exception>(this, googleMap)
        clusterManager.setRenderer(ExceptionRenderer(this, googleMap, clusterManager))
        clusterManager.addItems(exceptionList)
        clusterManager.setOnClusterClickListener(this)
        clusterManager.setOnClusterInfoWindowClickListener(this)
        clusterManager.setOnClusterItemClickListener(this)
        clusterManager.setOnClusterItemInfoWindowClickListener(this)
    }

    private fun setPosition(googleMap: GoogleMap, latLng: LatLng) {
        val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(10f)
                .tilt(30f)
                .build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onClusterClick(cluster: Cluster<Exception>?): Boolean {
        return true
    }

    override fun onClusterInfoWindowClick(cluster: Cluster<Exception>?) {

    }

    override fun onClusterItemClick(exception: Exception?): Boolean {
        return true
    }

    override fun onClusterItemInfoWindowClick(exception: Exception?) {

    }

    public class ExceptionRenderer(context: Context, googleMap: GoogleMap, clusterManager: ClusterManager<Exception>)
    : DefaultClusterRenderer<Exception>(context, googleMap, clusterManager) {

        @Inject
        lateinit var metadataStore: MetadataStore
        @Inject
        lateinit var friendStore: FriendStore
        private val sentMarker by lazy { BitmapDescriptorFactory.fromResource(R.drawable.outgoing_big) }
        private val receivedMarker by lazy { BitmapDescriptorFactory.fromResource(R.drawable.incoming_big) }

        init {
            Injector.INSTANCE.applicationComponent.inject(this)
        }

        override fun onBeforeClusterItemRendered(exception: Exception?, markerOptions: MarkerOptions?) {
            exception?.let {
                if (it.fromWho == metadataStore.user.id) {
                    markerOptions?.icon(sentMarker)?.title("To ${friendStore.findById(it.toWho).getName()}")
                } else {
                    markerOptions?.icon(receivedMarker)?.title("From ${friendStore.findById(it.fromWho).getName()}")
                }
            }
        }
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
