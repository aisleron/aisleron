package com.aisleron.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aisleron.databinding.FragmentNavigationDrawerBinding

class NavigationDrawerFragment : Fragment() {

    private var _binding: FragmentNavigationDrawerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNavigationDrawerBinding.inflate(inflater, container, false)

        with(binding) {
            //Set onclick listener for views that navigate based on their Id matching a navigation graph destination
            val navButtons = setOf(navInStock, navNeeded, navAllItems, navSettings, navAllShops)
            for (view in navButtons) {
                view.setOnClickListener {
                    findNavController().navigate(it.id, null)
                }
            }
        }

        return binding.root

    }
}