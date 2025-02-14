package com.woohyman.xml.ui.menu

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.woohyman.keyboard.utils.DialogUtils.show
import com.woohyman.keyboard.utils.EmuUtils.getDisplayWidth
import com.woohyman.xml.R
import com.woohyman.xml.databinding.GameMenuItemBinding

class GameMenu constructor(
    private val activity: Context,
    private val listener: OnGameMenuListener
) {

    private var items = ArrayList<GameMenuItem>()
    private var inflater: LayoutInflater =
        activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var dialog: Dialog? = null

    init {
        listener.onGameMenuCreate(this)
    }

    @JvmOverloads
    fun add(label: String, iconRID: Int = -1): GameMenuItem {
        val item = GameMenuItem()
        item.id = items.size
        item.title = label
        item.iconRID = iconRID
        items.add(item)
        return item
    }

    fun add(labelRID: Int): GameMenuItem {
        val item = add(activity.getText(labelRID) as String, -1)
        item.id = labelRID
        return item
    }

    fun add(labelRID: Int, iconRID: Int): GameMenuItem {
        val item = add(activity.getText(labelRID) as String, iconRID)
        item.id = labelRID
        return item
    }

    fun dismiss() {
        if (isOpen) {
            dialog?.dismiss()
        }
    }

    val isOpen: Boolean get() = dialog?.isShowing ?: false

    fun open() {
        dialog?.dismiss()
        dialog = Dialog(activity, R.style.GameDialogTheme).also {
            listener.onGameMenuPrepare(this)
            val surroundContainer =
                inflater.inflate(R.layout.game_menu_surround, null) as RelativeLayout
            surroundContainer.setOnClickListener { _ -> it.cancel() }
            val container = surroundContainer.findViewById<LinearLayout>(R.id.game_menu_container)
            val params = container.layoutParams as RelativeLayout.LayoutParams
            val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val width = getDisplayWidth(display)
            val px = width / 10
            params.setMargins(px, 0, px, 0)
            container.layoutParams = params
            val padding = activity.resources.getDimensionPixelSize(
                R.dimen.dialog_back_padding
            )
            container.setPadding(padding, padding, padding, padding)
            val margin = activity.resources.getDimensionPixelSize(
                R.dimen.dialog_button_margin
            )
            val landsacpe =
                activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            var i = 0
            while (i < items.size) {
                if (landsacpe) {
                    val pp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    pp.gravity = Gravity.CENTER_VERTICAL
                    val menuRow = LinearLayout(activity)
                    val item = items[i]
                    menuRow.addView(createButton(item, margin, it), pp)
                    i++
                    if (i < items.size) {
                        val lineSeparator = LinearLayout(activity)
                        lineSeparator.setBackgroundColor(-0x1)
                        menuRow.addView(lineSeparator, 1, LinearLayout.LayoutParams.MATCH_PARENT)
                        val item2 = items[i]
                        menuRow.addView(createButton(item2, margin, it), pp)
                    }
                    container.addView(menuRow)
                } else {
                    val item = items[i]
                    container.addView(
                        createButton(item, margin, it),
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                if (i < items.size - 1) {
                    val linSeperator = LinearLayout(activity)
                    linSeperator.setBackgroundColor(-0x1)
                    container.addView(linSeperator, LinearLayout.LayoutParams.MATCH_PARENT, 1)
                }
                i++
            }
            it.setContentView(surroundContainer)
            it.setOnCancelListener {
                listener.onGameMenuClosed(this@GameMenu)
                dialog = null
            }
            it.setOnDismissListener {
                listener.onGameMenuClosed(this@GameMenu)
                dialog = null
            }
            show(it, true)
            listener.onGameMenuOpened(this)
        }
    }

    private fun createButton(item: GameMenuItem, margin: Int, dialog: Dialog): View {
        val view = inflater.inflate(R.layout.game_menu_item, null)
        val binding = GameMenuItemBinding.bind(view)
        binding.gameMenuItemLabel.text = item.title

        view.setOnClickListener { v: View? ->
            listener.onGameMenuItemSelected(this@GameMenu, item)
            dialog.dismiss()
            listener.onGameMenuClosed(this@GameMenu)
        }

        val iconRID = item.iconRID
        if (iconRID > 0) {
            binding.gameMenuItemIcon.setImageResource(iconRID)
        }

        view.isFocusable = true
        view.isEnabled = item.enable
        binding.gameMenuItemLabel.isEnabled = item.enable
        return view
    }

    fun getItem(id: Int): GameMenuItem? {
        for (item in items) {
            if (item.id == id) {
                return item
            }
        }
        return null
    }

    interface OnGameMenuListener {
        fun onGameMenuCreate(menu: GameMenu)
        fun onGameMenuPrepare(menu: GameMenu)
        fun onGameMenuOpened(menu: GameMenu?)
        fun onGameMenuClosed(menu: GameMenu)
        fun onGameMenuItemSelected(menu: GameMenu?, item: GameMenuItem)
    }

    inner class GameMenuItem {
        var title = ""
        var id = 0
        var iconRID = -1
        var enable = true

        operator fun set(title: String, id: Int) {
            this.title = title
            this.id = id
        }
    }
}