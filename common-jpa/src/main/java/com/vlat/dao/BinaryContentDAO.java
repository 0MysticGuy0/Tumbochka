package com.vlat.dao;

import com.vlat.entity.AppDocument;
import com.vlat.entity.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinaryContentDAO extends JpaRepository<BinaryContent, Long> {
}
