package com.kylukz.agitofoods.DAO;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.kylukz.agitofoods.JavaBeans.EntidadeProdutosGrupoEscolhido;
import com.kylukz.agitofoods.Routes.Rotas;
import com.kylukz.agitofoods.Toolbox.Json;
import com.kylukz.agitofoods.Toolbox.VariaveisGlobais;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.kylukz.agitofoods.DAO.UsuarioDAO.ENTIDADE_USUARIO;

/**
 * Classe responsável por manipular
 * o carrinho e finalizar ou cancelar pedidos
 *
 * @author      Igor Maximo
 * @date        09/03/2021
 */
public class PedidoDAO {


    /**
     * Finaliza e envia o pedido para o servidor
     *
     * @author      Igor Maximo
     * @date        09/03/2021
     */
    public Object[] setEnviarFinalizarPedido(double totalCarrinho, double totalCredito, double totalDebito, double totalReceberLocal, ArrayList<EntidadeProdutosGrupoEscolhido> carrinho) {
        ArrayList<Object> listaIds = new ArrayList<>();
        ArrayList<Object> listaGruposIds = new ArrayList<>();
        ArrayList<Object> listaValorIndividual = new ArrayList<>();
        ArrayList<Object> listaObs = new ArrayList<>();

        for (int i = 0; i < carrinho.size(); i++) {
            listaIds.add(i, carrinho.get(i).getId());
            listaGruposIds.add(i, carrinho.get(i).getGrupo());
            listaValorIndividual.add(i, carrinho.get(i).getValor());
            listaObs.add(i, carrinho.get(i).getObservacao());
        }

        JSONArray jsonArray = new Json().setEnviaPostRetornaJsonObjectServidor(Rotas.INSERT_NOVO_PEDIDO,
                new Uri.Builder()
                        .appendQueryParameter("fkCliente", ENTIDADE_USUARIO.getId() + "")
                        .appendQueryParameter("listaIds", String.valueOf(listaIds))
                        .appendQueryParameter("listaGruposIds", String.valueOf(listaGruposIds))
                        .appendQueryParameter("listaValorIndividual", String.valueOf(listaValorIndividual))
                        .appendQueryParameter("listaObs", String.valueOf(listaObs))
                        .appendQueryParameter("totalCarrinho", totalCarrinho + "")
                        .appendQueryParameter("totalCredito", totalCredito + "")
                        .appendQueryParameter("totalDebito", totalDebito + "")
                        .appendQueryParameter("totalReceberLocal", totalReceberLocal + "")
        );

        return new Object[]{};
    }
}
