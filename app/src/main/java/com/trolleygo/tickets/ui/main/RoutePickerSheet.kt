package com.trolleygo.tickets.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.trolleygo.tickets.R
import com.trolleygo.tickets.data.model.Direction
import com.trolleygo.tickets.data.model.Route
import com.trolleygo.tickets.databinding.DialogRoutePickerBinding
import com.trolleygo.tickets.databinding.ItemRouteBinding
import com.trolleygo.tickets.util.TicketCode

/**
 * Лист «Новый билет»: пресеты и сохранённые маршруты (каждый — в двух
 * направлениях), ввод 4 цифр номера троллейбуса и создание билета.
 */
class RoutePickerSheet : BottomSheetDialogFragment() {

    private var _binding: DialogRoutePickerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private var selected: RouteChoice? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRoutePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SheetFonts.apply(requireContext(), binding)
        binding.editDigits.filters = arrayOf(
            android.text.InputFilter.LengthFilter(TicketCode.DIGIT_LENGTH),
            android.text.InputFilter { source, _, _, _, _, _ ->
                if (source.all { it in '0'..'9' }) source else ""
            }
        )
        binding.editDigits.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = updatePreview()
                override fun afterTextChanged(s: android.text.Editable?) = Unit
            }
        )
        binding.btnCreateTicket.setOnClickListener { createTicket() }
        binding.btnAddRoute.setOnClickListener {
            dismiss()
            AddRouteSheet().show(parentFragmentManager, AddRouteSheet.TAG)
        }

        viewModel.routes.observe(viewLifecycleOwner) { renderRoutes(it) }
        updatePreview()
    }

    private fun renderRoutes(routes: List<Route>) {
        if (_binding == null) return
        binding.routeListContainer.removeAllViews()
        routes.forEach { route ->
            Direction.values().forEach { direction ->
                val choice = RouteChoice(route, direction)
                val rowBinding = ItemRouteBinding.inflate(
                    layoutInflater, binding.routeListContainer, false
                )
                RouteRowBinder.bind(
                    binding = rowBinding,
                    choice = choice,
                    selected = selected?.key == choice.key,
                    onClick = {
                        selected = choice
                        renderRoutes(viewModel.routes.value.orEmpty())
                    },
                    onDelete = {
                        viewModel.deleteRoute(route.id)
                        if (selected?.route?.id == route.id) selected = null
                    }
                )
                binding.routeListContainer.addView(rowBinding.root)
            }
        }
    }

    private fun updatePreview() {
        if (_binding == null) return
        val digits = binding.editDigits.text?.toString().orEmpty()
        binding.digitsPreview.text = if (TicketCode.isValidDigits(digits)) {
            getString(
                R.string.digits_preview,
                TicketCode.registration(digits),
                TicketCode.boardNumber(digits)
            )
        } else {
            getString(R.string.digits_helper)
        }
    }

    private fun createTicket() {
        val choice = selected
        if (choice == null) {
            Toast.makeText(requireContext(), R.string.route_select_error, Toast.LENGTH_SHORT).show()
            return
        }
        val digits = binding.editDigits.text?.toString().orEmpty()
        if (!TicketCode.isValidDigits(digits)) {
            binding.inputDigits.error = getString(R.string.error_digits)
            return
        }
        binding.inputDigits.error = null
        viewModel.createTicket(choice.route, choice.direction, digits)
        Toast.makeText(requireContext(), R.string.ticket_created_toast, Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "RoutePickerSheet"
    }
}
