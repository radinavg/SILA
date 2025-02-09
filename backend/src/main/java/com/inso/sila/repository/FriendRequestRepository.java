package com.inso.sila.repository;

import com.inso.sila.entity.ApplicationUser;
import com.inso.sila.entity.Faqs;
import com.inso.sila.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import org.springframework.stereotype.Repository;


@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long>  {

    List<FriendRequest> findAllByFrom(ApplicationUser from);

    @Query("SELECT fr FROM FriendRequest fr "
            + "WHERE fr.to.email = :email AND fr.status = com.inso.sila.enums.RequestStatus.PENDING "
            + "ORDER BY fr.requestDateTime DESC")
    List<FriendRequest> findAllPendingFriendRequests(@Param("email") String email);

    FriendRequest findByFromAndTo(ApplicationUser fromUser, ApplicationUser toUser);
}
