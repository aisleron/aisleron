package com.aisleron.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.aisleron.databinding.FragmentShopBinding
import com.aisleron.ui.shoppinglist.ShoppingListFragment
import org.koin.android.ext.android.inject


class ShopFragment : Fragment() {

    private val viewModel: ShopViewModel by inject<ShopViewModel>()
    private var _binding: FragmentShopBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            viewModel.hydrate(it.getInt(ARG_LOCATION_ID))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        val button: Button = binding.btnSaveShop
        button.setOnClickListener {
            viewModel.saveLocation(
                binding.edtShopName.text.toString(),
                binding.swcShopPinned.isChecked
            )
            //TODO: Add UI State to back listener
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtShopName.setText(viewModel.locationName)
        binding.swcShopPinned.isChecked = viewModel.pinned ?: true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        if (viewModel.locationName != null) {
            (activity as AppCompatActivity?)!!.supportActionBar!!.title = viewModel.locationName
        }
        super.onResume()
    }

    companion object {

        private const val ARG_LOCATION_ID = "locationId"

        @JvmStatic
        fun newInstance(locationId: Int) =
            ShoppingListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LOCATION_ID, locationId)
                }
            }
    }
}