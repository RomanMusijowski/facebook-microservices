package com.mic.auth.repository;

import com.mic.auth.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameOrEmail(String username, String email);

    User getByUsername(String username);

    @Query(value = "select f.friend_id from friend f where f.user_id=?1 " +
            "union select f.user_id from friend f where f.friend_id=?1",
            nativeQuery = true)
    List<Long> getFriendsIdListByUserId(Long userId);

    @Query(value = "select * from app_user au inner join friend f on f.friend_id = au.id where f.user_id=?1 " +
            "union select * from app_user au inner join friend f on f.user_id = au.id where f.friend_id=?1 LIMIT 10",
            nativeQuery = true)
    List<User> getTop10FriendListByUserId(Long userId);

    Page<User> getAllByIdIn(Pageable pageable, List<Long> userIds);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsById(Long id);

    boolean existsByIdAndFriendsIdOrIdAndFriendsId(Long userId, Long friendId, Long friendId2, Long userId2);
}
