package be.ehb.aquafin.model;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MaandDAO extends CrudRepository<Maand, Integer> {

    List<Maand> findByJaar(int jaar);
    List<Maand> findByJaarAndMaand(int jaar, int maand);
}