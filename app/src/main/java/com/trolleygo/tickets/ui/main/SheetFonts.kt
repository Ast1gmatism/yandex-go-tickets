package com.trolleygo.tickets.ui.main

import android.content.Context
import com.trolleygo.tickets.databinding.DialogRoutePickerBinding
import com.trolleygo.tickets.databinding.ItemRouteBinding
import com.trolleygo.tickets.databinding.SheetAddRouteBinding
import com.trolleygo.tickets.ui.Fonts

/** Начертания YS Text для нижних листов и строк маршрутов. */
object SheetFonts {

    fun apply(context: Context, binding: DialogRoutePickerBinding) {
        binding.sheetTitle.typeface = Fonts.bold(context)
        binding.sheetSubtitle.typeface = Fonts.regular(context)
        binding.editDigits.typeface = Fonts.regular(context)
        binding.digitsPreview.typeface = Fonts.regular(context)
        binding.btnCreateTicket.typeface = Fonts.medium(context)
        binding.btnAddRoute.typeface = Fonts.medium(context)
    }

    fun apply(context: Context, binding: SheetAddRouteBinding) {
        binding.sheetTitle.typeface = Fonts.bold(context)
        binding.sheetSubtitle.typeface = Fonts.regular(context)
        binding.editTrolley.typeface = Fonts.regular(context)
        binding.editStopA.typeface = Fonts.regular(context)
        binding.editStopB.typeface = Fonts.regular(context)
        binding.btnSave.typeface = Fonts.medium(context)
        binding.btnCancel.typeface = Fonts.medium(context)
    }

    fun apply(context: Context, binding: ItemRouteBinding) {
        binding.routeName.typeface = Fonts.regular(context)
        binding.routeBadge.typeface = Fonts.medium(context)
        binding.routePath.typeface = Fonts.regular(context)
    }
}
