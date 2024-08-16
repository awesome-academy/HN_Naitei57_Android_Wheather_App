package com.example.sun.screen.search

import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.view.LayoutInflater
import android.widget.SearchView
import com.example.sun.screen.home.HomeFragment
import com.example.sun.utils.base.BaseFragment
import com.example.sun.utils.base.SharedPrefManager
import com.example.sun.utils.ext.replaceFragment
import com.example.weather.R
import com.example.weather.databinding.FragmentSearchBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class SearchFragment : BaseFragment<FragmentSearchBinding>(), OnMapReadyCallback {
    private var map: GoogleMap? = null
    private var currentMarker: Marker? = null

    override fun inflateViewBinding(inflater: LayoutInflater): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater)
    }

    override fun initData() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        if (mapFragment != null) {
            mapFragment.getMapAsync(this)
        } else {
            Log.d("SearchFragment", "SupportMapFragment not found!")
        }
        viewBinding.mapSearch.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    val location = viewBinding.mapSearch.query.toString()
                    var addressList: List<Address>? = null
                    if (location.isNotEmpty()) {
                        val geoCoder = Geocoder(requireContext())
                        try {
                            addressList = geoCoder.getFromLocationName(location, 1)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        val address: Address? = addressList?.get(0)
                        val latitude = address?.latitude ?: 0.0
                        val longitude = address?.longitude ?: 0.0
                        val place = LatLng(latitude, longitude)
                        currentMarker?.remove()
                        currentMarker = map?.addMarker(MarkerOptions().position(place).title(location))
                        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 15f))
                    }
                    return false
                }
            },
        )
    }

    override fun initView() {
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.setOnMarkerClickListener { marker ->
            marker?.let {
                SharedPrefManager.putString("selected_location", viewBinding.mapSearch.query.toString())
                replaceFragment(R.id.fragment_container, HomeFragment.newInstance(), true)
            }
            false
        }
    }

    companion object {
        fun newInstance() = SearchFragment()
    }
}
