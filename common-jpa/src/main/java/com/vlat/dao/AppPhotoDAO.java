package com.vlat.dao;

import com.vlat.entity.AppDocument;
import com.vlat.entity.AppPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppPhotoDAO extends JpaRepository<AppPhoto, Long> {
}
