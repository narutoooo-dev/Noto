package com.noto.app.ui.folder

import com.airbnb.epoxy.EpoxyController
import com.noto.app.databinding.NoteItemBinding

interface NoteItemTouchHelperCallbackInfo {

    val epoxyController: EpoxyController

    val dragFlags: Int

    fun onItemSelected(item: NoteItem, binding: NoteItemBinding)

    fun onItemMoved(item: NoteItem, binding: NoteItemBinding)

    fun onItemReleased(item: NoteItem, binding: NoteItemBinding)

}