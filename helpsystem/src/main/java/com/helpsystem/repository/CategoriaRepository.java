package com.helpsystem.repository;

import com.helpsystem.model.Categoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    List<Categoria> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
