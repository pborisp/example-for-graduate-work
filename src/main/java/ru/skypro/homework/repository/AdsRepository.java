package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.model.Ads;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdsRepository extends JpaRepository<Ads, Long> {
    Optional<Ads> findByPk(Long id);

    Optional<List<Ads>> findByAuthorId(Long id);

    @Modifying
    @Query("UPDATE Ads a SET a.image = :image WHERE a.pk = :id")
    void updateImage(@Param("id") Long id, @Param("image") String image);

    @Modifying
    @Query("UPDATE Ads a SET a.title = :title WHERE a.pk = :id")
    void updateTitle(@Param("id") Long id, @Param("title") String title);

    @Modifying
    @Query("UPDATE Ads a SET a.price = :price WHERE a.pk = :id")
    void updatePrice(@Param("id") Long id, @Param("price") Integer price);

    @Modifying
    @Query("UPDATE Ads a SET a.description = :description WHERE a.pk = :id")
    void updateDescription(@Param("id") Long id, @Param("description") String description);
}
