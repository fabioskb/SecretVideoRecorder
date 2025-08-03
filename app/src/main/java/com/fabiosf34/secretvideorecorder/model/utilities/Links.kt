package com.fabiosf34.secretvideorecorder.model.utilities

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.widget.TextView

class Links {
    companion object {
        fun setPrivacyPolicyLink(textView: TextView, clickableSpan: ClickableSpan) {
            val spannableString = SpannableString(textView.text)

            // Encontra o início e o fim do texto do link dentro da string completa
            // Se o texto do TextView for exatamente o texto do link:
            spannableString.setSpan(
                clickableSpan,
                0,
                textView.text.toString().length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textView.text = spannableString
            textView.movementMethod =
                LinkMovementMethod.getInstance() // Essencial para tornar os links clicáveis
        }
    }
}