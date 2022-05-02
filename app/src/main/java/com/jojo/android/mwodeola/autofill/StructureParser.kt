package com.jojo.android.mwodeola.autofill

import android.app.assist.AssistStructure
import android.app.assist.AssistStructure.ViewNode
import android.os.Build
import androidx.annotation.RequiresApi
import com.jojo.android.mwodeola.autofill.model.AutofillFieldMetadata

@RequiresApi(Build.VERSION_CODES.O)
object StructureParser {
    const val TAG = "StructureParser"

    fun parseForFill(autofillStructure: AssistStructure): ArrayList<AutofillFieldMetadata> =
        traverseStructure(autofillStructure, true)

    fun parseForSave(autofillStructure: AssistStructure): ArrayList<AutofillFieldMetadata> =
        traverseStructure(autofillStructure, false)

    private fun traverseStructure(structure: AssistStructure,
                                  forFill: Boolean): ArrayList<AutofillFieldMetadata> {
        val metadataList = arrayListOf<AutofillFieldMetadata>()

        val windowNodes: List<AssistStructure.WindowNode> =
            structure.run {
                (0 until windowNodeCount).map { getWindowNodeAt(it) }
            }

        windowNodes.forEach { windowNode: AssistStructure.WindowNode ->
            val viewNode: ViewNode? = windowNode.rootViewNode
            traverseNode(viewNode, metadataList, forFill)
        }

        return metadataList
    }

    private fun traverseNode(viewNode: ViewNode?,
                             metadataList: ArrayList<AutofillFieldMetadata>, forFill: Boolean) {
        if (viewNode == null) return

        AutofillFieldMetadata.getInstance(viewNode)?.let {
            metadataList.add(it)
        }

        if (forFill) { // 자동 완성 채우기.
        } else { // 자동 완성 필드 저장.
        }

        val children: List<ViewNode> =
            viewNode.run {
                (0 until childCount).map { getChildAt(it) }
            }

        children.forEach { childNode: ViewNode ->
            traverseNode(childNode, metadataList, forFill)
        }
    }

    fun String.containsAll(vararg text: String): Boolean {
        text.forEach {
            if (this.contains(it)) {
                return true
            }
        }
        return false
    }
}