package dev.codescreen.repository;

import dev.codescreen.model.Event;
import dev.codescreen.model.User;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface EventRepository extends KeyValueRepository<Event, String> {

    <S extends Event> S save(S entity);
    List<Event> findAllByOrderByTimestampAsc();
    List<Event> findByUserIdOrderByTimestampAsc(String userId);

}
