package com.bdaim.customer.dao;

import com.bdaim.customer.entity.CustomerMsg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CustomerMsgRepository extends JpaRepository<CustomerMsg, Integer> {
    CustomerMsg findById(int id);
}
