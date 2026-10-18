package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {

    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

    List<Serie> findByAtoresContainingIgnoreCase(String nomeAtor);

    List<Serie> findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(
            String nomeAtor, Double avaliacao);

    List<Serie> findTop5ByOrderByAvaliacaoDesc();

    @Query("""
            SELECT s FROM Serie s
            WHERE s.totalTemporadas <= :totalTemporadas
            AND s.avaliacao >= :avaliacao
            """)
    List<Serie> seriesPorTemporadaEAvaliacao(
            int totalTemporadas,
            double avaliacao
    );

    @Query("""
        SELECT e FROM Serie s
        JOIN s.episodios e
        WHERE LOWER(e.titulo) LIKE LOWER(CONCAT('%', :trechoEpisodio, '%'))
        """)
    List<Episodio> episodiosPorTrecho(String trechoEpisodio);

    @Query("""
        SELECT e FROM Serie s
        JOIN s.episodios e
        WHERE s = :serie
        ORDER BY e.avaliacao DESC
        """)
    List<Episodio> topEpisodiosPorSerie(
            Serie serie,
            org.springframework.data.domain.Pageable pageable
    );
    @Query("""
        SELECT e FROM Serie s
        JOIN s.episodios e
        WHERE s = :serie
        AND YEAR(e.dataLancamento) >= :anoLancamento
        """)
    List<Episodio> episodiosPorSerieEAno(
            Serie serie,
            int anoLancamento
    );
}