package com.aisleron.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aisleron.databinding.FragmentShopBinding
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.placeholder.LocationData


class ShopFragment : Fragment() {

    companion object {
        fun newInstance() = ShopFragment()
    }

    private val viewModel: ShopViewModel by viewModels()
    private var _binding: FragmentShopBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)

        val button: Button = binding.btnSaveShop
        button.setOnClickListener {
            LocationData.locations.add(
                Location(
                    id = (LocationData.locations.size + 1).toLong(),
                    name = binding.edtShopName.text.toString(),
                    defaultFilter = FilterType.NEEDED,
                    type = LocationType.SHOP,
                    pinned = binding.swcShopPinned.isChecked
                ),
            )
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
        lateinit var shopListItem: Location
        //the data passing with bundle and serializable
        if (bundle != null) {
            shopListItem = bundle.getSerializable("key") as Location
            binding.edtShopName.setText(shopListItem.name)
            binding.swcShopPinned.isChecked = shopListItem.pinned
            (activity as AppCompatActivity?)!!.supportActionBar!!.title = shopListItem.name
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}