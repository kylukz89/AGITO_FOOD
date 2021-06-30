package com.kylukz.agitofoods.Animation;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Coloca efeito de sombreamento em componentes
 *
 * @author Igor Maximo1
 */
public abstract class Sombreamento {

    /**
     * Seta sombreamento em texto de button
     * @author Igor Maximo
     */
    public static void setSombraComponente(float tamanho, int distanciaX, int distanciaY, Button button, int cor) {
        button.setShadowLayer(tamanho, distanciaX, distanciaY, cor);
    }

    /**
     * Seta sombreamento em texto de editText
     * @author Igor Maximo
     */
    public static void setSombraComponente(float tamanho, int distanciaX, int distanciaY, EditText editText, int cor) {
        editText.setShadowLayer(tamanho, distanciaX, distanciaY, cor);
    }

    /**
     * Seta sombreamento em texto de textView
     * @author Igor Maximo
     */
    public static void setSombraComponente(float tamanho, int distanciaX, int distanciaY, TextView textView, int cor) {
        textView.setShadowLayer(tamanho, distanciaX, distanciaY, cor);
    }


}
