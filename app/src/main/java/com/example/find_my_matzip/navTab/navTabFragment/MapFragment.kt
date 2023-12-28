package com.example.find_my_matzip.navTab.navTabFragment

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.find_my_matzip.HomeTabActivity
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentMapBinding
import com.example.find_my_matzip.model.RestaurantDto
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import kotlin.math.pow


class MapFragment : Fragment() , OnMapReadyCallback {
    private val LOCATION_PERMISSION_REQUEST_CODE = 5000
    lateinit var binding: FragmentMapBinding
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var searchBtn: Button
    var restaurantsInsideCircle = mutableListOf<RestaurantDto>()


    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("SdoLifeCycle","MapFragment onCreate")
        super.onCreate(savedInstanceState)
        binding = FragmentMapBinding.inflate(layoutInflater)
        if (!hasPermission()) {
            ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            initMapView()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SdoLifeCycle","MapFragment onCreateView")
        return binding.root
    }
    private fun initMapView() {
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(com.example.find_my_matzip.R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(com.example.find_my_matzip.R.id.map, it).commit()
            }
        // fragment의 getMapAsync() 메서드로 OnMapReadyCallback 콜백을 등록하면 비동기로 NaverMap 객체를 얻을 수 있다.
        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }
    // hasPermission()에서는 위치 권한이 있을 경우 true를, 없을 경우 false를 반환한다.
    private fun hasPermission(): Boolean {
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onMapReady(naverMap: NaverMap) {
        val bounds = LatLngBounds(LatLng(34.0, 36.0), LatLng(128.0, 130.0))
        val cameraPosition = CameraPosition(
            LatLng(35.15690579523921, 129.05957113473747),  // 지도 시작 위치 지정
            14.0 // 줌 레벨
        )
        val restaurantService = (context?.applicationContext as MyApplication).restaurantService
        val restaurantList = restaurantService.getAllRestaurantsByAvgScore()

        restaurantList.enqueue(object : Callback<List<RestaurantDto>> {
            override fun onResponse(
                call: Call<List<RestaurantDto>>,
                response: Response<List<RestaurantDto>>
            ) {
                val restaurantList = response.body()
                if (restaurantList != null && restaurantList.isNotEmpty()) {
                    for (i in restaurantList.indices) {
                        val currentRestaurant = restaurantList[i]
                        val latitude: Double = currentRestaurant.res_lat.toDouble()
                        val longitude: Double = currentRestaurant.res_lng.toDouble()

                        // 네이버 지도 API를 이용하여 마커 생성
                        val marker = Marker()
                        marker.position = LatLng(latitude, longitude)

                        // 마커를 지도에 추가
                        marker.map = naverMap

                        marker.setOnClickListener(Overlay.OnClickListener {
                            // 클릭한 마커의 좌표를 destination에 설정
//                            val destination = marker.position ?: return@OnClickListener true
//
//                            // 여기에 길 안내 코드 추가
//                            val path = PathOverlay()
//                            path.coords = listOf(naverMap.locationOverlay.position, destination)
//                            path.map = naverMap
//
                            val bundle = Bundle().apply {
                                putLong("resInfoId", currentRestaurant.res_id)
                                putString("resInfoName", currentRestaurant.res_name)
                                putDouble("resInfoAvgScore", currentRestaurant.avgScore)
                                putString("resInfoMenu", currentRestaurant.res_menu)
                                putString("resInfoOT", currentRestaurant.operate_time)
                                putString("resInfoIntro", currentRestaurant.res_intro)
                                putString("resInfoThumbnail", currentRestaurant.res_thumbnail)
                            }


                            // 기존의 ResInfoFragment 인스턴스를 제거

                            var resInfoFragment = ResInfoFragment()
                            resInfoFragment.arguments = bundle

                            val transaction = parentFragmentManager.beginTransaction()
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            transaction.add(R.id.fragmentContainer, resInfoFragment)
                            transaction.addToBackStack(null)
                            transaction.show(resInfoFragment)
                            transaction.commit()

                            false
                        })

//                        marker.setOnClickListener(Overlay.OnClickListener {
//                            val intent = Intent(context, ResInfoActivity::class.java)
//                            intent.putExtra("resInfoId", currentRestaurant.res_id)
//                            intent.putExtra("resInfoName", currentRestaurant.res_name)
//                            intent.putExtra("resInfoAvgScore", currentRestaurant.avgScore)
//                            intent.putExtra("resInfoMenu", currentRestaurant.res_menu)
//                            intent.putExtra("resInfoOT", currentRestaurant.operate_time)
//                            intent.putExtra("resInfoIntro", currentRestaurant.res_intro)
//                            intent.putExtra("resInfoThumbnail", currentRestaurant.res_thumbnail)
//
////                            Log.d("infotest", "식당사진 ${currentRestaurant.res_thumbnail}")
////                            Log.d("infotest", "식당이름 ${currentRestaurant.res_name}")
////                            Log.d("infotest", "식당메뉴 ${currentRestaurant.res_menu}")
////                            Log.d("infotest", "영업시간 ${currentRestaurant.operate_time}")
////                            Log.d("infotest", "식당소개 ${currentRestaurant.res_intro}")
//                            Log.d("infotest", "식당평점 ${currentRestaurant.avgScore}")
//                            Log.d("infotest", "식당아디 ${currentRestaurant.res_id}")
//
//
//                            startActivity(intent)
//                            false
//                        })

                        //    Log.d("sdo", "식당 $i - 위도: ${currentRestaurant.res_lat}, 경도: ${currentRestaurant.res_lng}")

                    }
                    //    Log.d("sdo", "Full Response: $restaurantList")
                } else {
                    //    Log.e("sdo", "Response body is null or empty.")

                }
            }

            override fun onFailure(call: Call<List<RestaurantDto>>, t: Throwable) {
                t.printStackTrace()
                call.cancel()
                Log.e("sdo", " 통신 실패")
            }
        })


        //  marker.position = LatLng(35.15690579523921, 129.05957113473747)
        //  marker.map = naverMap


        naverMap.cameraPosition = cameraPosition

        this.naverMap = naverMap
        // 현재 위치
        naverMap.locationSource = locationSource
        // 현재 위치 버튼 기능
        naverMap.uiSettings.isLocationButtonEnabled = true
        // 위치를 추적하면서 카메라도 따라 움직인다.
        // naverMap.locationTrackingMode = LocationTrackingMode.Follow


        // Btn 시작

        searchBtn = binding.searchBtn

        // 이전에 생성된 PolylineOverlay를 저장할 변수
        var perimeterOverlay: com.naver.maps.map.overlay.PolylineOverlay? = null

        // searchBtn에 OnClickListener 설정
        searchBtn.setOnClickListener {
            var radiusInMeters = 1000.0

            // 현재 지도 중심 좌표 가져오기
            val center = naverMap.cameraPosition.target

            // 현재 지도의 줌 레벨 가져오기
            val zoomLevel = naverMap.cameraPosition.zoom

            // 원의 크기 동적으로 조절
            radiusInMeters = calculateRadiusForZoomLevel(zoomLevel)/17


            // 반경 10km에 해당하는 둘레 좌표 계산
            val perimeterPoints = mutableListOf<LatLng>()
            val numberOfPoints = 100 // 둘레를 부드럽게 만들려면 값을 증가시킵니다.

            for (i in 0 until numberOfPoints) {
                val theta = (i.toDouble() / numberOfPoints) * (2.0 * Math.PI)
                val x = center.longitude + radiusInMeters / 111000.0 * Math.cos(theta)
                val y = center.latitude + radiusInMeters / 111000.0 * Math.sin(theta)
                perimeterPoints.add(LatLng(y, x))
            }


            // 새로운 PolylineOverlay 설정
            val newPerimeterOverlay = com.naver.maps.map.overlay.PolylineOverlay()
            newPerimeterOverlay.coords = perimeterPoints
            newPerimeterOverlay.color =
                ContextCompat.getColor(requireContext(), com.example.find_my_matzip.R.color.black) // 원하는 색상으로 변경
            newPerimeterOverlay.width = 5 // 테두리 두께 조절

            // 이전에 생성된 PolylineOverlay 제거
            perimeterOverlay?.map = null

            // 현재 PolylineOverlay 지도에 추가
            newPerimeterOverlay.map = naverMap

            // 이전 PolylineOverlay를 새로 생성된 것으로 업데이트
            perimeterOverlay = newPerimeterOverlay

            // 1초 후에 원을 지우는 Coroutine 시작
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000) // 1초 딜레이
                perimeterOverlay?.map = null // 원 지우기
            }


            val restaurantService = (context?.applicationContext as MyApplication).restaurantService
            val nearRestaurantList = restaurantService.getAllRestaurantsByAvgScore()

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val nearRestaurants = nearRestaurantList.await()
                    // 원 안에 있는 식당들을 저장할 배열
                    val restaurantsInsideCircle = mutableListOf<RestaurantDto>()
                    if (nearRestaurants != null && nearRestaurants.isNotEmpty()) {
                        for (currentRestaurant in nearRestaurants) {
                            val restaurantLatLng = LatLng(
                                currentRestaurant.res_lat.toDouble(),
                                currentRestaurant.res_lng.toDouble()
                            )
                            Log.d("LatLnttest","${restaurantLatLng}")
                            Log.d("LatLnttest","${center}")

                            // 식당 좌표가 원 안에 속하는지 확인
                            if (isLatLngInsideCircle(restaurantLatLng, center, radiusInMeters)) {
                                restaurantsInsideCircle.add(currentRestaurant)
                            }
                        }
                        showNearbyRestaurants(restaurantsInsideCircle)

                        //Toast.makeText(requireContext(), "원 안의 식당 ${restaurantsInsideCircle.size}개", Toast.LENGTH_SHORT).show()

                       // Log.d("nearres", "원 안에 있는 식당: ${restaurantsInsideCircle}")


                        // 추가로 필요한 작업 수행
                    } else {
                        // Response body is null or empty.
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("sdo", "통신 실패")
                }
            }
        }
    }

    // 지도 줌 레벨에 따른 반경 계산 함수
    private fun calculateRadiusForZoomLevel(zoomLevel: Double): Double {
        // 여기에서 적절한 방법으로 지도의 줌 레벨에 따른 반경을 계산합니다.
        // 이 예제에서는 간단하게 1000미터를 기준으로 줌 레벨에 비례하여 계산합니다.
        return 1000 * Math.pow(2.0, 18 - zoomLevel)
    }

    private fun showNearbyRestaurants(restaurants: List<RestaurantDto>) {
        val nearRestaurantFragment = NearRestaurantFragment()
        nearRestaurantFragment.restaurantsInsideCircle = restaurants

        // FragmentTransaction을 사용하여 NearRestaurantFragment를 표시하는 코드를 작성
//        val transaction = parentFragmentManager.beginTransaction()
//        transaction.hide(this)
//        transaction.add(R.id.fragmentContainer, nearRestaurantFragment)
//        transaction.addToBackStack(null)
//        transaction.commit()
        val transaction = fragmentManager?.beginTransaction()
        transaction?.commitNow()
        nearRestaurantFragment.show(parentFragmentManager, nearRestaurantFragment.tag)

    }

    // 원 안에 포함되는지 확인하는 함수
    private fun isLatLngInsideCircle(point: LatLng, center: LatLng, radius: Double): Boolean {
        val distance = calculateDistanceBetweenPoints(point, center)
        return distance <= radius
    }

    // 두 지점 간의 거리를 계산하는 함수
    private fun calculateDistanceBetweenPoints(point1: LatLng, point2: LatLng): Double {
        val lat1 = Math.toRadians(point1.latitude)
        val lon1 = Math.toRadians(point1.longitude)
        val lat2 = Math.toRadians(point2.latitude)
        val lon2 = Math.toRadians(point2.longitude)

        // Haversine 공식을 사용한 거리 계산
        val dlon = lon2 - lon1
        val dlat = lat2 - lat1
        val a = Math.sin(dlat / 2).pow(2.0) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2).pow(2.0)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        // 지구 반지름 (미터)
        val earthRadius = 6371000.0

        // 거리 반환 (미터)
        return earthRadius * c
    }

    @Override
    override fun onResume() {
        Log.d("SdoLifeCycle","MapFragment onResume")
        super.onResume()
    }
    @Override
    override fun onPause() {
        Log.d("SdoLifeCycle","MapFragment onPause")
        super.onPause()
    }
    @Override
    override fun onDestroy() {
        Log.d("SdoLifeCycle","MapFragment onDestroy")
        super.onDestroy()
    }

    fun showExitDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Exit?")
        builder.setMessage("앱을 종료하시겠습니까?")
        builder.setNegativeButton("아니오") { dialog, which ->
            // 아무 작업도 수행하지 않음
        }
        builder.setPositiveButton("예") { dialog, which ->
            (requireActivity() as? HomeTabActivity)?.onBackPressed2()
        }
        builder.show()
    }


}