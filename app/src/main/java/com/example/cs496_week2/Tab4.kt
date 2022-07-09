package com.example.cs496_week2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cs496_week2.databinding.FragmentTab4Binding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Tab4 : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentTab4Binding
    lateinit var root: View
    lateinit var histroyList: View
    lateinit var historyListAdapter: historyListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_tab4, container, false)
        histroyList = root.findViewById(R.id.history_list)
        historyListAdapter = historyListAdapter(MainActivity.user.running)

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tab4().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}