<?php

namespace App\Model\DAO;

use App\Model\DAO\ConexaoDAO;
use App\Model\DAO\ErroDAO;
use App\Model\Entidade\EntidadePedido;

/**
 * 
 * Classe responsável por manipular
 * tudo referente aos pedidos
 * 
 * @author      Igor Maximo
 * @date        09/03/2021
 */
class PedidoDAO {

    private $NOME_TABELA = "pedido";
    private $NOME_TABELA_PEDIDO = "pedido_produto";

    /**
     * Grava um pedido finalizado
     * 
     * @author      Igor Maximo
     * @date        09/03/2021
     */
    public function setCadastrarNovoPedido(EntidadePedido $entidade) {
        try {
            $conexao = new ConexaoDAO();
            $conn = $conexao->conexao();

            mysqli_query($conn, "SET NAMES 'utf8'");

            $sql = "INSERT INTO " . $this->NOME_TABELA . " "
                    . "("
                    . "fk_cliente, "
                    . "fk_empresa, "
                    . "fk_forma_pagamento, "
                    . "valor_total, "
                    . "situacao, "
                    . "data_entrega, "
                    . "data_cadastro)"
                    . " values "
                    . "('" . $entidade->getFkCliente() . "',"
                    . "'1',"
                    . "'1',"
                    . "'" . $entidade->getTotal() . "',"
                    . "'1',"
                    . "'" . date('Y-m-d H:i:s') . "',"
                    . "'" . date('Y-m-d H:i:s') . "')";

            error_log($sql);


            try {
                if (mysqli_query($conn, $sql)) {
                    return $this->getUltimoIdPedidoCliente($entidade->getFkCliente());
                } else {
                    return 0;
                }
                mysqli_close($conn);
            } catch (Exception $e1) {
//                $this->setErro(__CLASS__, __METHOD__, __LINE__, $e1->getMessage());
            }
        } catch (Exception $e1) {
            echo $e1;
        }
        return 0;
    }

    /**
     * Retorna lista grupos de uma empresas
     */
    public function getUltimoIdPedidoCliente($fkCliente) {
        $lista = [];
        try {
            $conexao = new ConexaoDAO();
            $conn = $conexao->conexao();

            mysqli_query($conn, "SET NAMES 'utf8'");

            $sql = "SELECT id FROM pedido WHERE fk_cliente = " . $fkCliente . " order by id desc limit 1";

            if ($result = mysqli_query($conn, $sql)) {
                return mysqli_fetch_assoc($result)['id'];
            } else {
                return 0;
            }
        } catch (Exception $e1) {
            echo $e1;
        }
        return 0;
    }

    /**
     * Grava um pedido finalizado
     * 
     * @author      Igor Maximo
     * @date        09/03/2021
     */
    public function setCadastrarProdutosNovoPedido(EntidadePedido $entidade, $fkPedido) {
        try {
            $conexao = new ConexaoDAO();
            $conn = $conexao->conexao();

            mysqli_query($conn, "SET NAMES 'utf8'");


            for ($i = 0; $i < count($entidade->getListaIds()); $i++) {
                $sql = "INSERT INTO " . $this->NOME_TABELA_PEDIDO . " "
                        . "(fk_produto, "
                        . "fk_pedido, "
                        . "valor_total, "
                        . "comentario)"
                        . " values "
                        . "('" . $entidade->getListaIds()[$i] . "',"
                        . "'" . $fkPedido . "',"
                        . "'" . $entidade->getListaValorIndividual()[$i] . "',"
                        . "'" . $entidade->getListaComentario()[$i] . "')";

                try {
                    if (mysqli_query($conn, $sql)) {
                        
                    } else {
                        
                    }
                } catch (Exception $e1) {
//                $this->setErro(__CLASS__, __METHOD__, __LINE__, $e1->getMessage());
                }
            }
            mysqli_close($conn);
        } catch (Exception $e1) {
            echo $e1;
        }
    }

}
