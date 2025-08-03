package com.fabiosf34.secretvideorecorder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.databinding.FragmentFormPasswordBinding
import com.fabiosf34.secretvideorecorder.viewModel.SettingViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [FormPasswordFragment.newInstance"] factory method to
 * create an instance of this fragment.
 */
class FormPasswordFragment : Fragment() {
    private var _binding: FragmentFormPasswordBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var passwdFormViewModel: SettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentFormPasswordBinding.inflate(inflater, container, false)
        passwdFormViewModel = ViewModelProvider(this)[SettingViewModel::class.java]

        listeners()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        passwdFormViewModel.getPassword()
        observer()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun observer() {
        passwdFormViewModel.appPassword.observe(viewLifecycleOwner) {
            binding.passwordEditTextForm.setText(it)
            binding.repeatPasswordEditText.setText(it)
        }
    }

    private fun listeners() {
        binding.sendPasswordBtnForm.setOnClickListener {
            if (!binding.passwordEditTextForm.text.isNullOrEmpty() && binding.passwordEditTextForm.text.toString() == binding.repeatPasswordEditText.text.toString()) {
                passwdFormViewModel.setPassword(binding.passwordEditTextForm.text.toString())
                activity?.supportFragmentManager?.popBackStack()
            } else {
                if (binding.passwordEditTextForm.text.isNullOrEmpty()) {
                    binding.passwordEditTextForm.error = getString(R.string.empty_field_text)
                } else if (binding.passwordEditTextForm.text.toString() != binding.repeatPasswordEditText.text.toString()) {
                    binding.repeatPasswordEditText.error =
                        getString(R.string.passwords_dont_match_text)
                }
            }
        }

        binding.deletePasswordBtnForm.setOnClickListener {
            if (passwdFormViewModel.getPassword().isNotEmpty()) {
                passwdFormViewModel.setPassword("")
                passwdFormViewModel.enablePasswdApp(false)
                Toast.makeText(
                    context,
                    getString(R.string.password_deleted_text),
                    Toast.LENGTH_SHORT
                ).show()
                activity?.supportFragmentManager?.popBackStack()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.no_password_text),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}