package com.kylukz.agitofoods.DAO;

import android.net.Uri;

import com.kylukz.agitofoods.JavaBeans.EntidadeProdutosGrupoEscolhido;
import com.kylukz.agitofoods.Routes.Rotas;
import com.kylukz.agitofoods.Toolbox.Ferramentas;
import com.kylukz.agitofoods.Toolbox.Json;
import com.kylukz.agitofoods.Toolbox.VariaveisGlobais;
import com.kylukz.agitofoods.View.EmpresaActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    Json json = new Json();

    /**
     * Retorna lista de todos os grupos de
     * uma empresa
     *
     * @author  Igor Maximo
     * @date    31/05/2020
     */
    public List<EntidadeProdutosGrupoEscolhido> getListaProdutosGruposEmpresa(int empresa, int grupo) {
        System.out.println("empresa ======> " + empresa);
        System.out.println("grupo ======> " + grupo);

        List<EntidadeProdutosGrupoEscolhido> listaGrupoProdutosEmpresa = new ArrayList<>();
        try {
            JSONArray jsonArray = json.setEnviaPostRetornaJsonObjectServidor(Rotas.SELECT_LISTA_PRODUTOS_GRUPOS_EMPRESA, new Uri.Builder()
                    .appendQueryParameter("tokenX", VariaveisGlobais.TOKEN_X)
                    .appendQueryParameter("idEmpresa", empresa + "")
                    .appendQueryParameter("idGrupo", grupo + ""));

            JSONObject jsonObject = null;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                // Carrega lista
                listaGrupoProdutosEmpresa.add(new EntidadeProdutosGrupoEscolhido(
                        jsonObject.getInt("id"),
                        grupo,
                        jsonObject.getString("nome"),
                        Ferramentas.convertBase64ToBitmap(jsonObject.getString("foto")),
                        jsonObject.getDouble("preco_custo"),
                        EmpresaActivity.CTX,
                        jsonObject.getBoolean("sePizza"),
                        jsonObject.getString("descricao"),
                        null,
                        null)
                );
            }
        } catch (Exception e) {
      //      LogDAO.setErro("UsuarioDAO", "getUsuario", e.getMessage(), Integer.parseInt(String.valueOf(e.getStackTrace()[0])), 0, new EntidadeUsuario().getId(), new EntidadeUsuario().getFkVersionamento());
        }
        return listaGrupoProdutosEmpresa;
    }

}
