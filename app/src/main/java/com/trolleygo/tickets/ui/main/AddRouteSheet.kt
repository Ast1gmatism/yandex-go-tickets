package com.trolleygo.tickets.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.trolleygo.tickets.R
import com.trolleygo.tickets.databinding.SheetAddRouteBinding

/**
 * Лист «Новый маршрут»: только номер троллейбуса и две конечные
 * остановки — маршрут сохраняется и появляется в списке.
 */
class AddRouteSheet : BottomSheetDialogFragment() {

    private var _binding: SheetAddRouteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetAddRouteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SheetFonts.apply(requireContext(), binding)
        binding.btnSave.setOnClickListener { save() }
        binding.btnCancel.setOnClickListener { dismiss() }
    }

    private fun save() {
        val numberText = binding.editTrolley.text?.toString()?.trim().orEmpty()
        val stopA = binding.editStopA.text?.toString()?.trim().orEmpty()
        val stopB = binding.editStopB.text?.toString()?.trim().orEmpty()

        val number = numberText.toIntOrNull()
        var valid = true

        if (number == null || number !in 1..999) {
            binding.inputTrolley.error = getString(R.string.error_trolley_number)
            valid = false
        } else {
            binding.inputTrolley.error = null
        }
        if (stopA.isEmpty()) {
            binding.inputStopA.error = getString(R.string.error_stop_a)
            valid = false
        } else {
            binding.inputStopA.error = null
        }
        if (stopB.isEmpty()) {
            binding.inputStopB.error = getString(R.string.error_stop_b)
            valid = false
        } else {
            binding.inputStopB.error = null
        }
        if (!valid) return

        viewModel.addRoute(number!!, stopA, stopB)
        Toast.makeText(requireContext(), R.string.route_saved_toast, Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "AddRouteSheet"
    }
}
