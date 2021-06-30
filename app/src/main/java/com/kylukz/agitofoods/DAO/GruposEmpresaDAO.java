package com.kylukz.agitofoods.DAO;

import android.net.Uri;

import com.kylukz.agitofoods.JavaBeans.EntidadeEmpresa;
import com.kylukz.agitofoods.JavaBeans.EntidadeGrupoProdutosEmpresa;
import com.kylukz.agitofoods.JavaBeans.EntidadeUsuario;
import com.kylukz.agitofoods.Routes.Rotas;
import com.kylukz.agitofoods.Toolbox.Ferramentas;
import com.kylukz.agitofoods.Toolbox.Json;
import com.kylukz.agitofoods.Toolbox.VariaveisGlobais;
import com.kylukz.agitofoods.View.MenuPrincipalEmpresa;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsável por manipular todas
 * as empresas cadastradas no sistema
 *
 * @author Igor Maximo
 * @date 21/05/2020
 */
public class GruposEmpresaDAO {

    Json json = new Json();
    public static EntidadeEmpresa entidadeEmpresa = new EntidadeEmpresa();

    /**
     * Retorna lista de todos os grupos de
     * uma empresa
     *
     * @author  Igor Maximo
     * @date    31/05/2020
     */
    public List<EntidadeGrupoProdutosEmpresa> getListaGruposEmpresa(int empresa) {
        List<EntidadeGrupoProdutosEmpresa> listaGrupoProdutosEmpresa = new ArrayList<>();
        try {
            JSONArray jsonArray = json.setEnviaPostRetornaJsonObjectServidor(Rotas.SELECT_GRUPOS_EMPRESAS, new Uri.Builder()
                            .appendQueryParameter("tokenX", VariaveisGlobais.TOKEN_X)
                            .appendQueryParameter("idEmpresa", empresa + ""));
            JSONObject jsonObject = null;

            for (int i = 0; i < jsonArray.length(); i++) {

                jsonObject = jsonArray.getJSONObject(i);
                System.out.println("Nome===========>"+jsonObject.getString("Nome"));

                listaGrupoProdutosEmpresa.add(new EntidadeGrupoProdutosEmpresa(
                        jsonObject.getInt("Cod_GRUPO"),
                        jsonObject.getString("Nome"),
                        Ferramentas.convertBase64ToBitmap(jsonObject.getString("foto")),
                        MenuPrincipalEmpresa.CTX,
                        jsonObject.getInt("fk_empresa"),
                        true/*jsonObject.getBoolean("sePizza"))*/
                ));
            }
        } catch (Exception e) {
            LogDAO.setErro("UsuarioDAO", "getUsuario", e.getMessage(), Integer.parseInt(String.valueOf(e.getStackTrace()[0])), 0, new EntidadeUsuario().getId(), new EntidadeUsuario().getFkVersionamento());
        }
        return listaGrupoProdutosEmpresa;
    }

    /**
     * Retorna lista de todos os grupos de
     * uma empresa
     *
     * @author  Igor Maximo
     * @date    31/05/2020
     */
//    public List<EntidadeProdutosGrupoEscolhido> getListaProdutosGruposEmpresa(int empresa, int grupo) {
//        List<EntidadeProdutosGrupoEscolhido> listaGrupoProdutosEmpresa = new ArrayList<>();
//        try {
//            JSONArray jsonArray = json.setEnviaPostRetornaJsonObjectServidor(Rotas.SELECT_LISTA_PRODUTOS_GRUPOS_EMPRESA, new Uri.Builder()
//                    .appendQueryParameter("tokenX", VariaveisGlobais.TOKEN_X)
//                    .appendQueryParameter("idEmpresa", empresa + "")
//                    .appendQueryParameter("idGrupo", grupo + ""));
//
//            JSONObject jsonObject = null;
//
//            for (int i = 0; i < jsonArray.length(); i++) {
//                jsonObject = jsonArray.getJSONObject(i);
//                listaGrupoProdutosEmpresa.add(new EntidadeProdutosGrupoEscolhido(
//                        grupo,
//                        jsonObject.getString("Nome"),
//                        Ferramentas.convertBase64ToBitmap(jsonObject.getString("foto")),
//                        jsonObject.getDouble("valor"),
//                        EmpresaActivity.CTX,
//                        jsonObject.getBoolean("sePizza"),
//                        0,
//                        null,
//                        null)
//                );
//            }
//        } catch (Exception e) {
//            LogDAO.setErro("UsuarioDAO", "getUsuario", e.getMessage(), Integer.parseInt(String.valueOf(e.getStackTrace()[0])), 0, new EntidadeUsuario().getId(), new EntidadeUsuario().getFkVersionamento());
//        }
//        return listaGrupoProdutosEmpresa;
//    }

    /**
     * Retorna lista de todas as empresas
     * cadastradas no sistema
     *
     * @author Igor Maximo
     * @date 21/05/2020
     */
    public EntidadeEmpresa getEmpresaPorNome(String nomeEmpresa) {
        EntidadeEmpresa entidadeEmpresas = new EntidadeEmpresa();
        try {
            JSONArray jsonArray = json.setEnviaPostRetornaJsonObjectServidor(Rotas.SELECT_EMPRESAS,
                    new Uri.Builder()
                            .appendQueryParameter("tokenX", VariaveisGlobais.TOKEN_X)
                            .appendQueryParameter("nomeEmpresa", nomeEmpresa)
            );
            JSONObject jsonObject = null;
            EntidadeEmpresa entidadeEmpresa = null;

            jsonObject = jsonArray.getJSONObject(0);

            entidadeEmpresa.setNome(jsonObject.getString("Fantasia"));
            entidadeEmpresa.setNome(jsonObject.getString("foto"));

        } catch (Exception e) {
            LogDAO.setErro("UsuarioDAO", "getUsuario", e.getMessage(), Integer.parseInt(String.valueOf(e.getStackTrace()[0])), 0, new EntidadeUsuario().getId(), new EntidadeUsuario().getFkVersionamento());
        }
        return entidadeEmpresas;
    }

}

