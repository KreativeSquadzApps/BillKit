package com.kreativesquadz.billkit.ui.bills.creditNote

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kreativesquadz.billkit.R

class CreditNoteFrag : Fragment() {

    companion object {
        fun newInstance() = CreditNoteFrag()
    }

    private val viewModel: CreditNoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_credit_note, container, false)
    }
}