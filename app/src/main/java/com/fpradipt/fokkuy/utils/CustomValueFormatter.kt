package com.fpradipt.fokkuy.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

class CustomValueFormatter(
    private val xLabel: Array<Any>
): ValueFormatter() {
    private val format = DecimalFormat("###,##0.0")
    private val days = arrayOf("Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su")

    override fun getBarLabel(barEntry: BarEntry?): String {
        return format.format(barEntry?.y)
    }

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return xLabel.toString()
    }
}