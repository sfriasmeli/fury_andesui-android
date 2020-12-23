package com.mercadolibre.android.andesui.dropdown

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.mercadolibre.android.andesui.R
import com.mercadolibre.android.andesui.dropdown.factory.AndesDropdownAttrParser
import com.mercadolibre.android.andesui.dropdown.factory.AndesDropdownAttrs
import com.mercadolibre.android.andesui.dropdown.factory.AndesDropdownConfiguration
import com.mercadolibre.android.andesui.dropdown.factory.AndesDropdownConfigurationFactory
import com.mercadolibre.android.andesui.dropdown.size.AndesDropdownSize
import com.mercadolibre.android.andesui.dropdown.type.AndesDropdownMenuType
import com.mercadolibre.android.andesui.dropdown.utils.AndesDropdownDelegate
import com.mercadolibre.android.andesui.dropdown.utils.DropdownBottomSheetDialog
import com.mercadolibre.android.andesui.list.AndesList
import com.mercadolibre.android.andesui.list.AndesListViewItem
import com.mercadolibre.android.andesui.list.AndesListViewItemSimple
import com.mercadolibre.android.andesui.list.utils.AndesListDelegate
import com.mercadolibre.android.andesui.typeface.getFontOrDefault


class AndesDropDownStandalone : ConstraintLayout, AndesListDelegate {
    private lateinit var andesDropdownDelegate: AndesDropdownDelegate
    private lateinit var andesDropdownAttrs: AndesDropdownAttrs
    private lateinit var andesDropDownStandaloneChevron: ImageView
    private lateinit var andesDropDownStandaloneContent: TextView
    private val chevronUpIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.andes_ui_chevron_up_12)
    private val chevronDownIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.andes_ui_chevron_down_12)
    private val bottomSheetDialog = DropdownBottomSheetDialog(context, R.style.BottomSheetDialog)

    var listItems: MutableList<AndesDropDownItem> = mutableListOf()

    /**
     * Getter and setter for [label].
     */
    var label: String?
        get() = andesDropdownAttrs.andesDropdownLabel
        set(value) {
            andesDropdownAttrs = andesDropdownAttrs.copy(andesDropdownLabel = value)
            setupLabelComponent(createConfig())
        }

    /**
     * Getter and setter for [size].
     */
    var size: AndesDropdownSize
        get() = andesDropdownAttrs.andesDropdownSize
        set(value) {
            andesDropdownAttrs = andesDropdownAttrs.copy(andesDropdownSize = value)
            updateDynamicComponents(createConfig())
        }

    /**
     * Getter and setter for [delegate].
     */
    var delegate: AndesDropdownDelegate
        get() = andesDropdownDelegate
        set(value) {
            andesDropdownDelegate = value
        }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(attrs)
    }

    constructor(
            context: Context,
            menuType: AndesDropdownMenuType = AndesDropdownMenuType.BOTTOMSHEET,
            size: AndesDropdownSize,
            label: String
    ) : super(context) {
        initAttrs(menuType, size, label)
    }

    /**
     * Sets the proper [config] for this component based on the [attrs] received via XML.
     *
     * @param attrs attributes from the XML.
     */
    private fun initAttrs(attrs: AttributeSet?) {
        andesDropdownAttrs = AndesDropdownAttrParser.parse(context, attrs)
        setupComponents(createConfig())
    }

    private fun initAttrs(
            menuType: AndesDropdownMenuType,
            size: AndesDropdownSize,
            label: String
    ) {
        andesDropdownAttrs = AndesDropdownAttrs(
                andesDropdownMenuType = menuType,
                andesDropdownSize = size,
                andesDropdownLabel = label,
                andesDropdownHelper = null,
                andesDropdownPlaceHolder = null)
        setupComponents(createConfig())
    }

    /**
     * Responsible for setting up all properties of each component that is part of this andesDropdown.
     * Is like a choreographer 😉
     */
    private fun setupComponents(config: AndesDropdownConfiguration) {
        initComponents()
        setupViewId()
        setupBottomSheet()

        // TODO ubiar en un mejor lugar.
        this.setOnClickListener {
            openBottomSheet()
        }

    }

    /**
     * Set up the text component.
     */
    private fun setupLabelComponent(config: AndesDropdownConfiguration) {
        andesDropDownStandaloneContent.typeface = context.getFontOrDefault(R.font.andes_font_regular)
        andesDropDownStandaloneContent.text = config.label
        andesDropDownStandaloneContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.size.titleFontSize(context))
    }

    /**
     * Responsible for update all properties related to components that can change dynamically
     *
     * @param config current AndesListConfiguration
     */
    private fun updateDynamicComponents(config: AndesDropdownConfiguration) {
        setupLabelComponent(config)
    }

    private fun setupBottomSheet() {
        val layout: View = (context as AppCompatActivity).layoutInflater.inflate(R.layout.andes_layout_dropdown_bottom_sheet, null)
        val andesList = layout.findViewById<AndesList>(R.id.andesListDropdown)

        andesList?.delegate = this

        bottomSheetDialog.setContentView(layout)

        bottomSheetDialog.setOnShowListener {
            andesList?.refreshListAdapter()

            andesDropDownStandaloneChevron.setImageDrawable(
                    chevronUpIcon
            )
        }

        bottomSheetDialog.setOnDismissListener {
            andesDropDownStandaloneChevron.setImageDrawable(
                    chevronDownIcon
            )
        }

    }

    private fun openBottomSheet() {
        bottomSheetDialog.show()
    }

    /**
     * Creates all the views that are part of this Dropdown.
     * After a view is created then a view id is added to it.
     */
    private fun initComponents() {
        val container = LayoutInflater.from(context).inflate(R.layout.andes_layout_dropdown_standalone, this)
        andesDropDownStandaloneContent = container.findViewById(R.id.text_view_andes_dropdown_trigger_content)
        andesDropDownStandaloneChevron = container.findViewById(R.id.image_view_andes_dropdown_trigger_chevron)
//
//        //TODO esto va en el menu type standalone
//        dropdownBottomSheetDialog = DropdownBottomSheetDialog.newInstance(Bundle())
    }

    /**
     * Sets a view id to this dropdown.
     */
    private fun setupViewId() {
        if (id == NO_ID) { // If this view has no id
            id = View.generateViewId()
        }
    }

    override fun onItemClick(position: Int) {
        val itemSelected = listItems[position]

        listItems.forEach {
            it.isSelected = itemSelected == it
        }

        andesDropDownStandaloneContent.text = itemSelected.title

//        delegate.onItemSelected(this, position)

        bottomSheetDialog.dismiss()
    }

    override fun bind(andesList: AndesList, view: View, position: Int): AndesListViewItem {
        val item = listItems[position]

        val row: AndesListViewItem?

        row = AndesListViewItemSimple(
                context,
                item.title,
                size = andesList.size,
                avatar = item.avatar,
                itemSelected = item.isSelected,
                titleMaxLines = 50
        )

        return row
    }

    override fun getDataSetSize(): Int = listItems.size

    private fun createConfig() = AndesDropdownConfigurationFactory.create(andesDropdownAttrs)

}
