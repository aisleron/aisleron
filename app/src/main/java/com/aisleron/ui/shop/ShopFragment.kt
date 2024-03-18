package com.aisleron.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aisleron.databinding.FragmentShopBinding
import com.aisleron.model.Location


class ShopFragment : Fragment() {

    companion object {
        fun newInstance() = ShopFragment()
    }

    private val viewModel: ShopViewModel by viewModels()
    private var _binding: FragmentShopBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.Name = "Bob"

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
        lateinit var shopListItem: Location
        //the data passing with bundle and serializable
        if (bundle != null) {
            shopListItem = bundle.getSerializable("key") as Location
            binding.storeName.text = shopListItem.name
            (activity as AppCompatActivity?)!!.supportActionBar!!.title = shopListItem.name
        }

    }
}