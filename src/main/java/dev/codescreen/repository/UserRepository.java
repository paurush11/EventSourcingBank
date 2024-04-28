package dev.codescreen.repository;

import dev.codescreen.model.User;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends KeyValueRepository<User, String> {
    <S extends User> S save(S entity);
    Optional<User> findById(Long Id);
    List<User> findByEmail(String email);
    Optional<User> findByUserName(String username);
    void deleteById(Long id);
}
