<?php

namespace App\Model\DAO;

/**
 * Credenciais de acesso ao banco de dados
 * 
 * @author      Igor Maximo
 * @date        10/11/2020
 */
class ConexaoDAO {
 
    // Banco
    public static $SERVIDOR_IP = "mysql.modularesolution.com";
    public static $USUARIO = "modularesoluti";
    public static $SENHA = "mod100solu1898";
    public static $BANCO_NOME = "modularesoluti";
     
    /**
    * Abre conexão com banco de dados
    * 
    * @author       Igor Maximo
    * @date         10/11/2020
    */
    public function conexao() {
        try {
            $conn = mysqli_connect(ConexaoDAO::$SERVIDOR_IP, ConexaoDAO::$USUARIO, ConexaoDAO::$SENHA, ConexaoDAO::$BANCO_NOME) or die(mysqli_error());
        } catch (\Exception $e1) {
            error_log($e1);
        }
        return $conn;
    }
}

?>
