package com.sun.weather.screen.setting

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.sun.weather.R
import com.sun.weather.databinding.FragmentSettingBinding
import com.sun.weather.utils.Constant.LANGUAGE_CODE_ENGLISH
import com.sun.weather.utils.Constant.LANGUAGE_CODE_VIETNAMESE
import com.sun.weather.utils.SharedPrefManager
import com.sun.weather.utils.base.BaseFragment
import java.util.Locale

class SettingFragment : BaseFragment<FragmentSettingBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater)
    }

    override fun initData() {
        val flagResId = SharedPrefManager.getInt(KEY_FLAG_RES_ID, DEFAULT_FLAG_RES_ID)
        viewBinding.flagImageView.setImageResource(flagResId)
        viewBinding.languageButton.setOnClickListener {
            showLanguageSelectionDialog()
        }
    }

    override fun initView() {
        viewBinding.flagImageView.setImageResource(R.drawable.vn_flag)
    }

    private fun showLanguageSelectionDialog() {
        val languageCode = Locale.getDefault().language
        val languages =
            when (languageCode) {
                LANGUAGE_CODE_VIETNAMESE -> resources.getStringArray(R.array.languages_vietnamese)
                LANGUAGE_CODE_ENGLISH -> resources.getStringArray(R.array.languages_english)
                else -> resources.getStringArray(R.array.languages_english)
            }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_language))
            .setItems(languages) { _, which ->
                when (which) {
                    INDEX_VIETNAMESE -> setLocale(LANGUAGE_CODE_VIETNAMESE, FLAG_RES_ID_VIETNAMESE)
                    INDEX_ENGLISH -> setLocale(LANGUAGE_CODE_ENGLISH, FLAG_RES_ID_ENGLISH)
                }
            }
            .show()
    }

    private fun setLocale(
        languageCode: String,
        flagResId: Int,
    ) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        requireContext().createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
        SharedPrefManager.putInt(KEY_FLAG_RES_ID, flagResId)
        viewBinding.flagImageView.setImageResource(flagResId)
        activity?.recreate()
    }

    companion object {
        const val KEY_FLAG_RES_ID = "flagResId"
        val DEFAULT_FLAG_RES_ID = R.drawable.vn_flag
        val FLAG_RES_ID_VIETNAMESE = R.drawable.vn_flag
        val FLAG_RES_ID_ENGLISH = R.drawable.en_flag
        const val INDEX_VIETNAMESE = 0
        const val INDEX_ENGLISH = 1

        fun newInstance() = SettingFragment()
    }
}
