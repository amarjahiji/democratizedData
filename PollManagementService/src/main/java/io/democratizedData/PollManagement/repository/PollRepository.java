package io.democratizedData.PollManagement.repository;

import io.democratizedData.PollManagement.model.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PollRepository extends JpaRepository<Poll, String> {
    List<Poll> findByActiveTrue();
}
