package com.genterix.motm.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.genterix.motm.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.maps.android.data.kml.KmlContainer
import com.google.maps.android.data.kml.KmlLayer
import com.google.maps.android.data.kml.KmlPoint
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import com.tonyodev.dispatch.DispatchQueue


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var viewModel: LocationListViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var listView: ListView
    private lateinit var searchView: SearchView
    private lateinit var recenterButton: ImageButton
    private lateinit var infoButton: ImageButton
    private lateinit var drawerTitle: TextView
    private lateinit var drawerLayout: SlidingUpPanelLayout

    private lateinit var defaultCameraUpdate: CameraUpdate
    private lateinit var savedTitle: String
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var aAdapter: ArrayAdapter<String>
    private lateinit var rAdapter: ArrayAdapter<String>
    private var markers: Array<Marker> = emptyArray<Marker>()
    private var aMarkers: Array<Marker> = emptyArray<Marker>()
    private var rMarkers: Array<Marker> = emptyArray<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel = LocationListViewModel()

        listView = findViewById(R.id.list)

        searchView = findViewById(R.id.searchView)
        drawerTitle = findViewById(R.id.drawerTitle)
        recenterButton = findViewById(R.id.recenterButton)
        infoButton = findViewById(R.id.infoButton)


        recenterButton.setOnClickListener {
            mMap.moveCamera(defaultCameraUpdate)
        }

        infoButton.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }


        savedTitle = drawerTitle.text.toString()

        listView.setOnItemClickListener { parent, view, position, id ->

            searchView.hideKeyboard()

            // needs to wait for the keyboard to dismiss before anchoring
            DispatchQueue.io.post(150) {
                runOnUiThread {
                    drawerLayout.panelState = PanelState.ANCHORED


                    val place = viewModel.locations[position]
                    val aPlace = viewModel.aLocations[position]
                    val rPlace = viewModel.rLocations[position]
                    val marker = markers.first { it.position == place.latlng }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.latlng, 20.0f))
                    marker.showInfoWindow()

                    val aMarker = aMarkers.first { it.position == aPlace.latlng }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(aPlace.latlng, 20.0f))
                    aMarker.showInfoWindow()

                    val rMarker = rMarkers.first { it.position == rPlace.latlng }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rPlace.latlng, 20.0f))
                    rMarker.showInfoWindow()

                    // launchDetails(place)
                }
            }.start()
        }

        reloadData()
        reloadAData()
        reloadRData()

        drawerLayout = findViewById<SlidingUpPanelLayout>(R.id.sliding_layout)

        drawerLayout.addPanelSlideListener(object: SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
//        TODO("Not yet implemented")
            }

            override fun onPanelStateChanged(panel: View?, previousState: PanelState?, newState: PanelState?) {
                Log.i("drawer", "state: " + newState.toString())

                if (newState == PanelState.COLLAPSED) {
//            searchEditText.hideKeyboard()
                } else if (newState == PanelState.EXPANDED) {
//            searchEditText.showKeyboard()
                }

//        else if (previousState == PanelState.EXPANDED && newState == PanelState.DRAGGING) {
//            searchEditText.hideKeyboard()
//        }
            }
        })

        drawerLayout.anchorPoint = 0.4F

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            searchView.setOnSearchClickListener {
                drawerTitle.text = ""
                drawerLayout.panelState = PanelState.EXPANDED
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {

                    viewModel.filterLocations(newText)

                    // TODO: jangelsb why doesn't this work? Maybe need to implement our own Adapter
    //        adapter.notifyDataSetChanged()
                    reloadData()
                    reloadAData()
                    reloadRData()

                    return false
                }
            })
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            searchView.setOnCloseListener(object: SearchView.OnCloseListener {
                override fun onClose(): Boolean {
                    drawerTitle.text = savedTitle

                    DispatchQueue.io.post(150) {
                        runOnUiThread {
                            // drawerLayout.panelState = PanelState.ANCHORED
                        }
                    }.start()
                    return false
                }
            })
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        val layer = KmlLayer(mMap, R.raw.data, this)
        layer.addLayerToMap()

        val aLayer = KmlLayer(mMap, R.raw.accomodationsLayer, this)
        aLayer.addLayerToMap()

        val rLayer = KmlLayer(mMap, R.raw.restaurantLayer, this)
        rLayer.addLayerToMap()

        val locations = accessContainers(layer.containers, emptyArray())
        locations.sortBy { it.name }

        val aLocations = accessAContainers(aLayer.containers, emptyArray())
        aLocations.sortBy { it.name }

        val rLocations = accessRContainers(rLayer.containers, emptyArray())
        rLocations.sortBy { it.name }

        // NOTE: can remove this to go back to default styling and callouts
        // ------------------------------------------
        layer.removeLayerFromMap()
        aLayer.removeLayerFromMap()
        rLayer.removeLayerFromMap()

        for (location in locations) {
            print(location)

            val content = location.content?.subSequence(0, 100).toString() + "...\n"
            val marker = mMap.addMarker(MarkerOptions()
                    .position(location.latlng!!)
                    .title(location.name)
                    .snippet(content))
//            marker.showInfoWindow() // unfortunately you can have one at a time...

            markers += marker
        }

        for (aLocation in aLocations) {
            print(aLocation)

            val aContent = aLocation.content?.subSequence(0, 100).toString() + "...\n"
            val aMarker = mMap.addMarker(MarkerOptions()
                    .position(aLocation.latlng!!)
                    .title(aLocation.name)
                    .snippet(aContent))
//            marker.showInfoWindow() // unfortunately you can have one at a time...

            aMarkers += aMarker
        }

        for (rLocation in rLocations) {
            print(rLocation)

            val rContent = rLocation.content?.subSequence(0, 100).toString() + "...\n"
            val rMarker = mMap.addMarker(MarkerOptions()
                    .position(rLocation.latlng!!)
                    .title(rLocation.name)
                    .snippet(rContent))
//            marker.showInfoWindow() // unfortunately you can have one at a time...

            rMarkers += rMarker
        }
        // ------------------------------------------

        // https://stackoverflow.com/a/31629308/9605061
        mMap.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                val info = LinearLayout(baseContext)
                info.orientation = LinearLayout.VERTICAL
                val title = TextView(baseContext)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker.title
                val snippet = TextView(baseContext)
                snippet.setTextColor(Color.GRAY)
                snippet.text = marker.snippet
                val action = TextView(baseContext)
                action.gravity = Gravity.CENTER
                action.setTextColor(Color.GRAY)
                action.text = getString(R.string.Tap)
                info.addView(title)
                info.addView(snippet)
                info.addView(action)
                return info
            }
        })

        viewModel.allLocations = locations
        viewModel.allALocations = aLocations
        viewModel.allRLocations = rLocations

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, viewModel.locationNames)
        aAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, viewModel.locationANames)
        rAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, viewModel.locationRNames)
        listView.adapter = adapter
        listView.adapter = rAdapter
        listView.adapter = aAdapter

        val builder = LatLngBounds.Builder()
        for (location in locations) {
            builder.include(location.latlng)
        }

        val aBuilder = LatLngBounds.Builder()
        for (aLocation in aLocations) {
            aBuilder.include(aLocation.latlng)
        }

        val rBuilder = LatLngBounds.Builder()
        for (rLocation in rLocations) {
            rBuilder.include(rLocation.latlng)
        }

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels // seems to also zoome out a bit

        // NOTE: the last value seems to be zoom level
        defaultCameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, 275)
        mMap.moveCamera(defaultCameraUpdate)

        mMap.setOnInfoWindowClickListener {
            val selectedLocation = it.title
            val place = viewModel.allLocations.first { it.name == selectedLocation }

            launchDetails(place)
        }
    }

    private fun accessContainers(containers: Iterable<KmlContainer>, locations: Array<LocationInfo>): Array<LocationInfo> {
        var newLocations: Array<LocationInfo> = emptyArray()

        for (container in containers) {
            if (container.hasProperty("name")) {
                if (container.hasPlacemarks()) {
                    for (placemark in container.placemarks) {
                        var name: String? = null
                        var content: String? = null
                        if (placemark.hasProperty("name")) {
                            name = placemark.getProperty("name")
                            Log.i("KML", name)
                        }

                        if (placemark.hasProperty("description")) {
                            content = placemark.getProperty("description")
                            Log.i("KML", content)
                        }

                        var latLng: LatLng? = null
                        try {
                            val point = placemark.geometry as KmlPoint
                            latLng = LatLng(point.geometryObject.latitude, point.geometryObject.longitude)

                        } catch (e: Exception) {
                            // may fail depending on the KML being shown
                            e.printStackTrace()
                        }

                        if (name != null && content != null) {


                            val imageRegex = Regex("<img src=\"([^\\\"]*)\"", RegexOption.IGNORE_CASE)
                            val descriptionRegex = Regex("<br><br>(.*)", RegexOption.IGNORE_CASE)

                            val imageMatch = imageRegex.find(content)
                            var imageURL: String? = imageMatch?.destructured?.component1()

                            val descriptionMatch = descriptionRegex.find(content)
                            var descriptionText: String? = descriptionMatch?.destructured?.component1()

                            descriptionText = descriptionText?.replace("<br>", "\n\n")

                            newLocations += LocationInfo(name, descriptionText, imageURL, latLng)
                        }
                    }
                }
            }

            if (container.hasContainers()) {
                return accessContainers(container.containers, locations + newLocations)
            }
        }

        return locations + newLocations
    }

    private fun accessAContainers(aContainers: Iterable<KmlContainer>, aLocations: Array<LocationInfo>): Array<LocationInfo> {
        var newALocations: Array<LocationInfo> = emptyArray()

        for (aContainer in aContainers) {
            if (aContainer.hasProperty("name")) {
                if (aContainer.hasPlacemarks()) {
                    for (placemark in aContainer.placemarks) {
                        var name: String? = null
                        var content: String? = null
                        if (placemark.hasProperty("name")) {
                            name = placemark.getProperty("name")
                            Log.i("KML", name)
                        }

                        if (placemark.hasProperty("description")) {
                            content = placemark.getProperty("description")
                            Log.i("KML", content)
                        }

                        var latLng: LatLng? = null
                        try {
                            val point = placemark.geometry as KmlPoint
                            latLng = LatLng(point.geometryObject.latitude, point.geometryObject.longitude)

                        } catch (e: Exception) {
                            // may fail depending on the KML being shown
                            e.printStackTrace()
                        }

                        if (name != null && content != null) {


                            val imageRegex = Regex("<img src=\"([^\\\"]*)\"", RegexOption.IGNORE_CASE)
                            val descriptionRegex = Regex("<br><br>(.*)", RegexOption.IGNORE_CASE)

                            val imageMatch = imageRegex.find(content)
                            var imageURL: String? = imageMatch?.destructured?.component1()

                            val descriptionMatch = descriptionRegex.find(content)
                            var descriptionText: String? = descriptionMatch?.destructured?.component1()

                            descriptionText = descriptionText?.replace("<br>", "\n\n")

                            newALocations += LocationInfo(name, descriptionText, imageURL, latLng)
                        }
                    }
                }
            }

            if (aContainer.hasContainers()) {
                return accessAContainers(aContainer.containers, aLocations + newALocations)
            }
        }

        return aLocations + newALocations
    }

    private fun accessRContainers(rContainers: Iterable<KmlContainer>, rLocations: Array<LocationInfo>): Array<LocationInfo> {
        var newRLocations: Array<LocationInfo> = emptyArray()

        for (rContainer in rContainers) {
            if (rContainer.hasProperty("name")) {
                if (rContainer.hasPlacemarks()) {
                    for (placemark in rContainer.placemarks) {
                        var name: String? = null
                        var content: String? = null
                        if (placemark.hasProperty("name")) {
                            name = placemark.getProperty("name")
                            Log.i("KML", name)
                        }

                        if (placemark.hasProperty("description")) {
                            content = placemark.getProperty("description")
                            Log.i("KML", content)
                        }

                        var latLng: LatLng? = null
                        try {
                            val point = placemark.geometry as KmlPoint
                            latLng = LatLng(point.geometryObject.latitude, point.geometryObject.longitude)

                        } catch (e: Exception) {
                            // may fail depending on the KML being shown
                            e.printStackTrace()
                        }

                        if (name != null && content != null) {


                            val imageRegex = Regex("<img src=\"([^\\\"]*)\"", RegexOption.IGNORE_CASE)
                            val descriptionRegex = Regex("<br><br>(.*)", RegexOption.IGNORE_CASE)

                            val imageMatch = imageRegex.find(content)
                            var imageURL: String? = imageMatch?.destructured?.component1()

                            val descriptionMatch = descriptionRegex.find(content)
                            var descriptionText: String? = descriptionMatch?.destructured?.component1()

                            descriptionText = descriptionText?.replace("<br>", "\n\n")

                            newRLocations += LocationInfo(name, descriptionText, imageURL, latLng)
                        }
                    }
                }
            }

            if (rContainer.hasContainers()) {
                return accessContainers(rContainer.containers, rLocations + newRLocations)
            }
        }

        return rLocations + newRLocations
    }

    fun View.hideKeyboard() {
        val imm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        } else {
            TODO("VERSION.SDK_INT < CUPCAKE")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }

    fun View.showKeyboard() {
        val imm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        } else {
            TODO("VERSION.SDK_INT < CUPCAKE")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            imm.showSoftInput(this, 0)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.panelState == PanelState.EXPANDED || drawerLayout.panelState == PanelState.ANCHORED) {
            drawerLayout.panelState = PanelState.COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    private fun reloadData() {
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, viewModel.locationNames)
        listView.adapter = adapter
    }

    private fun reloadAData() {
        aAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, viewModel.locationANames)
        listView.adapter = aAdapter
    }

    private fun reloadRData() {
        rAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, viewModel.locationRNames)
        listView.adapter = rAdapter
    }

    private fun launchDetails(place: LocationInfo) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("ITEM", Gson().toJson(place))
        startActivity(intent)
    }
}
