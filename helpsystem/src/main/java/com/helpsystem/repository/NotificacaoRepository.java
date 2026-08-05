package com.helpsystem.repository;

import com.helpsystem.model.Notificacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    List<Notificacao> findByUsuarioIdOrderByLidaAscDataCriacaoDesc(Integer usuarioId);

    List<Notificacao> findByUsuarioIdAndLidaOrderByDataCriacaoDesc(Integer usuarioId, boolean lida);

    long countByUsuarioIdAndLidaFalse(Integer usuarioId);
}
