package com.kylukz.agitofoods.DAO;

import android.net.Uri;

import com.kylukz.agitofoods.JavaBeans.EntidadeEmpresa;
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
public class EmpresaDAO {

    Json json = new Json();
    public static EntidadeEmpresa entidadeEmpresa = new EntidadeEmpresa();

    /**
     * Retorna lista de todas as empresas
     * cadastradas no sistema
     *
     * @author Igor Maximo
     * @date 21/05/2020
     */
    public List<EntidadeEmpresa> getListaEmpresa() {
        List<EntidadeEmpresa> entidadeEmpresas = new ArrayList<>();
        try {
            JSONArray jsonArray = json.setEnviaPostRetornaJsonObjectServidor(Rotas.SELECT_EMPRESAS, new Uri.Builder().appendQueryParameter("tokenX", VariaveisGlobais.TOKEN_X));
            JSONObject jsonObject = null;
            EntidadeEmpresa entidadeEmpresa = null;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                entidadeEmpresa = new EntidadeEmpresa(
                        jsonObject.getInt("Cod_empresa"),
                        Ferramentas.convertBase64ToBitmap(jsonObject.getString("foto")),
                        jsonObject.getString("Fantasia"),
                        jsonObject.getString("Endereco"),
                        "Super Lanches",
                        "Lanchonetes",
                        "",
                        MenuPrincipalEmpresa.CTX
                );
                entidadeEmpresas.add(entidadeEmpresa);
            }
        } catch (Exception e) {
            LogDAO.setErro("UsuarioDAO", "getUsuario", e.getMessage(), Integer.parseInt(String.valueOf(e.getStackTrace()[0])), 0, new EntidadeUsuario().getId(), new EntidadeUsuario().getFkVersionamento());
        }
        return entidadeEmpresas;
    }

    /**
     * Retorna lista de todas as empresas
     * com base na string filtro pesquisa
     *
     * @author Igor Maximo
     * @date 16/06/2020
     */
    public List<EntidadeEmpresa> getListaEmpresa(String pesquisa) {
        List<EntidadeEmpresa> entidadeEmpresas = new ArrayList<>();
        try {
            JSONArray jsonArray = json.setEnviaPostRetornaJsonObjectServidor(Rotas.SELECT_EMPRESAS, new Uri.Builder()
                    .appendQueryParameter("tokenX", VariaveisGlobais.TOKEN_X)
                    .appendQueryParameter("nomeEmpresa", pesquisa));

            JSONObject jsonObject = null;
            EntidadeEmpresa entidadeEmpresa = null;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                entidadeEmpresa = new EntidadeEmpresa(
                        jsonObject.getInt("Cod_empresa"),
                        Ferramentas.convertBase64ToBitmap(jsonObject.getString("foto")),
                        jsonObject.getString("Fantasia"),
                        jsonObject.getString("Endereco"),
                        "Super Lanches",
                        "Lanchonetes",
                        "",
                        MenuPrincipalEmpresa.CTX
                );
                entidadeEmpresas.add(entidadeEmpresa);
            }
        } catch (Exception e) {
            LogDAO.setErro("UsuarioDAO", "getUsuario", e.getMessage(), Integer.parseInt(String.valueOf(e.getStackTrace()[0])), 0, new EntidadeUsuario().getId(), new EntidadeUsuario().getFkVersionamento());
        }
        return entidadeEmpresas;
    }

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

