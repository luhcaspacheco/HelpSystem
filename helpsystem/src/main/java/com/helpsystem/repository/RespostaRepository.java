package com.helpsystem.repository;

import com.helpsystem.model.Resposta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespostaRepository extends JpaRepository<Resposta, Integer> {

    List<Resposta> findBySolicitacaoIdOrderByDataCriacaoAsc(Integer solicitacaoId);

    void deleteBySolicitacaoId(Integer solicitacaoId);
}
