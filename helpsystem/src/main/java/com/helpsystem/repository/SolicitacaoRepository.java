package com.helpsystem.repository;

import com.helpsystem.model.Solicitacao;
import com.helpsystem.model.enums.StatusSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Integer> {

    List<Solicitacao> findAllByOrderByDataCriacaoDesc();

    List<Solicitacao> findByStatusOrderByDataCriacaoDesc(StatusSolicitacao status);

    List<Solicitacao> findByCategoriaIdOrderByDataCriacaoDesc(Integer categoriaId);

    List<Solicitacao> findByStatusAndCategoriaIdOrderByDataCriacaoDesc(StatusSolicitacao status, Integer categoriaId);

    @Query("""
            select s from Solicitacao s
            where (:status is null or s.status = :status)
              and (:categoriaId is null or s.categoria.id = :categoriaId)
              and (:autorId is null or s.autor.id = :autorId)
            order by s.dataCriacao desc
            """)
    List<Solicitacao> filtrar(StatusSolicitacao status, Integer categoriaId, Integer autorId);

    @Query("""
            select s from Solicitacao s
            where (:status is null or s.status = :status)
              and (:categoriaId is null or s.categoria.id = :categoriaId)
              and (:autorId is null or s.autor.id = :autorId)
            order by
              case s.prioridade
                when com.helpsystem.model.enums.Prioridade.ALTA then 1
                when com.helpsystem.model.enums.Prioridade.MEDIA then 2
                when com.helpsystem.model.enums.Prioridade.BAIXA then 3
              end,
              s.dataCriacao desc
            """)
    List<Solicitacao> filtrarOrdenandoPorPrioridade(StatusSolicitacao status, Integer categoriaId, Integer autorId);
}
