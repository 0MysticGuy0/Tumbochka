package com.vlat.dao;

import com.vlat.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {
   Optional<AppUser> findByTelegramId(Long id);
   Optional<AppUser> findById(Long id);
   Optional<AppUser> findByEmail(String email);
}
