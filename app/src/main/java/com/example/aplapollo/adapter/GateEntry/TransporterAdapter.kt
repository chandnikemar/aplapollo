import android.content.Context
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.aplapollo.model.GateEntry.TransporterResponse

class TransporterAutoAdapter(
    context: Context,
    private var originalList: List<TransporterResponse>
) : ArrayAdapter<TransporterResponse>(
    context,
    android.R.layout.simple_dropdown_item_1line,
    ArrayList(originalList)
) {

    private var filteredList: List<TransporterResponse> =
        originalList

    private var queryText: String = ""

    // ================= UPDATE DATA =================

    fun updateData(
        newList: List<TransporterResponse>
    ) {

        originalList = newList
        filteredList = newList

        clear()
        addAll(newList)

        notifyDataSetChanged()
    }
    // ================= ITEM COUNT =================

    override fun getCount(): Int =
        filteredList.size

    // ================= GET ITEM =================

    override fun getItem(position: Int): TransporterResponse {

        return if (position < filteredList.size) {

            filteredList[position]

        } else {

            filteredList.last()
        }
    }

    // ================= VIEW =================
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        val view =
            convertView ?: LayoutInflater
                .from(context)
                .inflate(
                    android.R.layout.simple_dropdown_item_1line,
                    parent,
                    false
                )

        val textView =
            view.findViewById<TextView>(
                android.R.id.text1
            )

        // ✅ SAFETY CHECK
        if (position >= filteredList.size) {
            return view
        }

        val item =
            filteredList[position]

        val text =
            "${item.transporterCode} - ${item.transporterName}"

        textView.text =
            highlightText(
                text,
                queryText
            )

        return view
    }

    // ================= HIGHLIGHT =================
    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        return getView(
            position,
            convertView,
            parent
        )
    }
    private fun highlightText(
        text: String,
        query: String
    ): Spanned {

        if (query.isEmpty()) {

            return Html.fromHtml(
                text,
                Html.FROM_HTML_MODE_LEGACY
            )
        }

        val lowerText =
            text.lowercase()

        val lowerQuery =
            query.lowercase()

        val startIndex =
            lowerText.indexOf(lowerQuery)

        return if (startIndex >= 0) {

            val endIndex =
                startIndex + query.length

            val highlighted =
                text.substring(0, startIndex) +
                        "<font color='#FF9800'><b>" +
                        text.substring(startIndex, endIndex) +
                        "</b></font>" +
                        text.substring(endIndex)

            Html.fromHtml(
                highlighted,
                Html.FROM_HTML_MODE_LEGACY
            )

        } else {

            Html.fromHtml(
                text,
                Html.FROM_HTML_MODE_LEGACY
            )
        }
    }

    // ================= FILTER =================

    override fun getFilter(): Filter {

        return object : Filter() {

            override fun performFiltering(
                constraint: CharSequence?
            ): FilterResults {

                queryText =
                    constraint?.toString()
                        ?.trim()
                        ?: ""

                val result =
                    FilterResults()

                    filteredList =
                        if (queryText.isEmpty()) {

                            originalList

                        } else {

                        originalList
                            .filter {

                                it.transporterCode.contains(
                                    queryText,
                                    true
                                ) ||

                                        it.transporterName.contains(
                                            queryText,
                                            true
                                        )
                            }
                            .sortedWith(

                                compareBy<TransporterResponse> {

                                    !it.transporterCode.startsWith(
                                        queryText,
                                        true
                                    ) &&

                                            !it.transporterName.startsWith(
                                                queryText,
                                                true
                                            )

                                }.thenBy {

                                    it.transporterCode
                                }

                            )
                    }

                result.values =
                    filteredList

                result.count =
                    filteredList.size

                return result
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults?
            ) {

                filteredList =
                    results?.values
                            as? List<TransporterResponse>
                        ?: emptyList()

                notifyDataSetChanged()
            }
        }
    }

}
