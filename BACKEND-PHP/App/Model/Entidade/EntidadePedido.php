<?php

namespace App\Model\Entidade;

/**
 * Entidade responsável por grupo
 * dos produtos de uma empresa
 * 
 * @author      Igor Maximo  
 * @date        31/05/2020
 */
class EntidadePedido {

    private $id;
    private $fkCliente;
    private $listaIds;
    private $listaGruposIds;
    private $listaValorIndividual;
    private $listaComentario;
    private $totalCarrinho;
    private $totalCredito;
    private $totalDebito;
    private $totalReceberLocal;
    private $total;

    function getListaValorIndividual() {
        return $this->listaValorIndividual;
    }

    function setListaValorIndividual($listaValorIndividual) {
        $this->listaValorIndividual = $listaValorIndividual;
    }

    function getListaComentario() {
        return $this->listaComentario;
    }

    function setListaComentario($listaComentario) {
        $this->listaComentario = $listaComentario;
    }

    function getTotal() {
        return $this->total;
    }

    function setTotal($total) {
        $this->total = $total;
    }

    function getFkCliente() {
        return $this->fkCliente;
    }

    function setFkCliente($fkCliente) {
        $this->fkCliente = $fkCliente;
    }

    function getId() {
        return $this->id;
    }

    function getListaIds() {
        return $this->listaIds;
    }

    function getListaGruposIds() {
        return $this->listaGruposIds;
    }

    function getTotalCarrinho() {
        return $this->totalCarrinho;
    }

    function getTotalCredito() {
        return $this->totalCredito;
    }

    function getTotalDebito() {
        return $this->totalDebito;
    }

    function getTotalReceberLocal() {
        return $this->totalReceberLocal;
    }

    function setId($id) {
        $this->id = $id;
    }

    function setListaIds($listaIds) {
        $this->listaIds = $listaIds;
    }

    function setListaGruposIds($listaGruposIds) {
        $this->listaGruposIds = $listaGruposIds;
    }

    function setTotalCarrinho($totalCarrinho) {
        $this->totalCarrinho = $totalCarrinho;
    }

    function setTotalCredito($totalCredito) {
        $this->totalCredito = $totalCredito;
    }

    function setTotalDebito($totalDebito) {
        $this->totalDebito = $totalDebito;
    }

    function setTotalReceberLocal($totalReceberLocal) {
        $this->totalReceberLocal = $totalReceberLocal;
    }

}
