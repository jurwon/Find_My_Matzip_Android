package com.matzip.find_my_matzip.navTab.navTabFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.matzip.find_my_matzip.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker

class MapCardViewFragment : Fragment(), OnMapReadyCallback {
    private var res_lat: Double? = null
    private var res_lng: Double? = null
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // 전달된 데이터 읽기
            res_lat = it.getString("res_lat")?.toDoubleOrNull()
            res_lng = it.getString("res_lng")?.toDoubleOrNull()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map_card_view, container, false)

        // MapView 초기화
        mapView = view.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return view
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d("LatLngtest", "$res_lat, $res_lng")

        if (res_lat != null && res_lng != null) {
            val cameraPosition = CameraPosition(
                LatLng(res_lat!!, res_lng!!),  // 지도 시작 위치 지정
                13.0 // 줌 레벨
            )

            naverMap.cameraPosition = cameraPosition

            // 마커 추가
            val marker = Marker()
            marker.position = LatLng(res_lat!!, res_lng!!)
            marker.map = naverMap
        }
    }

    // 생명주기 메서드 호출 전달
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}