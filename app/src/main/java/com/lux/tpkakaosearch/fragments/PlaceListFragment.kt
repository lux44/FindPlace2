package com.lux.tpkakaosearch.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lux.tpkakaosearch.activities.MainActivity
import com.lux.tpkakaosearch.adapters.PlaceLIstRecyclerAdapter
import com.lux.tpkakaosearch.databinding.FragmentPlaceListBinding
import com.lux.tpkakaosearch.databinding.FragmentPlaceMapBinding

class PlaceListFragment :Fragment() {
    val binding:FragmentPlaceListBinding by lazy { FragmentPlaceListBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setPlaceListRecyclerAdapter()
    }

    private fun setPlaceListRecyclerAdapter(){
        val main:MainActivity = activity as MainActivity

        // 아직 MainActivity 의 파싱작업이 완료되지 않았다면 데이터가 없음
        if (main.searchPlaceResponse==null) return

        binding.recyclerView.adapter=PlaceLIstRecyclerAdapter(requireContext(),
            main.searchPlaceResponse!!.documents)
    }
}