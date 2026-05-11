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
    private val originalList: List<TransporterResponse>
) : ArrayAdapter<TransporterResponse>(
    context,
    android.R.layout.simple_dropdown_item_1line,
    ArrayList(originalList)
) {

    private var filteredList: List<TransporterResponse> = originalList
    private var queryText: String = ""

    override fun getCount(): Int = filteredList.size

    override fun getItem(position: Int): TransporterResponse {
        return filteredList[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)

        val textView = view.findViewById<TextView>(android.R.id.text1)

        val item = filteredList[position]

        val displayText = "${item.transporterCode} - ${item.transporterName}"

        textView.text = getHighlightedText(displayText, queryText) // ✅ FIX

        return view
    }

    private fun getHighlightedText(text: String, query: String): Spanned {

        if (query.isEmpty()) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        }

        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()

        val startIndex = lowerText.indexOf(lowerQuery)

        return if (startIndex >= 0) {

            val endIndex = startIndex + query.length

            val highlighted = text.substring(0, startIndex) +
                    "<b><font color='#FF9800'>" +
                    text.substring(startIndex, endIndex) +
                    "</font></b>" +
                    text.substring(endIndex)

            Html.fromHtml(highlighted, Html.FROM_HTML_MODE_LEGACY)

        } else {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): FilterResults {

                queryText = constraint?.toString()?.trim() ?: ""

                val result = FilterResults()

                filteredList = if (queryText.isEmpty()) {
                    originalList
                } else {
                    originalList.filter {
                        it.transporterCode.contains(queryText, true) ||
                                it.transporterName.contains(queryText, true)
                    }
                }

                result.values = filteredList
                result.count = filteredList.size

                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                filteredList = results?.values as List<TransporterResponse>

                notifyDataSetChanged()
            }
        }
    }
}