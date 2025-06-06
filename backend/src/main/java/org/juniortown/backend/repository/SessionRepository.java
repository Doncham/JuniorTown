package org.juniortown.backend.repository;

import java.util.Optional;

import javax.swing.text.html.Option;

import org.juniortown.backend.domain.Session;
import org.juniortown.backend.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface SessionRepository extends CrudRepository<Session, Long> {
	Optional<Session> findByAccessToken(String accessToken);
}
