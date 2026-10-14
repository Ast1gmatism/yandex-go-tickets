package com.trolleygo.tickets.ui.main

import android.view.View
import com.trolleygo.tickets.R
import com.trolleygo.tickets.databinding.ItemRouteBinding

/** Строка списка маршрутов (в листе «Новый билет»). */
object RouteRowBinder {

    fun bind(
        binding: ItemRouteBinding,
        choice: RouteChoice,
        selected: Boolean,
        onClick: () -> Unit,
        onDelete: (() -> Unit)? = null
    ) {
        SheetFonts.apply(binding.root.context, binding)
        binding.routeName.text = choice.route.name
        binding.routeBadge.setText(
            if (choice.route.isPreset) R.string.badge_preset else R.string.badge_custom
        )
        binding.routePath.text = choice.route.path(choice.direction)
        binding.routeRow.isSelected = selected
        binding.routeCheck.visibility = if (selected) View.VISIBLE else View.GONE
        binding.routeDelete.visibility =
            if (onDelete != null) View.VISIBLE else View.GONE
        binding.routeRow.setOnClickListener { onClick() }
        binding.routeDelete.setOnClickListener { onDelete?.invoke() }
    }
}
