package be.ehb.aquafin.model;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MaandDAO extends CrudRepository<Maand, Integer> {

    List<Maand> findByJaarOrderByMaand(int jaar);

    List<Maand> findAllByOrderByJaarDescMaandAsc();

    Maand findByJaarAndMaand(int jaar, int maand);

}