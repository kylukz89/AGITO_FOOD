package com.kylukz.agitofoods.Toolbox;

/**
 * Variáveis Globais
 * Armazena IP do servidor e demais variáveis diversas
 *
 * @author  Igor Maximo
 */
public abstract class VariaveisGlobais extends Parametros {

    //////////////////////
    // Controle de IPs (Conexão com Servidor)
    public static String IP = "http://modularesolution.com/desenvolvimento/AgitoFood/AgitoFoodApp_Cliente/App/";
    public static String TOKEN_X = "m3fq1traNGgtMBM3gcuBKSPhY4S2JYN";
    private static String TOKEN_Y = "Jzqw1K3dxfRpIE8kNXiCqX7jAwK9dq";
    public static String AUT_GET = "&tokenY=" + TOKEN_Y;

    //////////////////////
    // Controle de versionamento
    public static volatile int[] VERSAO_APP_LOCAL = {1, 0, 0}; // Para exibição
    public static volatile int FK_VERSIONAMENTO = 1; // Para banco
    public transient static volatile int FK_APP_VERSIONAMENTO = 0; // Para uso geral e pedidos
    //////////////////////
    // SQLite
    public static final int VERSAO_DB = 2;
    public static final String NOME_BANCO = "agitofoods.db";

    //////////////////////
    // Conexões por http // PADRÃO 7K
    public static int HTTP_CONNECTION_TIME = 100;
    public static int HTTP_TIME_OUT = 30000; // Usado para json
    public static int HTTP_URL_CONNECTION = 35000; // Usado para json

    ////////////////////
    // Para trabalhar com com.kylukz.fooddev.JSON
    public static final String FILE = "saida.json";

    ////////////////////
    // Mascara Data e Hora
    public static String MASCARA_DATA_HORA = "dd/MM/yyyy";

    ////////////////////
    // Qualidade de todas as imagens do App
    public static int qualidadeImagem = 100;

    ////////////////////
    // Ajuste de brilho para textos
    public static float ESPESSURA_BRILHO_OU_SOMBRA_TEXTOS = 8.5f;
}
