package com.genterix.motm.ui

import com.google.android.gms.maps.model.LatLng

data class LocationInfo(val name: String, val content: String?, val imageURL: String?, val latlng: LatLng?)

class LocationListViewModel {
    // TODO: Implement the ViewModel

    var allLocations: Array<LocationInfo> = arrayOf(LocationInfo("one", null, null, null), LocationInfo("two", null, null, null), LocationInfo("three", null, null, null), LocationInfo("four", null, null, null)) //emptyArray()
    var allALocations: Array<LocationInfo> = arrayOf(LocationInfo("one", null, null, null), LocationInfo("two", null, null, null), LocationInfo("three", null, null, null), LocationInfo("four", null, null, null)) //emptyArray()
    var allRLocations: Array<LocationInfo> = arrayOf(LocationInfo("one", null, null, null), LocationInfo("two", null, null, null), LocationInfo("three", null, null, null), LocationInfo("four", null, null, null)) //emptyArray()

    private var filteredLocations: Array<LocationInfo> = emptyArray()
    private var filteredALocations: Array<LocationInfo> = emptyArray()
    private var filteredRLocations: Array<LocationInfo> = emptyArray()

    val locations: Array<LocationInfo>
        get() {
            if (isFiltering) {
                return filteredLocations
            }

            return allLocations
        }

    val locationNames: List<String>
        get() {
            return locations.map { it.name }
        }

    val aLocations: Array<LocationInfo>
        get() {
            if (isFiltering) {
                return filteredALocations
            }

            return allALocations
        }

    val locationANames: List<String>
        get() {
            return aLocations.map { it.name }
        }

    val rLocations: Array<LocationInfo>
        get() {
            if (isFiltering) {
                return filteredRLocations
            }

            return allRLocations
        }

    val locationRNames: List<String>
        get() {
            return rLocations.map { it.name }
        }


    private var isFiltering = false

    fun filterLocations(text: String?) {
        if (text == null || text.isEmpty()) {
            isFiltering = false
            filteredLocations = emptyArray()
        } else {
            isFiltering = true
            filteredLocations = allLocations.filter { it.name.contains(text, true) }.toTypedArray()
        }
    }
}